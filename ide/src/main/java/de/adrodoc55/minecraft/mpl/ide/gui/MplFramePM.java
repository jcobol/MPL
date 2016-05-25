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
package de.adrodoc55.minecraft.mpl.ide.gui;

import static de.adrodoc55.minecraft.mpl.compilation.CompilerOptions.CompilerOption.TRANSMITTER;

import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

import org.beanfabrics.model.AbstractPM;
import org.beanfabrics.model.ListPM;
import org.beanfabrics.model.OperationPM;
import org.beanfabrics.model.PMManager;
import org.beanfabrics.model.Selection;
import org.beanfabrics.support.Operation;

import com.evilco.mc.nbt.stream.NbtOutputStream;
import com.evilco.mc.nbt.tag.TagCompound;
import com.google.common.collect.ListMultimap;

import de.adrodoc55.commons.FileUtils;
import de.adrodoc55.minecraft.mpl.compilation.CompilationFailedException;
import de.adrodoc55.minecraft.mpl.compilation.CompilerException;
import de.adrodoc55.minecraft.mpl.compilation.CompilerOptions;
import de.adrodoc55.minecraft.mpl.compilation.MplCompilationResult;
import de.adrodoc55.minecraft.mpl.compilation.MplCompiler;
import de.adrodoc55.minecraft.mpl.conversion.CommandConverter;
import de.adrodoc55.minecraft.mpl.conversion.PythonConverter;
import de.adrodoc55.minecraft.mpl.conversion.SchematicConverter;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.command.CommandDialog;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.command.CommandDialogControler;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.command.CommandDialogPM;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.compilerexception.ExceptionDialog;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.searchandreplace.SearchAndReplaceDialog;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.searchandreplace.SearchAndReplaceDialogControler;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.searchandreplace.SearchAndReplaceDialogPM;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.unsaved.UnsavedResourcesDialog;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.unsaved.UnsavedResourcesDialogPM;

/**
 * @author Adrodoc55
 */
public class MplFramePM extends AbstractPM {
  public static final String COMPILE_TO_COMMAND = "Compile to Command";
  public static final String COMPILE_TO_SCHEMATIC = "Compile to Schematic";
  public static final String COMPILE_TO_FILTER = "Compile to MCEdit Filter";

  ListPM<MplEditorPM> editors = new ListPM<MplEditorPM>();
  OperationPM newFile = new OperationPM();
  OperationPM openFile = new OperationPM();
  OperationPM saveFile = new OperationPM();
  OperationPM saveFileUnder = new OperationPM();
  OperationPM compileToCommand = new OperationPM();
  OperationPM compileToSchematic = new OperationPM();
  OperationPM compileToSchematicUnder = new OperationPM();
  OperationPM compileToFilter = new OperationPM();
  OperationPM compileToFilterUnder = new OperationPM();

  SearchAndReplaceDialogControler sarController =
      new SearchAndReplaceDialogControler(new SearchAndReplaceDialogPM.Context() {
        @Override
        public JTextComponent getComponent() {
          MplEditorPM selected = editors.getSelection().getFirst();
          MplEditor view = selected.getView();
          if (view == null) {
            return null;
          } else {
            return view.getTextPane();
          }
        }
      });

  public MplFramePM() {
    newFile.setDescription("Create a new file");
    openFile.setDescription("Open a file");
    saveFile.setDescription("Save the current file");
    compileToCommand.setDescription(COMPILE_TO_COMMAND);
    compileToSchematic.setDescription(COMPILE_TO_SCHEMATIC);
    compileToSchematicUnder.setDescription(COMPILE_TO_SCHEMATIC);
    compileToFilter.setDescription(COMPILE_TO_FILTER);
    compileToFilterUnder.setDescription(COMPILE_TO_FILTER);
    PMManager.setup(this);
  }

  @Operation
  public void newFile() {
    MplEditorPM editorPm = new MplEditorPM(createDefaultContext());
    addMplEditorPm(editorPm);
  }

  @Operation
  public void openFile() {
    Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    JFileChooser chooser = MplEditor.getMplChooser();
    int userAction = chooser.showOpenDialog(activeWindow);
    if (userAction != JFileChooser.APPROVE_OPTION) {
      return;
    }
    File file = chooser.getSelectedFile();
    if (!file.exists()) {
      String message = "The File '" + file.getPath() + "' couldn't be found!";
      JOptionPane.showMessageDialog(chooser, message, "Error", JOptionPane.ERROR_MESSAGE);
      openFile();
      return;
    }
    if (!file.isFile()) {
      String message = file.getPath() + " is not a File!";
      JOptionPane.showMessageDialog(chooser, message, "Error", JOptionPane.ERROR_MESSAGE);
      openFile();
      return;
    }
    if (!file.canRead()) {
      String message = "The File '" + file.getPath() + "' couldn't be opened!";
      JOptionPane.showMessageDialog(chooser, message, "Error", JOptionPane.ERROR_MESSAGE);
      openFile();
      return;
    }
    for (MplEditorPM editorPm : editors) {
      if (file.equals(editorPm.getFile())) {
        Selection<MplEditorPM> selection = editors.getSelection();
        selection.clear();
        selection.add(editorPm);
        return;
      }
    }
    try {
      MplEditorPM editorPm = new MplEditorPM(createDefaultContext());
      editorPm.setFile(file);
      editorPm.load();
      addMplEditorPm(editorPm);
      editorPm.setUnsavedChanges(false);
    } catch (IOException ex) {
      String message = "The File '" + file.getPath() + "' couldn't be loaded!\n" + ex.getMessage();
      String title = ex.getClass().getSimpleName();
      JOptionPane.showMessageDialog(chooser, message, title, JOptionPane.ERROR_MESSAGE);
      openFile();
    }
  }

  private void addMplEditorPm(MplEditorPM editorPm) {
    editors.add(editorPm);
    editors.getSelection().clear();
    editors.getSelection().add(editorPm);
  }

  @Operation
  public void saveFile() {
    MplEditorPM selected = editors.getSelection().getFirst();
    if (selected == null) {
      return;
    }
    selected.save();
  }

  @Operation
  public void saveFileUnder() {
    MplEditorPM selected = editors.getSelection().getFirst();
    if (selected == null) {
      return;
    }
    selected.saveUnder();
  }

  @Operation
  public void compileToCommand() {
    MplCompilationResult result = compile();
    if (result == null) {
      return;
    }
    List<String> commands = CommandConverter.convert(result);
    CommandDialogControler ctrl = new CommandDialogControler();
    CommandDialogPM pm = ctrl.getPresentationModel();
    CommandDialog view = ctrl.getView();
    pm.setCommands(commands);
    view.setVisible(true);
  }

  @Operation
  public void compileToSchematic() {
    try {
      MplCompilationResult result = compile();
      if (result == null) {
        return;
      }
      MplEditorPM selected = editors.getSelection().getFirst();
      if (selected == null) {
        return;
      }
      File dir = getCompilationDir(COMPILE_TO_SCHEMATIC);
      if (dir == null) {
        return;
      }
      String name = FileUtils.getFilenameWithoutExtension(selected.getTitle());
      String targetFileName = name + ".schematic";
      File outputFile = new File(dir, targetFileName);
      outputFile.getParentFile().mkdirs();
      outputFile.createNewFile();
      TagCompound schematic = SchematicConverter.convert(result);
      try (NbtOutputStream out = new NbtOutputStream(new FileOutputStream(outputFile));) {
        out.write(schematic);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
      JOptionPane.showMessageDialog(activeWindow, ex.getMessage(), ex.getClass().getSimpleName(),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  @Operation
  public void compileToSchematicUnder() {
    chooseCompilationDir(COMPILE_TO_SCHEMATIC);
    compileToSchematic();
  }

  @Operation
  public void compileToFilter() {
    try {
      MplCompilationResult result = compile();
      if (result == null) {
        return;
      }
      MplEditorPM selected = editors.getSelection().getFirst();
      if (selected == null) {
        return;
      }
      File dir = getCompilationDir(COMPILE_TO_FILTER);
      if (dir == null) {
        return;
      }
      String name = FileUtils.getFilenameWithoutExtension(selected.getTitle());
      String targetFileName = name + ".py";
      File outputFile = new File(dir, targetFileName);
      outputFile.getParentFile().mkdirs();
      outputFile.createNewFile();
      String python = PythonConverter.convert(result, name);
      try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath());) {
        writer.write(python);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
      JOptionPane.showMessageDialog(activeWindow, ex.getMessage(), ex.getClass().getSimpleName(),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  @Operation
  public void compileToFilterUnder() {
    chooseCompilationDir(COMPILE_TO_FILTER);
    compileToFilter();
  }

  private MplCompilationResult compile() {
    if (warnAboutUnsavedResources()) {
      return null;
    }

    MplEditorPM selected = editors.getSelection().getFirst();
    if (selected == null) {
      return null;
    }
    File file = selected.getFile();
    if (file == null || !file.exists()) {
      Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
      JOptionPane.showMessageDialog(activeWindow,
          "You need to save this File before it can be compiled!", "Compilation Failed!",
          JOptionPane.ERROR_MESSAGE);
      return null;
    }
    try {
      MplCompilationResult result = MplCompiler.compile(file, new CompilerOptions(TRANSMITTER));
      for (MplEditorPM editorPm : editors) {
        editorPm.setCompilerExceptions(Collections.emptyList());
      }
      return result;
    } catch (CompilationFailedException ex) {
      ExceptionDialog dialog = ExceptionDialog.create("Compilation Failed!",
          "The Compiler encountered Errors!", ex.toString());
      dialog.setVisible(true);
      ListMultimap<File, CompilerException> exceptions = ex.getExceptions();
      for (File programFile : exceptions.keySet()) {
        for (MplEditorPM editorPm : editors) {
          if (programFile.equals(editorPm.getFile())) {
            editorPm.setCompilerExceptions(exceptions.get(programFile));
          }
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
      JOptionPane.showMessageDialog(activeWindow, ex.getMessage(), ex.getClass().getSimpleName(),
          JOptionPane.ERROR_MESSAGE);
    }
    return null;
  }

  private File compilationDir;

  private File getCompilationDir(String title) {
    if (compilationDir == null) {
      chooseCompilationDir(title);
    }
    return compilationDir;
  }

  private void chooseCompilationDir(String title) {
    Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    JFileChooser chooser = MplEditor.getDirChooser();
    int userAction = chooser.showDialog(activeWindow, title);
    if (userAction == JFileChooser.APPROVE_OPTION) {
      compilationDir = chooser.getSelectedFile();
    }
  }

  /**
   * Warn the User about any unsaved Resources, if there are any. Returns true if the User canceled
   * the Action. <br>
   * This should be called like this:<br>
   *
   * <pre>
   * <code>
   * if (warnAboutUnsavedResources()) {
   *   return;
   * }
   * </code>
   * </pre>
   *
   * @return canceled - whether or not the Action should be canceled.
   */
  private boolean warnAboutUnsavedResources() {
    checkFiles();
    LinkedList<MplEditorPM> unsaved = new LinkedList<MplEditorPM>();
    for (MplEditorPM editorPm : editors) {
      if (editorPm.hasUnsavedChanges() && isRelevant(editorPm)) {
        unsaved.add(editorPm);
      }
    }
    if (!unsaved.isEmpty()) {
      return showUnsavedResourcesDialog(unsaved);
    }
    return false;
  }

  /**
   * Checks for the existence of every editor's file. See {@link MplEditorPM#checkFile()} for more
   * details.
   */
  void checkFiles() {
    for (MplEditorPM editorPm : editors) {
      editorPm.checkFile();
    }
  }

  private MplEditorPM.Context createDefaultContext() {
    return new MplEditorPM.Context() {
      @Override
      public void close(MplEditorPM editorPm) {
        if (editorPm.hasUnsavedChanges() && isRelevant(editorPm)) {
          ArrayList<MplEditorPM> unsaved = new ArrayList<MplEditorPM>(1);
          unsaved.add(editorPm);

          if (showUnsavedResourcesDialog(unsaved)) {
            return;
          }
        }
        editors.remove(editorPm);
      }

      @Override
      public SearchAndReplaceDialog getSearchAndReplaceDialog() {
        return sarController.getView();
      }
    };
  }

  private boolean isRelevant(MplEditorPM editorPm) {
    return !editorPm.code.isEmpty() || editorPm.getFile() != null;
  }

  /**
   * Shows an {@link UnsavedResourcesDialog} to the user. Returns true if the User canceled the
   * Action. <br>
   * This should be called like this:<br>
   *
   * <pre>
   * <code>
   * if (showUnsavedResourcesDialog(unsaved)) {
   *   return;
   * }
   * </code>
   * </pre>
   *
   * @return canceled - whether or not the Action should be canceled.
   */
  private boolean showUnsavedResourcesDialog(Collection<MplEditorPM> unsaved) {
    Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    UnsavedResourcesDialog dialog = new UnsavedResourcesDialog(activeWindow);
    UnsavedResourcesDialogPM dialogPm = new UnsavedResourcesDialogPM(unsaved);
    dialog.setPresentationModel(dialogPm);
    dialog.setVisible(true);
    return dialogPm.isCanceled();
  }

  public void terminate() {
    if (warnAboutUnsavedResources()) {
      return;
    }
    System.exit(0);
  }

}