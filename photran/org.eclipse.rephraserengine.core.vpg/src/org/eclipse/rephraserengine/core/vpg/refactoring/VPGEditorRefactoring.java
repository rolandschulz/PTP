package org.eclipse.rephraserengine.core.vpg.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.rephraserengine.core.refactorings.IEditorRefactoring;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPGLog;

public abstract class VPGEditorRefactoring<A, T, V extends EclipseVPG<A, T, ? extends TokenRef<T>, ? extends VPGDB<A, T, ?, ?>, ? extends EclipseVPGLog<T, ?>>>
    extends VPGRefactoring<A, T, V>
    implements IEditorRefactoring
{
    protected IFile fileInEditor;
    protected ITextSelection selectedRegionInEditor;
    protected A astOfFileInEditor;

    public void initialize(IFile file, ITextSelection selection)
    {
        if (file == null) throw new IllegalArgumentException("file argument cannot be null");

        this.fileInEditor = file;
        this.selectedRegionInEditor = selection;
        this.astOfFileInEditor = null; // until #checkInitialConditions invoked
    }

    public void initialize(IFile file)
    {
        initialize(file, null);
    }

    @Override
    protected void checkFiles(RefactoringStatus status) throws PreconditionFailure
    {
        assert fileInEditor != null;

        checkIfFileIsAccessibleAndWritable(fileInEditor);

        this.astOfFileInEditor = vpg.acquireTransientAST(fileInEditor);
        logVPGErrors(status);
        if (astOfFileInEditor == null)
            fail("The file in the editor cannot be parsed.");
    }
}
