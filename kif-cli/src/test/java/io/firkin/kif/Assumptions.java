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
