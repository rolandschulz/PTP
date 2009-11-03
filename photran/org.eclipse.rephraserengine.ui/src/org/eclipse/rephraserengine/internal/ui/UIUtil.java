/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.ui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;

/**
 * A collection of static utility methods for use by Eclipse user interfaces.
 *
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public class UIUtil
{
    private UIUtil() {;}

    public static File createTempFile() throws IOException
    {
        File tempFile = File.createTempFile("rephraser-tmp", ".txt");
        tempFile.deleteOnExit();
        return tempFile;
    }

    public static PrintStream createPrintStream(File tempFile) throws FileNotFoundException
    {
        return new PrintStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
    }

    public static void openHtmlViewerOn(String title, String str)
    {
        try
        {
            File temp = createTempFile();
            PrintStream text = createPrintStream(temp);
            text.print(str);
            text.close();
            openHtmlViewerOn(title, temp);
        }
        catch (IOException e)
        {
            MessageDialog.openError(determineActiveShell(),
                                    "Error",
                                    "Unable to create temporary file.\n\n" + e.getMessage());
        }
    }

    public static void openHtmlViewerOn(final String title, final File file)
    {
        final class HtmlRunnable implements Runnable
        {
            public void run()
            {
                try
                {
                    // This is cheating, but it works
                    //new URLHyperlink(new Region(0, 0), filPath).open();

                    PlatformUI.getWorkbench().getBrowserSupport().createBrowser(0, null, title, null).openURL(file.toURI().toURL());
                }
                catch (final Throwable e)
                {
                    Display.getDefault().asyncExec(new Runnable()
                    {
                        public void run()
                        {
                            MessageDialog.openError(determineActiveShell(),
                                                    "Error",
                                                    "Unable to open Web browser.\n\n" + e.getMessage());
                        }
                    });
                }
            }
        }

        Display.getDefault().syncExec(new HtmlRunnable());
    }

    public static Shell determineActiveShell()
    {
        if (Workbench.getInstance().getWorkbenchWindowCount() == 0)
            return null;
        else
            return Workbench.getInstance().getActiveWorkbenchWindow().getShell();
    }

    public static void displayErrorDialog(String message)
    {
        MessageDialog.openError(determineActiveShell(),
                                "Error",
                                message);
    }

    public static void displayInfoDialog(String message)
    {
        MessageDialog.openInformation(determineActiveShell(),
                                      "Note",
                                      message);
    }

    public static IEditorPart[] getDirtyEditors()
    {
        IWorkbenchWindow window = Workbench.getInstance().getActiveWorkbenchWindow();
        if (window == null) return new IEditorPart[0];

        IWorkbenchPage page = window.getActivePage();
        if (page == null) return new IEditorPart[0];

        return page.getDirtyEditors();
    }

    /**
     * @return false iff the given file was open in an editor and had been modified, but
     *         the user refused to save those changes
     */
    public static boolean askUserToSaveModifiedFiles()
    {
        return askUserToSaveModifiedFiles((List<IFile>)null);
    }

    /**
     * @return false iff the given file was open in an editor and had been modified, but
     *         the user refused to save those changes
     */
    public static boolean askUserToSaveModifiedFiles(IFile fileInEditor)
    {
        return askUserToSaveModifiedFiles(Collections.<IFile>singletonList(fileInEditor));
    }

    /**
     * @return false iff one of the given files was open in an editor and had been modified, but
     *         the user refused to save those changes
     */
    public static boolean askUserToSaveModifiedFiles(List<IFile> filesToCheck)
    {
        return askUserToSaveModifiedFiles(filesToCheck, getDirtyEditors());
    }

    /**
     * @return false iff one of the given files was open in one of the given editors and had been
     *         modified, but the user refused to save those changes
     */
    public static boolean askUserToSaveModifiedFiles(List<IFile> filesToCheck, IEditorPart[] dirtyEditors)
    {
        boolean promptUser = true;
        for (int i = 0; i < dirtyEditors.length; i++)
        {
            IEditorInput editorInput = dirtyEditors[i].getEditorInput();
            if (editorInput instanceof IFileEditorInput)
            {
                IFileEditorInput fileEditorInput = (IFileEditorInput)editorInput;
                IFile file = fileEditorInput == null ? null : fileEditorInput.getFile();
                IPath fullPath = file == null ? null : file.getFullPath();
                if (fullPath != null && hasFile(filesToCheck, fullPath) && dirtyEditors[i].isDirty())
                {
                    if (promptUser(promptUser))
                    {
                        //If user agreed to save files, proceed
                        saveFileInEditorIfDirty(dirtyEditors[i]);
                    }
                    else return false;

                    //Make sure the user is not prompted for every file
                    promptUser = false;
                }
            }
        }
        return true;
    }

    private static boolean hasFile(List<IFile> files, IPath fullPath)
    {
        if (files == null) return true;

        for (IFile f : files)
        {
            IPath fileFullPath = f.getFullPath();
            if (fileFullPath != null && fileFullPath.equals(fullPath))
                return true;
        }
        return false;
    }

    //This function returns user selection (OK or Cancel) as a boolean.
    // If it is passed in "false", user is not prompted, and "true" is returned by default
    private static boolean promptUser(boolean shouldPrompt)
    {
        boolean userSelection = true;

        if (shouldPrompt)
        {
            userSelection = MessageDialog.openConfirm(null,
                "Save Files Before Refactoring",
                "Some file(s) currently being edited have unsaved changes; these must be saved " +
                "before the refactoring can proceed.\n\nDo you want to save these changes?");
        }

        return userSelection;
    }

    //TODO: Possibly prompt the user if he/she wants to save the files that
    // we are trying to refactor? Or should we save them by default?
    private static void saveFileInEditorIfDirty(IEditorPart editor)
    {
        if (editor.isDirty())
            editor.doSave(null);
    }
}
