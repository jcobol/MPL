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
package de.adrodoc55.minecraft.mpl.ast.visitor;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.adrodoc55.minecraft.mpl.ast.Conditional.CONDITIONAL;
import static de.adrodoc55.minecraft.mpl.ast.Conditional.UNCONDITIONAL;
import static de.adrodoc55.minecraft.mpl.ast.ProcessType.INLINE;
import static de.adrodoc55.minecraft.mpl.ast.chainparts.MplIntercept.INTERCEPTED;
import static de.adrodoc55.minecraft.mpl.ast.chainparts.MplNotify.NOTIFY;
import static de.adrodoc55.minecraft.mpl.commands.Mode.CHAIN;
import static de.adrodoc55.minecraft.mpl.commands.Mode.IMPULSE;
import static de.adrodoc55.minecraft.mpl.commands.Mode.REPEAT;
import static de.adrodoc55.minecraft.mpl.commands.chainlinks.Commands.newInvertingCommand;
import static de.adrodoc55.minecraft.mpl.commands.chainlinks.ReferencingCommand.REF;
import static de.adrodoc55.minecraft.mpl.compilation.CompilerOptions.CompilerOption.DEBUG;
import static de.adrodoc55.minecraft.mpl.compilation.CompilerOptions.CompilerOption.TRANSMITTER;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

import org.antlr.v4.runtime.CommonToken;

import com.google.common.annotations.VisibleForTesting;

import de.adrodoc55.commons.CopyScope;
import de.adrodoc55.minecraft.coordinate.Coordinate3D;
import de.adrodoc55.minecraft.coordinate.Orientation3D;
import de.adrodoc55.minecraft.mpl.antlr.MplLexer;
import de.adrodoc55.minecraft.mpl.ast.ProcessType;
import de.adrodoc55.minecraft.mpl.ast.chainparts.ChainPart;
import de.adrodoc55.minecraft.mpl.ast.chainparts.ModifiableChainPart;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplBreakpoint;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplCall;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplCommand;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplIf;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplIntercept;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplNotify;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplStart;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplStop;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplWaitfor;
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplBreak;
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplContinue;
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplWhile;
import de.adrodoc55.minecraft.mpl.ast.chainparts.program.MplProcess;
import de.adrodoc55.minecraft.mpl.ast.chainparts.program.MplProgram;
import de.adrodoc55.minecraft.mpl.chain.ChainContainer;
import de.adrodoc55.minecraft.mpl.chain.CommandChain;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.ChainLink;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.Command;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.InternalCommand;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.MplSkip;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.ReferencingCommand;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.ResolveableCommand;
import de.adrodoc55.minecraft.mpl.compilation.CompilerException;
import de.adrodoc55.minecraft.mpl.compilation.MplCompilerContext;
import de.adrodoc55.minecraft.mpl.compilation.MplSource;
import de.adrodoc55.minecraft.mpl.interpretation.IllegalModifierException;
import de.adrodoc55.minecraft.mpl.interpretation.ModifierBuffer;

/**
 * @author Adrodoc55
 */
public class MplMainAstVisitor extends MplBaseAstVisitor {
  private final MplCompilerContext context;
  @VisibleForTesting
  MplProgram program;

  private MplSource breakpoint;

  public MplMainAstVisitor(MplCompilerContext context) {
    super(context.getOptions());
    this.context = checkNotNull(context, "context == null!");
  }

  public ChainContainer visitProgram(MplProgram program) {
    this.program = program;
    Orientation3D orientation = program.getOrientation();
    Coordinate3D max = program.getMax();
    File file = program.getProgramFile();
    CommandChain install = visitUnInstall("install", file, program.getInstall());
    CommandChain uninstall = visitUnInstall("uninstall", file, program.getUninstall());

    List<CommandChain> chains = new ArrayList<>(program.getProcesses().size());
    for (MplProcess process : program.getProcesses()) {
      CommandChain chain = visitProcess(process);
      if (chain != null) {
        chains.add(chain);
      }
    }
    if (breakpoint != null) {
      chains.add(getBreakpointProcess(program));
    }
    return new ChainContainer(orientation, max, install, uninstall, chains, program.getHash());
  }

  private @Nullable CommandChain visitUnInstall(String name, File programFile,
      @Nullable MplProcess process) {
    if (process == null) {
      process =
          new MplProcess(name, new MplSource(programFile, new CommonToken(MplLexer.PROCESS), ""));
    }
    return visitProcess(process);
  }

  @CheckReturnValue
  private CommandChain getBreakpointProcess(MplProgram program) {
    String hash = program.getHash();
    MplProcess process = new MplProcess("breakpoint", breakpoint);
    List<ChainPart> commands = new ArrayList<>();

    // Pause
    if (!options.hasOption(TRANSMITTER)) {
      commands.add(new MplCommand("/execute @e[tag=" + hash + "] ~ ~ ~ clone ~ ~ ~ ~ ~ ~ ~ ~1 ~",
          breakpoint));
    }
    commands.add(new MplCommand("/tp @e[tag=" + hash + "] ~ ~1 ~", breakpoint));
    if (!options.hasOption(TRANSMITTER)) {
      commands.add(new MplCommand("/execute @e[tag=" + hash + "] ~ ~ ~ blockdata ~ ~ ~ {Command:}",
          breakpoint));
    }

    commands.add(new MplCommand(
        "tellraw @a [{\"text\":\"[tp to breakpoint]\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/tp @p @e[name=breakpoint_NOTIFY,c=-1]\"}},{\"text\":\" \"},{\"text\":\"[continue program]\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/execute @e[name=breakpoint_CONTINUE] ~ ~ ~ "
            + getStartCommand("~ ~ ~") + "\"}}]",
        breakpoint));

    commands.add(new MplWaitfor("breakpoint_CONTINUE", breakpoint));
    commands.add(new MplCommand("/kill @e[name=breakpoint_CONTINUE]", breakpoint));

    // Unpause
    commands.add(new MplCommand(
        "/execute @e[tag=" + hash + "] ~ ~ ~ clone ~ ~ ~ ~ ~ ~ ~ ~-1 ~ force move", breakpoint));
    commands.add(new MplCommand("/tp @e[tag=" + hash + "] ~ ~-1 ~", breakpoint));
    if (!options.hasOption(TRANSMITTER)) {
      commands.add(new MplCommand("/execute @e[tag=" + hash + "] ~ ~ ~ blockdata ~ ~ ~ {Command:"
          + getStopCommand("~ ~ ~") + "}", breakpoint));
    }

    commands.add(new MplNotify("breakpoint", breakpoint));

    process.setChainParts(commands);
    return visitProcess(process);
  }

  /**
   * Returns null if the specified {@code process} is of type {@link ProcessType#INLINE}.
   *
   * @param process the {@link MplProcess} to convert
   * @return result a new {@link CommandChain}
   */
  public @Nullable CommandChain visitProcess(MplProcess process) {
    if (process.getType() == INLINE) {
      return null;
    }
    List<ChainPart> chainParts = new CopyScope().copy(process.getChainParts());
    List<ChainLink> result = new ArrayList<>(chainParts.size());
    boolean containsSkip = containsHighlevelSkip(chainParts);
    String name = process.getName();
    if (name != null) {
      if (process.isRepeating()) {
        if (options.hasOption(TRANSMITTER)) {
          result.add(new MplSkip());
        }
        if (chainParts.isEmpty()) {
          chainParts.add(new MplCommand("", process.getSource()));
        }
        ChainPart first = chainParts.get(0);
        try {
          if (containsSkip) {
            first.setMode(IMPULSE);
          } else {
            first.setMode(REPEAT);
          }
          first.setNeedsRedstone(true);
        } catch (IllegalModifierException ex) {
          throw new IllegalStateException(ex.getMessage(), ex);
        }
      } else {
        result.addAll(getTransmitterReceiverCombo(false));
      }
    } else if (options.hasOption(TRANSMITTER)) {
      result.add(new MplSkip());
    }
    for (ChainPart chainPart : chainParts) {
      result.addAll(chainPart.accept(this));
    }
    if (process.isRepeating() && containsSkip) {
      result.addAll(getRestartBackref(result.get(0), false));
      resolveReferences(result);
    }
    if (!process.isRepeating() && name != null && !"install".equals(name)
        && !"uninstall".equals(name)) {
      result.addAll(visitIgnoringWarnings(new MplNotify(name, process.getSource())));
    }
    return new CommandChain(name, result, process.getTags());
  }

  private boolean containsHighlevelSkip(List<ChainPart> chainParts) {
    for (ChainPart chainPart : chainParts) {
      if (chainPart instanceof MplWaitfor//
          || chainPart instanceof MplIntercept//
          || chainPart instanceof MplBreakpoint//
          || chainPart instanceof MplWhile//
          || chainPart instanceof MplBreak//
          || chainPart instanceof MplContinue//
      ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if a process with the specified {@code processName} is part of the program. If there is
   * such a process, this method returns {@code true}, otherwise it returns {@code false} and adds a
   * compiler warning.
   *
   * @param chainpart where to display the warning
   * @param processName the required process
   * @return {@code true} if the process was found, {@code false} otherwise
   */
  private boolean checkProcessExists(ModifiableChainPart chainpart, String processName) {
    checkNotNull(processName, "processName == null!");
    if (!program.containsProcess(processName)) {
      context.addWarning(
          new CompilerException(chainpart.getSource(), "Could not resolve process " + processName));
      return false;
    }
    return true;
  }

  /**
   * Checks if the process with the specified {@code processName} is an inline process. If there is
   * such a process and it is inline, this method returns true, otherwise returns false and adds a
   * compiler exception.
   *
   * @param chainpart
   * @param processName
   * @return
   */
  private boolean checkNotInlineProcess(ModifiableChainPart chainpart, String processName) {
    checkNotNull(processName, "processName == null!");
    MplProcess process = program.getProcess(processName);
    if (process != null && process.getType() == ProcessType.INLINE) {
      context.addError(new CompilerException(chainpart.getSource(),
          "Cannot " + chainpart.getName() + " an inline process"));
      return false;
    }
    return true;
  }

  @Override
  public List<ChainLink> visitCommand(MplCommand command) {
    List<ChainLink> result = new ArrayList<>(2);
    addInvertingCommandIfInvert(result, command);

    String cmd = command.getCommand();
    result.add(new Command(cmd, command));
    return result;
  }

  @Override
  public List<ChainLink> visitCall(MplCall mplCall) {
    List<ChainLink> result = new ArrayList<>();
    String processName = mplCall.getProcess();
    if (checkProcessExists(mplCall, processName)) {
      MplProcess process = program.getProcess(processName);
      if (process.getType() == INLINE) {
        for (ChainPart cp : process.getChainParts()) {
          result.addAll(cp.accept(this));
        }
        return result;
      }
    }
    ModifierBuffer modifier = new ModifierBuffer();
    modifier.setConditional(mplCall.isConditional() ? CONDITIONAL : UNCONDITIONAL);
    MplStart mplStart = new MplStart("@e[name=" + processName + "]", mplCall, mplCall.getPrevious(),
        mplCall.getSource());
    result.addAll(mplStart.accept(this));

    MplWaitfor mplWaitfor = new MplWaitfor(processName, modifier, mplCall.getSource());
    result.addAll(visitIgnoringWarnings(mplWaitfor));
    return result;
  }

  private List<ChainLink> visitIgnoringWarnings(ChainPart cp) {
    MplCompilerContext context = new MplCompilerContext(options);
    MplMainAstVisitor visitor = new MplMainAstVisitor(context);
    visitor.program = program;
    List<ChainLink> accept = cp.accept(visitor);
    context.clearWarnings();
    this.context.addContext(context);
    return accept;
  }

  @Override
  public List<ChainLink> visitStart(MplStart start) {
    List<ChainLink> result = new ArrayList<>(2);
    String selector = start.getSelector();
    String processName = selector.substring(8, selector.length() - 1);
    checkProcessExists(start, processName);
    checkNotInlineProcess(start, processName);
    addInvertingCommandIfInvert(result, start);

    String command = "execute " + selector + " ~ ~ ~ " + getStartCommand("~ ~ ~");
    result.add(new Command(command, start));
    return result;
  }

  @Override
  public List<ChainLink> visitStop(MplStop stop) {
    List<ChainLink> result = new ArrayList<>(2);
    String selector = stop.getSelector();
    String processName = selector.substring(8, selector.length() - 1);
    checkProcessExists(stop, processName);
    checkNotInlineProcess(stop, processName);
    addInvertingCommandIfInvert(result, stop);

    String command = "execute " + selector + " ~ ~ ~ " + getStopCommand("~ ~ ~");
    result.add(new Command(command, stop));
    return result;
  }

  @Override
  public List<ChainLink> visitWaitfor(MplWaitfor waitfor) {
    List<ChainLink> result = new ArrayList<>();
    checkIsUsed(waitfor);
    String event = waitfor.getEvent();
    checkNotInlineProcess(waitfor, event);

    ReferencingCommand summon = new ReferencingCommand("summon ArmorStand " + REF + " {CustomName:"
        + event + NOTIFY + ",NoGravity:1b,Invisible:1b,Invulnerable:1b,Marker:1b}");

    if (waitfor.getConditional() == UNCONDITIONAL) {
      summon.setRelative(1);
      result.add(summon);
    } else {
      summon.setConditional(true);
      ReferencingCommand jump = new ReferencingCommand(getStartCommand(REF), true);
      if (waitfor.getConditional() == CONDITIONAL) {
        summon.setRelative(3);
        jump.setRelative(1);
        result.add(summon);
        result.add(newInvertingCommand(CHAIN));
        result.add(jump);
      } else { // conditional == INVERT
        jump.setRelative(3);
        summon.setRelative(1);
        result.add(jump);
        result.add(newInvertingCommand(CHAIN));
        result.add(summon);
      }
    }
    result.addAll(getTransmitterReceiverCombo(false));
    return result;
  }

  /**
   * Checks if the specified {@link MplWaitfor} can be triggered in the program, that is if there is
   * a process with the same name as {@link MplWaitfor#getEvent()} or if there is a
   * {@link MplNotify} for the event of the waitfor. If the waitfor is not used, a compiler warning
   * is added.
   *
   * @param waitfor the {@link MplWaitfor}
   * @return {@code true} if the specified waitfor is used, {@code false} otherwise
   */
  private boolean checkIsUsed(MplWaitfor waitfor) {
    String event = waitfor.getEvent();
    boolean triggeredByProcess = program.streamProcesses()//
        .anyMatch(p -> event.equals(p.getName()));
    if (triggeredByProcess) {
      return true;
    }
    boolean triggeredByNotify = program.streamProcesses()//
        .flatMap(p -> p.getChainParts().stream())//
        .anyMatch(c -> {
          if (c instanceof MplNotify)
            return event.equals(((MplNotify) c).getEvent());
          return false;
        });
    if (triggeredByNotify) {
      return true;
    }
    context.addWarning(
        new CompilerException(waitfor.getSource(), "The event " + event + " is never triggered"));
    return false;
  }

  @Override
  public List<ChainLink> visitNotify(MplNotify notify) {
    List<ChainLink> result = new ArrayList<>(3);
    String event = notify.getEvent();
    checkIsUsed(notify);
    addInvertingCommandIfInvert(result, notify);

    boolean conditional = notify.isConditional();
    result.add(new InternalCommand(
        "execute @e[name=" + event + NOTIFY + "] ~ ~ ~ " + getStartCommand("~ ~ ~"), conditional));
    result.add(new Command("kill @e[name=" + event + NOTIFY + "]", conditional));
    return result;
  }

  /**
   * Checks if the specified {@link MplNotify} is used in the program, that is if there is a
   * {@link MplWaitfor} for the event of the notify. If the notify is not used, a compiler warning
   * is added.
   *
   * @param notify the {@link MplNotify}
   * @return {@code true} if the specified notify is used, {@code false} otherwise
   */
  private boolean checkIsUsed(MplNotify notify) {
    String event = notify.getEvent();
    boolean used = program.streamProcesses()//
        .flatMap(p -> p.getChainParts().stream())//
        .anyMatch(c -> {
          if (c instanceof MplWaitfor)
            return event.equals(((MplWaitfor) c).getEvent());
          return false;
        });
    if (!used) {
      context.addWarning(
          new CompilerException(notify.getSource(), "The event " + event + " is never used"));
    }
    return used;
  }

  @Override
  public List<ChainLink> visitIntercept(MplIntercept intercept) {
    List<ChainLink> result = new ArrayList<>();
    String event = intercept.getEvent();
    checkProcessExists(intercept, event);
    checkNotInlineProcess(intercept, event);
    boolean conditional = intercept.isConditional();

    InternalCommand entitydata = new InternalCommand(
        "entitydata @e[name=" + event + "] {CustomName:" + event + INTERCEPTED + "}", conditional);

    ResolveableCommand summon = new ResolveableCommand("summon ArmorStand " + REF + " {CustomName:"
        + event + ",NoGravity:1b,Invisible:1b,Invulnerable:1b,Marker:1b}", conditional);


    ResolveableCommand jump = new ResolveableCommand(getStartCommand(REF), true);
    if (intercept.getConditional() == UNCONDITIONAL) {
      result.add(entitydata);
      result.add(summon);
    } else {
      if (intercept.getConditional() == CONDITIONAL) {
        result.add(entitydata);
        result.add(summon);
        result.add(newInvertingCommand(CHAIN));
        result.add(jump);
      } else { // conditional == INVERT
        result.add(jump);
        result.add(newInvertingCommand(CHAIN));
        result.add(entitydata);
        result.add(summon);
      }
    }
    List<ChainLink> trc = getTransmitterReceiverCombo(false);
    ChainLink ref = trc.get(0);
    summon.setReferenced(ref);
    jump.setReferenced(ref);
    result.addAll(trc);
    result.add(new InternalCommand("kill @e[name=" + event + ",r=2]"));
    result.add(new InternalCommand(
        "entitydata @e[name=" + event + INTERCEPTED + "] {CustomName:" + event + "}"));
    return resolveReferences(result);
  }

  @Override
  public List<ChainLink> visitBreakpoint(MplBreakpoint mplBreakpoint) {
    if (!options.hasOption(DEBUG)) {
      return Collections.emptyList();
    }
    List<ChainLink> result = new ArrayList<>();
    this.breakpoint = mplBreakpoint.getSource();

    addInvertingCommandIfInvert(result, mplBreakpoint);

    result.add(new InternalCommand("say " + mplBreakpoint.getMessage(), mplBreakpoint));

    ModifierBuffer modifier = new ModifierBuffer();
    modifier.setConditional(mplBreakpoint.isConditional() ? CONDITIONAL : UNCONDITIONAL);
    // new MplCall("breakpoint", modifier, mplBreakpoint.getSource()).accept(this);
    MplStart mplStart = new MplStart("@e[name=breakpoint]", modifier, mplBreakpoint.getSource());
    MplWaitfor mplWaitfor = new MplWaitfor("breakpoint", modifier, mplBreakpoint.getSource());
    result.addAll(mplStart.accept(this));
    result.addAll(mplWaitfor.accept(this));
    return result;
  }

  @Override
  public List<ChainLink> visitSkip(MplSkip skip) {
    List<ChainLink> result = new ArrayList<>(1);
    result.add(skip);
    return result;
  }

  @Override
  public List<ChainLink> visitIf(MplIf mplIf) {
    return new MplIfVisitor(this, options).visitIf(mplIf);
  }

  @Override
  public List<ChainLink> visitWhile(MplWhile mplWhile) {
    return new MplWhileVisitor(this, options).visitWhile(mplWhile);
  }

  @Override
  public List<ChainLink> visitBreak(MplBreak mplBreak) {
    throw new IllegalStateException("break can only occur within while");
  }

  @Override
  public List<ChainLink> visitContinue(MplContinue mplContinue) {
    throw new IllegalStateException("continue can only occur within while");
  }
}