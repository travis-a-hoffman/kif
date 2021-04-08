package io.firkin.kif.config;

import io.firkin.kif.KifException;

public class KifConfigException extends KifException {
  public KifConfigException(String message) {
    super(message);
  }

  public KifConfigException(String message, Throwable cause) {
    super(message, cause);
  }

  public KifConfigException(String message, String... parts) {
    super(message, parts);
  }

  public KifConfigException(String message, Throwable cause, String... parts) {
    super(message, cause, parts);
  }
}
