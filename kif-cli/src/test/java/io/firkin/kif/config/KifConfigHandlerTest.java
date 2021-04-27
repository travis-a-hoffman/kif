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

import io.firkin.kif.Assumptions;
import io.firkin.kif.config.context.Config;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

import static io.firkin.kif.Assertions.assertStringEmpty;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class KifConfigHandlerTest {

  private static String testTmpFolderPath =
      System.getProperty("KIF_TEST_TMP_PATH", "target/test-tmp");

  private static String testResourcePath =
      System.getProperty("KIF_TEST_RESOURCE_PATH", "target/test-classes");

  private static boolean keepTestFiles =
      Boolean.parseBoolean(System.getProperty("KIF_TEST_KEEP_TMP_FILES", "true"));

  private static String testRunId = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  private static String testPath = Path
      .of(testTmpFolderPath + "/KifConfigHandlerTest/" + testRunId)
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
  public void loadCreatedNewConfigFile() {
    Path configDirPath = Path.of(testPath, ".kif");
    assumeFalse(configDirPath.toFile().exists());

    Path configFilePath = Path.of(testPath, ".kif", "config.json");
    assumeTrue(configFilePath != null);
    assumeFalse(configFilePath.toFile().exists());

    ConfigHandler handler = KifConfigHandler.of(configFilePath);
    assertNotNull(handler);
    assertDoesNotThrow(() -> handler.load());

    File configDir = configDirPath.toFile();
    assertAll("configDir",
        () -> assertTrue(configDir.exists()),
        () -> assertTrue(configDir.isDirectory()),
        () -> assertTrue(configDir.canRead()),
        () -> assertTrue(configDir.canWrite()));
    // And should be no files in there yet?

    File configFile = configFilePath.toFile();
    assertAll("configFile",
        () -> assertTrue(configFile.exists()),
        () -> assertTrue(configFile.isFile()),
        () -> assertTrue(configFile.canRead()),
        () -> assertTrue(configFile.canWrite()));
  }

  @Test
  public void loadsEmptyKifConfigFile() {
    Path configFilePath = Path.of(testResourcePath, "io/firkin/kif/config/ConfigHandlerTest/loadsEmptyKifConfigFile.json");
    Assumptions.assumeFileEmpty(configFilePath.toFile());

    ConfigHandler configHandler = KifConfigHandler.of(configFilePath);
    assertNotNull(configHandler);
    assertDoesNotThrow(() -> {
      Config emptyConfig = configHandler.load();
      assertNotNull(emptyConfig);
      assertNull(emptyConfig.version);
      assertNull(emptyConfig.disable_update_check);
      assertNull(emptyConfig.disable_updates);
      assertNull(emptyConfig.no_browser);
      assertNull(emptyConfig.platforms);
      assertNull(emptyConfig.credentials);
      assertNull(emptyConfig.contexts);
      assertNull(emptyConfig.context_states);
      assertNull(emptyConfig.current_context);
      assertNull(emptyConfig.anonymous_id);
      /*
          assertAll("platforms",
              () -> assertNotNull(emptyConfig.platforms),
              () -> assertTrue(emptyConfig.platforms.isEmpty()));
          assertAll("credentials",
              () -> assertNotNull(emptyConfig.credentials),
              () -> assertTrue(emptyConfig.credentials.isEmpty()));
          assertAll("contexts",
              () -> assertNotNull(emptyConfig.contexts),
              () -> assertTrue(emptyConfig.contexts.isEmpty()));
          assertAll("states",
              () -> assertNotNull(emptyConfig.states),
              () -> assertTrue(emptyConfig.states.isEmpty()));
       */
    });

  }

  @Test
  public void loadsKifConfigFile() {

  }
}
