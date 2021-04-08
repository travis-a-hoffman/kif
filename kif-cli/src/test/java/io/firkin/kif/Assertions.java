package io.firkin.kif;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Assertions {

  public static void assertStringEmpty(String actual, String message) {
    assertAll(message,
        () -> assertNotNull(actual, "not null"),
        () -> assertTrue(actual.isEmpty(), "is empty"));
  }

  public static void assertStringEmpty(String actual) {
    assertAll(
        () -> assertNotNull(actual, "not null"),
        () -> assertTrue(actual.isEmpty(), "is empty"));
  }

  public static void assertStringNotEmpty(String actual, String message) {
    assertAll(message,
        () -> assertNotNull(actual, "not null"),
        () -> assertFalse(actual.isEmpty(), "is not empty"));
  }

  public static void assertStringNotEmpty(String actual) {
    assertAll(
        () -> assertNotNull(actual, "not null"),
        () -> assertFalse(actual.isEmpty(), "is not empty"));
  }

  public static void assertStringBlank(String actual, String message) {
    assertAll(message,
        () -> assertNotNull(actual, "is not null"),
        () -> assertTrue(actual.isBlank(), "is blank"));
  }

  public static void assertStringBlank(String actual) {
    assertAll(
        () -> assertNotNull(actual, "not null"),
        () -> assertTrue(actual.isBlank(), "is blank"));
  }
  public static void assertStringNotBlank(String actual, String message) {
    assertAll(message,
        () -> assertNotNull(actual, "is not null"),
        () -> assertFalse(actual.isBlank(), "is blank"));
  }

  public static void assertStringNotBlank(String actual) {
    assertAll(
        () -> assertNotNull(actual, "not null"),
        () -> assertFalse(actual.isBlank(), "is blank"));
  }


  public static void assertEmpty(Collection<?> collection) {
    assertAll(
        () -> assertNotNull(collection),
        () -> assertTrue(collection.isEmpty()));
  }
  public static void assertEmpty(String heading, Collection<?> collection) {
    assertAll(heading,
        () -> assertNotNull(collection),
        () -> assertTrue(collection.isEmpty()));
  }
  public static void assertNotEmpty(Collection<?> collection) {
    assertAll(
        () -> assertNotNull(collection),
        () -> assertFalse(collection.isEmpty()));
  }
  public static void assertNotEmpty(String heading, Collection<?> collection) {
    assertAll(heading,
        () -> assertNotNull(collection),
        () -> assertFalse(collection.isEmpty()));
  }

  public static void assertEmpty(Map<?,?> map) {
    assertAll(
        () -> assertNotNull(map),
        () -> assertTrue(map.isEmpty()));
  }
  public static void assertEmpty(String heading, Map<?,?> map) {
    assertAll(heading,
        () -> assertNotNull(map),
        () -> assertTrue(map.isEmpty()));
  }
  public static void assertNotEmpty(Map<?,?> map) {
    assertAll(
        () -> assertNotNull(map),
        () -> assertFalse(map.isEmpty()));
  }
  public static void assertNotEmpty(String heading, Map<?,?> map) {
    assertAll(heading,
        () -> assertNotNull(map),
        () -> assertFalse(map.isEmpty()));
  }

  public static void assertEmpty(File file) {
    assertAll(
        () -> assertNotNull(file),
        () -> assertTrue(file.exists()),
        () -> assertTrue(file.isFile()),
        () -> assertTrue(file.length() == 0));
  }
  public static void assertEmpty(String heading, File file) {
    assertAll(heading,
        () -> assertNotNull(file),
        () -> assertTrue(file.exists()),
        () -> assertTrue(file.isFile()),
        () -> assertTrue(file.length() == 0));
  }
  public static void assertNotEmpty(File file) {
    assertAll(
        () -> assertNotNull(file),
        () -> assertTrue(file.exists()),
        () -> assertTrue(file.isFile()),
        () -> assertTrue(file.length() > 0));
  }
  public static void assertNotEmpty(String heading, File file) {
    assertAll(heading,
        () -> assertNotNull(file),
        () -> assertTrue(file.exists()),
        () -> assertTrue(file.isFile()),
        () -> assertTrue(file.length() > 0));
  }
}
