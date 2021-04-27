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

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.protobuf.Message;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import org.apache.avro.generic.IndexedRecord;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

/**
 * Static class for building input and output record streams. Example usage:
 *
 * First, call one of these to begin configuration:
 * <pre>
 *   RecordStreams.stream()</pre>
 * Next, make (chained) calls to the following on the returned instance.
 * <pre>
 *   .topic(String name)
 *   .offset(long offset)</pre>
 * Define the Producer / Consumer based on the source/destination information:
 * <pre>
 *   .from(Consumer)
 *   .from(Uri)
 *   .from(InputStream)
 *   .to(Producer)
 *   .to(Uri)
 *   .to(OutputStream)</pre>
 * Define the RecordStream input/output object types.
 * <pre>
 *   .produces(Class<R> recordType)
 *   .consumes(Class<R> recordType)</pre>
 * Define the Serialization format
 * <pre>
 *   .avro()
 *   .avro(AvroSchema schema)
 *   .json()
 *   .json(JsonSchema schema)
 *   .protobuf()</pre>
 *   .protobuf(ProtoSchema schema)</pre>
 * Define
 * Define the Key serialization and types
 * <pre>
 *   .keyType(Class<K> keyType)
 *   .keySer(Serializer<K> ser)
 *   .keyDes(Deserializer<K> des)
 *   .keySerde(Serde<K> serde)</pre>
 * Define the value serialization:
 * <pre>
 *   .valType(Class<V> valType)
 *   .valSer(Serializer<V> ser)
 *   .valDes(Deserializer<V> des)
 *   .valSerde(Serde<V> serde)</pre>
 * Lastly, call {@code open()} to finalize the stream's configuration and create the
 * {@code Record{In|Out}putStream} instance. When using kafka {@code Producer}, this will
 * initialize the client and block until initialization is complete. When using a
 * {@code FileProducer}, {@code StreamProducer}, or {@code MockProducer}, this will return
 * almost immediately.
 *
 * RecordStreams support a number of serialization formats:
 *   * Avro: For the types of supported formats (Avro, JSONSchema, ProtoBuf)
 *   * Protobuf:
 *   * JsonSchema:
 *   * Json (no schema):
 *   * Bytes (raw bytes):
 * https://docs.confluent.io/platform/current/schema-registry/serdes-develop/index.html#serializer-and-formatter
 */
public class RecordStreams<T,K,V> {

  public static final long DEFAULT_CLOSE_TIMEOUT_MS = 30000L;

  // --- Initialization -------------------------------------------------------------------------------------

  public static <T,K,V> RecordStreams<T,K,V> stream() { return new RecordStreams<>(); }

  private RecordStreams() {
    topics = new ArrayList<>(128);
  }

  // --- Configuration --------------------------------------------------------------------------------------

  ObjectMapper objMapper; // Jackson specific
  Class<T> recordType;

  // --- Schema Configuration -------------------------------------------------------------------------------

  String schemaDef;

  public RecordStreams<T,K,V> schema(String schemaDef) {
    this.schemaDef = schemaDef;
    return this;
  }

  // --- Formatter Configuration ----------------------------------------------------------------------------

  IndexedRecord          avroRecord;
  KafkaAvroSerializer    avroSerializer;
  KafkaAvroDeserializer  avroDeserializer;

  KafkaProtobufSerializer<Message>    protobufSerializer;
  KafkaProtobufDeserializer<Message>  protobufDeserializer;

  KafkaJsonSerializer<T>    jsonSerializer;
  KafkaJsonDeserializer<T>  jsonDeserializer;

  KafkaJsonSchemaSerializer<T>    jsonSchemaSerializer;
  KafkaJsonSchemaDeserializer<T>  jsonSchemaDeserializer;

  // --- Serialization Configuration ------------------------------------------------------------------------

  Serde<K>         keySerde;
  Serde<V>         valSerde;

  Serializer<K>    keySer;
  Serializer<V>    valSer;

  Deserializer<K>  keyDes;
  Deserializer<V>  valDes;

  KifSerdes.KifSerde<?,?> kifSerde;

  public RecordStreams<T,K,V> avro() {
    this.kifSerde = KifSerdes.avro();
    return this;
  }
  public RecordStreams<T,K,V> avro(String schemaDef) {
    this.kifSerde = KifSerdes.avro();
    this.schemaDef = schemaDef;
    return this;
  }

  public RecordStreams<T,K,V> json() {
    this.kifSerde = KifSerdes.json();
    return this;
  }
  public RecordStreams<T,K,V> json(String schemaDef) {
    this.kifSerde = KifSerdes.json();
    this.schemaDef = schemaDef;
    return this;
  }

  public RecordStreams<T,K,V> protobuf() {
    this.kifSerde = KifSerdes.protobuf();
    return this;
  }
  public RecordStreams<T,K,V> protobuf(String schemaDef) {
    this.kifSerde = KifSerdes.protobuf();
    this.schemaDef = schemaDef;
    return this;
  }

  // --- InputStream Configuration --------------------------------------------------------------------------

  private Path         inPath;
  private InputStream  inStream;
  private URI          inUri;
  private String       inString;

  public RecordStreams<T,K,V> from(InputStream stream) {
    this.inStream = stream;
    return this;
  }
  public RecordStreams<T,K,V> from(Path path) {
    this.inPath = path;
    return this;
  }
  public RecordStreams<T,K,V> from(String inString) {
    this.inString = inString;
    return this;
  }
  public RecordStreams<T,K,V> from(URI uri) {
    this.inUri = uri;
    return this;
  }

  // --- OutputStream Configuration -------------------------------------------------------------------------

  private Path         outPath;
  private OutputStream outStream;
  private URI          outUri;
  private String       outString;

  public RecordStreams<T,K,V> to(OutputStream stream) {
    this.outStream = stream;
    return this;
  }
  public RecordStreams<T,K,V> to(Path path) {
    this.outPath = path;
    return this;
  }
  public RecordStreams<T,K,V> to(String string) {
    this.outString = string;
    return this;
  }
  public RecordStreams<T,K,V> to(URI uri) {
    this.outUri = uri;
    return this;
  }

  // --- KafkaClient Configuration --------------------------------------------------------------------------

  List<String> topics;

  // TODO Create methods to programmatically configure Kafka details

  public RecordStreams<T,K,V> kafka(KafkaCluster kafkaCluster) { return this; }
  public RecordStreams<T,K,V> kafka(Properties kafkaProperties) { return this; }

  /**
   * Configure the Schema Registry.
   *
   * @param kafkaCluster
   * @return
   */
  public RecordStreams<T,K,V> registry(SchemaRegistry schemaRegistry) { return this; }
  public RecordStreams<T,K,V> registry(Properties schemaProperties) { return this; }

  public RecordStreams<T,K,V> cluster(Properties kafkaProperties) { return this; }
  public RecordStreams<T,K,V> credentials(String apiToken) { return this; }
  public RecordStreams<T,K,V> credentials(String user, String password) { return this; }
  public RecordStreams<T,K,V> group(String consumerGroupId) { return this; }
  public RecordStreams<T,K,V> topic(String topic) { return this; }
  public RecordStreams<T,K,V> topics(String[] topics) { return this; }

  // --- Stream Realization ---------------------------------------------------------------------------------

  /**
   * Returns a DefaultRecordInputStream based on the configuration.
   *
   * @param <T> The POJO type returned by the Stream
   * @param <K> The Key Type of the keys read in by the Stream
   * @param <V> The Value Type of the records read in by the Stream
   *
   * @return
   */
  public DefaultRecordInputStream<T,K,V> in() {
    Map<String, Object> configMap = new HashMap();

    // TODO Return one of the 4 Consumer implementations:
//    FileConsumer<K, V> fileConsumer = new FileConsumer<K, V>(configMap);
//    StreamConsumer<K, V> streamConsumer = new StreamConsumer<K, V>(configMap);
//    KafkaConsumer<K, V> kafkaConsumer = new KafkaConsumer<K, V>(configMap);
    MockConsumer<K, V> mockConsumer = new MockConsumer<K, V>(OffsetResetStrategy.LATEST);

    // TODO Add Support for producing/consuming multiple topics
    Consumer<K,V> consumer = mockConsumer;
    consumer.subscribe(Arrays.asList(topics.get(0)));
    return in(consumer);
  }

  public DefaultRecordInputStream<T,K,V> in(Consumer<K,V> consumer) {

    // TODO Implement Schema Registry Client based on registry() settings above.
    SchemaRegistryClient srClient = new MockSchemaRegistryClient();
    Map<String, ?> deserializerConfig = new HashMap<>();
    Deserializer<T> deserializer = (Deserializer<T>) new KafkaAvroDeserializer(srClient, deserializerConfig);

    return new DefaultRecordInputStream<T,K,V>(consumer, deserializer);
  }

  public RecordOutputStream<T,K,V> out() {
    Producer<K,T> producer = new MockProducer<>();
    return new RecordOutputStream(producer);
  }

  /**
   * Holds configuration of a SchemaRegistry.
   */
  private class SchemaRegistry {
  }

  /**
   * Holds configuration of a KafkaCluster.
   */
  private class KafkaCluster {
  }
}
