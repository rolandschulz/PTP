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
 */
public class TokenRef<T> implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String filename;
	private int offset;
	private int length;

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
        return "(Offset " + offset + ", length " + length + " in " + filename + ")";
    }

    @Override public boolean equals(Object other)
    {
        if (!(other instanceof TokenRef<?>)) return false;

        TokenRef<?> o = (TokenRef<?>)other;
        return filename.equals(o.filename)
            && offset == o.offset
            && length == o.length;
    }

    @Override public int hashCode()
    {
        return offset + length + (filename == null ? 0 : filename.hashCode());
    }
}
