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
package de.adrodoc55.minecraft.mpl.materialize.process;

import static de.adrodoc55.minecraft.mpl.ast.Conditional.CONDITIONAL;
import static de.adrodoc55.minecraft.mpl.ast.Conditional.UNCONDITIONAL;
import static de.adrodoc55.minecraft.mpl.compilation.CompilerOptions.CompilerOption.DEBUG;

import java.util.Iterator;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.adrodoc55.minecraft.mpl.ast.chainparts.MplCommand;
import de.adrodoc55.minecraft.mpl.ast.chainparts.program.MplProcess;
import de.adrodoc55.minecraft.mpl.chain.CommandChain;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.ChainLink;
import de.adrodoc55.minecraft.mpl.compilation.CompilerOptions;
import de.adrodoc55.minecraft.mpl.compilation.MplCompilerContext;
import de.adrodoc55.minecraft.mpl.version.MinecraftVersion;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MplProcessMaterializerTest_OhneTransmitter extends MplProcessMaterializerTest {
  @Override
  protected MplCompilerContext newContext() {
    CompilerOptions options = new CompilerOptions(DEBUG);
    return new MplCompilerContext(MinecraftVersion.getDefault(), options);
  }

  // @formatter:off
  // ----------------------------------------------------------------------------------------------------
  //    ____
  //   |  _ \  _ __  ___    ___  ___  ___  ___
  //   | |_) || '__|/ _ \  / __|/ _ \/ __|/ __|
  //   |  __/ | |  | (_) || (__|  __/\__ \\__ \
  //   |_|    |_|   \___/  \___|\___||___/|___/
  //
  // ----------------------------------------------------------------------------------------------------
  // @formatter:on

  @Test
  @Override
  public void test_a_nameless_process_doesnt_have_startup_commands() {
    // given:
    MplCommand first = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand second = some($MplCommand()//
        .withPrevious(first)//
        .withConditional($oneOf(UNCONDITIONAL, CONDITIONAL)));
    MplProcess process = some($MplProcess()//
        .withName((String) null)//
        .withChainParts(listOf(first, second)));

    // when:
    CommandChain result = visitProcess(process);

    // then:
    Iterator<ChainLink> it = result.getCommands().iterator();
    assertThat(it.next()).matches(first);
    assertThat(it.next()).matches(second);
    assertThat(it).isEmpty();
  }
}
