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
import java.util.Iterator;

/**
 * @author Jeff Overbey
 *
 * @since 1.0
 */
public abstract class VPGDB<A, T, R extends TokenRef<T>, L extends VPGLog<T, R>>
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
    // VPG Access
    ///////////////////////////////////////////////////////////////////////////

    /** DO NOT ACCESS THIS FIELD DIRECTLY; call {@link #getVPG()} instead. */
    private VPG<A, T, R, ? extends VPGDB<A, T, R, L>, L> vpg = null;

    /**
     * <b>FOR INTERNAL USE ONLY. THIS IS NOT AN API METHOD.</b>
     * <p>
     * Sets the VPG for which we are storing information.
     * <p>
     * This method is called by the VPG class constructor.
     *
     * @param vpg the VPG for which we are storing information
     */
    public void setVPG(VPG<A, T, R, ? extends VPGDB<A, T, R, L>, L> vpg)
    {
        this.vpg = vpg;
    }

    /**
     * Returns the VPG for which we are storing information.
     * <p>
     * This value is set by the VPG class constructor.  The user will call the
     * VPGDB constructor <i>before</i> calling the VPG class constructor,
     * so this field should not be accessed by a VPGDB constructor.
     *
     * @return the VPG for which we are storing information
     */
    protected VPG<A, T, R, ? extends VPGDB<A, T, R, L>, L> getVPG()
    {
        if (vpg == null)
            throw new IllegalStateException("This VPG database has not been "
                + "assigned to a VPG.  Construct a VPGDB object, and then "
                + "pass it to the VPG or EclipseVPG constructor.");
        else
            return vpg;
    }

    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    /** Forces any in-memory data to be flushed to disk */
    abstract public void flush();

    /** Called when the database is no longer needed.  Typically ensures that
     * any data in memory is flushed to disk and any locks are released.
     */
    abstract public void close();

    /** Removes ALL data from the database; also clears the error/warning log. */
    abstract public void clearDatabase();

    // HYPOTHETICAL UPDATING ///////////////////////////////////////////////////
    
    abstract public void enterHypotheticalMode() throws IOException;
    
    abstract public void leaveHypotheticalMode() throws IOException;
    
    abstract public boolean isInHypotheticalMode();

    // FILES ///////////////////////////////////////////////////////////////////

    /** Marks the VPG database entries for the given file as being up-to-date. */
    abstract public void updateModificationStamp(String filename);

    /** @return true iff the VPG entries for the given file are not up-to-date */
    abstract public boolean isOutOfDate(String filename);

    /** Removes all dependencies, edges, and annotations for the given file. */
    abstract public void deleteAllEntriesFor(String filename);

    /** Removes all edges and annotations (but not dependencies) for the given file. */
    abstract public void deleteAllEdgesAndAnnotationsFor(String filename);

    /** Removes all edges pointing inward to any token in the given file. */
    abstract public void deleteAllIncomingDependenciesFor(String filename);

    /** Removes all edges pointing outward from any token in the given file. */
    abstract public void deleteAllOutgoingDependenciesFor(String filename);

    /** Returns all filenames present in the VPG database. */
    abstract public Iterable<String> listAllFilenames();

    /** Returns the name of every file on which at least one other file is
     *  dependent. */
    abstract public Iterable<String> listAllFilenamesWithDependents();

    /** Returns the name of every file which depends on at least one other
     *  file. */
    abstract public Iterable<String> listAllDependentFilenames();

    // DEPENDENCIES ////////////////////////////////////////////////////////////

    /** Adds the given dependency to the VPG database if a dependency between
     *  its files does not already exist. */
    abstract public void ensure(VPGDependency<A, T, R> dependency);

    /** Deletes the given dependency from the VPG database. */
    abstract public void delete(VPGDependency<A, T, R> dependency);

    /** @return all of the files on which the given file depends */
    abstract public Iterable<String> getOutgoingDependenciesFrom(String filename);

    /** @return all of the files dependent on the given file */
    abstract public Iterable<String> getIncomingDependenciesTo(String filename);

    // EDGES ///////////////////////////////////////////////////////////////////

    /** Adds the given edge to the VPG database if an edge of the given type
     *  between its tokens does not already exist. */
    abstract public void ensure(VPGEdge<A, T, R> edge);

    /** Deletes the given edge from the VPG database. */
    abstract public void delete(VPGEdge<A, T, R> edge);

    /**
     * Returns a list of all of the edges with at least one endpoint in the given file.
     * <p>
     * Due to implementation details, some edges may be listed more than once.
     */
    abstract public Iterable<? extends VPGEdge<A, T, R>> getAllEdgesFor(String filename);

    /**
     * Returns a list of the edges extending from the given token.
     * <p>
     * To only return edges of a particular type, set the <code>edgeType</code>
     * parameter to that type.
     *
     * @param edgeType the type of edge (an arbitrary non-negative integer), or
     *                 {@link VPG#ALL_EDGES} to return all edges, regardless
     *                 of type
     */
    abstract public Iterable<? extends VPGEdge<A, T, R>> getOutgoingEdgesFrom(R tokenRef, int edgeType);

    /**
     * Returns a list of the edges pointing at the given token.
     * <p>
     * To only return edges of a particular type, set the <code>edgeType</code>
     * parameter to that type.
     *
     * @param edgeType the type of edge (an arbitrary non-negative integer), or
     *                 {@link VPG#ALL_EDGES} to return all edges, regardless
     *                 of type
     */
    abstract public Iterable<? extends VPGEdge<A, T, R>> getIncomingEdgesTo(R tokenRef, int edgeType);

    /**
     * Returns a list of the tokens pointed at by an edge extending from the given token.
     * <p>
     * To only return edges of a particular type, set the <code>edgeType</code>
     * parameter to that type.
     *
     * @param edgeType the type of edge (an arbitrary non-negative integer), or
     *                 {@link VPG#ALL_EDGES} to process all edges, regardless
     *                 of type
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
     */
    abstract public void setAnnotation(R token, int annotationID, Serializable annotation);

    /** Deletes the annotation with the given IDfor the given token, if it exists. */
    abstract public void deleteAnnotation(R token, int annotationID);

    /** @return the annotation with the given ID for the given token, or <code>null</code>
     *  if it does not exist */
    abstract public Serializable getAnnotation(R tokenRef, int annotationID);

    // UTILITY METHODS /////////////////////////////////////////////////////////

    public abstract void printOn(PrintStream out);

    public abstract void printStatisticsOn(PrintStream out);

    public abstract void resetStatistics();

    protected String describeEdgeType(int edgeType)
    {
        return "Edge of type " + edgeType;
    }

    protected String describeAnnotationType(int annotationType)
    {
        return "Annotation of type " + annotationType;
    }

    protected String describeToken(String filename, int offset, int length)
    {
        return filename + ", offset " + offset + ", length " + length;
    }

    ////////////////////////////////////////////////////////////////////////////
    // UTILITY CLASSES
    ////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("hiding")
    private class EdgeIterable<A, T, R extends TokenRef<T>> implements Iterable<R>
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
