/*
 * MPL (Minecraft Programming Language): A language for easy development of commandblock
 * applications including and IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * This file is part of MPL (Minecraft Programming Language).
 *
 * MPL is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MPL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MPL. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 *
 * MPL (Minecraft Programming Language): Eine Sprache für die einfache Entwicklung von Commandoblock
 * Anwendungen, beinhaltet eine IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * Diese Datei ist Teil von MPL (Minecraft Programming Language).
 *
 * MPL ist Freie Software: Sie können es unter den Bedingungen der GNU General Public License, wie
 * von der Free Software Foundation, Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
 * veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 *
 * MPL wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG,
 * bereitgestellt; sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN
 * BESTIMMTEN ZWECK. Siehe die GNU General Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit MPL erhalten haben. Wenn
 * nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.adrodoc55.minecraft.mpl;

public class ReferencingCommand extends Command {
  private final String head;
  private final String tail;
  private int relative;
  private boolean referenceInserted = false;

  public ReferencingCommand(String head, String tail, int relative) {
    this(head, tail, relative, null);
  }

  public ReferencingCommand(String head, String tail, int relative, Boolean conditional) {
    this(head, tail, relative, null, conditional);
  }

  public ReferencingCommand(String head, String tail, int relative, Mode mode,
      Boolean conditional) {
    this(head, tail, relative, mode, conditional, null);
  }

  public ReferencingCommand(String head, String tail, int relative, Mode mode, Boolean conditional,
      Boolean needsRedstone) {
    super(null, mode, conditional, needsRedstone);
    this.head = head;
    this.tail = tail;
    this.relative = relative;
  }

  public void addToRelative(int r) {
    relative += r;
  }

  @Override
  public String getCommand() {
    if (referenceInserted) {
      return super.getCommand();
    }
    int abs = Math.abs(relative);
    String operator = relative < 0 ? "-" : "+";
    return head + "${this " + operator + " " + abs + "}" + tail;
  }

  @Override
  public void setCommand(String command) {
    super.setCommand(command);
    referenceInserted = true;
  }
}