/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranResourceRefactoring;

/*
 * This file is for documentation only.  It provides classes that can be used as templates for
 * creating new refactorings.  For information on creating refactorings, see the
 * Photran Developer's Guide.
 */

class SampleResourceRefactoring extends FortranResourceRefactoring
{
    @Override
    public String getName()
    {
        return "Sample Resource Refactoring";
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        removeFixedFormFilesFrom(this.selectedFiles, status);
        removeCpreprocessedFilesFrom(this.selectedFiles, status);
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        try
        {
            for (IFile file : selectedFiles)
            {
                IFortranAST ast = vpg.acquirePermanentAST(file);
                if (ast == null)
                    status.addError("One of the selected files (" + file.getName()
                        + ") cannot be parsed.");
                makeChangesTo(file, ast, status, pm);
                vpg.releaseAST(file);
            }
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    private void makeChangesTo(IFile file, IFortranAST ast, RefactoringStatus status,
        IProgressMonitor pm) throws Error
    {
        try
        {
            if (ast == null) return;

            // Do something with ast
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        // Change created in #doCheckFinalConditions
    }
}

class SampleEditorRefactoring extends FortranEditorRefactoring
{
    @Override
    public String getName()
    {
        return "Sample Editor Refactoring";
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        @SuppressWarnings("unused")
        IASTNode node = findEnclosingNode(this.astOfFileInEditor, this.selectedRegionInEditor);
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        // No final preconditions
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        // Do something with

        this.addChangeFromModifiedAST(this.fileInEditor, pm);

        vpg.releaseAST(this.fileInEditor);
    }
}