package org.eclipse.rephraserengine.core.vpg.refactoring;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.rephraserengine.core.refactorings.IRefactoring;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.VPGLog;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPGLog;
import org.eclipse.text.edits.ReplaceEdit;

public abstract class VPGRefactoring<A, T, V extends EclipseVPG<A, T, ? extends TokenRef<T>, ? extends VPGDB<A, T, ?, ?>, ? extends EclipseVPGLog<T, ?>>>
    extends Refactoring
    implements IRefactoring
{
    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    /** The OS-dependent end-of-line sequence (\n or \r\n) */
    protected static final String EOL = System.getProperty("line.separator");

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    protected V vpg;
    protected CompositeChange allChanges = null;

    ///////////////////////////////////////////////////////////////////////////
    // LTK Refactoring Implementation
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    public final RefactoringStatus checkInitialConditions(IProgressMonitor pm)
    {
        this.vpg = getVPG();
        
        RefactoringStatus status = new RefactoringStatus();

        pm.beginTask("Ensuring index is up-to-date", IProgressMonitor.UNKNOWN);
        vpg.ensureVPGIsUpToDate(pm);
        pm.done();

        try
        {
            checkFiles(status);
            if (!status.hasFatalError())
            {
                preCheckInitialConditions(status, new ForwardingProgressMonitor(pm));
                doCheckInitialConditions(status, new ForwardingProgressMonitor(pm));
            }
        }
        catch (PreconditionFailure f)
        {
            status.addFatalError(f.getMessage());
        }

        return status;
    }

    protected abstract V getVPG();

    protected abstract void checkFiles(RefactoringStatus status) throws PreconditionFailure;

    protected void preCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
    }

    protected abstract void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure;

    protected void logVPGErrors(RefactoringStatus status)
    {
        for (VPGLog<T, ? extends TokenRef<T>>.Entry entry : vpg.log.getEntries())
        {
            if (entry.isWarning())
                status.addWarning(entry.getMessage(), createContext(entry.getTokenRef()));
            else
                status.addError(entry.getMessage(), createContext(entry.getTokenRef()));
        }
    }

    @Override
    public final RefactoringStatus checkFinalConditions(IProgressMonitor pm)
    {
        allChanges = new CompositeChange(getName());

        RefactoringStatus status = new RefactoringStatus();
        //pm.beginTask("Checking final preconditions; please wait...", IProgressMonitor.UNKNOWN);
        try
        {
            preCheckFinalConditions(status, new ForwardingProgressMonitor(pm));
            doCheckFinalConditions(status, new ForwardingProgressMonitor(pm));
        }
        catch (PreconditionFailure f)
        {
            status.addFatalError(f.getMessage());
        }
        //pm.done();
        return status;
    }

    protected void preCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
    }

    protected abstract void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure;

    /**
     * To get text to display in the GUI, the precondition checking methods
     * must call {@link IProgressMonitor#setTaskName(String)} rather than
     * {@link IProgressMonitor#subTask(String)}.  However, the change creation
     * method <i>can</i> call {@link IProgressMonitor#subTask(String)}.  This
     * &quot;forwards&quot; calls to {@link #subTask(String)} to
     * {@link #setTaskName(String)}.
     *
     * @author Jeff Overbey
     */
    protected static class ForwardingProgressMonitor implements IProgressMonitor
    {
        private IProgressMonitor pm;
        private String prefix = "";

        public ForwardingProgressMonitor(IProgressMonitor pm)
        {
            this.pm = pm;
        }

        public void beginTask(String name, int totalWork) { pm.beginTask(name, totalWork); pm.setTaskName(name); prefix = name + " - "; }
        public void done() { prefix = ""; pm.setTaskName(""); pm.done(); }
        public void internalWorked(double work) { pm.internalWorked(work); }
        public boolean isCanceled() { return pm.isCanceled(); }
        public void setCanceled(boolean value) { pm.setCanceled(value); }
        public void setTaskName(String name) { pm.setTaskName(prefix + name); }
        public void worked(int work) { pm.worked(work); }

        public void subTask(String name)
        {
            pm.setTaskName(prefix + name);
        }
    }

    @Override
    public final Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        assert pm != null;

        //pm.beginTask("Constructing workspace transformation; please wait...", IProgressMonitor.UNKNOWN);
        // allChanges constructed above in #checkFinalConditions
        doCreateChange(pm);
        //pm.done();
        return allChanges;
    }

    protected abstract void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;

    ///////////////////////////////////////////////////////////////////////////
    // Utilities for Subclasses
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A <code>PreconditionFailure</code> is thrown (e.g., by {@link AbstractFortranRefactoring#fail(String)})
     * to indicate an error severe enough that the refactoring cannot be completed.
     */
    protected static class PreconditionFailure extends Exception
    {
        private static final long serialVersionUID = 1L;

        public PreconditionFailure(String message)
        {
            super(message);
        }
    }

    /**
     * Throws a <code>PreconditionFailure</code>, indicating an error severe enough
     * that the refactoring cannot be completed.
     *
     * @param message an error message to display to the user
     */
    protected void fail(String message) throws PreconditionFailure
    {
        throw new PreconditionFailure(message);
    }

    // REFACTORING STATUS /////////////////////////////////////////////////////

    protected RefactoringStatusContext createContext(TokenRef<T> tokenRef)
    {
        if (tokenRef == null) return null;

        IFile file = EclipseVPG.getIFileForFilename(tokenRef.getFilename());
        if (file == null) return null;

        return new FileStatusContext(file,
                                     new Region(tokenRef.getOffset(), tokenRef.getLength()));
    }

    // CHANGE CREATION ////////////////////////////////////////////////////////

    /**
     * This method should be called from within the <code>doCreateChange</code> method after all
     * of the changes to a file's AST have been made.
     * <p>
     * If calling <code>#toString</code> on the AST does not reproduce the modified
     * source code for the given file, this method should be overridden.
     */
    protected void addChangeFromModifiedAST(IFile file, IProgressMonitor pm) {
        try {
            A ast = vpg.acquireTransientAST(file);
            TextFileChange changeThisFile = new TextFileChange(getName() + " - "
                    + file.getFullPath().toOSString(), file);
            changeThisFile.initializeValidationData(pm);
            changeThisFile.setEdit(new ReplaceEdit(0, getSizeOf(file), getSourceCodeFromAST(ast)));
            allChanges.add(changeThisFile);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    protected String getSourceCodeFromAST(A ast)
    {
        return ast.toString();
    }

    private int getSizeOf(IFile file) throws CoreException, IOException {
        int size = 0;
        InputStream in = file.getContents(true);
        while (in.read() > -1)
            size++;
        in.close();
        return size;
    }

    // PRECONDITIONS //////////////////////////////////////////////////////////

    protected void checkIfFileIsAccessibleAndWritable(IFile file) throws PreconditionFailure
    {
        if (!file.isAccessible())
            fail("The file in the editor (" + file.getName() + ") is not accessible.");

        if (file.isReadOnly())
            fail("The file in the editor (" + file.getName() + ") is read-only.");
    }
}
