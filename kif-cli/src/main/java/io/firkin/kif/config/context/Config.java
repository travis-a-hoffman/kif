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
