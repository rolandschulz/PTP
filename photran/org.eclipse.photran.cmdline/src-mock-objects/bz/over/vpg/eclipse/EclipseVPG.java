package bz.over.vpg.eclipse;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

import bz.over.vpg.TokenRef;
import bz.over.vpg.VPG;
import bz.over.vpg.VPGDB;

public abstract class EclipseVPG<A, T, R extends TokenRef<T>, D extends VPGDB<A, T, R, L>, L extends EclipseVPGLog<T, R>>
			  extends VPG<A, T, R, D, L>
{
    @SuppressWarnings("unused")
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
	    ensureVPGIsUpToDate(new NullProgressMonitor()
	    {
            public void beginTask(String string, int unknown2)
            {
                System.err.println("- " + string);
            }

            public void subTask(String string)
            {
                System.err.println("  - " + string);
            }
	    });
	}
    
    ///////////////////////////////////////////////////////////////////////////
    // Resource Visitor
    ///////////////////////////////////////////////////////////////////////////

    /** Ensures that the VPG database contains up-to-date information for all of
     *  the resources in the workspace.
     */
    public IStatus ensureVPGIsUpToDate(IProgressMonitor monitor)
    {
        try
        {
            IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
            WorkspaceSyncResourceVisitor visitor = new WorkspaceSyncResourceVisitor();
            monitor.subTask("Searching for workspace modifications...");
            workspaceRoot.accept(visitor); // Collect list of files to index
            visitor.calculateDependencies(monitor);
            visitor.index(monitor);
            return Status.OK_STATUS;
        }
        catch (CoreException e)
        {
            return e.getStatus();
        }
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
                
                monitor.subTask(filename + " (calculating dependencies - file " + (++completed) + " of " + total + ")");
                calculateDependenciesIfNotUpToDate(filename);
            }
        }
        
        public void index(IProgressMonitor monitor)
        {
            ArrayList<String> queue = sortFilesAccordingToDependencies(files);

            int completed = 0, total = countFilesInQueue(queue);
            for (String filename : queue)
            {
                if (monitor.isCanceled()) throw new OperationCanceledException();
                
                if (shouldListFileInIndexerProgressMessages(filename))
                    monitor.subTask(filename + " (" + (++completed) + " of " + total + ")");
                
                indexIfNotUpToDate(filename);
            }
        }
    }

    private int countFilesInQueue(ArrayList<String> queue)
    {
        int total = 0;
        for (String filename : queue)
            if (shouldListFileInIndexerProgressMessages(filename))
                total++;
        return total;
    }
    
    protected abstract boolean shouldListFileInIndexerProgressMessages(String filename);
    
    private ArrayList<String> sortFilesAccordingToDependencies(final ArrayList<String> files)
    {
        // Enqueue the reflexive transitive closure of the dependencies
        for (int i = 0; i < files.size(); i++)
            enqueueNewDependents(files.get(i), files);
        
        // Topological Sort -- from Cormen et al. pp. 550, 541
        class DFS
        {
            final Integer WHITE = 0, GRAY = 1, BLACK = 2;
            
            final int numFiles = files.size();
            ArrayList<String> result = new ArrayList<String>(numFiles);
            HashMap<String, Integer> color = new HashMap<String, Integer>();
            int time;
            
            DFS()
            {
                for (String filename : files)
                    color.put(filename, WHITE);
                
                time = 0;
                
                for (String filename : files)
                    if (color.get(filename) == WHITE)
                        dfsVisit(filename);
            }
            
            private void dfsVisit(String u)
            {
                color.put(u, GRAY);
                time++;
                
                for (String v : db.getIncomingDependenciesTo(u))
                    if (color.get(v) == WHITE)
                        dfsVisit(v);
                
                color.put(u, BLACK);
                result.add(0, u);
            }
        }
        
        return new DFS().result;
    }
    
    /** Updates the VPG database's dependency information for the given file
     *  if the stored information is out of date 
     * @param monitor */
    protected void calculateDependenciesIfNotUpToDate(String filename)
    {
        if (db.isOutOfDate(filename))
        {
            debug("Indexing", filename);
            forceRecomputationOfDependencies(filename);
        }
        else
        {
            debug("Index is up to date", filename);
        }
    }
    
    /** Updates the VPG database information for the given file if the stored
     *  information is out of date 
     * @param monitor */
    protected void indexIfNotUpToDate(String filename)
    {
        if (db.isOutOfDate(filename))
        {
            debug("Indexing", filename);
            forceRecomputationOfEdgesAndAnnotations(filename);
        }
        else
        {
            debug("Index is up to date", filename);
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
}
