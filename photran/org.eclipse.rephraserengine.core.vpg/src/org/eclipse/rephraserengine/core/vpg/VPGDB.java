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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.rephraserengine.core.util.Pair;
import org.eclipse.rephraserengine.core.vpg.db.profiling.ProfilingDB;

/**
 * A VPG database, which stores dependencies, edges, and annotations.
 * 
 * @author Jeff Overbey
 * 
 * @param <A> AST type
 * @param <T> token type
 * @param <R> {@link IVPGNode}/{@link NodeRef} type
 *
 * @since 1.0
 */
public abstract class VPGDB<A, T, R extends IVPGNode<T>>
{
    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Constant that can be passed to <code>getIncoming/OutgoingEdge...()</code> to
     * indicate that edges of all types should be returned.
     */
    public static final int ALL_EDGES = Integer.MIN_VALUE;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    /** @since 3.0 */
    protected final IVPGComponentFactory<A, T, R> factory;
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /** @since 3.0 */
    public VPGDB(IVPGComponentFactory<A, T, R> factory)
    {
        this.factory = factory;
    }
    
    /**
     * Constructor for use by subclasses that wrap another database (e.g., {@link ProfilingDB}.
     * <p>
     * Rather than providing an explicit {@link IVPGComponentFactory}, this uses the locator from the
     * wrapped database.
     * 
     * @since 3.0
     */
    protected VPGDB(VPGDB<A, T, R> wrappedDB)
    {
        this.factory = wrappedDB.factory;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    /** Forces any in-memory data to be flushed to disk */
    public abstract void flush();

    /** Called when the database is no longer needed.  Typically ensures that
     * any data in memory is flushed to disk and any locks are released.
     */
    public abstract void close();

    /** Removes ALL data from the database; also clears the error/warning log. */
    public abstract void clearDatabase();

    // HYPOTHETICAL UPDATING ///////////////////////////////////////////////////
    
    public abstract void enterHypotheticalMode() throws IOException;
    
    public abstract void leaveHypotheticalMode() throws IOException;
    
    public abstract boolean isInHypotheticalMode();

    // FILES ///////////////////////////////////////////////////////////////////

    /** Marks the VPG database entries for the given file as being up-to-date. */
    public abstract void updateModificationStamp(String filename);

    /** @return true iff the VPG entries for the given file are not up-to-date */
    public abstract boolean isOutOfDate(String filename);

    /** Removes all dependencies, edges, and annotations for the given file. */
    public abstract void deleteAllEntriesFor(String filename);

    /** Removes all edges and annotations (but not dependencies) for the given file. */
    public abstract void deleteAllEdgesAndAnnotationsFor(String filename);

    /** Removes all edges pointing inward to any token in the given file. */
    public abstract void deleteAllIncomingDependenciesFor(String filename);

    /** Removes all edges pointing outward from any token in the given file. */
    public abstract void deleteAllOutgoingDependenciesFor(String filename);

    /** Returns all filenames present in the VPG database. */
    public abstract Iterable<String> listAllFilenames();

    /** Returns the name of every file on which at least one other file is
     *  dependent. */
    public abstract Iterable<String> listAllFilenamesWithDependents();

    /** Returns the name of every file which depends on at least one other
     *  file. */
    public abstract Iterable<String> listAllDependentFilenames();

    // DEPENDENCIES ////////////////////////////////////////////////////////////

    /** Adds the given dependency to the VPG database if a dependency between
     *  its files does not already exist. */
    public abstract void ensure(VPGDependency<A, T, R> dependency);

    /** Deletes the given dependency from the VPG database. */
    public abstract void delete(VPGDependency<A, T, R> dependency);

    /** @return all of the files on which the given file depends */
    public abstract Iterable<String> getOutgoingDependenciesFrom(String filename);

    /** @return all of the files dependent on the given file */
    public abstract Iterable<String> getIncomingDependenciesTo(String filename);
    
    /** @since 3.0 */
    public List<String> sortFilesAccordingToDependencies(final List<String> files)
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

                for (String v : getIncomingDependenciesTo(u))
                    if (color.get(v) == WHITE)
                        dfsVisit(v);

                color.put(u, BLACK);
                result.add(0, u);
            }
        }

        return new DFS().result;
    }

    private void enqueueNewDependents(String filename, List<String> dependents)
    {
        for (String f : findAllFilesDependentUpon(filename))
            if (!dependents.contains(f))
                dependents.add(f);
    }

    private Set<String> findAllFilesDependentUpon(String filename)
    {
        Set<String> dependents = new HashSet<String>();

        for (String dependentFilename : getIncomingDependenciesTo(filename))
            dependents.add(dependentFilename);

        return dependents;
    }

    // EDGES ///////////////////////////////////////////////////////////////////

    /** Adds the given edge to the VPG database if an edge of the given type
     *  between its tokens does not already exist. */
    public abstract void ensure(VPGEdge<A, T, R> edge);

    /** Deletes the given edge from the VPG database. */
    public abstract void delete(VPGEdge<A, T, R> edge);

    /**
     * Returns a list of all of the edges with at least one endpoint in the given file.
     * <p>
     * Due to implementation details, some edges may be listed more than once.
     */
    public abstract Iterable<? extends VPGEdge<A, T, R>> getAllEdgesFor(String filename);

    /**
     * Returns a list of the edges extending from the given token.
     * <p>
     * To only return edges of a particular type, set the <code>edgeType</code>
     * parameter to that type.
     *
     * @param edgeType the type of edge (an arbitrary non-negative integer), or
     *                 {@link VPG#ALL_EDGES} to return all edges, regardless
     *                 of type
     * @since 3.0
     */
    public abstract Iterable<? extends VPGEdge<A, T, R>> getOutgoingEdgesFrom(R tokenRef, int edgeType);

    /**
     * Returns a list of the edges pointing at the given token.
     * <p>
     * To only return edges of a particular type, set the <code>edgeType</code>
     * parameter to that type.
     *
     * @param edgeType the type of edge (an arbitrary non-negative integer), or
     *                 {@link VPG#ALL_EDGES} to return all edges, regardless
     *                 of type
     * @since 3.0
     */
    public abstract Iterable<? extends VPGEdge<A, T, R>> getIncomingEdgesTo(R tokenRef, int edgeType);

    /**
     * Returns a list of the tokens pointed at by an edge extending from the given token.
     * <p>
     * To only return edges of a particular type, set the <code>edgeType</code>
     * parameter to that type.
     *
     * @param edgeType the type of edge (an arbitrary non-negative integer), or
     *                 {@link VPG#ALL_EDGES} to process all edges, regardless
     *                 of type
     * @since 3.0
     */
    public Iterable<R> getOutgoingEdgeTargets(R tokenRef, int edgeType)
    {
        return new EdgeIterable<A, T, R>(getOutgoingEdgesFrom(tokenRef, edgeType), false);
    }

    /**
     * Returns a list of the tokens which have an edges pointing at the given token.
     * <p>
     * To only return edges of a particular type, set the <code>edgeType</code>
     * parameter to that type.
     *
     * @param edgeType the type of edge (an arbitrary non-negative integer), or
     *                 {@link VPG#ALL_EDGES} to process all edges, regardless
     *                 of type
     * @since 3.0
     */
    public Iterable<R> getIncomingEdgeSources(R tokenRef, int edgeType)
    {
        return new EdgeIterable<A, T, R>(getIncomingEdgesTo(tokenRef, edgeType), true);
    }

    // ANNOTATIONS /////////////////////////////////////////////////////////////

    /**
     * Annotates the given token with the given (serializable) object
     * (which may be <code>null</code>).
     * If an annotation for the given token with the given ID already exists,
     * it will be replaced.
     * <p>
     * A token can have several annotations, but each annotation must be given
     * a unique ID.  For example, annotation 0 might describe
     * the type of an identifier, while annotation 1 might hold documentation
     * for that identifier.
     * 
     * @since 3.0
     */
    public abstract void setAnnotation(R token, int annotationID, Serializable annotation);

    /** @since 3.0 */
    public void setAnnotation(R token, Enum<?> annotationID, Serializable annotation)
    {
        setAnnotation(token, annotationID.ordinal(), annotation);
    }

    /** Deletes the annotation with the given ID for the given token, if it exists.
     * @since 3.0
     */
    public abstract void deleteAnnotation(R token, int annotationID);

    /** @since 3.0 */
    public void deleteAnnotation(R token, Enum<?> annotationID)
    {
        deleteAnnotation(token, annotationID.ordinal());
    }

    /** @return the annotation with the given ID for the given token, or <code>null</code>
     *  if it does not exist
     * @since 3.0
     */
    public abstract Serializable getAnnotation(R tokenRef, int annotationID);

    /** @since 3.0 */
    public Serializable getAnnotation(R tokenRef, Enum<?> annotationID)
    {
        return getAnnotation(tokenRef, annotationID.ordinal());
    }

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
    public abstract Iterable<Pair<R, Integer>> getAllAnnotationsFor(String filename);
    
    // UTILITY METHODS /////////////////////////////////////////////////////////

    public abstract void printOn(PrintStream out);

    public abstract void printStatisticsOn(PrintStream out);

    public abstract void resetStatistics();

    /** @since 3.0 */
    public String describeEdgeType(int edgeType)
    {
        return Messages.bind(Messages.VPGDB_EdgeOfType, edgeType);
    }

    /** @since 3.0 */
    public String describeAnnotationType(int annotationType)
    {
        return Messages.bind(Messages.VPGDB_AnnotationOfType, annotationType);
    }

    /** @since 3.0 */
    public String describeToken(String filename, int offset, int length)
    {
        return Messages.bind(Messages.VPGDB_FilenameOffsetLength,
                             new Object[] { filename, offset, length });
    }

    ////////////////////////////////////////////////////////////////////////////
    // UTILITY CLASSES
    ////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("hiding")
    private class EdgeIterable<A, T, R extends IVPGNode<T>> implements Iterable<R>
    {
        private Iterable<? extends VPGEdge<A, T, R>> edges;
        private boolean returnSources;

        EdgeIterable(Iterable<? extends VPGEdge<A, T, R>> edges, boolean returnSources)
        {
            this.edges = edges;
            this.returnSources = returnSources;
        }

        public Iterator<R> iterator()
        {
            final Iterator<? extends VPGEdge<A, T, R>> edgeIterator = edges.iterator();

            return new Iterator<R>()
            {
                public boolean hasNext()
                {
                    return edgeIterator.hasNext();
                }

                public R next()
                {
                    try
                    {
                        VPGEdge<A, T, R> edge = edgeIterator.next();
                        return returnSources ? edge.getSource() : edge.getSink();
                    }
                    catch (Exception e)
                    {
                        throw new Error(e);
                    }
                }

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
