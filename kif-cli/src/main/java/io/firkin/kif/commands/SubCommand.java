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

import org.apache.commons.text.WordUtils;

/**
 * The Base class for all Kif Sub Commands
 */
public abstract class SubCommand {

  private static final String[] KIF_UGH = {
      "                     „„-~^**¯¯¯¯¯¯**^~-„„ ",
      "                  .„-^*''¯: : : : : : : : : : : : : : : *-„ ",
      "                .„-^*'': : : : : : : : : : : : : : : : : : : : : *-„ ",
      "              ..„-^*: : : : : : : : : : : : : : : : : : : : : : : : : : '\\ ",
      "             .„-^*: : : : : : : : : : : : : : : : : : : : : : : : : : : : : '\\ ",
      "            „-*: : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : :| ",
      "           „-*: : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : '| ",
      "          „-*: : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : :'| ",
      "         ../: : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : :| ",
      "         /': : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : | ",
      "        ../: : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : :| ",
      "       .../': : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : | ",
      "      _„-^*: : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : / ",
      "     „-*: : : : : : : : : : : : : : : : : :„: : : : : : : : : : : : : : : : : : : : : : : :/ ",
      "    ../: : : : :„: : : : : : : : : : : : : : :*^~-----„„„„„„„„„„„„„_: : : : : : : : : : : '/ ",
      "    ..\\: : „„-*-„: : : : : : : : : : : : : : / . . . . . . . . . . . .¯|: : : : : : : : : '/ ",
      "     ,^* . . . *-„: : : : : : : : : : : /' . . . . . . . . . . . . . |: : : : : : : : : / ",
      "     | . . . . . . *-„: : : : : : : : : / . . . . . . . . . . . . . '/: : : : : : : : :'/ ",
      "     .\\ . . . . . . . \\: : : : : : : : | . . . . . . . . . . . . . /: : : : : : : : : / ",
      "     ..\\ . . . . . . . '\\: : : : : : : :| . . . . . . . . . . . .'/: : : : : : : : : :|„„-^^*****^„ ",
      "      .\\ .„„. . . . . |: : : : : : : :\\*^„ . . . . . . . .„-*: : : : : : : : „-^*: : : : :„-~^* ",
      "      ..*-„;\\ . . . ./: : : : : : : : *-„;\\ . . . .„„-^*: : : : : : : : : *: : : : : „-* ",
      "       ...*^~~^*: : : : : : : : : : :*^^^*¯: : : : : : : : : : : : : : : : : / ",
      "        ..\\: : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : : '/",
      "         \\: : : : : :„: : :„: : : : : : : : : : : : : : : : : : : : : : :/ ",
      "         .*-„: : : : *: : : *-: : : : : : : : : : : : : : : : : : : : :'| ",
      "          .*-„„: : : : : : : : : : : : : : : : : : : : : : : : : : : :| ",
      "           ..*~„: : : : : : : : : : : : : : : : : : : : : : : : : :| ",
      "            ...\\: : : : : : : : : : : : : : : : : : : : : : : : : \\_„„„„_        ..__ ",
      "             ..\\: : : : : : : : : : _„„„„-~~~-„: : : : : : : '\\;;;;;;*-„     .„-^**¯„--„¯*-„ ",
      "             ...|: : : :_„„„„-^^**-„: );;;/„„„„„-*: : : : : : : :\\;;;;;;;;\\   .„-*: : : /: : : :\\ : | ",
      "              ¯***¯   (¯: ^**¯: : : : : : : : : : : :„*-;;;;;;;\\ ..„-* : : : :/: : : : : '| :| ",
      "                   ..*~----„„: : : : : : : : : :„-*;;;;;;;;;;;;\\.„-* : : : : '/: : : : : : | :| ",
      "               ../¯¯¯¯**^~-„-*;;;;;*-„: : : : : ,-^*;;;;;;;;;;;;;;;;/ : : : : : :/: : : : : : : | '| ",
      "               .| : : : : : : : *-„;;;;;;;;;\\: : : : /;;;;;;;;;;;;;;;;;;/ : : : : : :'|~--„„: : : : :| :| ",
      "               ..\\ : : : : : : : : *-„;;;;;;;'\\: : :|';;;;;;;;;;;;;;;;;;| : : : : : : '|;;;;;;;*-„: : :| '| ",
      "                \\ : : : : : : : : : :*-„_;;;\\„„„|;;;;;;;;;;„„„-^^**| : : : : : : |;;;;;;;;;;;*-„:| | ",
      "                ..\\ : : : : : : : : : :|;;¯**;;;¯**^^^^*;;;;;;;;;;\\ : : : : : :|;;;;;;;;;;;;;;;*-„ ",
      "                 *-„ : : : : : : : :|;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\\ : : : : :|;;;;;;;;;;;;;;;;;;;*-„ ",
      "                 ....*-„ : : : : : :|;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;*-„ : : : :\\;;;;;;;;;;;;;;*-„"
  };

  public static String[] usage() {
    return KIF_UGH;
  }

  public static String[] man() {
    return KIF_UGH;
  }

  public void run(String args[]) {
    System.out.println();
    System.out.println(usage());
    System.out.println();
    System.out.println("  Ugh.");
  }

  /**
   * Run a command as a background process
   *
   * @param args
   */
  public void start(String args[]) {
    this.run(args);
  }

  public static class HelpLine {
    public final String name;
    public final String description;

    public HelpLine(String name, String description) {
      this.name = name;
      this.description = description;
    }

    private HelpLine() {
      this("kif", "ugh");
    }
  }

  public static class HelpFormatter {
    private final String firstLineFormat;
    private final String wrapLinePrefix;
    private final int    descLength;

    public HelpFormatter(int indentLevel, int nameWidth, int lineWidth) {

      int wrapIndent = indentLevel*2 + nameWidth;
      descLength = lineWidth - wrapIndent;

      StringBuilder sb = new StringBuilder(wrapIndent);
      for (int i=0; i<wrapIndent; i++) {
        sb.append(' ');
      }
      wrapLinePrefix = sb.toString();

      sb = new StringBuilder(indentLevel * 2);
      for (int i=0; i<indentLevel; i++) {
        sb.append(' ').append(' ');
      }
      firstLineFormat = sb.append("s%1$"+nameWidth+"s %2s").toString();
    }

    public String format(HelpLine line) {
      String desc = WordUtils.wrap(line.description, descLength, wrapLinePrefix, true);
      return String.format(firstLineFormat, line.name, desc);
    }
  }
}
