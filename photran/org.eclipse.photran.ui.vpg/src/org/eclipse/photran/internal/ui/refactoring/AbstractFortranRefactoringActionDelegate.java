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
package org.eclipse.photran.internal.ui.refactoring;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.ui.actions.FortranEditorActionDelegate;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.photran.internal.ui.vpg.PhotranResourceFilter;
import org.eclipse.rephraserengine.core.refactorings.IEditorRefactoring;
import org.eclipse.rephraserengine.core.refactorings.IResourceRefactoring;
import org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring;
import org.eclipse.rephraserengine.internal.ui.UIUtil;
import org.eclipse.rephraserengine.ui.WorkbenchSelectionInfo;
import org.eclipse.swt.widgets.Shell;

/**
 * This is the base class for most (all?) of the Eclipse action delegates for Fortran refactoring actions.
 *
 * @author Jeff Overbey, Timofey Yuvashev
 */
public abstract class AbstractFortranRefactoringActionDelegate extends FortranEditorActionDelegate
{
    private Class<?> refactoringClass, wizardClass;

    public AbstractFortranRefactoringActionDelegate(Class<?> refactoringClass, Class<?> wizardClass)
    {
        this.refactoringClass = refactoringClass;
        this.wizardClass = wizardClass;
    }

    /**
     * This method is invoked when the action is invoked from the UI by the user.
     * <p>
     * It checks if there are multiple selected files or if a file is selected from
     * PackageExplorer window. If that is the case, runs a separate routine for
     * creating a <code>FortranRefactoring</code> and <code>RefactoringWizard</code>.
     * Otherwise, uses its ancestor's run() method.
     *
     * @param action instanceof IAction
     */
    @Override public void run(IAction action)
    {
        WorkbenchSelectionInfo selection = new WorkbenchSelectionInfo(new PhotranResourceFilter());

        if (IEditorRefactoring.class.isAssignableFrom(refactoringClass)
            && (!IResourceRefactoring.class.isAssignableFrom(refactoringClass)
                || selection.isTextSelectedInEditor())
            && UIUtil.askUserToSaveModifiedFiles(selection.getFileInEditor()))
        {
            super.run(action);
        }
        else if (IResourceRefactoring.class.isAssignableFrom(refactoringClass)
            && UIUtil.askUserToSaveModifiedFiles(selection.getAllFilesInSelectedResources()))
        {
            runForSelectedFiles(selection);
        }
        else
        {
            throw new IllegalStateException(
                "refactoringClass " +
                refactoringClass.getName() +
                " passed to AbstractFortranRefactoringActionDelegate constructor " +
                " is not an instance of IResourceRefactoring or IEditorRefactoring");
        }
    }

    /**
     * Creates an <code>VPGRefactoring<IFortranAST, Token, PhotranVPG></code> and a <code>RefactoringWizard</code>
     * for refactorings that require/accept operations on multiple files(i.e. IntroduceImplicitNone)
     * @param selection
     *
     * @param structSel instanceof IStructured selection. Used to extract the list of selected files
     *          to be modified
     */
    private void runForSelectedFiles(WorkbenchSelectionInfo selection)
    {
        VPGRefactoring<IFortranAST, Token, PhotranVPG> refact = getRefactoring(selection.getAllFilesInSelectedResources());
        RefactoringWizard wizard = getRefactoringWizard(wizardClass, refact);
        try
        {
            //AbstractFortranEditor abstEditor = (AbstractFortranEditor)Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            //Shell shell = abstEditor == null ? null : abstEditor.getShell();

            Shell shell = UIUtil.determineActiveShell();
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
     * This method is invoked after this action has been scheduled.
     * <p>
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
                VPGRefactoring<IFortranAST, Token, PhotranVPG> refactoring = getRefactoring(files);
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
    protected abstract VPGRefactoring<IFortranAST, Token, PhotranVPG> getRefactoring(List<IFile> files);

    /**
     * Invoke the constructor of the given <code>RefactoringWizard</code> with the given
     * <code>FortranRefactoring</code> as its sole argument.
     * @param fortranRefactoringClass
     * @return FortranRefactoring
     */
    private RefactoringWizard getRefactoringWizard(Class<?> wizardClass, VPGRefactoring<IFortranAST, Token, PhotranVPG> refactoring)
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
