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

import java.io.Serializable;

import org.eclipse.rephraserengine.core.preservation.ReplacementList;

/**
 * A semantic edge in a VPG.
 * <a href="../../../overview-summary.html#DEA">More Information</a>
 *
 * @author Jeff Overbey
 * 
 * @param <A> AST type
 * @param <T> token type
 * @param <R> {@link IVPGNode}/{@link NodeRef} type
 *
 * @since 1.0
 */
public final class VPGEdge<A, T, R extends IVPGNode<T>> implements Comparable<VPGEdge<?,?,?>>, Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * @since 3.0
     */
    public static enum Classification
    {
        INCOMING,
        OUTGOING,
        INTERNAL,
        EXTERNAL
    }
    
    /** @since 2.0 */
    protected final R source;
    /** @since 2.0 */
    protected final R sink;
    /** @since 2.0 */
    protected final int type;
    /** @since 3.0 */
    protected transient VPGEdge<A, T, R> origEdge;

	/**
	 * Constructor. Creates an edge of the given type between the given tokens in the given VPG.
	 * <p>
	 * The edge is <i>not</i> added to the VPG database automatically.
	 * 
	 * @since 3.0
	 */
	public VPGEdge(R source, R sink, int type)
	{
		this.source = source;
		this.sink = sink;
		this.type = type;
		this.origEdge = this;
	}

    /**
     * Constructor. Creates an edge of the given type between the given tokens in the given VPG.
     * <p>
     * The edge is <i>not</i> added to the VPG database automatically.
     * 
     * @since 3.0
     */
    public VPGEdge(R source, R sink, Enum<?> type)
    {
        this(source, sink, type.ordinal());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Accessors
    ///////////////////////////////////////////////////////////////////////////

	/**
	 * @return a TokenRef describing the token from which this edge emanates
     * @since 3.0
     */
	public R getSource()
	{
		return source;
	}

    /**
     * @return a TokenRef describing the token to which this edge points
     * @since 3.0
     */
	public R getSink()
	{
		return sink;
	}

    /** @return the type of this edge */
    public int getType()
    {
        return type;
    }

    /**
     * @return the original edge, if this edge is a projection created by
     *         {@link #projectInitial(ReplacementList)} or {@link #projectFinal(ReplacementList)}
     * 
     * @since 3.0
     */
    public VPGEdge<A, T, R> getOriginalEdge()
    {
        return origEdge;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility Methods
    ///////////////////////////////////////////////////////////////////////////

    /** @return the token from which this edge emanates
     * (i.e., a pointer to the token in an AST) */
	public T findSource() throws Exception
	{
		return source.getASTNode();
	}

    /** @return the token to which this edge points
     * (i.e., a pointer to the token in an AST) */
	public T findSink() throws Exception
	{
		return sink.getASTNode();
	}

    ///////////////////////////////////////////////////////////////////////////

	/** @since 3.0 */
    public int compareTo(VPGEdge<?,?,?> that)
    {
        int result = 0;
        
        if (this.type < that.type)
            result = -1;
        else if (this.type > that.type)
            result = 1;
        if (result != 0) return result;
        
        result = this.getClassification().compareTo(that.getClassification());
        if (result != 0) return result;

        if (this.source != null && that.source == null)
            result = -1;
        else if (this.source == null && that.source != null)
            result = 1;
        else if (this.source != null && that.source != null)
            result = this.source.compareTo(that.source);
        if (result != 0) return result;
        
        if (this.sink != null && that.sink == null)
            result = -1;
        else if (this.sink == null && that.sink != null)
            result = 1;
        else if (this.sink != null && that.sink != null)
            result = this.sink.compareTo(that.sink);
        return result;
    }

	@Override public String toString()
	{
		return "Edge of type " + type + " from " + source + " to " + sink; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

    /**
     * @return the initial model projection of this edge
     * 
     * @since 3.0
     */
    public VPGEdge<A, T, R> projectInitial(ReplacementList replacements, VPG<A,T,R> vpg)
    {
        R srcProj = replacements.projectInitial(source, vpg);
        R sinkProj = replacements.projectInitial(sink, vpg);
        VPGEdge<A, T, R> result = new VPGEdge<A, T, R>(srcProj, sinkProj, type);
        result.origEdge = this;
        return result;
    }

    /**
     * @return the final model projection of this edge
     * 
     * @since 3.0
     */
    public VPGEdge<A, T, R> projectFinal(ReplacementList replacements, VPG<A,T,R> vpg)
    {
        R srcProj = replacements.projectFinal(source, vpg);
        R sinkProj = replacements.projectFinal(sink, vpg);
        VPGEdge<A, T, R> result = new VPGEdge<A, T, R>(srcProj, sinkProj, type);
        result.origEdge = this;
        return result;
    }

    /**
     * @return the classification of this edge
     * 
     * @since 3.0
     */
    public Classification getClassification()
    {
        if (source != null && sink != null)
            return Classification.EXTERNAL;
        else if (source != null && sink == null)
            return Classification.INCOMING;
        else if (source == null && sink != null)
            return Classification.OUTGOING;
        else // (source == null && sink == null)
            return Classification.INTERNAL;
    }
}
