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

import static io.firkin.kif.Assertions.assertStringEmpty;
import static org.junit.jupiter.api.Assertions.*;

public class CpConfigHandlerTest {
  private static String testTmpFolderPath =
      System.getProperty("KIF_TEST_TMP_PATH", "target/test-tmp");

  private static String testResourcePath =
      System.getProperty("KIF_TEST_RESOURCE_PATH", "target/test-classes");

  private static boolean keepTestFiles =
      Boolean.parseBoolean(System.getProperty("KIF_TEST_KEEP_TMP_FILES", "true"));

  private static String testRunId = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  private static String testPath = Path
      .of(testTmpFolderPath + "/CpConfigHandlerTest/"+ testRunId)
      .toAbsolutePath()
      .toString();

  @BeforeAll
  private static void createTestFolder() {
    Path path = Path.of(testPath);
    System.out.println("Test Run Path: "+path);
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
      System.out.println("Deleting Test Run Temp Folder: "+testPath);
      Path.of(testPath).toFile().delete();
    }
  }

  // ------------------------------------------------------------------------------------------------------

  @Test
  public void loadsConfluentPlatformConfigFile() {
    Path configFilePath = Path.of(testResourcePath, "io/firkin/kif/config/ConfigHandlerTest/loadsConfluentPlatformConfigFile.json");
    Assumptions.assumeFileNotEmpty(configFilePath.toFile());

    ConfigHandler configHandler = KifConfigHandler.of(configFilePath);
    assertNotNull(configHandler);

    assertDoesNotThrow(() -> {
      Config emptyConfig = configHandler.load();
      assertNotNull(emptyConfig);

      assertEquals("3.0.0", emptyConfig.version, "emptyConfig.version");
      assertFalse(emptyConfig.disable_update_check, "emptyConfig.disable_update_check");
      assertFalse(emptyConfig.disable_updates, "emptyConfig.disable_updates");
      assertFalse(emptyConfig.no_browser, "emptyConfig.no_browser");
      assertNull(emptyConfig.platforms, "emptyConfig.platforms");
      assertNull(emptyConfig.credentials, "emptyConfig.credentials");
      assertNull(emptyConfig.contexts, "emptyConfig.contexts");
      assertNull(emptyConfig.context_states, "emptyConfig.states");
      assertStringEmpty(emptyConfig.current_context, "emptyConfig.current_context");
      assertEquals("1dd7be3a-9715-4178-9f05-220f811bb5a3", emptyConfig.anonymous_id, "emptyConfig.anonymous_id");
    });
  }
}
