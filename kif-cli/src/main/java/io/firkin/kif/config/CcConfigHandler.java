package io.firkin.kif.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.firkin.kif.KifException;
import io.firkin.kif.KifException.Reason;
import io.firkin.kif.config.context.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class CcConfigHandler implements ConfigHandler {
  private static final Logger log = LoggerFactory.getLogger(CcConfigHandler.class);

  private static final String DOT_CC = ".ccloud";
  private static final String DEFAULT_CONFIG_FILE = "config.json";
  private static final String DEFAULT_CONFIG_DIR =
      System.getProperty("CCLOUD_HOME", System.getProperty("user.home") + "/" + DOT_CC);

  public static ConfigHandler of() {
    return of(Path.of(DEFAULT_CONFIG_DIR, DOT_CC, DEFAULT_CONFIG_FILE));
  }

  // @VisibleForTest
  static ConfigHandler of(Path testConfigFilePath) {
    return new CcConfigHandler(testConfigFilePath);
  }

  private CcConfigHandler(Path configPath) {
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
  public Config load() throws KifConfigException {
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
  public Config reload() throws KifConfigException {
    return load();
  }

  // How to implement file locking?
  @Override
  public void save(Config config) throws KifConfigException {
    ensureConfigFileExists();
    try {
      mapper.writeValue(Path.of(configFilePathStr).toFile(), config);
    } catch (IOException e) {
      log.warn("Could not write config file: {}", configFilePathStr);
    }
  }

  // ------------------------------------------------------------------------------------------------------------------

  private void ensureConfigFileExists() throws KifConfigException {
    // TODO Only create the file when allowed by configurations. Default to 'false'
    // TODO Need to create the config file if it doesn't exit.
    File configDir = configDirPath.toFile();
    File configFile = configFilePath.toFile();

    KifException.Builder<KifConfigException> builder = KifException.builder().to(log);

    if (!configDir.exists()) {
      builder.add(new Reason("CC directory missing ({})", configDirPathStr));
    } else if (!configDir.isDirectory()) {
      builder.add(new Reason("CC directory wrong type ({})", configDirPathStr));
    }

    if (!configFile.exists()) {
      builder.add(new Reason("CC config file missing ({})", configFilePathStr));
    }

    if (!configFile.isFile()) {
      builder.add(new Reason("CC config file wrong type ({})", configFilePathStr));
    }

    if (!configFile.canWrite()) {
      builder.add(new Reason("CC config file not writeable ({})", configFilePathStr));
    }

    if (!configFile.canRead()) {
      builder.add(new Reason("CC config file not writeable ({})", configFilePathStr));
    }

    builder.warn();
    builder.toss();

    log.warn("CC config file not writeable ({})", configFilePathStr);
    log.warn("CC config file not readable ({})", configFilePathStr);
    builder.toss();
  }
}