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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.refactoring.IntroImplicitNoneRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring;
import org.eclipse.photran.internal.refactoring.ui.IntroImplicitNoneAction.FortranIntroImplicitNoneRefactoringWizard;
import org.eclipse.photran.internal.refactoring.ui.RenameAction.FortranRenameRefactoringWizard;
import org.eclipse.photran.internal.ui.actions.FortranEditorActionDelegate;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ObjectPluginAction;
import org.eclipse.ui.internal.Workbench;

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
     * Checks if there are multiple selected files or if a file is selected from 
     * PackageExplorer window. If that is the case, runs a separate routine for 
     * creating a <code>FortranRefactoring</code> and <code>RefactoringWizard</code>.
     * Otherwise, uses its ancestor's run() method. 
     * 
     * @param action instanceof IAction
     */
    @Override public void run(IAction action)
    {
        if(action instanceof ObjectPluginAction)
        {
            ISelection select = ((ObjectPluginAction)action).getSelection();
            if(select instanceof IStructuredSelection)
            {
                IStructuredSelection structSel = (IStructuredSelection)select;
                if(structSel.size() > 0)
                    runForSelectedFiles(structSel);
                else
                    super.run(action);
            }
        }
        else
            super.run(action);        
    }
       
    private ArrayList<IFile> populateFilesFromSelection(IStructuredSelection structSel)
    {
        ArrayList<IFile> myFiles = new ArrayList<IFile>();
        Iterator iter = structSel.iterator();
        for(;iter.hasNext();)
        {
            Object obj = iter.next();
            if(obj instanceof IFile)
                myFiles.add((IFile)obj);
        }
        return myFiles;
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
            AbstractFortranEditor abstEditor = (AbstractFortranEditor)Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            Shell shell = abstEditor == null ? null : abstEditor.getShell();
            String name = refact.getName();
            RefactoringWizardOpenOperation wiz = new RefactoringWizardOpenOperation(wizard);
            wiz.run(shell, name);
            getFortranEditor().forceOutlineViewUpdate();
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
