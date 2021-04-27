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

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.Deserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultRecordInputStream<T, K, V> extends RecordStream implements RecordInputStream<T> {

  // Consumers provide a stream of Records which can be deserialized into a Java object of some kind.
  Consumer<K, T> consumer; // V=T, correct?
  Deserializer<T> deserializer; // TODO Do I actually need the deserializer?

  Queue<ConsumerRecords<K,T>> recordsQueue = new LinkedList<>();
  String topic;

  private static final int DEFAULT_BUFFER_SIZE=2048;

  private Queue<T> pojoQueue = new ArrayBlockingQueue<T>(DEFAULT_BUFFER_SIZE);
  private ExecutorService execPool  = Executors.newSingleThreadExecutor();;

  public DefaultRecordInputStream(Consumer<K,V> consumer, Deserializer<T> deserializer) {
    pojoQueue = new ArrayBlockingQueue<T>(DEFAULT_BUFFER_SIZE);
    execPool  = Executors.newSingleThreadExecutor();;
    this.consumer = (Consumer<K,T>) consumer;
    this.consumer.subscribe(Collections.singleton(this.topic));
    this.deserializer = deserializer;
  }

  private DefaultRecordInputStream() {
    this.execPool = Executors.newSingleThreadExecutor();
    pojoQueue = new ArrayBlockingQueue<T>(DEFAULT_BUFFER_SIZE);
  }

  @Override
  public boolean hasNext() {
    return pojoQueue.peek() != null;
  }

  @Override
  public T read() {
    T result = pojoQueue.poll();
    // Launch an process which asynchronously polls() the underlying consumer
    if (pojoQueue.isEmpty())
      execPool.submit(() -> {
        // TODO base this poll() timeout on kafka configuration.
        ConsumerRecords<K,T> consumerRecords = consumer.poll(Duration.ofMillis(12000L));
        consumerRecords.records(topic).forEach(r -> pojoQueue.add(r.value())); // TODO Rework the
      });
    return result;
  }

  @Override
  public void close() {
    if (!execPool.isShutdown()) {
      execPool.shutdown();
    }
    consumer.close(Duration.ofMillis(30000L));
  }
}
