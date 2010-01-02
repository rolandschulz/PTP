package org.eclipse.rephraserengine.core.vpg.refactoring;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.rephraserengine.core.refactorings.IResourceRefactoring;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPGLog;

public abstract class VPGResourceRefactoring<A, T, V extends EclipseVPG<A, T, ? extends TokenRef<T>, ? extends VPGDB<A, T, ?, ?>, ? extends EclipseVPGLog<T, ?>>>
    extends VPGRefactoring<A, T, V>
    implements IResourceRefactoring
{
    protected List<IFile> selectedFiles = null;

    public void initialize(List<IFile> files)
    {
        if (files == null) throw new IllegalArgumentException("files argument cannot be null");
        if (files.isEmpty()) throw new IllegalArgumentException("files argument cannot be empty");
        
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
