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

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;

import io.firkin.kif.utils.RecordUtils.KeyResolver;
import io.firkin.kif.utils.RecordUtils.PartitionResolver;
import io.firkin.kif.utils.RecordUtils.ValueResolver;
import io.firkin.kif.utils.RecordUtils.HeaderResolver;

/**
 *
 * @param <T> The type of the Java Object produced to the Topic
 * @param <K> The type of the Key Object used to key the Record
 * @param <V> The type of the Value Object produced to the Topic,
 *
 */
public class RecordOutputStream <T, K, V> extends RecordStream {

  Producer<K,V> producer;

  KeyResolver<T,K> kr;
  ValueResolver<T,V> vr;
  PartitionResolver<T> pr;
  HeaderResolver<T> hr;

  public RecordOutputStream (Producer<K,V> producer) {
    this(new MockProducer<>(),
        RecordUtils.nullKey(),
        RecordUtils.identityValue(),
        RecordUtils.zeroPartition(),
        RecordUtils.emptyHeaders());
  }

  public RecordOutputStream(Producer<K,V> producer,
                            KeyResolver<T,K> keyResolver,
                            ValueResolver<T,V> valueResolver) {
    this(producer, keyResolver, valueResolver,
        RecordUtils.zeroPartition(),
        RecordUtils.emptyHeaders());
  }

  public RecordOutputStream(Producer<K,V> producer,
                            KeyResolver<T,K> keyResolver,
                            ValueResolver<T,V> valueResolver,
                            PartitionResolver<T> partitionResolver) {
    this(producer, keyResolver, valueResolver, partitionResolver,
        RecordUtils.emptyHeaders());
  }

  public RecordOutputStream(Producer<K,V> producer,
                            KeyResolver<T,K> keyResolver,
                            ValueResolver<T,V> valueResolver,
                            PartitionResolver<T> partitionResolver,
                            HeaderResolver<T> headerResolver) {
    this.kr = keyResolver;
    this.vr = valueResolver;
    this.pr = partitionResolver;
    this.hr = headerResolver;

    this.producer = producer;
  }

  public K key(T t) {
    return kr.key(t);
  }

  public V value(T t) {
    return vr.value(t);
  }

  public Headers headers(T t) {
    return hr.headers(t);
  }

  public Integer partition(T t) {
    return pr.partition(t);
  }

  public String getTopic() {
    return topic;
  }

  public void write(T t) {
    producer.send(
        new ProducerRecord<K,V>(topic,
            pr.partition(t),
            kr.key(t),
            vr.value(t),
            hr.headers(t)));
  }

  public void write(T obj, K key, V value) {
    producer.send(
        new ProducerRecord<>(topic,
            pr.partition(obj),
            key,
            value,
            hr.headers(obj)));
  }
}
