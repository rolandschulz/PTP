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
 * A TokenRef, i.e., a concise, serializable description of a unique token in an AST.
 * It consists of a filename, offset, and length.
 * <a href="../../../overview-summary.html#TokenRef">More Information</a>
 * <p>
 * N.B. If a VPG subclasses <code>TokenRef</code>, it <i>must</i> override
 * {@link VPG#createTokenRef(String, int, int)}.
 *
 * @author Jeff Overbey
 * 
 * @since 1.0
 */
public class TokenRef<T> implements Serializable, Comparable<TokenRef<?>>
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
	public TokenRef(String filename, int offset, int length)
	{
		this.filename = filename;
		this.offset = offset;
		this.length = length;
	}

    /** Copy constructor. */
	public TokenRef(TokenRef<T> copyFrom)
	{
		this.filename = copyFrom.filename;
		this.offset = copyFrom.offset;
		this.length = copyFrom.length;
	}

    ///////////////////////////////////////////////////////////////////////////
	// Accessors
    ///////////////////////////////////////////////////////////////////////////

	/** @return the filename containing the token being referenced */
	public String getFilename()
	{
		return filename;
	}

	/** @return the offset of the token being referenced */
	public int getOffset()
	{
		return offset;
	}

	/** @return the length of the token being referenced */
	public int getLength()
	{
		return length;
	}

    ///////////////////////////////////////////////////////////////////////////
	// Utility Methods
    ///////////////////////////////////////////////////////////////////////////

    /** @return the offset of the first character beyond the end of this token
     *  (i.e., the offset of last character in this token, plus one) */
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
        if (other == null || !(other instanceof TokenRef<?>)) return false;

        TokenRef<?> o = (TokenRef<?>)other;
        return filename.equals(o.filename)
            && offset == o.offset
            && length == o.length;
    }

    @Override public int hashCode()
    {
        return offset + length + (filename == null ? 0 : filename.hashCode());
    }

    /** @since 3.0 */
    public int compareTo(TokenRef<?> that)
    {
        int result = 0;
        
        if (this.filename != null && that.filename == null)
            result = -1;
        else if (this.filename == null && that.filename != null)
            result = 1;
        else if (this.filename != null && that.filename != null)
            result = this.filename.compareTo(that.filename);
        if (result != 0) return result;
        
        result = this.offset - that.offset;
        if (result != 0) return result;
        
        result = this.length - that.length;
        return result;
    }
}
