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

package io.firkin.kif.config.context;

import java.util.Map;

public class Config {
  public String version;
  public Boolean disable_update_check;
  public Boolean disable_updates;
  public Boolean no_browser;
  public Map<String, Platform> platforms;
  public Map<String, Credential> credentials;
  public Map<String, Context> contexts;
  public Map<String, ContextState> context_states;
  public String current_context;
  public String anonymous_id; // a UUID
}
