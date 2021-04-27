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

import com.google.protobuf.Message;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;

public class KifSerdes {

  public interface KifSerde<S,D> {
    D deserializer();
    S serializer();
  }

  private static final AvroSerde AVRO_SERDE = new AvroSerde();
  private static final JsonSerde JSON_SERDE = new JsonSerde();
  private static final JsonSchemaSerde JSON_SCHEMA_SERDE = new JsonSchemaSerde();
  private static final ProtobufSerde PROTOBUF_SERDE = new ProtobufSerde();

  private static class AvroSerde<T> implements KifSerde<KafkaAvroSerializer, KafkaAvroDeserializer> {
    final KafkaAvroDeserializer des = new KafkaAvroDeserializer();
    final KafkaAvroSerializer ser = new KafkaAvroSerializer();

    public KafkaAvroDeserializer deserializer() {
      return this.des;
    }

    public KafkaAvroSerializer serializer() {
      return this.ser;
    }
  }

  private static class JsonSerde<T> implements KifSerde<KafkaJsonSerializer, KafkaJsonDeserializer> {
    final KafkaJsonDeserializer des = new KafkaJsonDeserializer();
    final KafkaJsonSerializer ser = new KafkaJsonSerializer();

    public KafkaJsonDeserializer deserializer() {
      return this.des;
    }

    public KafkaJsonSerializer serializer() {
      return this.ser;
    }
  }

  private static class JsonSchemaSerde<T> implements KifSerde<KafkaJsonSchemaSerializer<T>, KafkaJsonSchemaDeserializer<T>> {
    final KafkaJsonSchemaDeserializer des = new KafkaJsonSchemaDeserializer();
    final KafkaJsonSchemaSerializer ser = new KafkaJsonSchemaSerializer();

    public KafkaJsonSchemaDeserializer<T> deserializer() {
      return this.des;
    }

    public KafkaJsonSchemaSerializer<T> serializer() {
      return this.ser;
    }
  }

  private static class ProtobufSerde<T extends Message> implements KifSerde<KafkaProtobufSerializer<T>, KafkaProtobufDeserializer<T>> {
    final KafkaProtobufDeserializer<T> des = new KafkaProtobufDeserializer();
    final KafkaProtobufSerializer<T> ser = new KafkaProtobufSerializer();

    public KafkaProtobufDeserializer<T> deserializer() {
      return this.des;
    }

    public KafkaProtobufSerializer<T> serializer() {
      return this.ser;
    }
  }

  public static <T> KifSerde<KafkaAvroSerializer, KafkaAvroDeserializer> avro() {
    return AVRO_SERDE;
  }

  public static <T> KifSerde<KafkaJsonSerializer, KafkaJsonDeserializer> json() {
    return JSON_SERDE;
  }

  public static <T> KifSerde<KafkaJsonSchemaSerializer<T>, KafkaJsonSchemaDeserializer<T>> jsonSchema() {
    return JSON_SCHEMA_SERDE;
  }

  public static <T extends Message> KifSerde<KafkaProtobufSerializer<T>, KafkaProtobufDeserializer<T>> protobuf() {
    return PROTOBUF_SERDE;
  }
}
