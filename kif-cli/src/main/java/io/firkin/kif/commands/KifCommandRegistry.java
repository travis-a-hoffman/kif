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

package io.firkin.kif.commands;

import org.jline.console.CmdDesc;
import org.jline.console.CommandMethods;
import org.jline.console.CommandRegistry;
import org.jline.reader.impl.completer.SystemCompleter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KifCommandRegistry implements CommandRegistry {

  private NamedCmdRegistry cmdRegistry;
  private Exception exception;

  /**
   * Returns the command names known by this registry.
   *
   * @return the set of known command names, excluding aliases
   */
  @Override
  public Set<String> commandNames() {
    return null;
  }

  /**
   * Returns a map of alias-to-command names known by this registry.
   *
   * @return a map with alias keys and command name values
   */
  @Override
  public Map<String, String> commandAliases() {
    return null;
  }

  /**
   * Returns a short info about command known by this registry.
   *
   * @param command the command name
   * @return a short info about command
   */
  @Override
  public List<String> commandInfo(String command) {
    return null;
  }

  /**
   * Returns whether a command with the specified name is known to this registry.
   *
   * @param command the command name to test
   * @return true if the specified command is registered
   */
  @Override
  public boolean hasCommand(String command) {
    return false;
  }

  /**
   * Returns a {@code SystemCompleter} that can provide detailed completion
   * information for all registered commands.
   *
   * @return a SystemCompleter that can provide command completion for all registered commands
   */
  @Override
  public SystemCompleter compileCompleters() {
    return null;
  }

  /**
   * Returns a command description for use in the JLine Widgets framework.
   * Default method must be overridden to return sub command descriptions.
   *
   * @param args command (args[0]) and its arguments
   * @return command description for JLine TailTipWidgets to be displayed
   * in the terminal status bar.
   */
  @Override
  public CmdDesc commandDescription(List<String> args) {
    return null;
  }

  // --- Internal Command Registry --------------------------------------------------------------------------------
  // See: See jline: org.jline.console.impl.AbstractCommandRegistry;
  private interface CmdRegistry {
    boolean hasCommand(String command);
    Set<String> commandNames();
    Map<String, String> commandAliases();
    Object command(String command);
    <V extends Enum<V>> void rename(V command, String newName);
    void alias(String alias, String command);
    SystemCompleter compileCompleters();
    CommandMethods getCommandMethods(String command);
  }

  // Non-interactive commands stored by name
  private class NamedCmdRegistry implements CmdRegistry {
    private final Map<String, CommandMethods> commandExecute;
    private final Map<String,String> aliasCommand = new HashMap<>();

    public NamedCmdRegistry(Map<String,CommandMethods> commandExecute) {
      this.commandExecute = commandExecute;
    }

    public Set<String> commandNames() {
      return commandExecute.keySet();
    }

    public Map<String, String> commandAliases() {
      return aliasCommand;
    }

    public <V extends Enum<V>> void rename(V command, String newName) {
      throw new IllegalArgumentException();
    }

    public void alias(String alias, String command) {
      if (!commandExecute.containsKey(command)) {
        throw new IllegalArgumentException("Command does not exists!");
      }
      aliasCommand.put(alias, command);
    }

    public boolean hasCommand(String name) {
      return commandExecute.containsKey(name) || aliasCommand.containsKey(name);
    }

    public SystemCompleter compileCompleters() {
      SystemCompleter out = new SystemCompleter();
      for (String c : commandExecute.keySet()) {
        out.add(c, commandExecute.get(c).compileCompleter().apply(c));
      }
      out.addAliases(aliasCommand);
      return out;
    }

    public String command(String name) {
      if (commandExecute.containsKey(name)) {
        return name;
      } else if (aliasCommand.containsKey(name)) {
        return aliasCommand.get(name);
      }
      return null;
    }

    public CommandMethods getCommandMethods(String command) {
      return commandExecute.get(command(command));
    }

  }

//  private class EnumCmdRegistry implements CmdRegistry { }
//  private class InteractiveCmdRegistry implements CmdRegistry { }
//  private class PluginCmdRegistry implements CmdRegistry { }

}
