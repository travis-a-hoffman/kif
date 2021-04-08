package io.firkin.kif.config;

import io.firkin.kif.config.context.Config;

public interface ConfigHandler {
  Config load() throws KifConfigException;

  // How to implement a reload handler?
  Config reload() throws KifConfigException;

  // How to implement file locking?
  void save(Config config) throws KifConfigException;
}
