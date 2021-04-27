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

package io.firkin.kif.config;

import io.firkin.kif.config.context.Config;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.firkin.kif.Assertions.*;
import static io.firkin.kif.Assumptions.assumeFileExists;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CcConfigHandlerTest {
  private static String testTmpFolderPath =
      System.getProperty("KIF_TEST_TMP_PATH", "target/test-tmp");

  private static String testResourcePath =
      System.getProperty("KIF_TEST_RESOURCE_PATH", "target/test-classes");

  private static boolean keepTestFiles =
      Boolean.parseBoolean(System.getProperty("KIF_TEST_KEEP_TMP_FILES", "true"));

  private static String testRunId = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  private static String testPath = Path
      .of(testTmpFolderPath + "/CounfluentCloudConfigHandlerTest/" + testRunId)
      .toAbsolutePath()
      .toString();

  @BeforeAll
  private static void createTestFolder() {
    Path path = Path.of(testPath);
    System.out.println("Test Run Path: " + path);
    File tmpFolder = path.toFile();
    if (tmpFolder.exists()) {
      if (!tmpFolder.isDirectory()) {
        // Not a Directory
      } else if (!tmpFolder.canWrite()) {
        // No Permissions
      }
    } else {
      tmpFolder.mkdirs();
    }
    if (!tmpFolder.exists()) {
      // Could not create the folder
    } else if (!keepTestFiles) {
      // Clean up
      tmpFolder.deleteOnExit();
    }
  }

  @AfterAll
  private static void deleteTestFolder() {
    if (!keepTestFiles) {
      System.out.println("Deleting Test Run Temp Folder: " + testPath);
      Path.of(testPath).toFile().delete();
    }
  }

  // ------------------------------------------------------------------------------------------------------

  @Test
  public void loadCCFileWithNullValues() {

  }

  @Test
  public void loadCCFileWithMissingFields() {

  }

  @Test
  public void loadCCFileWithExtraFields() {

  }

  @Test
  public void loadsSimpleCcConfigFile() {
    Path configFilePath = Path.of(testResourcePath, "io/firkin/kif/config/ConfigHandlerTest/loadsSimpleConfluentCloudConfigFile.json");
    assumeFileExists(configFilePath.toFile());

    ConfigHandler configHandler = KifConfigHandler.of(configFilePath);

    assertDoesNotThrow(() -> {
      Config simpleConfig = configHandler.load();
      assertNotNull(simpleConfig);

      assertEquals("3.0.0", simpleConfig.version, "simpleConfig.version");
      assertFalse(simpleConfig.disable_update_check, "simpleConfig.disable_update_check");
      assertFalse(simpleConfig.disable_updates, "simpleConfig.disable_updates");
      assertFalse(simpleConfig.no_browser, "simpleConfig.no_browser");
      assertEquals("", simpleConfig.current_context, "simpleConfig.current_context");
      assertStringEmpty(simpleConfig.anonymous_id, "simpleConfig.anonymous_id");

      assertEmpty("simpleConfig.platforms", simpleConfig.platforms);
      assertEmpty("simpleConfig.credentials", simpleConfig.credentials);
      assertEmpty("simpleConfig.contexts", simpleConfig.contexts);
      assertEmpty("simpleConfig.states", simpleConfig.context_states);
    });
  }

}
