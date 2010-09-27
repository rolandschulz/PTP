package org.eclipse.rephraserengine.core.vpg.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.rephraserengine.core.preservation.PreservationAnalysis;
import org.eclipse.rephraserengine.core.preservation.PreservationRuleset;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPGLog;

/**
 * @since 2.0
 */
public abstract class VPGPreservationBasedEditorRefactoring<A, T, V extends EclipseVPG<A, T, ? extends TokenRef<T>, ? extends VPGDB<A, T, ?, ?>, ? extends EclipseVPGLog<T, ?>>>
    extends VPGEditorRefactoring<A, T, V>
{
    protected PreservationAnalysis preservation = null;

    @Override
    protected final void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        try
        {
            pm.beginTask(Messages.VPGPreservationBasedEditorRefactoring_CheckingFinalPreconditions, 40);

            // If the user used the Back button in the refactoring wizard dialog,
            // the AST pointed to by astOfFileInEditor may have been released, so we
            // should re-acquire the current AST to make sure (1) we're not using
            // a modified AST, and (2) we're using an AST that the VPG is currently
            // aware of (i.e., not a stale AST no longer in its cache).
            this.astOfFileInEditor = vpg.acquireTransientAST(fileInEditor);

            doValidateUserInput(status, new SubProgressMonitor(pm, 5));
            if (!status.hasFatalError())
            {
                vpg.acquirePermanentAST(fileInEditor);

                preservation = new PreservationAnalysis(getVPG(), pm, 10,
                    fileInEditor,
                    getEdgesToPreserve());

                doTransform(status, new SubProgressMonitor(pm, 5));

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

    protected abstract void doValidateUserInput(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure;

    /** @since 3.0 */
    protected abstract PreservationRuleset getEdgesToPreserve();

    protected abstract void doTransform(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure;
}
