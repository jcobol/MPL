/*
 * Minecraft Programming Language (MPL): A language for easy development of command block
 * applications including an IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * This file is part of MPL.
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
 * Minecraft Programming Language (MPL): Eine Sprache für die einfache Entwicklung von Commandoblock
 * Anwendungen, inklusive einer IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * Diese Datei ist Teil von MPL.
 *
 * MPL ist freie Software: Sie können diese unter den Bedingungen der GNU General Public License,
 * wie von der Free Software Foundation, Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
 * veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 *
 * MPL wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG,
 * bereitgestellt; sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN
 * BESTIMMTEN ZWECK. Siehe die GNU General Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit MPL erhalten haben. Wenn
 * nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.adrodoc55.minecraft.mpl.ast.chainparts;

import java.util.Collection;

import de.adrodoc55.commons.CopyScope.Copyable;
import de.adrodoc55.commons.Named;
import de.adrodoc55.minecraft.mpl.ast.Conditional;
import de.adrodoc55.minecraft.mpl.ast.MplNode;
import de.adrodoc55.minecraft.mpl.blocks.MplBlock;
import de.adrodoc55.minecraft.mpl.commands.Mode;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.ChainLink;
import de.adrodoc55.minecraft.mpl.interpretation.IllegalModifierException;

/**
 * @author Adrodoc55
 */
public interface ChainPart extends MplNode, Named, Copyable, Dependable {
  /**
   * Set the {@link Mode} of this {@link ChainPart} (optional operation).
   * <p>
   * Using {@link #setMode(Mode)} primarily affects the first of the generated {@link ChainLink}s.
   * <p>
   * Subclasses should override this method if they support multiple {@link Mode}s.
   *
   * @param mode {@link Mode}
   * @throws IllegalModifierException if this {@link ChainPart} cannot possess the given
   *         {@link Mode}
   */
  default void setMode(Mode mode) throws IllegalModifierException {
    throw new IllegalModifierException(
        "The class " + getClass() + " does not support multiple modes");
  }

  /**
   * Set the conditionality of this {@link ChainPart} (optional operation).
   * <p>
   * Using {@link #setConditional(Conditional)} primarily affects the first of the generated
   * {@link ChainLink}s.
   * <p>
   * Subclasses should override this method if they support beeing conditional.
   *
   * @param conditional {@link Conditional}
   * @throws IllegalModifierException if this {@link ChainPart} cannot be conditional
   */
  default void setConditional(Conditional conditional) throws IllegalModifierException {
    throw new IllegalModifierException(
        "The class " + getClass() + " does not support beeing conditional");
  }

  /**
   * Set whether this {@link ChainPart} needs redstone (optional operation).
   * <p>
   * Using {@link #setNeedsRedstone(boolean)} primarily affects the first of the generated
   * {@link ChainLink}s.
   * <p>
   * Subclasses should override this method if they support the need for a redstone signal.
   *
   * @param needsRedstone boolean
   * @throws IllegalModifierException if this {@link ChainPart} cannot need a redstone signal
   */
  default void setNeedsRedstone(boolean needsRedstone) throws IllegalModifierException {
    throw new IllegalModifierException(
        "The class " + getClass() + " does not support the need for a redstone signal");
  }

  default void targetThisInserts(Collection<ChainPart> chainParts) {}

  default void resolveThisInserts(Collection<MplBlock> blocks) {}
}
