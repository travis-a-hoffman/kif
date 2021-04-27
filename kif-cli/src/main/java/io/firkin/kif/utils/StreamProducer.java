/*
 * Copyright © 2021 Kif Contributors (https://kif.firkin.io/)
 * Copyright © 2021 Firkin•IO (https://firkin.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.firkin.kif.utils;

import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.clients.producer.internals.FutureRecordMetadata;
import org.apache.kafka.clients.producer.internals.ProduceRequestResult;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.record.RecordBatch;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Time;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

/**
 * An implementation of the {@code Producer} interface which produces records to an {@code OutputStream}.
 * <p/>
 * A StreamProducer implementation may be used as a drop-in replacement for a KafkaProducer.
 * For example, this may be used directly to write records in their serialized form to an OutputStream.
 * <p/>
 * To simplify data generation to a variety of targets (Streams, LocalFiles, in addition to a KafkaClient),
 * KifCli provides Record Streams which wrap around Consumer / Producer implementations.  This abstracts
 * away much of the details of writing records in the simple read/write case of KifCli.
 * <pre>
 *  +----------------------+
 *  |     Application      | --> KifCli / KifGenCli app writes POJOs to {@link RecordOutputStream}
 *  +----------------------+
 *  |  RecordOutputStream  | --> Accepts POJOs, writes to a {@link Producer<K,V>}
 *  +----------------------+
 *  |    StreamProducer    | --> Accepts Keys/Values, writes {@code byte[]} to an OutputStream
 *  +----------------------+
 *  |     OutputStream     | --> Writes {@code byte[]} to target.
 *  +----------------------+</pre>
 * <p/>
 * To write to {@code System.out}:
 * <pre>
 *  +----------------------+
 *  |     Application      | --> KifCli / KifGenCli application code;
 *  +----------------------+
 *  |  RecordOutputStream  | --> Accepts POJOs, writes to a {@code Producer<K,V>}
 *  +----------------------+
 *  |    StreamProducer    | --> Accepts Keys/Values, writes {@code bytes[]} to an OutputStream
 *  +----------------------+
 *  |     System.out       | --> Writes to console.
 *  +----------------------+</pre>
 * <p/>
 *
 * @param <K> The Record Key type.
 * @param <V> The Record Value type.
 */
public class StreamProducer<K, V> implements Producer<K,V> {

  // TODO Add the actual OutputStream to which we write...

  private final Cluster cluster;
  private final Partitioner partitioner;
  private final List<ProducerRecord<K, V>> sent;
  private final List<ProducerRecord<K, V>> uncommittedSends;
  private final Deque<StreamProducer.Completion> completions;
  private final Map<TopicPartition, Long> offsets;
  private final List<Map<String, Map<TopicPartition, OffsetAndMetadata>>> consumerGroupOffsets;
  private Map<String, Map<TopicPartition, OffsetAndMetadata>> uncommittedConsumerGroupOffsets;
  private final Serializer<K> keySerializer;
  private final Serializer<V> valueSerializer;
  private boolean autoComplete;
  private boolean closed;
  private boolean transactionInitialized;
  private boolean transactionInFlight;
  private boolean transactionCommitted;
  private boolean transactionAborted;
  private boolean producerFenced;
  private boolean sentOffsets;
  private long commitCount = 0L;
  private final Map<MetricName, Metric> streamMetrics;

  // TODO These are part of Mock ... but may not be needed for a Stream?
  public RuntimeException initTransactionException = null;
  public RuntimeException beginTransactionException = null;
  public RuntimeException sendOffsetsToTransactionException = null;
  public RuntimeException commitTransactionException = null;
  public RuntimeException abortTransactionException = null;
  public RuntimeException sendException = null;
  public RuntimeException flushException = null;
  public RuntimeException partitionsForException = null;
  public RuntimeException closeException = null;

  public StreamProducer() {
    this(Cluster.empty(), false, null, null, null);
  }

  public StreamProducer(final boolean autoComplete,
                        final Serializer<K> keySerializer,
                        final Serializer<V> valueSerializer) {
    this(Cluster.empty(), autoComplete, new DefaultPartitioner(), keySerializer, valueSerializer);
  }

  public StreamProducer(final boolean autoComplete,
                        final Partitioner partitioner,
                        final Serializer<K> keySerializer,
                        final Serializer<V> valueSerializer) {
    this(Cluster.empty(), autoComplete, partitioner, keySerializer, valueSerializer);
  }

  public StreamProducer(final Cluster cluster,
                        final boolean autoComplete,
                        final Partitioner partitioner,
                        final Serializer<K> keySerializer,
                        final Serializer<V> valueSerializer) {
    this.cluster = cluster;
    this.autoComplete = autoComplete;
    this.partitioner = partitioner;
    this.keySerializer = keySerializer;
    this.valueSerializer = valueSerializer;
    this.offsets = new HashMap<>();
    this.sent = new ArrayList<>();
    this.uncommittedSends = new ArrayList<>();
    this.consumerGroupOffsets = new ArrayList<>();
    this.uncommittedConsumerGroupOffsets = new HashMap<>();
    this.completions = new ArrayDeque<>();
    this.streamMetrics = new HashMap<>();
  }

  @Override
  public void initTransactions() {
    verifyProducerState();
    if (this.transactionInitialized) {
      throw new IllegalStateException("MockProducer has already been initialized for transactions.");
    }
    if (this.initTransactionException != null) {
      throw this.initTransactionException;
    }
    this.transactionInitialized = true;
    this.transactionInFlight = false;
    this.transactionCommitted = false;
    this.transactionAborted = false;
    this.sentOffsets = false;
  }

  @Override
  public void beginTransaction() throws ProducerFencedException {
    verifyProducerState();
    verifyTransactionsInitialized();

    if (this.beginTransactionException != null) {
      throw this.beginTransactionException;
    }

    if (transactionInFlight) {
      throw new IllegalStateException("Transaction already started");
    }

    this.transactionInFlight = true;
    this.transactionCommitted = false;
    this.transactionAborted = false;
    this.sentOffsets = false;
  }

  @Override
  public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, String consumerGroupId) throws ProducerFencedException {
    Objects.requireNonNull(consumerGroupId);
    verifyProducerState();
    verifyTransactionsInitialized();
    verifyTransactionInFlight();

    if (this.sendOffsetsToTransactionException != null) {
      throw this.sendOffsetsToTransactionException;
    }

    if (offsets.size() == 0) {
      return;
    }
    Map<TopicPartition, OffsetAndMetadata> uncommittedOffsets =
        this.uncommittedConsumerGroupOffsets.computeIfAbsent(consumerGroupId, k -> new HashMap<>());
    uncommittedOffsets.putAll(offsets);
    this.sentOffsets = true;
  }

  /**
   * See {@link Producer#sendOffsetsToTransaction(Map, ConsumerGroupMetadata)}
   *
   * @param offsets
   * @param groupMetadata
   */
  @Override
  public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, ConsumerGroupMetadata groupMetadata) throws ProducerFencedException {
    Objects.requireNonNull(groupMetadata);
    sendOffsetsToTransaction(offsets, groupMetadata.groupId());
  }

  /**
   * See {@link Producer#commitTransaction()}
   */
  @Override
  public void commitTransaction() throws ProducerFencedException {
    verifyProducerState();
    verifyTransactionsInitialized();
    verifyTransactionInFlight();

    if (this.commitTransactionException != null) {
      throw this.commitTransactionException;
    }

    flush();

    this.sent.addAll(this.uncommittedSends);
    if (!this.uncommittedConsumerGroupOffsets.isEmpty())
      this.consumerGroupOffsets.add(this.uncommittedConsumerGroupOffsets);

    this.uncommittedSends.clear();
    this.uncommittedConsumerGroupOffsets = new HashMap<>();
    this.transactionCommitted = true;
    this.transactionAborted = false;
    this.transactionInFlight = false;

    ++this.commitCount;
  }

  /**
   * See {@link Producer#abortTransaction()}
   */
  @Override
  public void abortTransaction() throws ProducerFencedException {
    verifyProducerState();
    verifyTransactionsInitialized();
    verifyTransactionInFlight();

    if (this.abortTransactionException != null) {
      throw this.abortTransactionException;
    }

    flush();
    this.uncommittedSends.clear();
    this.uncommittedConsumerGroupOffsets.clear();
    this.transactionCommitted = false;
    this.transactionAborted = true;
    this.transactionInFlight = false;
  }

  /**
   * See {@link Producer#send(ProducerRecord)}
   *
   * @param record
   */
  @Override
  public synchronized Future<RecordMetadata> send(ProducerRecord<K, V> record) {
    return send(record, null);
  }

  /**
   * See {@link Producer#send(ProducerRecord, Callback)}
   *
   * @param record
   * @param callback
   */
  @Override
  public synchronized Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {
    if (this.closed) {
      throw new IllegalStateException("MockProducer is already closed.");
    }

    if (this.producerFenced) {
      throw new KafkaException("MockProducer is fenced.", new ProducerFencedException("Fenced"));
    }
    if (this.sendException != null) {
      throw this.sendException;
    }

    int partition = 0;
    if (!this.cluster.partitionsForTopic(record.topic()).isEmpty())
      partition = partition(record, this.cluster);
    else {
      //just to throw ClassCastException if serializers are not the proper ones to serialize key/value
      keySerializer.serialize(record.topic(), record.key());
      valueSerializer.serialize(record.topic(), record.value());
    }

    TopicPartition topicPartition = new TopicPartition(record.topic(), partition);
    ProduceRequestResult result = new ProduceRequestResult(topicPartition);
    FutureRecordMetadata future = new FutureRecordMetadata(result, 0, RecordBatch.NO_TIMESTAMP,
        0L, 0, 0, Time.SYSTEM);
    long offset = nextOffset(topicPartition);
    StreamProducer.Completion completion = new StreamProducer.Completion(offset, new RecordMetadata(topicPartition, 0, offset,
        RecordBatch.NO_TIMESTAMP, 0L, 0, 0), result, callback);

    if (!this.transactionInFlight)
      this.sent.add(record);
    else
      this.uncommittedSends.add(record);

    if (autoComplete)
      completion.complete(null);
    else
      this.completions.addLast(completion);

    return future;
  }

  /**
   * See {@link Producer#flush()}
   */
  @Override
  public void flush() {
    verifyProducerState();

    if (this.flushException != null) {
      throw this.flushException;
    }

    while (!this.completions.isEmpty())
      completeNext();
  }

  /**
   * See {@link Producer#partitionsFor(String)}
   *
   * @param topic
   */
  @Override
  public List<PartitionInfo> partitionsFor(String topic) {
    if (this.partitionsForException != null) {
      throw this.partitionsForException;
    }
    return this.cluster.partitionsForTopic(topic);
  }

  /**
   * See {@link Producer#metrics()}
   */
  @Override
  public Map<MetricName, ? extends Metric> metrics() {
    return streamMetrics;
  }

  /**
   * See {@link Producer#close()}
   */
  @Override
  public void close() {
    close(Duration.ofMillis(0));
  }

  /**
   * See {@link Producer#close(Duration)}
   *
   * @param timeout
   */
  @Override
  public void close(Duration timeout) {
    if (this.closeException != null) {
      throw this.closeException;
    }

    this.closed = true;
  }

  // --- Internal Implementation Methods --------------------------------------------------------------------

  public boolean closed() {
    return this.closed;
  }

  /**
   * Complete the earliest uncompleted call successfully.
   *
   * @return true if there was an uncompleted call to complete
   */
  public synchronized boolean completeNext() {
    return errorNext(null);
  }

  /**
   * Complete the earliest uncompleted call with the given error.
   *
   * @return true if there was an uncompleted call to complete
   */
  public synchronized boolean errorNext(RuntimeException e) {
    StreamProducer.Completion completion = this.completions.pollFirst();
    if (completion != null) {
      completion.complete(e);
      return true;
    } else {
      return false;
    }
  }

  public synchronized void fenceProducer() {
    verifyProducerState();
    verifyTransactionsInitialized();
    this.producerFenced = true;
  }

  /**
   * Get the next offset for the specified topic/partition
   */
  private long nextOffset(TopicPartition tp) {
    Long offset = this.offsets.get(tp);
    if (offset == null) {
      this.offsets.put(tp, 1L);
      return 0L;
    } else {
      Long next = offset + 1;
      this.offsets.put(tp, next);
      return offset;
    }
  }

  private int partition(ProducerRecord<K, V> record, Cluster cluster) {
    Integer partition = record.partition();
    String topic = record.topic();
    if (partition != null) {
      List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
      int numPartitions = partitions.size();
      // they have given us a partition, use it
      if (partition < 0 || partition >= numPartitions)
        throw new IllegalArgumentException("Invalid partition given with record: " + partition
            + " is not in the range [0..."
            + numPartitions
            + "].");
      return partition;
    }
    byte[] keyBytes = keySerializer.serialize(topic, record.headers(), record.key());
    byte[] valueBytes = valueSerializer.serialize(topic, record.headers(), record.value());
    return this.partitioner.partition(topic, record.key(), keyBytes, record.value(), valueBytes, cluster);
  }

  private synchronized void verifyProducerState() {
    if (this.closed) {
      throw new IllegalStateException("MockProducer is already closed.");
    }
    if (this.producerFenced) {
      throw new ProducerFencedException("MockProducer is fenced.");
    }
  }

  private void verifyTransactionsInitialized() {
    if (!this.transactionInitialized) {
      throw new IllegalStateException("MockProducer hasn't been initialized for transactions.");
    }
  }

  private void verifyTransactionInFlight() {
    if (!this.transactionInFlight) {
      throw new IllegalStateException("There is no open transaction.");
    }
  }

  /**
   * Tracks the Completion state of
   */
  private static class Completion {
    private final long offset;
    private final RecordMetadata metadata;
    private final ProduceRequestResult result;
    private final Callback callback;

    public Completion(long offset,
                      RecordMetadata metadata,
                      ProduceRequestResult result,
                      Callback callback) {
      this.metadata = metadata;
      this.offset = offset;
      this.result = result;
      this.callback = callback;
    }

    public void complete(RuntimeException e) {
      result.set(e == null ? offset : -1L, RecordBatch.NO_TIMESTAMP, e);
      if (callback != null) {
        if (e == null)
          callback.onCompletion(metadata, null);
        else
          callback.onCompletion(null, e);
      }
      result.done();
    }
  }
}
