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

import org.jline.builtins.Commands;
import org.jline.builtins.ConfigurationPath;
import org.jline.builtins.Less;
import org.jline.builtins.Nano;
import org.jline.builtins.Options;
import org.jline.builtins.Source;
import org.jline.terminal.Terminal;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class KifCommands extends Commands {

  public static void less(Terminal terminal, InputStream in, PrintStream out, PrintStream err,
                          Path currentDir, String[] argv) throws Exception {
    Commands.less(terminal, in, out, err, currentDir, argv);
  }

  public static void less(Terminal terminal, InputStream in, PrintStream out, PrintStream err,
                          Path currentDir, String[] argv, ConfigurationPath configPath) throws Exception {
    Commands.less(terminal, in, out, err, currentDir, argv, configPath);
  }

  public static void help(Terminal terminal, InputStream in, PrintStream out,
                          Path currentDir, String[] argv) throws Exception {

  }

  public static void nano(Terminal terminal, PrintStream out, PrintStream err,
                          Path currentDir, String[] argv) throws Exception {
    Commands.nano(terminal, out, err, currentDir, argv);
  }

  public static void nano(Terminal terminal, PrintStream out, PrintStream err,
                          Path currentDir, String[] argv, ConfigurationPath configPath) throws Exception {
    Commands.nano(terminal, out, err, currentDir, argv, configPath);
  }

  public static void echo(Terminal terminal, PrintStream out, PrintStream err,
                          Path currentDir, String[] argv) throws Exception {
    echo(terminal, out, err, currentDir, argv, null);
  }

  public static void echo(Terminal terminal, PrintStream out, PrintStream err,
                          Path currentDir, String[] argv,
                          ConfigurationPath configPath) throws Exception {

    Options opt = Options.compile(EchoCommand.usage()).parse(argv);
    if (opt.isSet("help")) {
      throw new Options.HelpException(opt.usage());
    }
    EchoCommand echo = new EchoCommand(terminal, currentDir, opt, configPath);
    //echo.open(opt.args());
    echo.run(argv);
  }

  public static void datagen(Terminal terminal, PrintStream out, PrintStream err,
                             Path currentDir, String[] argv) throws Exception {
    datagen(terminal, out, err, currentDir, argv, null);
  }

  public static void datagen(Terminal terminal, PrintStream out, PrintStream err,
                             Path currentDir, String[] argv, ConfigurationPath configPath) throws Exception {
    Options opt = Options.compile(DataGenCommand.usage()).parse(argv);
    if (opt.isSet("help")) {
      throw new Options.HelpException(opt.usage());
    }
    DataGenCommand generator = new DataGenCommand();
//    generator.open(opt.args());
    generator.run(argv);
  }

  public static void alias(Terminal terminal, PrintStream out, PrintStream err,
                           Path currentDir, String[] argv) throws Exception {
    alias(terminal, out, err, currentDir, argv, null);
  }

  public static void alias(Terminal terminal, PrintStream out, PrintStream err,
                           Path currentDir, String[] argv, ConfigurationPath configPath) throws Exception {
    Options opt = Options.compile(DataGenCommand.usage()).parse(argv);
    if (opt.isSet("help")) {
      throw new Options.HelpException(opt.usage());
    }
  }
}