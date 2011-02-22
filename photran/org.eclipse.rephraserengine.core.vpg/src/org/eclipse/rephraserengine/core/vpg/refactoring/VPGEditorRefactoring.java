package org.eclipse.rephraserengine.core.vpg.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.rephraserengine.core.refactorings.IEditorRefactoring;
import org.eclipse.rephraserengine.core.vpg.IVPGNode;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

/**
 * A refactoring which requires the user to make a selection in a text editor in order to invoke the
 * refactoring.
 * <p>
 * Contrast with {@link VPGResourceRefactoring}.
 * 
 * @author Jeff Overbey
 * 
 * @param <A> AST type
 * @param <T> node/token type (i.e., the type returned by {@link IVPGNode#getASTNode()})
 * @param <V> VPG
 * 
 * @since 2.0
 */
public abstract class VPGEditorRefactoring<A, T, V extends EclipseVPG<A, T, ? extends IVPGNode<T>>>
    extends VPGRefactoring<A, T, V>
    implements IEditorRefactoring
{
    protected IFile fileInEditor;
    protected ITextSelection selectedRegionInEditor;
    protected A astOfFileInEditor;

    public void initialize(IFile file, ITextSelection selection)
    {
        if (file == null) throw new IllegalArgumentException("file argument cannot be null"); //$NON-NLS-1$

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
        logVPGErrors(status, fileInEditor);
        if (astOfFileInEditor == null)
            fail(Messages.VPGEditorRefactoring_FileInTheEditorCannotBeParsed);
    }
}
