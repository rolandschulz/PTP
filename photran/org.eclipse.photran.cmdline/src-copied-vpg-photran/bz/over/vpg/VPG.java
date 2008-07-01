package bz.over.vpg;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
 */
public abstract class VPG<A, T, R extends TokenRef<T>, D extends VPGDB<A, T, R>>
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
	
	/** The error/warning log. */
	protected List<VPGErrorOrWarning<T, R>> log = new LinkedList<VPGErrorOrWarning<T, R>>();
	
	/** The VPG database, which persists edges and annotations. */
	public final D db;
	
    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////

    protected VPG(D database)
    {
        this(database, 5);
    }

	protected VPG(D database, int transientASTCacheSize)
	{
	    assert transientASTCacheSize > 0;
	    
		this.transientASTs = new HashMap<String, WeakReference<A>>();
		this.permanentASTs = new HashMap<String, A>();
		this.transientASTCache = new Object[transientASTCacheSize];
		this.db = database;
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
		A ast = null;
		
		if (!forceRecomputationOfEdgesAndAnnotations)
		{
			if (permanentASTs.containsKey(filename))
				ast = permanentASTs.get(filename);
			else if (transientASTs.containsKey(filename))
				ast = transientASTs.get(filename).get();

			if (ast != null) return ast;
		}

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

    private long computeEdgesAndAnnotations(String filename, A ast)
    {
        long start = System.currentTimeMillis();
        db.deleteAllEdgesAndAnnotationsFor(filename);
        populateVPG(filename, ast);
        db.updateModificationStamp(filename);
        return System.currentTimeMillis()-start;
    }

    protected void processingDependent(String filename, String dependentFilename)
    {
        debug("- Processing dependent file " + dependentFilename, filename);
    }

    protected void enqueueNewDependents(String filename,
                                        ArrayList<String> dependents)
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
     * Releases all permanent ASTs.
     * 
     * @see #acquirePermanentAST(String)
     * @see #makeTransientASTPermanent(String)
     */
	public void releaseAllASTs()
	{
		permanentASTs.clear();
	}
	
	////////////////////////////////////////////////////////////////////////////
	// API: DEPENDENCIES
	////////////////////////////////////////////////////////////////////////////
	
//	public boolean checkForCircularDependencies(String filename)
//	{
//		// TODO
//		throw new UnsupportedOperationException(); 
//	}
	
	////////////////////////////////////////////////////////////////////////////
	// API: ERROR LOGGING
	////////////////////////////////////////////////////////////////////////////
	
	/** Clears the error/warning log. */
	public void clearLog()
	{
		log.clear();
	}
	
	/** Removes all entries for the given file from the error/warning log. */
	public void clearLogEntriesFor(String filename)
	{
		Iterator<VPGErrorOrWarning<T, R>> it = log.iterator();
		while (it.hasNext())
		{
			R tokenRef = it.next().getTokenRef();
			if (tokenRef != null && tokenRef.getFilename().equals(filename))
				it.remove();
		}
	}

	/**
	 * Adds the given warning to the error/warning log.
	 * 
	 * @param message the warning message to display to the user
	 */
	public void logWarning(String message)
	{
		log.add(new VPGErrorOrWarning<T, R>(true, message, null));
	}
	
    /**
     * Adds the given warning to the error/warning log.
     * 
     * @param message the warning message to display to the user
     * @param filename the file with which the warning is associated
     */
	public void logWarning(String message, String filename)
	{
		log.add(new VPGErrorOrWarning<T, R>(true, message, createTokenRef(filename, 0, 0)));
	}
	
    /**
     * Adds the given warning to the error/warning log.
     * 
     * @param message the warning message to display to the user
     * @param tokenRef a specific token with which the warning is associated;
     *                 for example, if an identifier was used without being
     *                 initialized, it could reference that identifier
     */
	public void logWarning(String message, R tokenRef)
	{
		log.add(new VPGErrorOrWarning<T, R>(true, message, tokenRef));
	}

    /**
     * Adds the given error to the error/warning log.
     * 
     * @param e an exception that will be displayed to the user
     */
	public void logError(Throwable e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.getClass().getName());
		sb.append(": ");
		sb.append(e.getMessage());
		sb.append("\n");
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(bs));
		sb.append(bs);
		
		log.add(new VPGErrorOrWarning<T, R>(false, sb.toString(), null));
	}

    /**
     * Adds the given error to the error/warning log.
     * 
     * @param message the error message to display to the user
     */
	public void logError(String message)
	{
		log.add(new VPGErrorOrWarning<T, R>(false, message, null));
	}

    /**
     * Adds the given error to the error/warning log.
     * 
     * @param message the error message to display to the user
     * @param filename the file with which the warning is associated
     */
	public void logError(String message, String filename)
	{
		log.add(new VPGErrorOrWarning<T, R>(false, message, createTokenRef(filename, 0, 0)));
	}
	
    /**
     * Adds the given error to the error/warning log.
     * 
     * @param message the error message to display to the user
     * @param tokenRef a specific token with which the error is associated;
     *                 for example, if an identifier was used but not
     *                 declared, it could reference that identifier
     */
	public void logError(String message, R tokenRef)
	{
		log.add(new VPGErrorOrWarning<T, R>(false, message, tokenRef));
	}
	
	/** @return true iff at least one error exists in the error/warning log */
	public boolean hasErrorsLogged()
	{
		for (VPGErrorOrWarning<T, R> entry : log)
			if (entry.isError())
				return true;
		
		return false;
	}

    /** @return true iff at least one entry exists in the error/warning log */
	public boolean hasErrorsOrWarningsLogged()
	{
		return !log.isEmpty();
	}
	
	/** @return the error/warning log */
	public List<VPGErrorOrWarning<T, R>> getErrorLog()
	{
		return log;
	}
	
	/** Prints the error/warning log on the given <code>PrintStream</code> */
	public void printErrorLogOn(PrintStream out)
	{
	    for (VPGErrorOrWarning<T, R> entry : log)
	    {
	        out.print(entry.isError() ? "ERROR:   " : "Warning: ");
	        out.println(entry.getMessage());
	        
	        R t = entry.getTokenRef();
	        if (t != null)
	        {
	            out.print("         (");
	            out.print(t.getFilename());
                out.print(", offset ");
                out.print(t.getOffset());
                out.print(", length ");
                out.print(t.getLength());
                out.println(")");
	        }
	    }
	}

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
}
