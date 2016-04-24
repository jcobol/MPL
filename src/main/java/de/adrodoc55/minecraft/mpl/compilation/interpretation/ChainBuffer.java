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
package de.adrodoc55.minecraft.mpl.compilation.interpretation;

import java.util.LinkedList;

import de.adrodoc55.minecraft.mpl.commands.Mode;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.Command;
import de.adrodoc55.minecraft.mpl.commands.chainparts.ChainPart;

/**
 * @author Adrodoc55
 */
class ChainBuffer {

  private String name;
  private boolean install;
  private boolean uninstall;
  private boolean process;
  private boolean script;
  private boolean repeatingProcess;
  private boolean repeatingContext;
  protected final LinkedList<ChainPart> chainParts = new LinkedList<>();

  public boolean add(ChainPart cp) {
    updateContext(cp);
    return chainParts.add(cp);
  }

  @Deprecated
  public void add(int index, ChainPart cp) {
    updateContext(cp);
    chainParts.add(index, cp);
  }

  @Deprecated
  private void updateContext(ChainPart cp) {
    if (cp instanceof Command) {
      Command command = (Command) cp;
      if (command.getMode() == Mode.IMPULSE) {
        setRepeatingContext(false);
      } else if (command.getMode() == Mode.REPEAT) {
        setRepeatingContext(true);
      }
    }
  }

  /**
   * Returns the name of the current Context. The name is defined by the identifier if this is a
   * process. If this is a script this method will throw an {@link IllegalStateException}
   *
   * @return name
   * @throws IllegalStateException
   */
  @Deprecated
  public String getName() throws IllegalStateException {
    if (isProcess()) {
      if (name != null) {
        return name;
      } else {
        throw new IllegalStateException("The name of this process has not been set.");
      }
    } else {
      throw new IllegalStateException("This chain is not a process.");
    }
  }

  public void setName(String name) {
    this.name = name;
  }

  @Deprecated
  public boolean isInstall() {
    return install;
  }

  public void setInstall(boolean install) {
    this.install = install;
  }

  @Deprecated
  public boolean isUninstall() {
    return uninstall;
  }

  public void setUninstall(boolean uninstall) {
    this.uninstall = uninstall;
  }

  @Deprecated
  public boolean isProcess() {
    return process;
  }

  public void setProcess(boolean process) {
    this.process = process;
  }

  @Deprecated
  public boolean isScript() {
    return script;
  }

  public void setScript(boolean script) {
    this.script = script;
  }

  @Deprecated
  public boolean isRepeatingProcess() {
    return repeatingProcess;
  }

  public void setRepeatingProcess(boolean repeatingProcess) {
    this.repeatingProcess = repeatingProcess;
  }

  @Deprecated
  public boolean isRepeatingContext() {
    return repeatingContext;
  }

  public void setRepeatingContext(boolean repeatingContext) {
    this.repeatingContext = repeatingContext;
  }

  public LinkedList<ChainPart> getChainParts() {
    return chainParts;
  }

}
