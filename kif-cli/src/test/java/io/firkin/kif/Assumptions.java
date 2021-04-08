package io.firkin.kif;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class Assumptions {
  public static void assumeFileExists(File file) {
    assumeTrue(file != null);
    assumeTrue(file.exists());
    assumeTrue(file.isFile());
  }

  public static void assumeFileNotEmpty(File file) {
    assumeTrue(file != null);
    assumeTrue(file.exists());
    assumeTrue(file.isFile());
    assumeTrue(file.length() > 0);
  }

  public static void assumeFileEmpty(File file) {
    assumeTrue(file != null);
    assumeTrue(file.exists());
    assumeTrue(file.isFile());
    assumeTrue(file.length() == 0);
  }

  public static void assumeFileExists(Path path) {
    File file = path.toFile();
    assumeTrue(file != null);
    assumeTrue(file.exists());
    assumeTrue(file.isFile());
  }

  public static void assumeFileNotEmpty(Path path) {
    File file = path.toFile();
    assumeTrue(file != null);
    assumeTrue(file.exists());
    assumeTrue(file.isFile());
    assumeTrue(file.length() > 0);
  }

  public static void assumeFileEmpty(Path path) {
    File file = path.toFile();
    assumeTrue(file != null);
    assumeTrue(file.exists());
    assumeTrue(file.isFile());
    assumeTrue(file.length() == 0);
  }
}
