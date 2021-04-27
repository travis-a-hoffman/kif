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

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

public class RecordUtils {

  // --- Key Resolution -------------------------------------------------------------------------------------

  @FunctionalInterface
  public interface KeyResolver<T,K> {
    K key(T t);
  }

  public static <T,K> KeyResolver<T,K> nullKey() {
    return (t) -> (K) null;
  }
  public static <T> KeyResolver<T,Boolean> cKey(boolean value) {
    return (t) -> Boolean.valueOf(value);
  }
  public static <T> KeyResolver<T,Byte> cKey(byte value) {
    return (t) -> Byte.valueOf(value);
  }
  public static <T> KeyResolver<T,Short> cKey(short value) {
    return (t) -> Short.valueOf(value);
  }
  public static <T> KeyResolver<T,Integer> cKey(int value) {
    return (t) -> Integer.valueOf(value);
  }
  public static <T> KeyResolver<T,Long> cKey(long value) {
    return (t) -> Long.valueOf(value);
  }
  public static <T> KeyResolver<T,Float> cKey(float value) {
    return (t) -> Float.valueOf(value);
  }
  public static <T> KeyResolver<T,Double> cKey(double value) {
    return (t) -> Double.valueOf(value);
  }
  public static <T> KeyResolver<T,BigInteger> cKey(BigInteger value) {
    return (T) -> value;
  }
  public static <T> KeyResolver<T,BigDecimal> cKey(BigDecimal value) {
    return (T) -> value;
  }
  public static <T> KeyResolver<T,? extends Number> cKey(Number value) {
    return (t) -> value;
  }
  public static <T> KeyResolver<T,String> cKey(String value) {
    return (t) -> value;
  }
  public static <T,K> KeyResolver<T,K> cKey(K value) {
    return (t) -> value;
  }
  public static <T,K> KeyResolver<T,K> fnKey(Supplier<K> fn) {
    return (t) -> fn.get();
  }
  public static <T,K> KeyResolver<T,K> fnKey(Function<T,K> fn) {
    return (t) -> fn.apply(t);
  }

  // --- Partition Resolution -------------------------------------------------------------------------------

  @FunctionalInterface
  public interface PartitionResolver<T> {
    Integer partition(T t);
  }

  public static <T> PartitionResolver<T> nullPartition() {
    return (t) -> (Integer)null;
  }
  public static <T> PartitionResolver<T> zeroPartition() {
    return (t) -> 0;
  }
  public static <T> PartitionResolver<T> onePartition() {
    return (t) -> 1;
  }
  public static <T> PartitionResolver<T> cPartition(boolean value) {
    return (t) -> value? 1: 0;
  }
  public static <T> PartitionResolver<T> cPartition(byte value) {
    return (t) -> Byte.toUnsignedInt(value);
  }
  public static <T> PartitionResolver<T> cPartition(short value) {
    return (t) -> Short.toUnsignedInt(value);
  }
  public static <T> PartitionResolver<T> cPartition(int value) {
    return (t) -> Integer.valueOf(value & Integer.MAX_VALUE);
  }
  public static <T> PartitionResolver<T> cPartition(long value) {
    return (t) -> ((int)value) & Integer.MAX_VALUE;
  }
  public static <T> PartitionResolver<T> cPartition(BigInteger value) {
    return (t) -> value.intValue() & Integer.MAX_VALUE;
  }
  public static <T> PartitionResolver<T> fnPartition(Supplier<Integer> fn) {
    return (t) -> fn.get();
  }
  public static <T> PartitionResolver<T> fnPartition(Function<T,Integer> fn) {
    return (t) -> fn.apply(t);
  }

  // --- Value Resolution -----------------------------------------------------------------------------------

  @FunctionalInterface
  public interface ValueResolver<T,V> {
    V value(T t);
  }

  public static <T,V> ValueResolver<T,V> nullValue() {
    return (t) -> (V) null;
  }
  public static <T,V> ValueResolver<T,V> identityValue() {
    return (t) -> (V)t;
  }
  public static <T,V> ValueResolver<T,V> fnValue(Supplier<V> fn) {
    return (t) -> fn.get();
  }
  public static <T,V> ValueResolver<T,V> fnValue(Function<T,V> fn) {
    return (t) -> fn.apply(t);
  }

  // --- Header Resolution ----------------------------------------------------------------------------------

  public interface HeaderResolver<T> {
    Headers headers(T t);
  }

  public static <T> HeaderResolver<T> emptyHeaders() {
    // TODO Make this an unmodifiable Header...
    return (t) -> new RecordHeaders();
  }
  public static <T> HeaderResolver<T> cHeaders(Headers headers) {
    return (t) -> headers;
  }
  public static <T> HeaderResolver<T> fnHeaders(Supplier<Headers> fn) {
    return (t) -> fn.get();
  }
  public static <T> HeaderResolver<T> fnHeaders(Function<T,Headers> fn) {
    return (t) -> fn.apply(t);
  }

}

