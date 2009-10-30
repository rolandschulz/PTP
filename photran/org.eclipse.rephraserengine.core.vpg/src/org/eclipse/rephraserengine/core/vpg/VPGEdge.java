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

/**
 * An edge in a VPG.
 * <a href="../../../overview-summary.html#DEA">More Information</a>
 * <p>
 * N.B. If a VPG subclasses <code>VPGEdge</code> (e.g., to create individual
 * classes for the various edge types), it <i>must</i>
 * override {@link VPG#createEdge(TokenRef, TokenRef, int)}.
 *
 * @author Jeff Overbey
 *
 * @param <A> AST type
 * @param <T> token type
 */
public class VPGEdge<A, T, R extends TokenRef<T>>
{
	private VPG<A, T, R, ?, ?> vpg;
	private R source;
	private R sink;
	private int type;

	/**
	 * Constructor. Creates an edge of the given type between the given tokens in the given VPG.
	 * <p>
	 * The edge is <i>not</i> added to the VPG database automatically.
	 */
	public VPGEdge(VPG<A, T, R, ?, ?> vpg,
	                  R source,
	                  R sink,
	                  int type)
	{
		this.vpg = vpg;
		this.source = source;
		this.sink = sink;
		this.type = type;
	}

	public VPGEdge(VPG<A, T, R, ?, ?> vpg,
	                  T source,
	                  T sink,
	                  int type)
	{
		this.vpg = vpg;
		this.source = vpg.getTokenRef(source);
		this.sink = vpg.getTokenRef(sink);
		this.type = type;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Accessors
    ///////////////////////////////////////////////////////////////////////////

	/** @return a TokenRef describing the token from which this edge emanates */
	public R getSource()
	{
		return source;
	}

    /** @return a TokenRef describing the token to which this edge points */
	public R getSink()
	{
		return sink;
	}

	/** @return the type of this edge */
	public int getType()
	{
		return type;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Utility Methods
    ///////////////////////////////////////////////////////////////////////////

    /** @return the token from which this edge emanates
     * (i.e., a pointer to the token in an AST) */
	public T findSource() throws Exception
	{
		return vpg.findToken(source);
	}

    /** @return the token to which this edge points
     * (i.e., a pointer to the token in an AST) */
	public T findSink() throws Exception
	{
		return vpg.findToken(sink);
	}

    ///////////////////////////////////////////////////////////////////////////

	@Override public String toString()
	{
		return "Edge of type " + type + " from " + source + " to " + sink;
	}
}
