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
package de.adrodoc55.minecraft.mpl.compilation;

import static de.adrodoc55.TestBase.$String
import static de.adrodoc55.TestBase.$int
import static de.adrodoc55.TestBase.some
import static de.adrodoc55.minecraft.mpl.MplTestBase.$Identifier
import static org.apache.commons.io.FilenameUtils.getBaseName

import org.junit.Test;

import de.adrodoc55.minecraft.mpl.MplSpecBase;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplCommand
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplIf
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplWhile
import de.adrodoc55.minecraft.mpl.ast.chainparts.program.MplProcess
import de.adrodoc55.minecraft.mpl.ast.chainparts.program.MplProgram
import de.adrodoc55.minecraft.mpl.ast.variable.MplIntegerVariable
import de.adrodoc55.minecraft.mpl.ast.variable.MplSelectorVariable
import de.adrodoc55.minecraft.mpl.ast.variable.MplStringVariable
import de.adrodoc55.minecraft.mpl.ast.variable.MplValueVariable
import de.adrodoc55.minecraft.mpl.ast.variable.MplVariable
import de.adrodoc55.minecraft.mpl.ast.variable.selector.TargetSelector
import de.adrodoc55.minecraft.mpl.ast.variable.type.MplType;
import de.adrodoc55.minecraft.mpl.ast.variable.value.MplScoreboardValue
import de.adrodoc55.minecraft.mpl.ast.variable.value.MplValue
import de.adrodoc55.minecraft.mpl.compilation.CompilerOptions.CompilerOption
import de.adrodoc55.minecraft.mpl.interpretation.MplInterpreter
import de.adrodoc55.minecraft.mpl.interpretation.variable.VariableScope
import de.adrodoc55.minecraft.mpl.version.MinecraftVersion
import spock.lang.Unroll;

public class MplVariableSpec extends MplSpecBase {
  File lastProgramFile

  private MplProgram assembleProgram(String program, File file = newTempFile(), CompilerOption... options) {
    file.text = program
    assembleProgram(file, options)
  }

  private MplProgram assembleProgram(File programFile, CompilerOption... options) {
    lastProgramFile = programFile
    MplCompiler compiler = new MplCompiler(MinecraftVersion.getDefault(), new CompilerOptions(options))
    lastContext = compiler.provideContext()
    compiler.assemble(programFile)
  }

  private List<List<MplType>> typeCombinations() {
    List<List<MplType>> result = [MplType.values(), MplType.values()].combinations()
    result.removeIf { List<MplType> list -> list[0] == list[1] }
    result.removeIf { List<MplType> list -> list[0] == MplType.VALUE && list[1] == MplType.INTEGER }
    return result
  }

  private String valueForType(MplType type) {
    if (type == MplType.INTEGER) {
      return String.valueOf(some($int()))
    } else if (type == MplType.SELECTOR) {
      return '@e'
    } else if (type == MplType.STRING) {
      return '"' + some($String()) + '"'
    } else if (type == MplType.VALUE) {
      return '@e ' + some($Identifier())
    }
  }

  @Test
  @Unroll("Type mismatch at local script variable declaration from #actualType to #declaredType")
  public void "Type mismatch at local script variable declaration"(MplType declaredType, MplType actualType, String value) {
    given:
    String id = some($Identifier())
    String programString = """
    ${declaredType} ${id} = ${value}
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    interpreter.rootVariableScope.variables.isEmpty()

    lastContext.errors[0].message == "Type mismatch: cannot convert from ${actualType} to ${declaredType}"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == value
    lastContext.errors[0].source.lineNumber == 2
    lastContext.errors.size() == 1

    where:
    [declaredType, actualType]<< typeCombinations()
    value = valueForType(actualType)
  }

  @Test
  @Unroll("Type mismatch at global variable declaration from #actualType to #declaredType")
  public void "Type mismatch at global variable declaration"(MplType declaredType, MplType actualType, String value) {
    given:
    String id = some($Identifier())
    String programString = """
    ${declaredType} ${id} = ${value}
    impulse process main {}
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    interpreter.rootVariableScope.variables.isEmpty()

    lastContext.errors[0].message == "Type mismatch: cannot convert from ${actualType} to ${declaredType}"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == value
    lastContext.errors[0].source.lineNumber == 2
    lastContext.errors.size() == 1

    where:
    [declaredType, actualType]<< typeCombinations()
    value = valueForType(actualType)
  }

  @Test
  @Unroll("Type mismatch at local variable declaration from #actualType to #declaredType")
  public void "Type mismatch at local variable declaration"(MplType declaredType, MplType actualType, String value) {
    given:
    String id = some($Identifier())
    String programString = """
    impulse process main {
      ${declaredType} ${id} = ${value}
    }
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    interpreter.rootVariableScope.variables.isEmpty()

    lastContext.errors[0].message == "Type mismatch: cannot convert from ${actualType} to ${declaredType}"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == value
    lastContext.errors[0].source.lineNumber == 3
    lastContext.errors.size() == 1

    where:
    [declaredType, actualType]<< typeCombinations()
    value = valueForType(actualType)
  }

  @Test
  public void "Declaring a duplicate global variable"() {
    given:
    String id = some($Identifier())
    String programString = """
    Integer ${id} = ${some($int())}
    Integer ${id} = ${some($int())}
    impulse process main {}
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors[0].message == "Duplicate variable ${id}"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 3
    lastContext.errors.size() == 1
  }

  @Test
  public void "Declaring a global and a local variable with the same name"() {
    given:
    String id = some($Identifier())
    String programString = """
    Integer ${id} = ${some($int())}
    impulse process main {
      Integer ${id} = ${some($int())}
    }
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()
    interpreter.rootVariableScope.variables.containsKey(id)
    interpreter.rootVariableScope.children[0].variables.containsKey(id)
  }

  @Test
  public void "Declaring a global Integer variable"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    Integer ${id} = ${value}
    impulse process main {}
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()

    VariableScope scope = interpreter.rootVariableScope
    MplIntegerVariable variable = scope.variables[id]
    variable != null
    variable.value == value
  }

  @Test
  public void "Declaring a global Selector variable"() {
    given:
    String id = some($Identifier())
    String value = "@e[name=${some($Identifier())}]"
    String programString = """
    Selector ${id} = ${value}
    impulse process main {}
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()

    VariableScope scope = interpreter.rootVariableScope
    MplVariable<TargetSelector> variable = scope.findVariable(id)
    variable != null
    variable.value instanceof TargetSelector
    variable.value.toString() == value
  }

  @Test
  public void "Declaring a global String variable"() {
    given:
    String id = some($Identifier())
    String value = some($String())
    String programString = """
    String ${id} = "${value}"
    impulse process main {}
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()

    VariableScope scope = interpreter.rootVariableScope
    MplStringVariable variable = scope.findVariable(id)
    variable != null
    variable.value == value
  }

  @Test
  public void "Declaring a global Value variable"() {
    given:
    String id = some($Identifier())
    String selector = "@e[name=${some($Identifier())}]"
    String scoreboard = some($Identifier())
    String programString = """
    Value ${id} = ${selector} ${scoreboard}
    impulse process main {}
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()

    VariableScope scope = interpreter.rootVariableScope
    MplVariable<MplValue> variable = scope.findVariable(id)
    variable != null
    MplScoreboardValue value = variable.value
    value.selector instanceof TargetSelector
    value.selector.toString() == selector
    value.scoreboard == scoreboard
  }

  @Test
  public void "Inserting a global Integer variable"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    Integer ${id} = ${value}
    impulse process main {
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a global Selector variable"() {
    given:
    String id = some($Identifier())
    String value = "@e[name=${some($Identifier())}]"
    String programString = """
    Selector ${id} = ${value}
    impulse process main {
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a global String variable"() {
    given:
    String id = some($Identifier())
    String value = some($String())
    String programString = """
    String ${id} = "${value}"
    impulse process main {
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a global Value variable"() {
    given:
    String id = some($Identifier())
    String selector = "@e[name=${some($Identifier())}]"
    String scoreboard = some($Identifier())
    String programString = """
    Value ${id} = ${selector} ${scoreboard}
    impulse process main {
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors[0].message == "The variable '${id}' of type Value cannot be inserted"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 4
    lastContext.errors.size() == 1
  }

  @Test
  public void "Inserting a qualified global Integer variable from the same file"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    File file = newTempFile()
    String qualifiedName = getBaseName(file.name) + '.' + id
    file.text = """
    Integer ${id} = ${value}
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """

    when:
    MplProgram program = assembleProgram(file)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a qualified global Selector variable from the same file"() {
    given:
    String id = some($Identifier())
    String value = "@e[name=${some($Identifier())}]"
    File file = newTempFile()
    String qualifiedName = getBaseName(file.name) + '.' + id
    file.text = """
    Selector ${id} = ${value}
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """

    when:
    MplProgram program = assembleProgram(file)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a qualified global String variable from the same file"() {
    given:
    String id = some($Identifier())
    String value = some($String())
    File file = newTempFile()
    String qualifiedName = getBaseName(file.name) + '.' + id
    file.text = """
    String ${id} = "${value}"
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """

    when:
    MplProgram program = assembleProgram(file)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a qualified global Value variable from the same file"() {
    given:
    String id = some($Identifier())
    String selector = "@e[name=${some($Identifier())}]"
    String scoreboard = some($Identifier())
    File file = newTempFile()
    String qualifiedName = getBaseName(file.name) + ' . ' + id
    file.text = """
    Value ${id} = ${selector} ${scoreboard}
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """

    when:
    MplProgram program = assembleProgram(file)

    then:
    lastContext.errors[0].message == "The variable '${id}' of type Value cannot be inserted"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == qualifiedName
    lastContext.errors[0].source.lineNumber == 4
    lastContext.errors.size() == 1
  }

  @Test
  public void "Inserting an unknown qualified global variable from the same file"() {
    given:
    String id = some($Identifier())
    File file = newTempFile()
    String qualifiedName = getBaseName(file.name) + '.' + id
    file.text = """
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """

    when:
    assembleProgram(file)

    then:
    lastContext.errors[0].message == "${qualifiedName} cannot be resolved to a variable"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == qualifiedName
    lastContext.errors[0].source.lineNumber == 3
    lastContext.errors.size() == 1
  }

  @Test
  public void "Inserting a qualified global Integer variable from a different file"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    File mainFile = newTempFile()
    File otherFile = newTempFile()
    String qualifiedName = getBaseName(otherFile.name) + '.' + id
    mainFile.text = """
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """
    otherFile.text = """
    Integer ${id} = ${value}
    """

    when:
    MplProgram program = assembleProgram(mainFile)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a qualified global Selector variable from a different file"() {
    given:
    String id = some($Identifier())
    String value = "@e[name=${some($Identifier())}]"
    File mainFile = newTempFile()
    File otherFile = newTempFile()
    String qualifiedName = getBaseName(otherFile.name) + '.' + id
    mainFile.text = """
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """
    otherFile.text = """
    Selector ${id} = ${value}
    """

    when:
    MplProgram program = assembleProgram(mainFile)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a qualified global String variable from a different file"() {
    given:
    String id = some($Identifier())
    String value = some($String())
    File mainFile = newTempFile()
    File otherFile = newTempFile()
    String qualifiedName = getBaseName(otherFile.name) + '.' + id
    mainFile.text = """
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """
    otherFile.text = """
    String ${id} = "${value}"
    """

    when:
    MplProgram program = assembleProgram(mainFile)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a qualified global Value variable from a different file"() {
    given:
    String id = some($Identifier())
    String selector = "@e[name=${some($Identifier())}]"
    String scoreboard = some($Identifier())
    File mainFile = newTempFile()
    File otherFile = newTempFile()
    String qualifiedName = getBaseName(otherFile.name) + '.' + id
    mainFile.text = """
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """
    otherFile.text = """
    Value ${id} = ${selector} ${scoreboard}
    """

    when:
    MplProgram program = assembleProgram(mainFile)

    then:
    lastContext.errors[0].message == "The variable '${id}' of type Value cannot be inserted"
    lastContext.errors[0].source.file == mainFile
    lastContext.errors[0].source.text == qualifiedName
    lastContext.errors[0].source.lineNumber == 3
    lastContext.errors.size() == 1
  }

  @Test
  public void "Inserting a qualified local Integer variable from a script file"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    File mainFile = newTempFile()
    File otherFile = newTempFile()
    String qualifiedName = getBaseName(otherFile.name) + '.' + id
    mainFile.text = """
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """
    otherFile.text = """
    Integer ${id} = ${value}
    /say this is a script file
    """

    when:
    MplProgram program = assembleProgram(mainFile)

    then:
    lastContext.errors[0].message == "The local script variable '${id}' cannot be inserted"
    lastContext.errors[0].source.file == mainFile
    lastContext.errors[0].source.text == qualifiedName
    lastContext.errors[0].source.lineNumber == 3
    lastContext.errors.size() == 1
  }

  @Test
  public void "Inserting a qualified local Selector variable from a script file"() {
    given:
    String id = some($Identifier())
    String value = "@e[name=${some($Identifier())}]"
    File mainFile = newTempFile()
    File otherFile = newTempFile()
    String qualifiedName = getBaseName(otherFile.name) + '.' + id
    mainFile.text = """
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """
    otherFile.text = """
    Selector ${id} = ${value}
    /say this is a script file
    """

    when:
    MplProgram program = assembleProgram(mainFile)

    then:
    lastContext.errors[0].message == "The local script variable '${id}' cannot be inserted"
    lastContext.errors[0].source.file == mainFile
    lastContext.errors[0].source.text == qualifiedName
    lastContext.errors[0].source.lineNumber == 3
    lastContext.errors.size() == 1
  }

  @Test
  public void "Inserting a qualified local String variable from a script file"() {
    given:
    String id = some($Identifier())
    String value = some($String())
    File mainFile = newTempFile()
    File otherFile = newTempFile()
    String qualifiedName = getBaseName(otherFile.name) + '.' + id
    mainFile.text = """
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """
    otherFile.text = """
    String ${id} = "${value}"
    /say this is a script file
    """

    when:
    MplProgram program = assembleProgram(mainFile)

    then:
    lastContext.errors[0].message == "The local script variable '${id}' cannot be inserted"
    lastContext.errors[0].source.file == mainFile
    lastContext.errors[0].source.text == qualifiedName
    lastContext.errors[0].source.lineNumber == 3
    lastContext.errors.size() == 1
  }

  @Test
  public void "Inserting a qualified local Value variable from a script file"() {
    given:
    String id = some($Identifier())
    String selector = "@e[name=${some($Identifier())}]"
    String scoreboard = some($Identifier())
    File mainFile = newTempFile()
    File otherFile = newTempFile()
    String qualifiedName = getBaseName(otherFile.name) + '.' + id
    mainFile.text = """
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """
    otherFile.text = """
    Value ${id} = ${selector} ${scoreboard}
    /say this is a script file
    """

    when:
    MplProgram program = assembleProgram(mainFile)

    then:
    lastContext.errors[0].message == "The local script variable '${id}' cannot be inserted"
    lastContext.errors[0].source.file == mainFile
    lastContext.errors[0].source.text == qualifiedName
    lastContext.errors[0].source.lineNumber == 3
    lastContext.errors.size() == 1
  }

  @Test
  public void "Inserting an unknown qualified global variable from a different file"() {
    given:
    String id = some($Identifier())
    File mainFile = newTempFile()
    File otherFile = newTempFile()
    String qualifiedName = getBaseName(otherFile.name) + '.' + id
    mainFile.text = """
    impulse process main {
      /say The value is \${${qualifiedName}}!
    }
    """
    otherFile.text = ""

    when:
    assembleProgram(mainFile)

    then:
    lastContext.errors[0].message == "${qualifiedName} cannot be resolved to a variable"
    lastContext.errors[0].source.file == mainFile
    lastContext.errors[0].source.text == qualifiedName
    lastContext.errors[0].source.lineNumber == 3
    lastContext.errors.size() == 1
  }

  @Test
  public void "Declaring a duplicate local script variable"() {
    given:
    String id = some($Identifier())
    String programString = """
    Integer ${id} = ${some($int())}
    Integer ${id} = ${some($int())}
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors[0].message == "Duplicate variable ${id}"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 3
    lastContext.errors.size() == 1
  }

  @Test
  public void "Declaring a duplicate local script variable in two nested scopes"() {
    given:
    String id = some($Identifier())
    String programString = """
      Integer ${id} = ${some($int())}
      if: /testfor @p
      then {
        Integer ${id} = ${some($int())}
      }
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors[0].message == "Duplicate variable ${id}"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 5
    lastContext.errors.size() == 1
  }

  @Test
  public void "Declaring the same local script variable in two different scopes"() {
    given:
    String id = some($Identifier())
    String programString = """
      if: /testfor @p
      then {
        Integer ${id} = ${some($int())}
      } else {
        Integer ${id} = ${some($int())}
      }
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()
    interpreter.rootVariableScope.variables.isEmpty()
    interpreter.rootVariableScope.children[0].variables.containsKey(id)
    interpreter.rootVariableScope.children[1].variables.containsKey(id)
  }

  @Test
  public void "Declaring a local script Integer variable"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    Integer ${id} = ${value}
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()

    VariableScope scope = interpreter.rootVariableScope
    MplIntegerVariable variable = scope.findVariable(id)
    variable != null
    variable.value == value
  }

  @Test
  public void "Declaring a local script Selector variable"() {
    given:
    String id = some($Identifier())
    String value = "@e[name=${some($Identifier())}]"
    String programString = """
    Selector ${id} = ${value}
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()

    VariableScope scope = interpreter.rootVariableScope
    MplVariable<TargetSelector> variable = scope.findVariable(id)
    variable != null
    variable.value instanceof TargetSelector
    variable.value.toString() == value
  }

  @Test
  public void "Declaring a local script String variable"() {
    given:
    String id = some($Identifier())
    String value = some($String())
    String programString = """
    String ${id} = "${value}"
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()

    VariableScope scope = interpreter.rootVariableScope
    MplStringVariable variable = scope.findVariable(id)
    variable != null
    variable.value == value
  }

  @Test
  public void "Declaring a local script Value variable"() {
    given:
    String id = some($Identifier())
    String selector = "@e[name=${some($Identifier())}]"
    String scoreboard = some($Identifier())
    String programString = """
    Value ${id} = ${selector} ${scoreboard}
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()

    VariableScope scope = interpreter.rootVariableScope
    MplVariable<MplValue> variable = scope.findVariable(id)
    variable != null
    MplScoreboardValue value = variable.value
    value.selector instanceof TargetSelector
    value.selector.toString() == selector
    value.scoreboard == scoreboard
  }

  @Test
  public void "Inserting a local script Integer variable"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    Integer ${id} = ${value}
    /say The value is \${${id}}!
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a local script Selector variable"() {
    given:
    String id = some($Identifier())
    String value = "@e[name=${some($Identifier())}]"
    String programString = """
    Selector ${id} = ${value}
    /say The value is \${${id}}!
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a local script String variable"() {
    given:
    String id = some($Identifier())
    String value = some($String())
    String programString = """
    String ${id} = "${value}"
    /say The value is \${${id}}!
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a local script Value variable"() {
    given:
    String id = some($Identifier())
    String selector = "@e[name=${some($Identifier())}]"
    String scoreboard = some($Identifier())
    String programString = """
    Value ${id} = ${selector} ${scoreboard}
    /say The value is \${${id}}!
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors[0].message == "The variable '${id}' of type Value cannot be inserted"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 3
    lastContext.errors.size() == 1
  }

  @Test
  public void "Inserting an unknown local script variable"() {
    given:
    String id = some($Identifier())
    String programString = """
    /say The value is \${${id}}!
    """

    when:
    assembleProgram(programString)

    then:
    lastContext.errors[0].message == "${id} cannot be resolved to a variable"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 2
    lastContext.errors.size() == 1
  }

  @Test
  public void "Declaring a duplicate local variable"() {
    given:
    String id = some($Identifier())
    String programString = """
    impulse process main {
      Integer ${id} = ${some($int())}
      Integer ${id} = ${some($int())}
    }
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors[0].message == "Duplicate variable ${id}"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 4
    lastContext.errors.size() == 1
  }

  @Test
  public void "Declaring a duplicate local variable in two nested scopes"() {
    given:
    String id = some($Identifier())
    String programString = """
    impulse process main {
      Integer ${id} = ${some($int())}
      if: /testfor @p
      then {
        Integer ${id} = ${some($int())}
      }
    }
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors[0].message == "Duplicate variable ${id}"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 6
    lastContext.errors.size() == 1
  }

  @Test
  public void "Declaring the same local variable in two different scopes"() {
    given:
    String id = some($Identifier())
    String programString = """
    impulse process main {
      if: /testfor @p
      then {
        Integer ${id} = ${some($int())}
      } else {
        Integer ${id} = ${some($int())}
      }
    }
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()
    interpreter.rootVariableScope.variables.isEmpty()
    VariableScope processScope = interpreter.rootVariableScope.children[0]
    processScope.children[0].variables.containsKey(id)
    processScope.children[1].variables.containsKey(id)
  }

  @Test
  public void "Declaring a local Integer variable does not put it into the rootVariableScope"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      Integer ${id} = ${value}
    }
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()
    interpreter.rootVariableScope.variables.isEmpty()
    MplIntegerVariable variable = interpreter.rootVariableScope.children[0].variables[id]
    variable != null
    variable.value == value
  }

  @Test
  public void "Declaring a local Selector variable does not put it into the rootVariableScope"() {
    given:
    String id = some($Identifier())
    String value = "@e[name=${some($Identifier())}]"
    String programString = """
    impulse process main {
      Selector ${id} = ${value}
    }
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()
    interpreter.rootVariableScope.variables.isEmpty()
    MplSelectorVariable variable = interpreter.rootVariableScope.children[0].variables[id]
    variable != null
    variable.value.toString() == value
  }

  @Test
  public void "Declaring a local String variable does not put it into the rootVariableScope"() {
    given:
    String id = some($Identifier())
    String value = some($String())
    String programString = """
    impulse process main {
      String ${id} = "${value}"
    }
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()
    interpreter.rootVariableScope.variables.isEmpty()
    MplStringVariable variable = interpreter.rootVariableScope.children[0].variables[id]
    variable != null
    variable.value == value
  }

  @Test
  public void "Declaring a local Value variable does not put it into the rootVariableScope"() {
    given:
    String id = some($Identifier())
    String selector = "@e[name=${some($Identifier())}]"
    String scoreboard = some($Identifier())
    String programString = """
    impulse process main {
      Value ${id} = ${selector} ${scoreboard}
    }
    """

    when:
    MplInterpreter interpreter = interpret(programString)

    then:
    lastContext.errors.isEmpty()
    MplValueVariable variable = interpreter.rootVariableScope.children[0].variables[id]
    variable != null
    variable.value.selector.toString() == selector
    variable.value.scoreboard == scoreboard
  }

  @Test
  public void "Inserting a local Integer variable"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      Integer ${id} = ${value}
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a local Selector variable"() {
    given:
    String id = some($Identifier())
    String value = "@e[name=${some($Identifier())}]"
    String programString = """
    impulse process main {
      Selector ${id} = ${value}
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a local String variable"() {
    given:
    String id = some($Identifier())
    String value = some($String())
    String programString = """
    impulse process main {
      String ${id} = "${value}"
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplCommand command =  process.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "Inserting a local Value variable"() {
    given:
    String id = some($Identifier())
    String selector = "@e[name=${some($Identifier())}]"
    String scoreboard = some($Identifier())
    String programString = """
    impulse process main {
      Value ${id} = ${selector} ${scoreboard}
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors[0].message == "The variable '${id}' of type Value cannot be inserted"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 4
    lastContext.errors.size() == 1
  }

  @Test
  public void "Inserting an unknown local variable"() {
    given:
    String id = some($Identifier())
    String programString = """
    impulse process main {
      /say The value is \${${id}}!
    }
    """

    when:
    assembleProgram(programString)

    then:
    lastContext.errors[0].message == "${id} cannot be resolved to a variable"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 3
    lastContext.errors.size() == 1
  }

  @Test
  public void "A local variable from one process is not found in another process"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      Integer ${id} = ${value}
    }
    impulse process other {
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors[0].message == "${id} cannot be resolved to a variable"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 6
    lastContext.errors.size() == 1
  }

  @Test
  public void "A local variable from an install is not found in a process"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    install {
      Integer ${id} = ${value}
    }
    impulse process main {
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors[0].message == "${id} cannot be resolved to a variable"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 6
    lastContext.errors.size() == 1
  }

  @Test
  public void "A local variable from a process is not found in an install"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      Integer ${id} = ${value}
    }
    install {
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors[0].message == "${id} cannot be resolved to a variable"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 6
    lastContext.errors.size() == 1
  }

  @Test
  public void "A local variable from an uninstall is not found in a process"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    uninstall {
      Integer ${id} = ${value}
    }
    impulse process main {
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors[0].message == "${id} cannot be resolved to a variable"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 6
    lastContext.errors.size() == 1
  }

  @Test
  public void "A local variable from a process is not found in an uninstall"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      Integer ${id} = ${value}
    }
    uninstall {
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors[0].message == "${id} cannot be resolved to a variable"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 6
    lastContext.errors.size() == 1
  }

  @Test
  public void "A local variable from outside a then is found in the then"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      Integer ${id} = ${value}
      if: /testfor @p
      then {
        /say The value is \${${id}}!
      }
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplIf mplIf =  process.chainParts[0]
    MplCommand command = mplIf.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "A local variable from a then is found in the then"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      if: /testfor @p
      then {
        Integer ${id} = ${value}
        /say The value is \${${id}}!
      }
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplIf mplIf =  process.chainParts[0]
    MplCommand command = mplIf.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "A local variable from a then is not found outside of the then"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      if: /testfor @p
      then {
        Integer ${id} = ${value}
      }
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors[0].message == "${id} cannot be resolved to a variable"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 7
    lastContext.errors.size() == 1
  }

  @Test
  public void "A local variable from outside an else is found in the else"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      Integer ${id} = ${value}
      if: /testfor @p
      else {
        /say The value is \${${id}}!
      }
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplIf mplIf =  process.chainParts[0]
    MplCommand command = mplIf.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "A local variable from an else is found in the else"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      if: /testfor @p
      else {
        Integer ${id} = ${value}
        /say The value is \${${id}}!
      }
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplIf mplIf =  process.chainParts[0]
    MplCommand command = mplIf.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "A local variable from a then is not found in the else"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      if: /testfor @p
      then {
        Integer ${id} = ${value}
      } else {
        /say The value is \${${id}}!
      }
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors[0].message == "${id} cannot be resolved to a variable"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 7
    lastContext.errors.size() == 1
  }

  @Test
  public void "A local variable from an else is not found outside of the else"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      if: /testfor @p
      else {
        Integer ${id} = ${value}
      }
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors[0].message == "${id} cannot be resolved to a variable"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 7
    lastContext.errors.size() == 1
  }

  @Test
  public void "A local variable from outside a while is found in the while"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      Integer ${id} = ${value}
      while: /testfor @p
      repeat {
        /say The value is \${${id}}!
      }
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplWhile mplWhile =  process.chainParts[0]
    MplCommand command = mplWhile.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "A local variable from a while is found in the while"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      while: /testfor @p
      repeat {
        Integer ${id} = ${value}
        /say The value is \${${id}}!
      }
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors.isEmpty()

    program.processes.size() == 1
    MplProcess process = program.processes.first()

    MplWhile mplWhile =  process.chainParts[0]
    MplCommand command = mplWhile.chainParts[0]
    command.commandParts.join() == "say The value is ${value}!"
    process.chainParts.size() == 1
  }

  @Test
  public void "A local variable from a while is not found outside of the while"() {
    given:
    String id = some($Identifier())
    int value = some($int())
    String programString = """
    impulse process main {
      while: /testfor @p
      repeat {
        Integer ${id} = ${value}
      }
      /say The value is \${${id}}!
    }
    """

    when:
    MplProgram program = assembleProgram(programString)

    then:
    lastContext.errors[0].message == "${id} cannot be resolved to a variable"
    lastContext.errors[0].source.file == lastTempFile
    lastContext.errors[0].source.text == id
    lastContext.errors[0].source.lineNumber == 7
    lastContext.errors.size() == 1
  }
}
