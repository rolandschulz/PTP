/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring.infrastructure;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.preservation.PreservationAnalysis;
import org.eclipse.rephraserengine.core.preservation.Preserve;

/**
 * Base class for {@link SingleFileFortranRefactoring}s that use a {@link PreservationAnalysis} for
 * precondition checking.
 *
 * @author Jeff Overbey
 */
public abstract class PreservationBasedSingleFileFortranRefactoring extends SingleFileFortranRefactoring
{
    protected PreservationAnalysis preservation = null;

    @Override
    protected final void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        try
        {
            pm.beginTask("Checking final preconditions", 30);

            doValidateUserInput(status);
            if (!status.hasFatalError())
            {
                preservation = new PreservationAnalysis(PhotranVPG.getInstance(), pm, 10,
                    fileInEditor,
                    getEdgesToPreserve());

                doTransform();

                vpg.commitChangesFromInMemoryASTs(pm, 20, fileInEditor);
                preservation.checkForPreservation(status, pm, 0);

                this.addChangeFromModifiedAST(this.fileInEditor, pm);
            }

            pm.done();
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    @Override
    protected final void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
    }

    protected abstract void doValidateUserInput(RefactoringStatus status) throws PreconditionFailure;

    protected abstract Preserve getEdgesToPreserve();

    protected abstract void doTransform() throws PreconditionFailure;
}
