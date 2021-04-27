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

import org.jline.builtins.ConfigurationPath;
import org.jline.builtins.Options;
import org.jline.terminal.Terminal;

import java.nio.file.Path;

public class EchoCommand extends SubCommand {

  public EchoCommand(Terminal terminal, Path currentDir, Options opt, ConfigurationPath configPath) {

  }

  private static final String[] usage = {
    "echo -  print file contents",
    "Usage: nano [OPTIONS] [FILES]",
    "  -? --help                    Show help",
    "  -B --backup                  When saving a file, back up the previous version of it, using the current filename",
    "                               suffixed with a tilde (~).",
    "  -I --ignorercfiles           Don't look at the system's nanorc nor at the user's nanorc.",
    "  -Q --quotestr=regex          Set the regular expression for matching the quoting part of a line.",
    "  -T --tabsize=number          Set the size (width) of a tab to number columns.",
    "  -U --quickblank              Do quick status-bar blanking: status-bar messages will disappear after 1 keystroke.",
    "  -c --constantshow            Constantly show the cursor position on the status bar.",
    "  -e --emptyline               Do not use the line below the title bar, leaving it entirely blank.",
    "  -j --jumpyscrolling          Scroll the buffer contents per half-screen instead of per line.",
    "  -l --linenumbers             Display line numbers to the left of the text area.",
    "  -m --mouse                   Enable mouse support, if available for your system.",
    "  -$ --softwrap                Enable 'soft wrapping'. ",
    "  -a --atblanks                Wrap lines at whitespace instead of always at the edge of the screen.",
    "  -R --restricted              Restricted mode: don't allow suspending; don't allow a file to be appended to,",
    "                               prepended to, or saved under a different name if it already has one;",
    "                               and don't use backup files.",
    "  -Y --syntax=name             The name of the syntax highlighting to use.",
    "  -z --suspend                 Enable the ability to suspend nano using the system's suspend keystroke (usually ^Z).",
    "  -v --view                    Don't allow the contents of the file to be altered: read-only mode.",
    "  -k --cutfromcursor           Make the 'Cut Text' command cut from the current cursor position to the end of the line",
    "  -t --tempfile                Save a changed buffer without prompting (when exiting with ^X).",
    "  -H --historylog=name         Log search strings to file, so they can be retrieved in later sessions",
    "  -E --tabstospaces            Convert typed tabs to spaces.",
    "  -i --autoindent              Indent new lines to the previous line's indentation."
  };

  public static String[] usage() {
    return usage;
  }
}
