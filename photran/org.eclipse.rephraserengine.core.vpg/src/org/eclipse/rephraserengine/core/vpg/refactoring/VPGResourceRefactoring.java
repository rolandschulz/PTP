package org.eclipse.rephraserengine.core.vpg.refactoring;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.rephraserengine.core.refactorings.IResourceRefactoring;
import org.eclipse.rephraserengine.core.vpg.IVPGNode;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

/**
 * A refactoring which operates on entire files (or folders) at once, rather than requiring the user
 * to make a text selection in an editor.
 * <p>
 * Contrast with {@link VPGEditorRefactoring}.
 * 
 * @author Jeff Overbey
 * 
 * @param <A> AST type
 * @param <T> node/token type (i.e., the type returned by {@link IVPGNode#getASTNode()})
 * @param <V> VPG
 * 
 * @since 2.0
 */
public abstract class VPGResourceRefactoring<A, T, V extends EclipseVPG<A, T, ? extends IVPGNode<T>>>
    extends VPGRefactoring<A, T, V>
    implements IResourceRefactoring
{
    protected List<IFile> selectedFiles = null;

    public void initialize(List<IFile> files)
    {
        if (files == null) throw new IllegalArgumentException("files argument cannot be null"); //$NON-NLS-1$
        if (files.isEmpty()) throw new IllegalArgumentException("files argument cannot be empty"); //$NON-NLS-1$
        
        // Copy the list to ensure it is mutable
        this.selectedFiles = new LinkedList<IFile>();
        this.selectedFiles.addAll(files);
    }


    @Override
    protected void checkFiles(RefactoringStatus status) throws PreconditionFailure
    {
        assert selectedFiles != null;

        for (IFile file : selectedFiles)
            checkIfFileIsAccessibleAndWritable(file);
    }
}
