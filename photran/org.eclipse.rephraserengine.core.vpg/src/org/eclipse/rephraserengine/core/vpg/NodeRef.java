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

/**
 * Standard implementation of {@link IVPGNode}.
 * 
 * @author Jeff Overbey
 * 
 * @param <T> node/token type
 * 
 * @since 3.0
 */
public abstract class NodeRef<T> implements IVPGNode<T>
{
	private static final long serialVersionUID = 1L;

    /** @since 2.0 */
	protected final String filename;
    /** @since 2.0 */
	protected final int offset;
    /** @since 2.0 */
	protected final int length;

	/** Constructor.  Creates a TokenRef referring to the token at
	 *  the given position in the given file. */
	public NodeRef(String filename, int offset, int length)
	{
		this.filename = filename;
		this.offset = offset;
		this.length = length;
	}

    /** Copy constructor. */
	public NodeRef(NodeRef<T> copyFrom)
	{
		this.filename = copyFrom.filename;
		this.offset = copyFrom.offset;
		this.length = copyFrom.length;
	}

    ///////////////////////////////////////////////////////////////////////////
	// Accessors
    ///////////////////////////////////////////////////////////////////////////

	/* (non-Javadoc)
     * @see org.eclipse.rephraserengine.core.vpg.IVPGNode#getFilename()
     */
	public String getFilename()
	{
		return filename;
	}

	/* (non-Javadoc)
     * @see org.eclipse.rephraserengine.core.vpg.IVPGNode#getOffset()
     */
	public int getOffset()
	{
		return offset;
	}

	/* (non-Javadoc)
     * @see org.eclipse.rephraserengine.core.vpg.IVPGNode#getLength()
     */
	public int getLength()
	{
		return length;
	}

    ///////////////////////////////////////////////////////////////////////////
    // AST Mapping
    ///////////////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.eclipse.rephraserengine.core.vpg.IVPGNode#getASTNode()
     */
    public abstract T getASTNode();

    ///////////////////////////////////////////////////////////////////////////
    // AST Mapping
    ///////////////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see org.eclipse.rephraserengine.core.vpg.IVPGNode#followOutgoing(int)
     */
    @SuppressWarnings("unchecked")
    public <R extends IVPGNode<T>> Iterable<R> followOutgoing(int edgeType)
    {
        return this.<R>getDB().getOutgoingEdgeTargets((R)this, edgeType);
    }

    /* (non-Javadoc)
     * @see org.eclipse.rephraserengine.core.vpg.IVPGNode#followIncoming(int)
     */
    @SuppressWarnings("unchecked")
    public <R extends IVPGNode<T>> Iterable<R> followIncoming(int edgeType)
    {
        return this.<R>getDB().getIncomingEdgeSources((R)this, edgeType);
    }

    /* (non-Javadoc)
     * @see org.eclipse.rephraserengine.core.vpg.IVPGNode#getAnnotation(int)
     */
    @SuppressWarnings("unchecked")
    public <R extends Serializable> R getAnnotation(int annotationID)
    {
        return (R)getDB().getAnnotation(this, annotationID);
    }

    ///////////////////////////////////////////////////////////////////////////
	// Utility Methods
    ///////////////////////////////////////////////////////////////////////////

    protected abstract <R extends IVPGNode<T>> VPG<?, T, R> getVPG();
    
    protected <R extends IVPGNode<T>> VPGDB<?, T, R> getDB()
    {
        return this.<R>getVPG().getDB();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.rephraserengine.core.vpg.IVPGNode#getEndOffset()
     */
    public int getEndOffset()
    {
        return offset + length;
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override public String toString()
    {
        return "(Offset " + offset + ", length " + length + " in " + filename + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override public boolean equals(Object other)
    {
        if (other == null || !(other instanceof IVPGNode<?>)) return false;

        IVPGNode<?> o = (IVPGNode<?>)other;
        return filename.equals(o.getFilename())
            && offset == o.getOffset()
            && length == o.getLength();
    }

    @Override public int hashCode()
    {
        return offset + length + (filename == null ? 0 : filename.hashCode());
    }

    /** @since 3.0 */
    public int compareTo(IVPGNode<?> that)
    {
        int result = 0;
        
        if (this.filename != null && that.getFilename() == null)
            result = -1;
        else if (this.filename == null && that.getFilename() != null)
            result = 1;
        else if (this.filename != null && that.getFilename() != null)
            result = this.filename.compareTo(that.getFilename());
        if (result != 0) return result;
        
        result = this.offset - that.getOffset();
        if (result != 0) return result;
        
        result = this.length - that.getLength();
        return result;
    }
}
