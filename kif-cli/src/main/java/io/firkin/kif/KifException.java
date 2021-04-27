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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class KifException extends Exception {

  public KifException() {
    super();
  }

  public KifException(String message) {
    super(message);
  }

  public KifException(String message, Throwable cause) {
    super(message, cause);
  }

  public KifException(String message, String... parts) {
    this(String.format(message, parts));
  }

  public KifException(String message, Throwable cause, String... parts) {
    this(String.format(message, parts), cause);
  }

  public static class Reason {
    public Reason(String message, String... parts) {
      this.message = message;
      this.parts = parts;
      Throwable t = new Throwable();
      // of the caller...
      this.filename = t.getStackTrace()[1].getFileName();
      this.linenum = t.getStackTrace()[1].getLineNumber();
    }

    public Reason(String message, Throwable cause, String... parts) {
      this.message = message;
      this.parts = parts;
      this.cause = cause;
      this.filename = cause.getStackTrace()[1].getFileName();
      this.linenum = cause.getStackTrace()[1].getLineNumber();

    }

    String message;
    String[] parts;
    Throwable cause;
    String filename;
    int linenum;
    // Or track the
  }

  public static Builder<KifException> builder() {
    return new Builder<>(new KifException());
  }

  public static <T extends Throwable> Builder<T> builder(Throwable t) {
    return new Builder(new KifException());
  }

  public static class Builder<T extends Throwable> {
    private T cause;
    private Reason[] reasons;
    private int nReasons = 0;
    private Logger log = LoggerFactory.getLogger(Builder.class);

    private Builder(T ex) {
      this.reasons = new Reason[8];
      this.cause = ex;
    }

    public Builder because(Throwable cause) {
      add(new Reason("", cause));
      return this;
    }

    public Builder add(Reason reason) {
      synchronized (this) {
        if (nReasons + 1 >= reasons.length) {
          // not great for large numbers of reasons.
          reasons = Arrays.copyOf(reasons, nReasons + 8);
        }
        reasons[nReasons++] = reason;
      }
      return this;
    }

    public Builder to(Logger log) {
      this.log = log;
      return this;
    }

    public Builder error() {
      if (log.isErrorEnabled()) {
        Arrays.stream(reasons, 0, nReasons)
            .forEach(reason -> log.error(String.format(reason.message, reason.parts)));
      }
      return this;
    }

    public Builder warn() {
      if (log.isWarnEnabled()) {
        Arrays.stream(reasons, 0, nReasons)
            .forEach(reason -> log.warn(String.format(reason.message, reason.parts)));
      }
      return this;
    }

    public Builder info() {
      if (log.isInfoEnabled()) {
        Arrays.stream(reasons, 0, nReasons)
            .forEach(reason -> log.info(String.format(reason.message, reason.parts)));
      }
      return this;
    }

    public Builder debug() {
      if (log.isDebugEnabled()) {
        Arrays.stream(reasons, 0, nReasons)
            .forEach(reason -> log.debug(String.format(reason.message, reason.parts)));
      }
      return this;
    }

    public Builder trace() {
      if (log.isTraceEnabled()) {
        Arrays.stream(reasons, 0, nReasons)
            .forEach(reason -> log.trace(String.format(reason.message, reason.parts)));
      }
      return this;
    }

    public void toss() throws T {
      throw cause;
    }
  }
}
