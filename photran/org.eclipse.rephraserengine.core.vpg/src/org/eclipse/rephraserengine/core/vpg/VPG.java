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
package org.eclipse.rephraserengine.core.vpg;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.rephraserengine.core.util.Pair;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

/**
 * Base class for a Virtual Program Graph. <a href="../../../overview-summary.html#VPG">More
 * Information</a>
 * <p>
 * This class may be subclassed directly, although in an Eclipse environment, clients will usually
 * subclass {@link EclipseVPG} instead.
 * <p>
 * <i>Requirements/assumptions:</i>
 * <ul>
 * <li>Transient ASTs <b>require</b> the AST to have bi-directional pointers. Otherwise, the portion
 * of the tree above the acquired token can be garbage collected!
 * </ul>
 * 
 * @author Jeff Overbey
 * 
 * @param <A> AST type
 * @param <T> token type
 * @param <R> {@link IVPGNode}/{@link NodeRef} type
 * 
 * @since 1.0
 */
public abstract class VPG<A, T, R extends IVPGNode<T>>
{
    /** The VPG component factory, which creates the database, log, etc. for this VPG. */
    private final IVPGComponentFactory<A, T, R> factory;
    
    /** The VPG writer, which populates the dependencies, edges, and annotations in this VPG. */
    private final VPGWriter<A, T, R> vpgWriter;
    
    /** The AST cache, which provides access to ASTs and determines which files' ASTs are in memory. */
    private final ASTRepository<A> astCache;
    
    /** The VPG database, which stores and persists the VPG's dependencies, edges, and annotations. */
    private final DemandDB<A, T, R> db;

	/** The VPG error/warning log. */
	private final VPGLog<T, R> log;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////

	/** @since 3.0 */
    protected VPG(IVPGComponentFactory<A, T, R> factory)
    {
        this(factory, 5);
    }

    /** @since 3.0 */
	protected VPG(IVPGComponentFactory<A, T, R> factory, int transientASTCacheSize)
	{
        assert transientASTCacheSize > 0;

        this.factory = factory;
        this.log = factory.createLog();
        this.db = new DemandDB<A,T,R>(factory.createDatabase(log));
        this.vpgWriter = factory.createVPGWriter(db, log);
        db.setContentProvider(vpgWriter);

        this.astCache = new ASTRepository<A>(transientASTCacheSize);
	}
    
    ////////////////////////////////////////////////////////////////////////////
    // ACCESSORS
    ////////////////////////////////////////////////////////////////////////////

    /** @since 3.0 */
    @SuppressWarnings("unchecked")
    public <W extends VPGWriter<A, T, R>> W getVPGWriter()
    {
        return (W)vpgWriter;
    }
    
    /** @since 3.0 */
    public VPGLog<T, R> getLog()
    {
        return log;
    }
    
    /*package*/ VPGDB<A, T, R> getDB()
    {
        return db;
    }

    ////////////////////////////////////////////////////////////////////////////
    // CALLBACK: AST CONSTRUCTION
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Parses the given file.
     * @param filename (non-null)
     * @return an AST for the given file, or <code>null</code> if an error was encountered
     */
    protected abstract A parse(String filename);

	////////////////////////////////////////////////////////////////////////////
	// API: AST ACQUISITION/RELEASE
	////////////////////////////////////////////////////////////////////////////

	/** @return an AST for the given file which will be garbage collected after
	 *  no pointers to any of its nodes remain.
	 */
	public final A acquireTransientAST(String filename)
	{
		return astCache.acquireTransientAST(filename, false, this);
	}

	/** @return an AST for the given file.  The AST will remain in memory until it is
	 *  explicitly released using {@link #releaseAST(String)} or {@link #releaseAllASTs()}.
	 */
	public final A acquirePermanentAST(String filename)
	{
	    return astCache.acquirePermanentAST(filename, this);
	}

    /**
     * Changes the AST for the given file from a transient AST to a permanent
     * AST.  The AST will remain in memory until it is explicitly released
     * using {@link #releaseAST(String)} or {@link #releaseAllASTs()}.
     * 
     * @since 2.0
     */
    public final A makeTransientASTPermanent(String filename)
    {
        return astCache.makeTransientASTPermanent(filename, this);
    }

	/** Changes the AST for the given file from a transient AST to a permanent
	 *  AST.  The AST will remain in memory until it is explicitly released
	 *  using {@link #releaseAST(String)} or {@link #releaseAllASTs()}.
	 */
	public final A makeTransientASTPermanent(String filename, A ast)
	{
        return astCache.makeTransientASTPermanent(filename, ast);
	}

	/** Releases the AST for the given file, regardless of whether it was
	 *  acquired as a permanent or transient AST. */
	public final void releaseAST(String filename)
	{
	    astCache.releaseAST(filename);
	}

    /**
     * Releases all ASTs, regardless of whether they were acquired as
     * transient and permanent ASTs.
     *
     * @see #acquireTransientAST(String)
     * @see #acquirePermanentAST(String)
     * @see #makeTransientASTPermanent(String)
     */
	public final void releaseAllASTs()
	{
		astCache.releaseAllASTs();
	}

	/**
	 * If the given AST was acquired using {@link #acquireTransientAST(String)} or
	 * {@link #acquirePermanentAST(String)}, returns the filename to which it
	 * corresponds.  If it is not the root of an AST acquired from this VPG, returns
	 * <code>null</code>.
	 * <p>
	 * Note that this is not an O(1) operation: Internally, the filename is found
	 * by comparing the given argument with every AST in memory.
	 * 
	 * @return filename or <code>null</code>
	 * 
	 * @since 2.0
	 */
	public final String getFilenameCorrespondingTo(A ast)
	{
        return astCache.getFilenameCorrespondingTo(ast);
	}
    
    ////////////////////////////////////////////////////////////////////////////
    // API: VPG NODE ACCESS
    ////////////////////////////////////////////////////////////////////////////

    /**
     * @return a TokenRef referring to the token with the given position in the given file.
     * 
     * @since 3.0
     */
    public final R getVPGNode(String filename, int offset, int length)
    {
        return factory.getVPGNode(filename, offset, length);
    }

	////////////////////////////////////////////////////////////////////////////
	// API: DEPENDENCIES
	////////////////////////////////////////////////////////////////////////////

    /**
     * @return the name of every file on which at least one other file is dependent.
     * @since 3.0
     */
    public Iterable<String> listAllFilenamesWithDependents()
    {
        return db.listAllFilenamesWithDependents();
    }

    /**
     * @return the name of every file which depends on at least one other file.
     * @since 3.0
     */
    public Iterable<String> listAllDependentFilenames()
    {
        return db.listAllDependentFilenames();
    }

    /**
     * @return all of the files on which the given file depends
     * @since 3.0
     */
    public Iterable<String> getOutgoingDependenciesFrom(String filename)
    {
        return db.getOutgoingDependenciesFrom(filename);
    }

    /**
     * @return all of the files dependent on the given file
     * @since 3.0
     */
    public Iterable<String> getIncomingDependenciesTo(String filename)
    {
        return db.getIncomingDependenciesTo(filename);
    }
    
    /** @since 3.0 */
    public List<String> sortFilesAccordingToDependencies(List<String> files)
    {
        return db.sortFilesAccordingToDependencies(files);
    }

//  public boolean checkForCircularDependencies(String filename)
//  {
//      throw new UnsupportedOperationException();
//  }

    ////////////////////////////////////////////////////////////////////////////
    // API: EDGES
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a list of all of the edges with at least one endpoint in the given file.
     * <p>
     * Due to implementation details, some edges may be listed more than once.
     * 
     * @since 3.0
     */
    public Iterable<? extends VPGEdge<A, T, R>> getAllEdgesFor(String filename)
    {
        return db.getAllEdgesFor(filename);
    }

    ////////////////////////////////////////////////////////////////////////////
    // API: ANNOTATIONS
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a list of all of the annotations in the given file.
     * <p>
     * The first entry of each pair is a {@link IVPGNode}, and the second is an annotation type.
     * The annotation can be retrieved using {@link VPGDB#getAnnotation(VPGNode, int)}.
     * <p>
     * Due to implementation details, some annotations may be listed more than once.
     * 
     * @since 3.0
     */
    public Iterable<Pair<R, Integer>> getAllAnnotationsFor(String filename)
    {
        return db.getAllAnnotationsFor(filename);
    }
    
	////////////////////////////////////////////////////////////////////////////
	// PARSER/AST METHODS
	////////////////////////////////////////////////////////////////////////////

    /** Forces the database to be updated based on the current in-memory AST for the given file. */
    public void commitChangesFromInMemoryASTs(IProgressMonitor pm, int ticks, String... filenames)
    {
        List<String> files = new ArrayList<String>(Arrays.asList(filenames));
        files = sortFilesAccordingToDependencies(files); //, pm);

        pm = new SubProgressMonitor(pm, ticks, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask(Messages.VPG_PostTransformAnalysis, files.size());
        for (String thisFile : files)
        {
            if (!isVirtualFile(thisFile))
            {
                pm.subTask(lastSegmentOfFilename(thisFile));
                vpgWriter.computeEdgesAndAnnotationsFromModifiedAST(thisFile, acquireTransientAST(thisFile));
            }
            pm.worked(1);
        }
        pm.done();
    }

    public static String lastSegmentOfFilename(String filename)
    {
        if (filename == null) return ""; //$NON-NLS-1$

        int lastSlash = filename.lastIndexOf('/');
        int lastBackslash = filename.lastIndexOf('\\');
        if (lastSlash < 0 && lastBackslash < 0)
            return filename;
        else
            return filename.substring(Math.max(lastSlash + 1, lastBackslash + 1));
    }

    /**
     * Returns the source code for the given AST (which may have been modified).
     * <p>
     * In preservation-based refactorings, if a transformation will cause an error
     * in the modified source code, this is used in the refactoring wizard to display
     * the modified source code and highlight the erroroneous region when the error
     * list is displayed.  If preservation-based refactorings are not being implemented,
     * usually it is not necessary to override this method.
     * 
     * @return source code for the given AST (which may have been modified), or <code>null</code>
     *         if this capability is not supported.
     */
    public String getSourceCodeFromAST(A ast)
    {
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Files & Resource Filtering
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @return all filenames present in the VPG database.
     * @since 3.0
     */
    public Iterable<String> listAllFilenames()
    {
        return db.listAllFilenames();
    }

    /** @since 3.0 */
    public boolean isOutOfDate(String filename)
    {
        return db.isOutOfDate(filename);
    }

    /**
     * Returns <code>true</code> iff the given filename refers to a virtual file, i.e., a symbolic
     * name that does not represent an actual file on disk.
     * @param filename (non-null)
     * @return <code>true</code> iff the given filename refers to a virtual file
     */
    public boolean isVirtualFile(String filename)
    {
        return false;
    }

    /**
     * @return true iff the given file should be parsed
     * @since 3.0
     */
    public boolean shouldProcessFile(String filename)
    {
        return !isVirtualFile(filename);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // HYPOTHETICAL UPDATING
    ////////////////////////////////////////////////////////////////////////////
    
    /** @since 3.0 */
    public void enterHypotheticalMode() throws IOException
    {
        db.enterHypotheticalMode();
    }
    
    /** @since 3.0 */
    public void leaveHypotheticalMode() throws IOException
    {
        db.leaveHypotheticalMode();
    }
    
    /** @since 3.0 */
    public boolean isInHypotheticalMode()
    {
        return db.isInHypotheticalMode();
    }

    ////////////////////////////////////////////////////////////////////////////
    // API: INDEXING
    ////////////////////////////////////////////////////////////////////////////
    
    /** Recomputes the edges and annotations for the given file, regardless
     *  of whether or not the VPG database entries for that file are
     *  out of date.
     */
    public final void forceRecomputationOfDependencies(String filename)
    {
        vpgWriter.computeDependencies(filename);
    }

    /** Recomputes the edges and annotations for the given file, regardless
     *  of whether or not the VPG database entries for that file are
     *  out of date.
     */
    public final void forceRecomputationOfEdgesAndAnnotations(String filename)
    {
        releaseAST(filename);
        astCache.acquireTransientAST(filename, true, this);
    }

    /**
     * Callback method invoked by {@link EclipseVPG} when it detects that a file has been deleted
     * from the filesystem.
     * <p>
     * Typically, implementors should respond by deleting all information (dependencies, edges, and
     * annotations) for the file, since it no longer exists.
     * 
     * @param filename path to the deleted file
     * 
     * @since 3.0
     */
    public void deleteAllEntriesFor(String filename)
    {
        log.clearEntriesFor(filename);
        db.deleteAllEntriesFor(filename);
    }

    ////////////////////////////////////////////////////////////////////////////
    // UTILITY METHODS - DATABASE
    ////////////////////////////////////////////////////////////////////////////
    
    /** @since 3.0 */
    public void resetDatabaseStatistics()
    {
        db.resetStatistics();
    }
    
    /** @since 3.0 */
    public void printDatabaseStatisticsOn(PrintStream out)
    {
        db.printStatisticsOn(out);
    }
    
    /** @since 3.0 */
    public void printDatabaseOn(PrintStream out)
    {
        db.printOn(out);
    }
    
    /** @since 3.0 */
    public void clearDatabase()
    {
        db.clearDatabase();
    }
    
    /**
     * Forces any in-memory data to be flushed to disk
     * @since 3.0
     */
    public void flushDatabase()
    {
        db.flush();
    }

    /** Called when the database is no longer needed.  Typically ensures that
     * any data in memory is flushed to disk and any locks are released.
     */
    public void close()
    {
        db.close();
    }

    ////////////////////////////////////////////////////////////////////////////
    // UTILITY METHODS - DESCRIPTION/DEBUGGING
    ////////////////////////////////////////////////////////////////////////////

    public String describeEdgeType(int edgeType)
    {
        return Messages.bind(Messages.VPG_EdgeOfType, edgeType);
    }

    public String describeAnnotationType(int annotationType)
    {
        return Messages.bind(Messages.VPG_AnnotationOfType, annotationType);
    }

    /**
     * 
     * @param message
     * @param filename (possibly <code>null</code>
     * 
     * @since 3.0
     */
    public void debug(String message, String filename)
    {
    }
}
