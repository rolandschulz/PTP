/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.vpg;

import java.io.Serializable;

/**
 * A VPG node; in essence, a reference to an AST node.
 * <p>
 * A concise, serializable description of a unique node (or token) in an AST. It consists of a
 * filename, offset, and length. <a href="../../../overview-summary.html#TokenRef">More
 * Information</a>
 * <p>
 * These are usually created by {@link IVPGComponentFactory#getVPGNode(String, int, int)}.
 * 
 * @author Jeff Overbey
 * 
 * @param <T> node/token type
 * 
 * @since 3.0
 */
public interface IVPGNode<T> extends Serializable, Comparable<IVPGNode<?>>
{
    /** @return the filename containing the token being referenced */
    public abstract String getFilename();

    /** @return the offset of the token being referenced */
    public abstract int getOffset();

    /** @return the length of the token being referenced */
    public abstract int getLength();

    /**
     * @return a pointer to the AST node corresponding to this
     * {@link IVPGNode}, or <code>null</code> if it could not be found.
     */
    public abstract T getASTNode();

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
    public abstract <R extends IVPGNode<T>> Iterable<R> followOutgoing(int edgeType);

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
    public abstract <R extends IVPGNode<T>> Iterable<R> followIncoming(int edgeType);

    /** @return the annotation with the given ID for the given token, or <code>null</code>
     *  if it does not exist
     * @since 3.0
     */
    public abstract <R extends Serializable> R getAnnotation(int annotationID);

    /** @return the offset of the first character beyond the end of this token
     *  (i.e., the offset of last character in this token, plus one) */
    public abstract int getEndOffset();
}