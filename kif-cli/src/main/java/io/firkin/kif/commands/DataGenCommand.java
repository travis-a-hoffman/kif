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

public class DataGenCommand extends SubCommand {

  private static final String[] usage = {
      "gen -  generate random data",
      "Usage: gen [COMMAND] [TOPIC]",
      "  -? --help                    Show help",
      "  -o --output=[FORMAT]         Output data in the specified output format: avro, binary, base64, json, or protobuf",
      "  -i --input=[FORMAT]          Input data read in one of avro, binary, base64, json, or protobuf.",
      "  -s --schema=[ID]             Schema for decoding or encoding records.",
      "     --schemafile=[FILE]       Load schema from a file for reading/writing records.",
      "  -f                           Read from a file...",
      "  -u --url=[URL]               Load random data ",
      "     --stats                   Print statis",
      "  -q --quiet                   Do not print "
  };

  public DataGenCommand() {

  }

  public static String[] usage() {
    return usage;
  }

}
