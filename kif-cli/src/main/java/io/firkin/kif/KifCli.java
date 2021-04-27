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

import io.firkin.kif.commands.HelpCommand;
import io.firkin.kif.commands.KifCommandRegistry;
import org.jline.builtins.Completers.TreeCompleter;
import org.jline.builtins.Options;
import org.jline.console.CommandRegistry;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.MaskingCallback;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jline.builtins.Completers.TreeCompleter.node;
import static org.jline.terminal.Terminal.Signal;
import static org.jline.utils.InfoCmp.Capability;

/**
 * Kif CLI KifCli
 * <p>
 * Kif Daemon?
 * <p>
 * Kif UI?
 */
public class KifCli {
  private static final String PROMPT = "kif> ";
  private static final String VERSION = "0.1.0";

  private static final String[] OPTIONS = {
    "  -v --verbose       produce more verbose output",
    "  -q --quiet         produce less verbose output",
    "  -i, --interactive  run in interactive mode"
  };

  private static final String[] USAGE = {
      "kif - Kafka Improved interFace, a cli utility for interacting with the confluent ecosystem.",
      "Usage: ",
      "  kif [options] [command]",
      "",
      "Available Commands:",
      "  alias",
      "  config             config",
      "  echo               print back a message",
      "  gen                generate data randomly or from a file",
      "  help               help about any command",
      "  man                more version manual page",
      "  motion             run a motion stream",
      "  version            print version information",
      "  top                print performance metrics",
      "",
      "Options: ",
      OPTIONS[0],
      OPTIONS[1],
      OPTIONS[2]
  };

  public static Options globalOpts;
  // public static Options globalEnvs; // Environment Variables?

  public static CommandRegistry cmdRegistry;

  // TODO Move this to a "io.firkin.kif.command.*" package?
  public static void help(String cmd) {
    String[] help = {
        "List of available commands:"
        , "  Options:"
        , "  Commands:"
//          , "    --                 run the remaining commands in script mode"
        , "    echo               print back a message"
        , "    history            list history of commands"
        , "    version            prints kif version information"
//            , "    less               file pager"
//            , "    nano               nano editor"
//            , "    ttop               display and update sorted information about threads"
//            , "    setopt             set options"
//            , "    unsetopt           unset options"
//            , "    ssh: ??"
        , "    clear              clear screen"
        , "    q, quit, exit      exit from example app"
        , "    commands           list available commands"
        , "    help [command]     list available commands"
        , "    kafka | ak:"
        , "      cluster            interact with a cluster"
        , "      clusters           list available kafka clusters"
        , "      topics             list available kafka clusters"
        , "    connect:"
        , "      clusters           list available connect clusters"
        , "    ksql:"
        , "      clusters           list available ksql clusters"
        , "    zookeeper | zk:"
        , "      clusters           list available kafka clusters"
        , "    registry | sr:"
        , "      schemas            list available schemas"
        , "    confluent | cp:"
        , "      login              login to a confluent cloud account"
        , "      logout             logout of a confluent cloud account"
        , "    cloud | ccloud | cc:"
        , "      login              login to a confluent cloud account"
        , "      logout             logout of a confluent cloud account"
        , "      environments       list available environments"
        , "      clusters           list available environments"
        , "      connectors         list available environments"
        , "  Additional help:"
        , "    <command> --help"};

    Stream.of(help).forEach(l -> System.out.println(l));
  }

  public static void usage(String cmd) {
    // TODO Print command-specific usage information.

  }

  public static String[] usage() {
    return USAGE;
  }

  public static String summary() {
    return "kif - Kafka Improved interFace, a cli utility for interacting with the confluent ecosystem.";
  }

  public static void interactive(String[] args) {
    TerminalBuilder terminalBuilder = TerminalBuilder.builder();

    try {
      Map<String, Completer> completers = Map.of(
          "echo", new StringsCompleter("ansi", "rgb"),
          "help", new StringsCompleter("echo", "cluster", "clusters", "topics"),
//                "cluster", new StringsCompleter("<cluster name>", "brokers", "contract", "expand", "restart", "status", "stop"),
          "clusters", new StringsCompleter("list", "start", "stop"),
          "topics", new StringsCompleter("list", "create", "delete"),
//                "login", NullCompleter.INSTANCE,
//                "logout", NullCompleter.INSTANCE,
          "exit", NullCompleter.INSTANCE
      );

//            String mainPrompt = "franz> ";
      String secondaryPrompt = "%M%P > ";
      String rightPrompt = ""; // Use this to track the current cluster/scope

      Completer completer = new TreeCompleter(
          node("clear"),
          node("exit"),
          node("quit"),
          node("q"),
          node("echo",
              node("ansi:"),
              node("rgb:")),
          // history
          node("version"),
          node("commands"),
          node("help"),
          node("kafka", "ak",
              node("brokers"),
              node("cluster"),
              node("clusters"),
              node("topics")),
          node("confluent", "cp",
              node("login"),
              node("logout"),
              node("topics"),
              node("brokers")),
          node("cloud", "cc", "ccloud",
              node("login"),
              node("logout"),
              node("accounts"),
              node("environments"),
              node("clusters"),
              node("connectors")),
          node("zookeeper", "zk"), // What are the three letter wordds?
          node("connect",
              node("workers"),
              node("tasks")),
          node("ksql",
              node("ktables"),
              node("kstreams"),
              node("queries"))
      );
      Parser parser = null;

      // Used to run a daemon process which accept a callback signal?
      //List<Consumer<LineReader>> callback = List.of();

      // TODO Create the "help" command based on the list of other commands.

      for (int idx = 0; idx < args.length; idx++) {
        switch (args[idx]) {
          case "echo":
            break;

          case "help":
            break;

          case "cluster":

            break;

          case "clusters":
            break;

          case "topic":
            break;

          default:
            usage();
            break;
        }
      }

      // Terminal handles the interaction with the console and user input.
      Terminal terminal = terminalBuilder.build();

//            System.out.println(terminal.getName()+":"+terminal.getType());
//            System.out.println("\nhelp: list available commands");

      if (terminal.getWidth() == 0 || terminal.getHeight() == 0) {
        terminal.setSize(new Size(120, 40));   // hard coded terminal size when redirecting
      }

      Thread executeThread = Thread.currentThread();
      terminal.handle(Signal.INT, signal -> executeThread.interrupt());

      LineReader reader = LineReaderBuilder.builder()
          .terminal(terminal)
          .completer(completer)
          .parser(parser)
          .variable(LineReader.SECONDARY_PROMPT_PATTERN, secondaryPrompt)
          .variable(LineReader.INDENTATION, 2)
          .build();

      while (true) {
        String line;
        try {
          line = reader.readLine(PROMPT, rightPrompt, (MaskingCallback) null, null).trim();

          if (line.equalsIgnoreCase("q")
              || line.equalsIgnoreCase("quit")
              || line.equalsIgnoreCase("exit")) {
            break;
          }

          ParsedLine parsedLine = reader.getParser().parse(line, 0);
          List<String> parsedArgsList = parsedLine.words().subList(1, parsedLine.words().size());
          String[] parsedArgs = parsedArgsList.toArray(new String[0]);
          String parsedCmd = parsedLine.word();

          switch (parsedCmd) {

            case "clear":
              terminal.puts(Capability.clear_screen);
              terminal.flush();

            case "echo":
              // TODO Add rgb: and ansi: support
              // "echo", new StringsCompleter("ansi", "rgb"),
              String echoLine = parsedArgsList.stream().collect(Collectors.joining(" "));// TODO Add ansi: support
              terminal.writer().println(echoLine);
              break;

            case "kafka":
            case "ak":
              if (parsedArgsList.isEmpty()) {
                //usage("kafka");
                break;
              }
              String subCmd = parsedArgsList.get(0);
              List<String> akArgsList = parsedArgsList.subList(1, parsedArgsList.size());
              switch (subCmd) {
                case "cluster":
                  terminal.writer().println("Cluster Name, Bootstrap URL, ");
                  break;
                case "clusters":
                  //"clusters", new StringsCompleter("list", "start", "stop"),
                  terminal.writer().println("  kafka-1, kafka-2");
                  terminal.writer().println("  kafka-2");
                  break;
                case "topics":
                  terminal.writer().println("  topic-1");
                  terminal.writer().println("  topic-2");
                  terminal.writer().println("  topic-3");
                  terminal.writer().println("  topic-4");

                  // handle commands
                  break;
              }
            case "confluent":
            case "cp":
              if (parsedArgsList.isEmpty()) {
                //usage(parsedCmd);
                break;
              }
              break;


            case "cloud":
            case "ccloud":
            case "cc":
              if (parsedArgsList.isEmpty()) {
                //usage(parsedCmd);
                break;
              }
              break;

            case "topics":
              //"topics", new StringsCompleter("list", "create", "delete"),
              terminal.writer().println("topic-1, topic-2");
              break;

            // Exit from the Franz interactive terminal.
            case "exit":
            case "q":
            case "quit":
              terminal.writer().println("Goodbye.");
              return;

            // Print detailed manual / usage information.
            case "man":
              if (parsedArgsList.isEmpty()) {
                terminal.writer().println("man <command>");
              } else {
                //man(parsedCmd);
                //usage(parsedCmd);
              }
              break;

            // Print basic help information
            case "help":
              if (!parsedArgsList.isEmpty()) {
                // TODO enable per-command help "help topics", "help clusters", etc.
                //help(parsedCmd);
                //break;
              }

            default:
              help(null);
              break;
          }

//                } catch (HelpException e) {
//                    HelpException.highlight(e.getMessage(), HelpException.defaultStyle()).print(terminal);
        } catch (IllegalArgumentException e) {
          System.err.println(e.getMessage());
        } catch (UserInterruptException e) {
          // Ignore
        } catch (EndOfFileException e) {
          return;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  static {
    cmdRegistry = new KifCommandRegistry();
  }

  public static void main(String[] argv) {

    try {
      globalOpts = Options.compile(USAGE).parse(argv);

//      System.out.println("Global Opts:");
//      System.out.println(globalOpts.usage());

      // Compile options handler and parse the arguments.
      // TODO Only parse options up to the sub-command?
//      if (argv.length == 0 || globalOpts.isSet("help")) {
//        throw new Options.HelpException(globalOpts.usage());
//      }

//      if (globalOpts.isSet("interactive")) {
//        runInteractively(argv);
//      } else if (globalOpts.isSet("script")) {
        // TODO How to use "--" option to parse a script from args
        //         $> kif -q -- $(jq . "foo")
        // TODO How to enable piping input to kif:
        //         $> cat script.kif | kif
        // TODO More easy: read a file via the -f option:
        //         $> kif -f script.kif
        //         $> kif script.kif
//        runScriptedCommands(argv);
//      } else {
        runSingleCommand(argv);
//      }
//    } catch (Options.HelpException e) {
//      HelpCommand.usage();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }

//    System.out.println("Kif: Hello World!");
//    for(String s: usage()) {
//      System.out.println(s);
//    }
  }

  private static void runInteractively(String[] argv) {
    // TODO Implement something like org.jline.demo.Repl
  }

  // Run (sub) commands piped in as a "kif script"
  private static void runScriptedCommands(String[] argv) {
    // Step through a file, or command-line input -- Need a
  }

  private static void runSingleCommand(String[] args) {
    StringBuilder sb = new StringBuilder("[");
    List.of(args).stream().forEach((s) -> sb.append(s).append(','));
    sb.setCharAt(sb.length()-1, ']');
    System.out.println("runSingleCommand("+sb.toString()+")");

  }
}
