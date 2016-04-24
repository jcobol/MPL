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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import de.adrodoc55.commons.FileUtils;
import de.adrodoc55.minecraft.coordinate.Orientation3D;
import de.adrodoc55.minecraft.mpl.antlr.MplBaseListener;
import de.adrodoc55.minecraft.mpl.antlr.MplLexer;
import de.adrodoc55.minecraft.mpl.antlr.MplParser;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.AutoContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.BreakpointContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ChainContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.CommandContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ConditionalContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ElseDeclarationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.FileContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.IfDeclarationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ImportDeclarationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.IncludeContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.InstallContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.InterceptContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ModusContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.MplCommandContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.NotifyDeclarationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.OrientationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ProcessContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ProjectContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ProjectFileContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ScriptFileContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.SkipContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.StartContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.StopContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.UninstallContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.WaitforContext;
import de.adrodoc55.minecraft.mpl.chain.MplProcess;
import de.adrodoc55.minecraft.mpl.commands.Conditional;
import de.adrodoc55.minecraft.mpl.commands.Mode;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.Command;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.Skip;
import de.adrodoc55.minecraft.mpl.commands.chainparts.Breakpoint;
import de.adrodoc55.minecraft.mpl.commands.chainparts.ChainPart;
import de.adrodoc55.minecraft.mpl.commands.chainparts.Intercept;
import de.adrodoc55.minecraft.mpl.commands.chainparts.MplCommand;
import de.adrodoc55.minecraft.mpl.commands.chainparts.MplStart;
import de.adrodoc55.minecraft.mpl.commands.chainparts.MplStop;
import de.adrodoc55.minecraft.mpl.commands.chainparts.Notify;
import de.adrodoc55.minecraft.mpl.commands.chainparts.Waitfor;
import de.adrodoc55.minecraft.mpl.compilation.CompilerException;
import de.adrodoc55.minecraft.mpl.compilation.MplSource;
import de.adrodoc55.minecraft.mpl.compilation.interpretation.CommandBufferFactory.CommandBuffer;
import de.adrodoc55.minecraft.mpl.program.MplProgram;
import de.adrodoc55.minecraft.mpl.program.MplProject;
import de.adrodoc55.minecraft.mpl.program.MplScript;

/**
 * @author Adrodoc55
 */
public class MplInterpreter extends MplBaseListener {

  public static final String NOTIFY = "_NOTIFY";
  public static final String INTERCEPTED = "_INTERCEPTED";

  public static MplInterpreter interpret(File programFile) throws IOException {
    FileContext ctx = parse(programFile);
    MplInterpreter interpreter = new MplInterpreter(programFile);
    new ParseTreeWalker().walk(interpreter, ctx);
    return interpreter;
  }

  private static FileContext parse(File programFile) throws IOException {
    byte[] bytes = Files.readAllBytes(programFile.toPath());
    ANTLRInputStream input = new ANTLRInputStream(FileUtils.toUnixLineEnding(new String(bytes)));
    MplLexer lexer = new MplLexer(input);
    TokenStream tokens = new CommonTokenStream(lexer);
    MplParser parser = new MplParser(tokens);
    FileContext context = parser.file();
    // context.inspect(parser);
    return context;
  }

  private final File programFile;
  private final List<String> lines;
  private final ListMultimap<String, Include> includes = LinkedListMultimap.create();
  private final Set<File> imports = new HashSet<>();

  private MplInterpreter(File programFile) throws IOException {
    this.programFile = programFile;
    lines = Files.readAllLines(programFile.toPath());
    // FIXME was sinnvolleres als null reinstecken
    addFileImport(null, programFile.getParentFile());
  }

  public File getProgramFile() {
    return programFile;
  }

  /**
   * Every File can either be a script or a project/process file
   */
  private boolean isProject;
  private MplProgram program;

  /**
   * Every File can either be a script or a project/process file
   */
  public boolean isProject() {
    return isProject;
  }

  public MplScript getScript() {
    return (MplScript) program;
  }

  public MplProject getProject() {
    return (MplProject) program;
  }

  private void addException(CompilerException ex1) {
    program.getExceptions().add(ex1);
  }

  /**
   * Returns the mapping of process names to includes required by that process. A key of null
   * indicates that this include is not required by a specific process, but by an explicit include
   * of a project.
   *
   * @return
   */
  public ListMultimap<String, Include> getIncludes() {
    return includes;
  }

  // ----------------------------------------------------------------------------------------------------

  @Override
  public void visitErrorNode(ErrorNode node) {
    Token token = node.getSymbol();
    String line = lines.get(token.getLine() - 1);
    MplSource source = new MplSource(programFile, token, line);
    addException(new CompilerException(source, "Mismatched input"));
  }

  @Override
  public void enterScriptFile(ScriptFileContext ctx) {
    isProject = false;
    program = new MplScript();
    newChainBuffer();
    chainBuffer.setScript(true);
  }

  @Override
  public void enterProjectFile(ProjectFileContext ctx) {
    isProject = true;
    program = new MplProject();
  }

  @Override
  public void enterImportDeclaration(ImportDeclarationContext ctx) {
    Token token = ctx.STRING().getSymbol();
    String importPath = MplLexerUtils.getContainedString(token);
    File file = new File(programFile.getParentFile(), importPath);
    addFileImport(ctx, file);
  }

  @Override
  public void enterProject(ProjectContext ctx) {
    MplProject project = getProject();
    Token oldToken = project.getProjectToken();
    Token newToken = ctx.PROJECT().getSymbol();
    if (oldToken != null) {
      String oldLine = lines.get(oldToken.getLine());
      MplSource oldSource = new MplSource(programFile, oldToken, oldLine);
      addException(new CompilerException(oldSource, "A file may only contain a single project!"));
      String newLine = lines.get(newToken.getLine());
      MplSource newSource = new MplSource(programFile, newToken, newLine);
      addException(new CompilerException(newSource, "A file may only contain a single project!"));
      return;
    }
    String name = ctx.IDENTIFIER().getText();
    project.setName(name);
    project.setToken(newToken);
  }

  @Override
  public void enterOrientation(OrientationContext ctx) {
    String def = MplLexerUtils.getContainedString(ctx.STRING().getSymbol());

    Token newToken = ctx.ORIENTATION().getSymbol();

    Orientation3D oldOrientation = program.getOrientation();
    if (oldOrientation != null) {
      String type = isProject() ? "project" : "script";
      Token oldToken = oldOrientation.getToken();
      String oldLine = lines.get(oldToken.getLine());
      MplSource oldSource = new MplSource(programFile, oldToken, oldLine);
      addException(
          new CompilerException(oldSource, "A " + type + " may only have a single orientation!"));
      String newLine = lines.get(newToken.getLine());
      MplSource newSource = new MplSource(programFile, newToken, newLine);
      addException(
          new CompilerException(newSource, "A " + type + " may only have a single orientation!"));
      return;
    }

    Orientation3D newOrientation = new Orientation3D(def);
    newOrientation.setToken(newToken);
    program.setOrientation(newOrientation);
  }

  @Override
  public void enterInclude(IncludeContext ctx) {
    String includePath = MplLexerUtils.getContainedString(ctx.STRING().getSymbol());
    Token token = ctx.STRING().getSymbol();
    String line = lines.get(token.getLine() - 1);
    File file = new File(programFile.getParentFile(), includePath);
    LinkedList<File> files = new LinkedList<File>();
    if (!addFile(files, file, token)) {
      return;
    }
    MplSource source = new MplSource(programFile, token, line);
    Include include = new Include(source, files);
    includes.put(null, include);
  }

  /**
   * Adds the file to the list of imports that will be used to search processes. If the file is a
   * directory all direct subfiles will be added, this is not recursive.
   *
   * @param projectToken the token that this import originated from
   * @param file the file to import
   */
  private void addFileImport(ImportDeclarationContext ctx, File file) {
    Token token = ctx != null ? ctx.STRING().getSymbol() : null;
    String line = token != null ? lines.get(token.getLine() - 1) : null;
    if (imports.contains(file)) {
      MplSource source = new MplSource(programFile, token, line);
      addException(new CompilerException(source, "Duplicate import."));
      return;
    }
    addFile(imports, file, token);
  }

  /**
   * Adds the File to the specified Collection. If the File is a Directory all relevant children are
   * added instead. If any Problem occurs, an Exception will be added to the exceptions-List.
   *
   * @param files the Collection to add to
   * @param file the File
   * @param token the Token to display in potential Exceptions
   * @return true if something was added to the Collection, false otherwise
   */
  private boolean addFile(Collection<File> files, File file, Token token) {
    String line = token != null ? lines.get(token.getLine() - 1) : null;
    if (file.isFile()) {
      files.add(file);
      return true;
    } else if (file.isDirectory()) {
      boolean added = false;
      for (File f : file.listFiles()) {
        if (f.isFile() && (f.equals(programFile) || f.getName().endsWith(".mpl"))) {
          files.add(f);
          added = true;
        }
      }
      return added;
    } else if (!file.exists()) {
      String path = FileUtils.getCanonicalPath(file);
      MplSource source = new MplSource(programFile, token, line);
      addException(new CompilerException(source, "Could not find '" + path + "'"));
      return false;
    } else {
      MplSource source = new MplSource(programFile, token, line);
      addException(new CompilerException(source,
          "Can only import Files and Directories, not: '" + file + "'"));
      return false;
    }
  }


  private final LinkedList<ChainBuffer> chainBufferStack = new LinkedList<>();
  private ChainBuffer chainBuffer;
  private IfBuffer ifBuffer;

  private void newChainBuffer() {
    chainBufferStack.push(chainBuffer);
    this.chainBuffer = new ChainBuffer();
    this.ifBuffer = new IfBuffer(chainBuffer);
  }

  private void deleteChainBuffer() {
    // ifBuffer is not recovered, because it is currently not required
    chainBuffer = chainBufferStack.poll();
    ifBuffer = null;
  }

  @Override
  public void enterInstall(InstallContext ctx) {
    newChainBuffer();
  }

  @Override
  public void exitInstall(InstallContext ctx) {
    List<ChainPart> installation = program.getInstallation();
    installation.addAll(chainBuffer.getChainParts());
    deleteChainBuffer();
  }

  @Override
  public void enterUninstall(UninstallContext ctx) {
    newChainBuffer();
  }

  @Override
  public void exitUninstall(UninstallContext ctx) {
    List<ChainPart> uninstallation = program.getUninstallation();
    uninstallation.addAll(chainBuffer.getChainParts());
    deleteChainBuffer();
  }

  private MplProcess process;

  @Override
  public void enterProcess(ProcessContext ctx) {
    String name = ctx.IDENTIFIER().getText();
    boolean repeat = ctx.REPEAT() != null;
    Token token = ctx.IDENTIFIER().getSymbol();
    MplSource source = new MplSource(programFile, token, lines.get(token.getLine()));
    process = new MplProcess(name, repeat, source);

    newChainBuffer();
    chainBuffer.setProcess(true);
    chainBuffer.setName(name);

    if (repeat) {
      chainBuffer.setRepeatingProcess(true);
      chainBuffer.setRepeatingContext(true);
    } else {
      chainBuffer.setRepeatingProcess(false);
      chainBuffer.setRepeatingContext(false);
    }
  }

  private final LinkedList<CommandBufferFactory> factory = new LinkedList<>();

  @Override
  public void enterChain(ChainContext ctx) {
    factory.push(new CommandBufferFactory());
  }

  @Override
  public void exitChain(ChainContext ctx) {
    factory.pop();
  }

  private CommandBuffer commandBuffer;

  @Override
  public void enterMplCommand(MplCommandContext ctx) {
    commandBuffer = factory.peek().create();
  }

  @Override
  public void enterModus(ModusContext ctx) {
    Mode mode = Mode.valueOf(ctx.getText().toUpperCase());
    commandBuffer.setMode(mode);
  }

  @Override
  public void enterConditional(ConditionalContext ctx) {
    Conditional conditional = null;
    Token token;
    if (ctx.UNCONDITIONAL() != null) {
      conditional = Conditional.UNCONDITIONAL;
      token = ctx.UNCONDITIONAL().getSymbol();
    } else if (ctx.CONDITIONAL() != null) {
      conditional = Conditional.CONDITIONAL;
      token = ctx.CONDITIONAL().getSymbol();
    } else if (ctx.INVERT() != null) {
      conditional = Conditional.INVERT;
      token = ctx.INVERT().getSymbol();
    } else {
      throw new InternalError("Unreachable code");
    }
    try {
      commandBuffer.setConditional(conditional);
      commandBuffer.setConditionalToken(token);
    } catch (IllegalModifierException ex) {
      String line = lines.get(token.getLine() - 1);
      MplSource source = new MplSource(programFile, token, line);
      addException(new CompilerException(source, ex.getLocalizedMessage(), ex));
    }
  }

  @Override
  public void enterAuto(AutoContext ctx) {
    Boolean needsRedstone = null;
    if (ctx.ALWAYS_ACTIVE() != null) {
      needsRedstone = false;
    } else if (ctx.NEEDS_REDSTONE() != null) {
      needsRedstone = true;
    }
    commandBuffer.setNeedsRedstone(needsRedstone);
  }

  @Override
  public void enterCommand(CommandContext ctx) {
    String commandString = ctx.COMMAND().getText();
    Mode mode = commandBuffer.getMode();
    Conditional conditional = commandBuffer.getConditional();
    Boolean needsRedstone = commandBuffer.getNeedsRedstone();
    MplCommand command = new MplCommand(commandString, mode, conditional, needsRedstone);
    chainBuffer.add(command);
  }

  private String lastStartIdentifier;

  @Override
  public void enterStart(StartContext ctx) {
    TerminalNode identifier = ctx.IDENTIFIER();
    String process = identifier.getText();
    MplStart start = new MplStart(process);
    start.setConditional(commandBuffer.getConditional());
    chainBuffer.add(start);

    lastStartIdentifier = process;
    if (!isProject) {
      return;
    }

    String srcProcess = chainBuffer.isProcess() ? chainBuffer.getName() : null;
    Token token = identifier.getSymbol();
    String line = lines.get(token.getLine() - 1);
    MplSource source = new MplSource(programFile, token, line);
    Include include = new Include(source, process, imports);
    includes.put(srcProcess, include);
  }

  @Override
  public void enterStop(StopContext ctx) {
    String process;
    if (ctx.IDENTIFIER() != null) {
      process = ctx.IDENTIFIER().getText();
    } else if (chainBuffer.isRepeatingProcess()) {
      process = chainBuffer.getName();
    } else {
      Token token = ctx.STOP().getSymbol();
      String line = lines.get(token.getLine() - 1);
      MplSource source = new MplSource(programFile, token, line);
      addException(new CompilerException(source, "An impulse process can't be stopped."));
      return;
    }

    Conditional conditional = commandBuffer.getConditional();
    if (conditional == Conditional.UNCONDITIONAL) {
      chainBuffer.add(new MplStop(process, conditional));
    } else {
      ChainPart prev = chainBuffer.chainParts.peekLast();
      if (prev == null) {
        Token token = commandBuffer.getConditionalToken();
        String line = lines.get(token.getLine() - 1);
        MplSource source = new MplSource(programFile, token, line);
        addException(
            new CompilerException(source, "The first part of a chain must be unconditional!"));
      }
      if (conditional == Conditional.CONDITIONAL) {
        chainBuffer.add(new MplStop(process, conditional));
      } else if (conditional == Conditional.INVERT) {
        try {
          Mode modeToInvert = prev.getModeToInvert();
          chainBuffer.add(new MplStop(process, conditional, modeToInvert));
        } catch (IllegalModifierException e) {
          Token token = commandBuffer.getConditionalToken();
          String line = lines.get(token.getLine() - 1);
          MplSource source = new MplSource(programFile, token, line);
          addException(
              new CompilerException(source, "Can't invert a " + prev.getClass().getSimpleName()));
        }
      }
    }
  }

  @Override
  public void enterWaitfor(WaitforContext ctx) {
    Token token = ctx.WAITFOR().getSymbol();
    String line = lines.get(token.getLine() - 1);
    if (chainBuffer.isRepeatingContext()) {
      MplSource source = new MplSource(programFile, token, line);
      addException(new CompilerException(source, "Encountered waitfor in repeating context."));
      return;
    }
    TerminalNode identifier = ctx.IDENTIFIER();
    String event;
    if (identifier != null) {
      event = identifier.getText();
      if (ctx.NOTIFY() != null) {
        event += NOTIFY;
      }
    } else if (lastStartIdentifier != null) {
      event = lastStartIdentifier += NOTIFY;
      lastStartIdentifier = null;
    } else {
      MplSource source = new MplSource(programFile, token, line);
      addException(new CompilerException(source,
          "Missing Identifier. No previous start was found to wait for."));
      return;
    }
    Waitfor waitfor = new Waitfor(event);
    waitfor.setConditional(commandBuffer.getConditional());
    chainBuffer.add(waitfor);
  }

  @Override
  public void enterNotifyDeclaration(NotifyDeclarationContext ctx) {
    if (process == null) {
      Token token = ctx.NOTIFY().getSymbol();
      String line = lines.get(token.getLine() - 1);
      MplSource source = new MplSource(programFile, token, line);
      addException(
          new CompilerException(source, "Encountered notify outside of a process context."));
      return;
    }
    Notify notify = new Notify(process.getName());
    notify.setConditional(commandBuffer.getConditional());
    chainBuffer.add(notify);
  }

  @Override
  public void enterIntercept(InterceptContext ctx) {
    Token token = ctx.INTERCEPT().getSymbol();
    String line = lines.get(token.getLine() - 1);
    if (chainBuffer.isRepeatingContext()) {
      MplSource source = new MplSource(programFile, token, line);
      addException(new CompilerException(source, "Encountered intercept in repeating context."));
      return;
    }

    TerminalNode identifier = ctx.IDENTIFIER();
    String process = identifier.getText();

    Intercept intercept = new Intercept(process);
    intercept.setConditional(commandBuffer.getConditional());
    chainBuffer.add(intercept);
  }

  @Override
  public void enterSkip(SkipContext ctx) {
    if (chainBuffer.getChainParts().isEmpty()) {
      Token token = ctx.SKIP().getSymbol();
      String line = lines.get(token.getLine());
      MplSource source = new MplSource(programFile, token, line);
      addException(new CompilerException(source,
          "Skip may not be the first command of a repeating process!"));
      return;
    }
    chainBuffer.add(new Skip(false));
  }

  @Override
  public void enterBreakpoint(BreakpointContext ctx) {
    int line = ctx.BREAKPOINT().getSymbol().getLine();
    String source = programFile.getName() + " : line " + line;
    Breakpoint breakpoint = new Breakpoint(source);
    breakpoint.setConditional(commandBuffer.getConditional());
    chainBuffer.add(breakpoint);

    getProject().setHasBreakpoint(true);
  }

  @Override
  public void enterIfDeclaration(IfDeclarationContext ctx) {
    boolean not = ctx.NOT() != null;
    Command condition = new Command(ctx.COMMAND().getText());
    chainBuffer = ifBuffer.enterIf(not, condition);
  }

  @Override
  public void enterElseDeclaration(ElseDeclarationContext ctx) {
    ifBuffer.switchToElseBlock();
  }

  @Override
  public void exitIfDeclaration(IfDeclarationContext ctx) {
    chainBuffer = ifBuffer.exitIf();
  }

  @Override
  public void exitProcess(ProcessContext ctx) {
    LinkedList<ChainPart> chainParts = chainBuffer.getChainParts();
    process.setChainParts(chainParts);
    getProject().addProcess(process);
    deleteChainBuffer();
    process = null;
  }

  @Override
  public void exitScriptFile(ScriptFileContext ctx) {
    getScript().setChainParts(chainBuffer.getChainParts());
    deleteChainBuffer();
  }

  @Override
  public void exitFile(FileContext ctx) {
    if (program.getOrientation() == null) {
      program.setOrientation(new Orientation3D());
    }
  }
}
