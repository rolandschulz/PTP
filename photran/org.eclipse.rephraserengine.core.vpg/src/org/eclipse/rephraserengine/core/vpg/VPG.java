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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Base class for a Virtual Program Graph.  <a href="../../../overview-summary.html#VPG">More Information</a>
 * <p>
 * Usually, this class should not be subclassed directly; instead, subclass one of the <code>StandardVPG</code>
 * classes or the <code>EclipseVPG</code> class.
 * <p>
 * N.B. Transient ASTS <b>require</b> the AST to have bi-directional pointers.  Otherwise, the portion of the tree
 * above the acquired token can be garbage collected!
 * <p>
 * N.B. If a VPG inherits subclasses {@link VPGEdge} to create custom edge types, it <i>must</i>
 * override {@link StandardVPG#createEdge(TokenRef, TokenRef, int)}.  This method is called when edges are created
 * based on data in the database.  By default, its creates edges of type {@link VPGEdge}; subclasses must override
 * it to create edges of the proper subclass(es).  Otherwise, an edge may have a subclass type originally but later
 * have the {@link VPGEdge} type when it is reconstructed from the database.
 * <p>
 * Similarly, if a VPG subclasses {@link TokenRef}, it must override {@link #createEdge(TokenRef, TokenRef, int)}.
 *
 * @author Jeff Overbey
 *
 * @param <A> AST type
 * @param <T> token type
 * @param <R> TokenRef type
 * @param <D> database type
 * @param <L> error/warning log type
 */
public abstract class VPG<A, T, R extends TokenRef<T>, D extends VPGDB<A, T, R, L>, L extends VPGLog<T, R>>
{
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

	/** Cache of ASTs acquired using {@link #acquirePermanentAST(String)} or converted
	 *  using {@link #makeTransientASTPermanent(String)}. */
	protected HashMap<String, A> permanentASTs;

	/** Cache of ASTs acquired using {@link #acquireTransientAST(String)}. */
	protected HashMap<String, WeakReference<A>> transientASTs;

	/** Small queue of <i>recent</i> ASTs acquired using {@link #acquireTransientAST(String)}. */
	protected Object[] transientASTCache;
	private int transientASTCacheIndex = 0;

	/** The VPG database, which persists edges and annotations. */
	public final D db;

	/** The VPG error/warning log. */
	public final VPGLog<T, R> log;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////

    protected VPG(L log, D database)
    {
        this(log, database, 5);
    }

	protected VPG(L log, D database, int transientASTCacheSize)
	{
	    assert transientASTCacheSize > 0;

		this.transientASTs = new HashMap<String, WeakReference<A>>();
		this.permanentASTs = new HashMap<String, A>();
		this.transientASTCache = new Object[transientASTCacheSize];
		this.db = database;
		this.db.setVPG(this);
	    this.log = log;
		this.db.setVPG(this);
	}

	////////////////////////////////////////////////////////////////////////////
	// API: AST ACQUISITION/RELEASE
	////////////////////////////////////////////////////////////////////////////

	/** @return an AST for the given file which will be garbage collected after
	 *  no pointers to any of its nodes remain.
	 */
	public A acquireTransientAST(String filename)
	{
		return acquireTransientAST(filename, false);
	}

	private A acquireTransientAST(String filename, boolean forceRecomputationOfEdgesAndAnnotations)
	{
		if (isVirtualFile(filename) || !shouldProcessFile(filename)) return null;

		A ast = null;

		if (!forceRecomputationOfEdgesAndAnnotations)
		{
			if (permanentASTs.containsKey(filename))
				ast = permanentASTs.get(filename);
			else if (transientASTs.containsKey(filename))
				ast = transientASTs.get(filename).get();

			if (ast != null) return ast;
		}

		log.clearEntriesFor(filename);

		long start = System.currentTimeMillis();
		ast = parse(filename);
		long parseTime = System.currentTimeMillis() - start;

		if (ast != null)
		{
			WeakReference<A> astRef = new WeakReference<A>(ast);
			transientASTs.put(filename, astRef);
			//astFilenames.put(astRef, filename);

			transientASTCache[transientASTCacheIndex] = ast;
			transientASTCacheIndex = (transientASTCacheIndex+1) % transientASTCache.length;
		}

		long computeTime = -1L;
		if (forceRecomputationOfEdgesAndAnnotations || db.isOutOfDate(filename))
            computeTime = computeEdgesAndAnnotations(filename, ast);

		debug(parseTime, computeTime, filename);

		return ast;
	}

	/** @return an AST for the given file.  The AST will remain in memory until it is
	 *  explicitly released using {@link #releaseAST(String)} or {@link #releaseAllASTs()}.
	 */
	public A acquirePermanentAST(String filename)
	{
	    A ast = acquireTransientAST(filename);
		return makeTransientASTPermanent(filename, ast);
	}
//
//	private long computeEdgesAndAnnotations(String filename, A ast)
//	{
//		// We must store the old list of dependents since deleteAllEntriesFor()
//		// will delete all of the dependencies to this file
//		ArrayList<String> dependents = new ArrayList<String>();
//		dependents.add(filename);
//        enqueueNewDependents(filename, dependents);
//
//        long start = System.currentTimeMillis();
//		db.deleteAllEdgesAndAnnotationsFor(filename);
//		populateVPG(filename, ast);
//		db.updateModificationStamp(filename);
//		long computeTime = System.currentTimeMillis()-start;
//
//        // This was done above, populateVPG may have added new dependents
//        enqueueNewDependents(filename, dependents);
//
//        // Traverse the dependency tree in breadth-first order so each file
//        // will only need to be processed once (assuming there are no cycles)
//        for (int i = 1; i < dependents.size(); i++) // 1: Skip current file
//        {
//            String dependentFilename = dependents.get(i);
//            processingDependent(filename, dependentFilename);
//            // Call acquireTransientAST, forcing recomputation of edges and annotations and processing dependents
//            acquireTransientAST(dependentFilename, true);
//
//            enqueueNewDependents(dependentFilename, dependents);
////                for (String f : findAllFilesDependentUpon(dependentFilename))
////                    if (dependents.indexOf(f) < i && !f.equals(filename))
////                        dependents.add(f);
//        }
//
//		return computeTime;
//	}

    protected long computeEdgesAndAnnotations(String filename, A ast)
    {
        long start = System.currentTimeMillis();
        db.deleteAllEdgesAndAnnotationsFor(filename);
        populateVPG(filename, ast);
        db.updateModificationStamp(filename);
        db.flush();
        return System.currentTimeMillis()-start;
    }

    public List<String> sortFilesAccordingToDependencies(final List<String> files, final IProgressMonitor monitor)
    {
        // Enqueue the reflexive transitive closure of the dependencies
        for (int i = 0; i < files.size(); i++)
        {
            if (monitor.isCanceled()) throw new OperationCanceledException();
            monitor.subTask("Sorting files according to dependencies - enqueuing dependents (" + i + " of " + files.size() + ")");

            enqueueNewDependents(files.get(i), files);
        }

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
                if (monitor.isCanceled()) throw new OperationCanceledException();
                monitor.subTask("Sorting files according to dependencies - sorting dependents of " + u + " (" + time + " of " + files.size() + ")");

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

    protected void processingDependent(String filename, String dependentFilename)
    {
        debug("- Processing dependent file " + dependentFilename, filename);
    }

    protected void enqueueNewDependents(String filename,
                                        List<String> dependents)
    {
        for (String f : findAllFilesDependentUpon(filename))
            if (!dependents.contains(f))
                dependents.add(f);
    }

    /** Recomputes the edges and annotations for the given file, regardless
     *  of whether or not the VPG database entries for that file are
     *  out of date.
     */
    public void forceRecomputationOfDependencies(String filename)
    {
        calculateDependencies(filename);
    }

    /** Recomputes the edges and annotations for the given file, regardless
     *  of whether or not the VPG database entries for that file are
     *  out of date.
     */
    public void forceRecomputationOfEdgesAndAnnotations(String filename)
    {
    	releaseAST(filename);
        acquireTransientAST(filename, true);
    }

	private Set<String> findAllFilesDependentUpon(String filename)
	{
        Set<String> dependents = new HashSet<String>();

		for (String dependentFilename : db.getIncomingDependenciesTo(filename))
			dependents.add(dependentFilename);

		return dependents;
	}

    /** Changes the AST for the given file from a transient AST to a permanent
     *  AST.  The AST will remain in memory until it is explicitly released
     *  using {@link #releaseAST(String)} or {@link #releaseAllASTs()}.
     */
    public A makeTransientASTPermanent(String filename)
    {
        return makeTransientASTPermanent(filename, acquireTransientAST(filename));
    }

	/** Changes the AST for the given file from a transient AST to a permanent
	 *  AST.  The AST will remain in memory until it is explicitly released
	 *  using {@link #releaseAST(String)} or {@link #releaseAllASTs()}.
	 */
	public A makeTransientASTPermanent(String filename, A ast)
	{
        transientASTs.remove(filename);
		permanentASTs.put(filename, ast);
		return ast;
	}

	/** Releases the AST for the given file, regardless of whether it was
	 *  acquired as a permanent or transient AST. */
	public void releaseAST(String filename)
	{
		transientASTs.remove(filename);
		permanentASTs.remove(filename);
	}

    /**
     * Releases all ASTs, regardless of whether they were acquired as
     * transient and permanent ASTs.
     *
     * @see #acquireTransientAST(String)
     * @see #acquirePermanentAST(String)
     * @see #makeTransientASTPermanent(String)
     */
	public void releaseAllASTs()
	{
		transientASTs.clear();
		permanentASTs.clear();
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
	 */
	public String getFilenameCorrespondingTo(A ast)
	{
        for (String filename : transientASTs.keySet())
            if (transientASTs.get(filename).get() == ast)
                return filename;
        
        for (String filename : permanentASTs.keySet())
            if (permanentASTs.get(filename) == ast)
                return filename;
        
        return null;
	}
	
	////////////////////////////////////////////////////////////////////////////
	// API: DEPENDENCIES
	////////////////////////////////////////////////////////////////////////////

//	public boolean checkForCircularDependencies(String filename)
//	{
//		// TODO
//		throw new UnsupportedOperationException();
//	}

    ///////////////////////////////////////////////////////////////////////////
    // Abstract Methods (Resource Filtering)
    ///////////////////////////////////////////////////////////////////////////

	/** @return true iff the given file should be parsed */
	protected abstract boolean shouldProcessFile(String filename);

	////////////////////////////////////////////////////////////////////////////
	// CREATION METHODS
	////////////////////////////////////////////////////////////////////////////

	/**
	 *  Creates a TokenRef referring to the token with the given position in the
	 *  given file.
	 *  <p>
	 *  Subclasses should override this method if they subclass {@link TokenRef}.
	 */
	public abstract R createTokenRef(String filename, int offset, int length);

    /**
     *  Creates an edge of the given type with the given tokens as endpoints.
     *  <p>
     *  Subclasses should override this method if they subclass {@link VPGEdge}.
     */
	public VPGEdge<A, T, R> createEdge(R fromRef, R toRef, int type)
	{
		return new VPGEdge<A, T, R>(this, fromRef, toRef, type);
	}

    /**
     *  Creates an edge of the given type with the given tokens as endpoints.
     */
	public final VPGEdge<A, T, R> createEdge(T from, T to, int type)
	{
		return createEdge(getTokenRef(from), getTokenRef(to), type);
	}

	////////////////////////////////////////////////////////////////////////////
	// PARSER/AST METHODS
	////////////////////////////////////////////////////////////////////////////

    /**
     * Calculates dependencies for the given file.
     * @param filename (non-null)
     */
    abstract protected void calculateDependencies(String filename);

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
	 * Parses the given file.
	 * @param filename (non-null)
	 * @return an AST for the given file, or <code>null</code> if an error was
	 *         encountered
	 */
	abstract protected A parse(String filename);

	/**
	 * Computes dependencies, edges, and annotations for the given file, adding them
	 * to the VPG database.
	 * <p>
	 * If the parser was unable to parse the given file, the AST will be <code>null</code>.
	 *
	 * @param filename the name of the parsed file (not null)
	 * @param ast the AST for the given file, as returned from the parser (possibly null)
	 */
	abstract protected void populateVPG(String filename, A ast);

	/** @return a TokenRef for the given token */
	abstract protected R getTokenRef(T forToken);

	/** Dereferences the given TokenRef, returning a pointer to that token in an
	 *  AST, or <code>null</code> if it could not be found. */
	abstract public T findToken(R tokenRef);

    /** Forces the database to be updated based on the current in-memory AST for the given file. */
    public void commitChangesFromInMemoryASTs(IProgressMonitor pm, int ticks, String... filenames)
    {
        List<String> files = new ArrayList<String>(Arrays.asList(filenames));
        files = sortFilesAccordingToDependencies(files, pm);

        pm = new SubProgressMonitor(pm, ticks, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        pm.beginTask("Post-transform analysis:", files.size());
        for (String thisFile : files)
        {
            pm.subTask(lastSegmentOfFilename(thisFile));
            doCommitChangeFromAST(thisFile);
            pm.worked(1);
        }
        pm.done();
    }

    protected void doCommitChangeFromAST(String filename)
    {
        if (!isVirtualFile(filename))
        {
            computeEdgesAndAnnotations(filename, acquireTransientAST(filename));
        }
    }

    public static String lastSegmentOfFilename(String filename)
    {
        if (filename == null) return "";

        int lastSlash = filename.lastIndexOf('/');
        int lastBackslash = filename.lastIndexOf('\\');
        if (lastSlash < 0 && lastBackslash < 0)
            return filename;
        else
            return filename.substring(Math.max(lastSlash + 1, lastBackslash + 1));
    }

    /** @return source code for the given AST (which may have been modified), or <code>null</code>
     *  if this capability is not supported */
    public String getSourceCodeFromAST(A ast)
    {
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // UTILITY METHODS - LOGGING
    ////////////////////////////////////////////////////////////////////////////

    protected void debug(String message, String filename)
    {
    }

    protected void debug(long parseTimeMillisec,
                         long computeEdgesAndAnnotationsMillisec,
                         String filename)
    {
    }

    ////////////////////////////////////////////////////////////////////////////
    // UTILITY METHODS - DATABASE
    ////////////////////////////////////////////////////////////////////////////

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
        return "Edge of type " + edgeType;
    }

    public String describeAnnotationType(int annotationType)
    {
        return "Annotation of type " + annotationType;
    }
}
