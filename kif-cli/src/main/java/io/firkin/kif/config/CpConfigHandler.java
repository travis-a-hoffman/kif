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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.firkin.kif.config.context.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class CpConfigHandler implements ConfigHandler {
  private static final Logger log = LoggerFactory.getLogger(CpConfigHandler.class);

  private static final String DOT_CP = ".confluent";
  private static final String DEFAULT_CONFIG_FILE = "config.json";
  private static final String DEFAULT_CONFIG_DIR =
      System.getProperty("CONFLUENT_HOME", System.getProperty("user.home") + "/" + DOT_CP);

  public static ConfigHandler of() {
    return of(Path.of(DEFAULT_CONFIG_DIR, DOT_CP, DEFAULT_CONFIG_FILE));
  }

  // @VisibleForTest
  static ConfigHandler of(Path testConfigFilePath) {
    return new CpConfigHandler(testConfigFilePath);
  }

  // ------------------------------------------------------------------------------------------------------------------

  private CpConfigHandler(Path configPath) {
    if (configPath == null) {
      throw new Error("Config File Path must not be null");
    }
    Path path = configPath.toAbsolutePath();
    File file = path.toFile();

    if (file.isDirectory()) {
      this.configDirPath = Path.of(file.getAbsolutePath());
      this.configFilePath = Path.of(file.getAbsolutePath(), DEFAULT_CONFIG_FILE);
    } else if (file.isFile()) {
      this.configDirPath = Path.of(file.getParent());
      this.configFilePath = Path.of(file.getAbsolutePath());
    } else if (!file.exists()) {
      // We lazily create the actual file. When the file doesn't actually exist, we have to guess
      // at whether it's a directory path, or a file path.
      this.configDirPath = Path.of(file.getParent());
      this.configFilePath = Path.of(file.getAbsolutePath());
    }

    this.configDirPathStr = configDirPath.toAbsolutePath().toString();
    this.configFilePathStr = configFilePath.toAbsolutePath().toString();

    mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  // TODO Extract this to an Abstract Class...
  private Config config;
  private ObjectMapper mapper;

  private String configFilePathStr;
  private String configDirPathStr;

  private Path configFilePath;
  private Path configDirPath;

  // ------------------------------------------------------------------------------------------------------------------


  @Override
  public Config load() {
    ensureConfigFileExists();
    try {
      return mapper.readValue(configFilePath.toFile(), Config.class);
    } catch (IOException e) {
      log.warn("Could not load config file: {}", configFilePathStr);
      return new Config();
    }
  }

  // How to implement a reload handler?
  @Override
  public Config reload() {
    return load();
  }

  // How to implement file locking?
  @Override
  public void save(Config config) {
    ensureConfigFileExists();
    try {
      mapper.writeValue(Path.of(configFilePathStr).toFile(), config);
    } catch (IOException e) {
      log.warn("Could not write config file: {}", configFilePathStr);
    }
  }

  // ------------------------------------------------------------------------------------------------------------------

  private void ensureConfigFileExists() {
    // TODO Only create the file when allowed by configurations. Default to 'false'
    // TODO Need to create the config file if it doesn't exit.
    File configDir = configDirPath.toFile();
    File configFile = configFilePath.toFile();

    if (!configDir.exists()) {
      if (configDir.mkdirs()) {
        configDir.setReadable(true);
        configDir.setWritable(true);
      }
    }

    if (!configFile.exists()) {
      try {
        configFile.createNewFile();
      } catch (IOException ioException) {
        log.warn("Cannot create config file ({})", configFilePathStr);
      }
    }

    if (configFile.isFile()) {
      if (!configFile.canRead() && !configFile.canWrite()) {
        log.warn("Cannot read or write config file ({})", configFilePathStr);
      } else if (!configFile.canWrite()) {
        log.warn("Cannot write config file ({})", configFilePathStr);
      } else if (!configFile.canRead()) {
        log.warn("Cannot read config file ({})", configFilePathStr);
      }
    } else {
      log.warn("Path is not a file ({})");
    }
  }
}
