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
package org.eclipse.rephraserengine.core.vpg.eclipse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPG;
import org.eclipse.rephraserengine.core.vpg.VPGDB;


/**
 * A virtual program graph for use in an Eclipse environment.
 * <a href="../../../../overview-summary.html#Eclipse">More Information</a>
 * <p>
 * This class is intended to be subclassed directly.
 *
 * @author Jeff Overbey
 * 
 * @since 1.0
 */
public abstract class EclipseVPG<A, T, R extends TokenRef<T>, D extends VPGDB<A, T, R, L>, L extends EclipseVPGLog<T, R>>
              extends VPG<A, T, R, D, L>
{
    private String syncMessage;

    public EclipseVPG(L log, D database, String syncMessage, int transientASTCacheSize)
    {
        super(log, database, transientASTCacheSize);
        this.syncMessage = syncMessage;
    }

    public EclipseVPG(L log, D database, String syncMessage)
    {
        super(log, database);
        this.syncMessage = syncMessage;
    }

    /** Enqueues a job to make sure the VPG is up-to-date with the
     *  workspace, and instructs it to automatically update itself
     *  when files are added, changed, or deleted in the workspace.
     */
    public void start()
    {
        log.readLogFromFile();
        
        // Now listen for changes to workspace resources
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new VPGResourceChangeListener(), IResourceChangeEvent.POST_CHANGE);

        // The C/C++ Development Tool (see org.eclipse.cdt.internal.core.model.CModelManager) handles
        // IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_CLOSE

        // and also executes
        // Platform.getContentTypeManager().addContentTypeChangeListener(this);

        queueJobToEnsureVPGIsUpToDate();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Resource Visitor
    ///////////////////////////////////////////////////////////////////////////

    public WorkspaceJob queueJobToEnsureVPGIsUpToDate()
    {
        WorkspaceJob job = new VPGInitialWorkspaceSyncJob(syncMessage);
        job.setRule(MultiRule.combine(VPGSchedulingRule.getInstance(),
                    ResourcesPlugin.getWorkspace().getRoot()));
        job.schedule();
        return job;
    }

    private final class VPGInitialWorkspaceSyncJob extends VPGJob<A, T>
    {
        private VPGInitialWorkspaceSyncJob(String name)
        {
            super(name);
        }

        @Override public IStatus runInWorkspace(final IProgressMonitor monitor)
        {
            try
            {
                monitor.beginTask(Messages.EclipseVPG_Indexing, IProgressMonitor.UNKNOWN);
                return ensureVPGIsUpToDate(monitor);
            }
            finally
            {
                monitor.done();
            }
        }
    }

    /** Ensures that the VPG database contains up-to-date information for all of
     *  the resources in the workspace.
     */
    public IStatus ensureVPGIsUpToDate(IProgressMonitor monitor)
    {
        try
        {
            WorkspaceSyncResourceVisitor visitor = new WorkspaceSyncResourceVisitor();
            collectFilesToIndex(visitor, monitor);
            visitor.calculateDependencies(monitor);
            visitor.index(monitor);
            flushDatabaseNoFail();
            return Status.OK_STATUS;
        }
        catch (CoreException e)
        {
            return e.getStatus();
        }
    }
    
    private void flushDatabaseNoFail()
    {
        try
        {
            db.flush();
        }
        catch (Throwable e)
        {
            // Ignore errors
        }
    }

    private void collectFilesToIndex(WorkspaceSyncResourceVisitor visitor, IProgressMonitor monitor) throws CoreException
    {
        monitor.subTask(Messages.EclipseVPG_SearchingForWorkspaceModifications);
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        workspaceRoot.accept(visitor); // Collect list of files to index
    }

    private final class WorkspaceSyncResourceVisitor implements IResourceVisitor
    {
        private final ArrayList<String> files;

        private WorkspaceSyncResourceVisitor()
        {
            this.files = new ArrayList<String>(1024);
        }

        public boolean visit(IResource resource)
        {
            try
            {
                if (!resource.isAccessible()) return false;

                if (resource instanceof IProject && !shouldProcessProject((IProject)resource)) return false;

                if (resource instanceof IFile && shouldProcessFile((IFile)resource))
                    files.add(EclipseVPG.getFilenameForIFile((IFile)resource));
            }
            catch (Exception e)
            {
                log.logError(e);
            }
            return !(resource instanceof IFile);
        }

        public void calculateDependencies(IProgressMonitor monitor)
        {
            ArrayList<String> queue = files;
            int completed = 0, total = queue.size();
            for (String filename : queue)
            {
                if (monitor.isCanceled()) throw new OperationCanceledException();

                monitor.subTask(
                    filename + " " + //$NON-NLS-1$
                    Messages.bind(Messages.EclipseVPG_CalculatingDependencies,
                                  ++completed,
                                  total));
                calculateDependenciesIfNotUpToDate(filename);
            }
        }

        public void index(IProgressMonitor monitor)
        {
            List<String> queue = sortFilesAccordingToDependencies(files, monitor);

            int completed = 0, total = countFilesInQueue(queue);
            for (String filename : queue)
            {
                if (monitor.isCanceled()) throw new OperationCanceledException();

                if (shouldListFileInIndexerProgressMessages(filename))
                    monitor.subTask(filename + " " + //$NON-NLS-1$
                        Messages.bind(Messages.EclipseVPG_XofY,
                                      ++completed,
                                      total));

                indexIfNotUpToDate(filename);
            }
        }
    }

    private int countFilesInQueue(List<String> queue)
    {
        int total = 0;
        for (String filename : queue)
            if (shouldListFileInIndexerProgressMessages(filename))
                total++;
        return total;
    }

    protected boolean shouldListFileInIndexerProgressMessages(String filename) {
        return !isVirtualFile(filename);
    }

    //public ArrayList<String> sortFilesAccordingToDependencies(final ArrayList<String> files, final IProgressMonitor monitor)

    ///////////////////////////////////////////////////////////////////////////
    // Resource Change Listener
    ///////////////////////////////////////////////////////////////////////////

    private final class VPGResourceChangeListener implements IResourceChangeListener
    {
        public void resourceChanged(IResourceChangeEvent event)
        {
            if (!(event.getSource() instanceof IWorkspace)) return;

            if (event.getType() != IResourceChangeEvent.POST_CHANGE) return;

            final IResourceDelta delta = event.getDelta();
            if (delta == null) return;

            new VPGResourceDeltaJob(syncMessage, delta).schedule();
        }
    }

    private final class VPGResourceDeltaJob extends VPGJob<A, T>
    {
        private final IResourceDelta delta;

        private VPGResourceDeltaJob(String name, IResourceDelta delta)
        {
            super(name);
            this.delta = delta;
        }

        @Override public IStatus runInWorkspace(final IProgressMonitor monitor)
        {
            try
            {
                monitor.beginTask(Messages.EclipseVPG_Indexing, IProgressMonitor.UNKNOWN);
                // Re-index or delete entries for files when they are added/changed or deleted, respectively
                VPGResourceDeltaVisitor visitor = new VPGResourceDeltaVisitor();
                monitor.subTask(Messages.EclipseVPG_SearchingForWorkspaceModifications);
                delta.accept(visitor); // Collect files to index
                visitor.calculateDependencies(monitor);
                visitor.index(monitor);
                flushDatabaseNoFail();
                return Status.OK_STATUS;
            }
            catch (CoreException e)
            {
                return e.getStatus();
            }
            finally
            {
                monitor.done();
            }
        }
    }

    private final class VPGResourceDeltaVisitor implements IResourceDeltaVisitor
    {
        private ArrayList<String> files;
        private HashMap<String, Boolean> forceReindex;

        public VPGResourceDeltaVisitor()
        {
            this.files = new ArrayList<String>(256);
            this.forceReindex = new HashMap<String, Boolean>(256);
        }

        public boolean visit(IResourceDelta delta)
        {
            try
            {
                IResource resource = delta.getResource();
                if (!(resource instanceof IFile))
                {
                    if (resource instanceof IProject)
                        return shouldProcessProject((IProject)resource);
                    else
                        return true;
                }
                IFile file = (IFile)resource;
                if (!shouldProcessFile(file)) return true;

                String filename = getFilenameForIFile(file);

                switch (delta.getKind())
                {
                case IResourceDelta.ADDED:
                    debug("Resource Delta: Add", filename); //$NON-NLS-1$
                    files.add(filename);
                    forceReindex.put(filename, true); // Was false
                    break;

                case IResourceDelta.CHANGED:
                    debug("Resource Delta: Change", filename); //$NON-NLS-1$
                    if ((delta.getFlags() & (IResourceDelta.CONTENT|IResourceDelta.REPLACED)) != 0)
                    {
                        files.add(filename);
                        forceReindex.put(filename, true);
                    }
                    break;

                case IResourceDelta.REMOVED:
                    debug("Resource Delta: Remove", filename); //$NON-NLS-1$
                    log.clearEntriesFor(filename);
                    db.deleteAllEntriesFor(filename);
                    break;
                }
            }
            catch (Exception e)
            {
                log.logError(e);
            }
            return true;
        }

        public void calculateDependencies(IProgressMonitor monitor)
        {
            ArrayList<String> queue = files;
            int completed = 0, total = queue.size();
            for (String filename : queue)
            {
                if (monitor.isCanceled()) throw new OperationCanceledException();

                monitor.subTask(filename + " " + //$NON-NLS-1$
                    Messages.bind(Messages.EclipseVPG_XofY,
                        ++completed,
                        total));
                Boolean force = forceReindex.get(filename);
                if (force == null || force)
                    forceRecomputationOfDependencies(filename);
                else
                    calculateDependenciesIfNotUpToDate(filename);
            }
        }

        public void index(IProgressMonitor monitor)
        {
            List<String> queue = sortFilesAccordingToDependencies(files, monitor);
            int completed = 0, total = countFilesInQueue(queue);
            for (String filename : queue)
            {
                if (monitor.isCanceled()) throw new OperationCanceledException();

                if (shouldListFileInIndexerProgressMessages(filename))
                    monitor.subTask(filename + " " + //$NON-NLS-1$
                        Messages.bind(Messages.EclipseVPG_XofY,
                            ++completed,
                            total));

                Boolean force = forceReindex.get(filename);
                if (force == null || force)
                    forceRecomputationOfEdgesAndAnnotations(filename);
                else
                    indexIfNotUpToDate(filename);
            }
        }
    }

    /** Updates the VPG database's dependency information for the given file
     *  if the stored information is out of date
     * @param monitor */
    protected void calculateDependenciesIfNotUpToDate(String filename)
    {
        if (db.isOutOfDate(filename))
        {
            debug(Messages.EclipseVPG_Indexing, filename);
            forceRecomputationOfDependencies(filename);
        }
        else
        {
            debug(Messages.EclipseVPG_IndexIsUpToDate, filename);
        }
    }

    /** Updates the VPG database information for the given file if the stored
     *  information is out of date
     * @param monitor */
    protected void indexIfNotUpToDate(String filename)
    {
        if (db.isOutOfDate(filename))
        {
            debug(Messages.EclipseVPG_Indexing, filename);
            forceRecomputationOfEdgesAndAnnotations(filename);
        }
        else
        {
            debug(Messages.EclipseVPG_IndexIsUpToDate, filename);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Abstract Methods (Resource Filtering)
    ///////////////////////////////////////////////////////////////////////////

    /** @return true iff the given file should be parsed */
    @Override protected boolean shouldProcessFile(String filename)
    {
        IFile file = getIFileForFilename(filename);
        return file == null ? false : shouldProcessFile(file);
    }

    /** @return true if the given project should be indexed */
    protected abstract boolean shouldProcessProject(IProject project);

    /** @return true iff the given file should be indexed */
    protected abstract boolean shouldProcessFile(IFile file);

    ///////////////////////////////////////////////////////////////////////////
    // Utility Methods (IFile<->Filename Mapping)
    ///////////////////////////////////////////////////////////////////////////

    public static IFile getIFileForFilename(String filename)
    {
        IResource resource = getIResourceForFilename(filename);
        if (resource instanceof IFile)
            return (IFile)resource;
        else
            return null;
    }

    public static String getFilenameForIFile(IFile file)
    {
        return getFilenameForIResource(file);
    }

    public static IResource getIResourceForFilename(String filename)
    {
        return ResourcesPlugin.getWorkspace().getRoot().findMember(filename);
    }

    public static String getFilenameForIResource(IResource resource)
    {
        if (resource == null)
            return null;
        else
            return resource.getFullPath().toString();
    }

    ///////////////////////////////////////////////////////////////////////////
    // IFile-Based AST Acquisition & Release API
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @since 2.0
     */
    public A acquireTransientAST(IFile file)
    {
        return file == null ? null : acquireTransientAST(getFilenameForIFile(file));
    }

    /**
     * @since 2.0
     */
    public A acquirePermanentAST(IFile file)
    {
        return file == null ? null : acquirePermanentAST(getFilenameForIFile(file));
    }
    
    /**
     * @since 2.0
     */
    public A makeTransientASTPermanent(IFile file)
    {
        return file == null ? null : makeTransientASTPermanent(getFilenameForIFile(file));
    }

    /**
     * @since 2.0
     */
    public A makeTransientASTPermanent(IFile file, A ast)
    {
        return file == null ? null : makeTransientASTPermanent(getFilenameForIFile(file), ast);
    }

    /**
     * @since 2.0
     */
    public void releaseAST(IFile file)
    {
        if (file != null) releaseAST(getFilenameForIFile(file));
    }
    
    /**
     * @since 2.0
     */
    public IFile getIFileCorrespondingTo(A ast)
    {
        String filename = getFilenameCorrespondingTo(ast);
        return filename == null ? null : getIFileForFilename(filename);
    }

    ///////////////////////////////////////////////////////////////////////////
    // VPG Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override protected void processingDependent(String filename, String dependentFilename)
    {
//        if (progressMonitor != null)
//            progressMonitor.subTask(filename
//                                    + " - reindexing dependent file "
//                                    + dependentFilename);
        super.processingDependent(filename, dependentFilename);
    }

    /** Forces the database to be updated based on the current in-memory AST for the given file */
    public void commitChangesFromInMemoryASTs(IProgressMonitor pm, int ticks, IFile file)
    {
        commitChangesFromInMemoryASTs(pm, ticks, getFilenameForIFile(file));
    }

    /**
     * Forces the database to be updated based on the current in-memory ASTs for the given files
     * @since 2.0
     */
    public void commitChangesFromInMemoryASTs(IProgressMonitor pm, int ticks, Set<IFile> files)
    {
        String[] filenames = new String[files.size()];
        int i = 0;
        for (IFile file : files)
            filenames[i++] = getFilenameForIFile(file);
        
        commitChangesFromInMemoryASTs(pm, ticks, filenames);
    }
}