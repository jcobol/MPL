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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Joiner;

import de.adrodoc55.commons.CopyScope;
import de.adrodoc55.minecraft.mpl.ast.ExtendedModifiable;
import de.adrodoc55.minecraft.mpl.ast.visitor.MplAstVisitor;
import de.adrodoc55.minecraft.mpl.commands.Mode;
import de.adrodoc55.minecraft.mpl.compilation.MplSource;
import de.adrodoc55.minecraft.mpl.interpretation.CommandPartBuffer;
import de.adrodoc55.minecraft.mpl.interpretation.ModifierBuffer;
import de.adrodoc55.minecraft.mpl.interpretation.insert.GlobalVariableInsert;
import de.adrodoc55.minecraft.mpl.interpretation.insert.RelativeOriginInsert;
import de.adrodoc55.minecraft.mpl.interpretation.insert.RelativeThisInsert;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.karneim.pojobuilder.GenerateMplPojoBuilder;

/**
 * @author Adrodoc55
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
public class MplCommand extends ModifiableChainPart {
  private final List<Object> commandParts = new ArrayList<>();
  private final List<RelativeThisInsert> thisInserts = new ArrayList<>();
  private final List<RelativeOriginInsert> originInserts = new ArrayList<>();
  private final List<GlobalVariableInsert> variableInserts = new ArrayList<>();

  public MplCommand(String command, @Nonnull MplSource source) {
    this(command, new ModifierBuffer(), source);
  }

  @GenerateMplPojoBuilder
  public MplCommand(String command, ExtendedModifiable modifier, @Nonnull MplSource source) {
    this(new CommandPartBuffer(command), modifier, source);
  }

  public MplCommand(CommandPartBuffer command, ExtendedModifiable modifier,
      @Nonnull MplSource source) {
    super(modifier, source);
    commandParts.addAll(command.getCommandParts());
    thisInserts.addAll(command.getThisInserts());
    originInserts.addAll(command.getOriginInserts());
    variableInserts.addAll(command.getVariableInserts());
  }

  @Deprecated
  protected MplCommand(MplCommand original) {
    super(original);
  }

  @Deprecated
  @Override
  public MplCommand createFlatCopy(CopyScope scope) {
    return new MplCommand(this);
  }

  @Override
  @SuppressWarnings("deprecation")
  public void completeDeepCopy(CopyScope scope) throws NullPointerException {
    super.completeDeepCopy(scope);
    MplCommand original = scope.getCache().getOriginal(this);
    commandParts.addAll(scope.copyObjects(original.commandParts));
    thisInserts.addAll(scope.copyObjects(original.thisInserts));
    originInserts.addAll(scope.copyObjects(original.originInserts));
    variableInserts.addAll(scope.copyObjects(original.variableInserts));
  }

  public String getCommand() {
    return Joiner.on("").join(commandParts);
  }

  @Override
  public String getName() {
    return "command";
  }

  @Override
  public <T> T accept(MplAstVisitor<T> visitor) {
    return visitor.visitCommand(this);
  }

  @Override
  public boolean canBeDependedOn() {
    return true;
  }

  @Override
  public Mode getModeForInverting() {
    return getMode();
  }
}
