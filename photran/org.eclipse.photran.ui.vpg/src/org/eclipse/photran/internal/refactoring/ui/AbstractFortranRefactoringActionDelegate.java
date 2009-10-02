/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.refactoring.ui;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring;
import org.eclipse.photran.internal.ui.actions.FortranEditorActionDelegate;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.internal.ObjectPluginAction;
import org.eclipse.ui.internal.Workbench;

/**
 * This is the base class for most (all?) of the Eclipse action delegates for Fortran refactoring actions.
 *
 * @author Jeff Overbey, Timofey Yuvashev
 */
@SuppressWarnings("restriction")
public abstract class AbstractFortranRefactoringActionDelegate extends FortranEditorActionDelegate
{
    @SuppressWarnings("unused")
    private Class<?> refactoringClass, wizardClass;

    public AbstractFortranRefactoringActionDelegate(Class<?> refactoringClass, Class<?> wizardClass)
    {
        this.refactoringClass = refactoringClass;
        this.wizardClass = wizardClass;
    }

    private static boolean hasFile(ArrayList<IFile> files, IPath fullPath)
    {
        for(IFile f : files)
        {
            IPath fileFullPath = f.getFullPath();
            if(fileFullPath.equals(fullPath))
                return true;
        }
        return false;
    }

    private boolean saveFile(AbstractFortranEditor editor)
    {
        ArrayList<IFile> filesToCheck = new ArrayList<IFile>();
        filesToCheck.add(editor.getIFile());
        IEditorPart[] dirtyEditors = {editor};
        return saveModifiedFiles(filesToCheck, dirtyEditors);
    }

    private boolean saveSelectedFiles(IStructuredSelection structSel)
    {
        ArrayList<IFile> filesToCheck = populateFilesFromSelection(structSel);
        IEditorPart[] dirtyEditors = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getDirtyEditors();
        return saveModifiedFiles(filesToCheck, dirtyEditors);
    }

    //This function returns user selection (OK or Cancel) as a boolean.
    // If it is passed in "false", user is not prompted, and "true" is returned by default
    private boolean promptUser(boolean shouldPrompt)
    {
        boolean userSelection = true;

        if(shouldPrompt)
        {
            userSelection = MessageDialog.openConfirm(null,
                "Files need to be saved",
                "In order to refactor the selected file(s) they will be saved. " +
                "Do you want to proceed?");
        }

        return userSelection;
    }

    private boolean saveModifiedFiles(ArrayList<IFile> filesToCheck, IEditorPart[] dirtyEditors)
    {
        boolean promptUser = true;
        for(int i = 0; i < dirtyEditors.length; i++)
        {
            IEditorInput editorInput = dirtyEditors[i].getEditorInput();
            if(editorInput instanceof IFileEditorInput)
            {
                IFileEditorInput fileEditorInput = (IFileEditorInput)editorInput;
                IPath fullPath = fileEditorInput.getFile().getFullPath();
                if(hasFile(filesToCheck, fullPath) && dirtyEditors[i].isDirty())
                {
                    if(promptUser(promptUser))
                    {
                        //If user agreed to save files, proceed
                        saveFileInEditorIfDirty(dirtyEditors[i]);
                    }
                    //Otherwise, return false
                    else
                        return false;

                    //Make sure the user is not prompted for every file
                    promptUser = false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if there are multiple selected files or if a file is selected from
     * PackageExplorer window. If that is the case, runs a separate routine for
     * creating a <code>FortranRefactoring</code> and <code>RefactoringWizard</code>.
     * Otherwise, uses its ancestor's run() method.
     *
     * @param action instanceof IAction
     */
    @Override public void run(IAction action)
    {
        //If there are unsaved files, and the user chooses not to save them, don't run
        // the refacotring
        boolean shouldRun = true;

        if(action instanceof ObjectPluginAction)
        {
            ISelection select = ((ObjectPluginAction)action).getSelection();
            if(select instanceof IStructuredSelection)
            {
                IStructuredSelection structSel = (IStructuredSelection)select;
                if(structSel.size() > 0)
                {
                    shouldRun = saveSelectedFiles(structSel);
                    if(shouldRun)
                        runForSelectedFiles(structSel);
                }
            }
        }
        else
        {
            //HACK: For some reason the editor is not set at certain times
            AbstractFortranEditor editor =
                (AbstractFortranEditor)Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

            shouldRun = saveFile(editor);
            if(shouldRun)
                super.run(action);
        }
    }

    //TODO: Possibly prompt the user if he/she wants to save the files that
    // we are trying to refactor? Or should we save them by default?
    private void saveFileInEditorIfDirty(IEditorPart editor)
    {
        if(editor.isDirty())
            editor.doSave(null);
    }

    private ArrayList<IFile> populateFilesFromSelection(IStructuredSelection structSel)
    {
        ArrayList<IFile> myFiles = new ArrayList<IFile>();
        Iterator<?> iter = structSel.iterator();
        while (iter.hasNext())
        {
            Object obj = iter.next();
            if (obj instanceof IFile)
            {
                IFile file = (IFile)obj;
                //TODO: Add support for Fixed-form files
                if (PhotranVPG.hasFreeFormContentType(file))
                    myFiles.add(file);
            }
            else if (obj instanceof IFolder || obj instanceof IProject)
            {
                IContainer tempC = (IContainer)obj;
                try
                {
                    myFiles.addAll(extractFiles(tempC.members()));
                }
                catch (CoreException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return myFiles;
    }

    /**
     * Helper method for populateFilesFromSelection. Since selection takes in a
     * IStructuredSelection, we can't recurse on it. That's when the helper method
     * comes in useful. This extracts all the files from selected files/folders/projects
     * @param resources
     * @return
     */
    private ArrayList<IFile> extractFiles(IResource[] resources)
    {
        ArrayList<IFile> files = new ArrayList<IFile>();
        for(IResource r : resources)
        {
            if(r instanceof IFile)
            {
                if(PhotranVPG.hasFreeFormContentType((IFile)r))
                    files.add((IFile)r);
            }
            else if(r instanceof IFolder || r instanceof IProject)
            {
                IContainer tempC = (IContainer)r;
                try
                {
                    files.addAll(extractFiles(tempC.members()));
                }
                catch (CoreException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return files;
    }

    /**
     * Creates an <code>AbstractFortranRefactoring</code> and a <code>RefactoringWizard</code>
     * for refactorings that require/accept operations on multiple files(i.e. IntroduceImplicitNone)
     *
     * @param structSel instanceof IStructured selection. Used to extract the list of selected files
     *          to be modified
     */
    private void runForSelectedFiles(IStructuredSelection structSel)
    {
        ArrayList<IFile> myFiles = populateFilesFromSelection(structSel);

        AbstractFortranRefactoring refact = getRefactoring(myFiles);
        RefactoringWizard wizard = getRefactoringWizard(wizardClass, refact);
        try
        {
            //AbstractFortranEditor abstEditor = (AbstractFortranEditor)Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            //Shell shell = abstEditor == null ? null : abstEditor.getShell();

            Shell shell = Workbench.getInstance().getActiveWorkbenchWindow().getShell();
            if (shell == null) return;

            String name = refact.getName();
            RefactoringWizardOpenOperation wiz = new RefactoringWizardOpenOperation(wizard);
            wiz.run(shell, name);

            AbstractFortranEditor activeFortranEditor = getFortranEditor();
            if (activeFortranEditor != null)
                activeFortranEditor.forceOutlineViewUpdate();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            PhotranVPG.getInstance().releaseAllASTs();
        }
    }


    /**
     * (Required by FortranEditorActionDelegate)
     *
     * Runs a <code>FortranRefactoring</code> on the contents of the active editor, using the
     * given <code>RefactoringWizard</code> for user interaction.
     *
     * @param refactoringClass instanceof FortranRefactoring
     * @param wizardClass instanceof RefactoringWizard
     */
    public void run(IProgressMonitor progressMonitor)
    {
        try
        {
            if (getFortranEditor().getIFile() == null)
            {
                MessageDialog.openError(getFortranEditor().getShell(), "Error",
                    "The file in the editor cannot be refactored.\n\nFortran files can only be refactored if they " +
                    "are located inside a Fortran project in your workspace.");
            }
            else
            {
                ArrayList<IFile> files = new ArrayList<IFile>();
                files.add(getFortranEditor().getIFile());
                AbstractFortranRefactoring refactoring = getRefactoring(files);
                RefactoringWizard wizard = getRefactoringWizard(wizardClass, refactoring);

                new RefactoringWizardOpenOperation(wizard).run(getFortranEditor().getShell(), refactoring.getName());

                getFortranEditor().forceOutlineViewUpdate();
            }
        }
        catch (InterruptedException e)
        {
            ;
        }
        finally
        {
            PhotranVPG.getInstance().releaseAllASTs();
        }
    }

    /**
     * Invoke the constructor of the given <code>FortranRefactoring</code> with arguments corresponding to the
     * contents of the active editor.
     * @param fortranRefactoringClass
     * @return FortranRefactoring
     */
    protected abstract AbstractFortranRefactoring getRefactoring(ArrayList<IFile> files);

    /**
     * Invoke the constructor of the given <code>RefactoringWizard</code> with the given
     * <code>FortranRefactoring</code> as its sole argument.
     * @param fortranRefactoringClass
     * @return FortranRefactoring
     */
    private RefactoringWizard getRefactoringWizard(Class<?> wizardClass, AbstractFortranRefactoring refactoring)
    {
        try
        {
            Constructor<?> ctor = wizardClass.getConstructors()[0];
            return (RefactoringWizard)ctor.newInstance(new Object[] { refactoring });
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }
}
