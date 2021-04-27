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

public class CCloudCommand {

  public static String[] usage() {
    return new String[] {
        "less -  file pager",
        "Usage: less [OPTIONS] [FILES]",
        "  -? --help                    Show help",
        "  -e --quit-at-eof             Exit on second EOF",
        "  -E --QUIT-AT-EOF             Exit on EOF",
        "  -F --quit-if-one-screen      Exit if entire file fits on first screen",
        "  -q --quiet --silent          Silent mode",
        "  -Q --QUIET --SILENT          Completely silent",
        "  -S --chop-long-lines         Do not fold long lines",
        "  -i --ignore-case             Search ignores lowercase case",
        "  -I --IGNORE-CASE             Search ignores all case",
        "  -x --tabs=N[,...]            Set tab stops",
        "  -N --LINE-NUMBERS            Display line number for each line",
        "  -Y --syntax=name             The name of the syntax highlighting to use.",
        "     --no-init                 Disable terminal initialization",
        "     --no-keypad               Disable keypad handling",
        "     --ignorercfiles           Don't look at the system's lessrc nor at the user's lessrc.",
        "  -H --historylog=name         Log search strings to file, so they can be retrieved in later sessions"
    };
  }

}
