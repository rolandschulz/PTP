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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranRefactoring;
import org.eclipse.photran.internal.ui.actions.FortranEditorActionDelegate;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;

/**
 * This is the base class for most (all?) of the Eclipse action delegates for Fortran refactoring actions.
 * 
 * @author Jeff Overbey
 */
public class AbstractFortranRefactoringActionDelegate extends FortranEditorActionDelegate
{
    private Class refactoringClass, wizardClass;
    
    public AbstractFortranRefactoringActionDelegate(Class refactoringClass, Class wizardClass)
    {
        this.refactoringClass = refactoringClass;
        this.wizardClass = wizardClass;
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
            FortranRefactoring refactoring = getRefactoring();
            RefactoringWizard wizard = getRefactoringWizard(wizardClass, refactoring);
            
            new RefactoringWizardOpenOperation(wizard).run(getFortranEditor().getShell(), refactoring.getName());
            
            getFortranEditor().forceOutlineViewUpdate();
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
    private FortranRefactoring getRefactoring()
    {
        try
        {
            AbstractFortranEditor editor = getFortranEditor();
            Constructor ctor = refactoringClass.getConstructors()[0];
            return (FortranRefactoring)ctor.newInstance(new Object[] {
                editor.getIFile(),
                /*Boolean.valueOf(editor.isFixedForm()),*/
                editor.getSelection() });
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    /**
     * Invoke the constructor of the given <code>RefactoringWizard</code> with the given
     * <code>FortranRefactoring</code> as its sole argument.
     * @param fortranRefactoringClass
     * @return FortranRefactoring
     */
    private RefactoringWizard getRefactoringWizard(Class wizardClass, FortranRefactoring refactoring)
    {
        try
        {
            Constructor ctor = wizardClass.getConstructors()[0];
            return (RefactoringWizard)ctor.newInstance(new Object[] { refactoring });
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }
}
