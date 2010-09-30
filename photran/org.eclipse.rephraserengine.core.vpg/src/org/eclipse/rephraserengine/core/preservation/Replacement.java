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
package org.eclipse.rephraserengine.core.preservation;

import org.eclipse.rephraserengine.core.vpg.TokenRef;

/**
 * A replacement [ i, j ) <small>//</small> [ i, k ), i.e., an object representing
 * the replacement of a textual span with offset <i>i</i> and length
 * |<i>j</i> - <i>i</i>| with new text of length |<i>k</i> - <i>i</i>|.
 * 
 * @author Jeff Overbey
 * 
 * @since 3.0
 */
public final class Replacement
{
    private final String filename;
    private final int offset;
    private final int origLength;
    private final int newLength;

    /**
     * @param filename
     * @param offset
     * @param oldLength
     * @param newLength
     */
    public Replacement(String filename, int offset, int oldLength, int newLength)
    {
        this.filename = filename;
        this.offset = offset;
        this.origLength = oldLength;
        this.newLength = newLength;
    }

    /** @return the filename */
    public String getFilename()
    {
        return filename;
    }

    /** @return the start offset */
    public int getOffset()
    {
        return offset;
    }

    /** @return the end offset of the original text ({@link #getOffset()} + {@link #getOrigLength()}) */
    public int getOrigEndOffset()
    {
        return offset + origLength;
    }

//    /** @return the end offset of the replacement text ({@link #getOffset()} + {@link #getNewLength()}) */
//    public int getNewEndOffset()
//    {
//        return offset + newLength;
//    }

    /** @return the length of the original text */
    public int getOrigLength()
    {
        return origLength;
    }

    /** @return the length of the replacement text */
    public int getNewLength()
    {
        return newLength;
    }
    
    public boolean isAddition()
    {
        return origLength == 0;
    }
    
    public boolean isRemoval()
    {
        return newLength == 0;
    }

    int adjust(String filename, int n)
    {
        if (this.filename.equals(filename) && n >= offset+origLength)
            return newLength - origLength;
        else
            return 0;
    }

    boolean overlaps(Replacement that)
    {
        return !doesNotOverlap(that);
    }

    private boolean doesNotOverlap(Replacement that)
    {
        return !this.getFilename().equals(that.getFilename())
            || this.getOrigEndOffset() <= that.getOffset()
            || this.getOffset() >= that.getOrigEndOffset();
    }
    
    boolean origIntervalContains(TokenRef<?> tokenRef)
    {
        return filename.equals(tokenRef.getFilename())
            && this.offset <= tokenRef.getOffset()
            && tokenRef.getEndOffset() <= this.getOrigEndOffset();
    }
    
    boolean newIntervalContains(TokenRef<?> tokenRef, ReplacementList list)
    {
        if (!filename.equals(tokenRef.getFilename())) return false;
        
        list = list.without(this);
        
        int newOffset = list.offset(filename, getOffset());
        int newEndOffset = list.offset(filename, offset+newLength-1) + 1;
        
        return newOffset <= tokenRef.getOffset()
            && tokenRef.getEndOffset() <= newEndOffset;
    }

    @Override public String toString()
    {
        return filename + ": "                                //$NON-NLS-1$
            + "[" + offset + ", " + (offset+origLength) + ")"  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + " // "                                          //$NON-NLS-1$
            + "[" + offset + ", " + (offset+newLength) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filename == null) ? 0 : filename.hashCode());
        result = prime * result + newLength;
        result = prime * result + offset;
        result = prime * result + origLength;
        return result;
    }

    @Override public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Replacement other = (Replacement)obj;
        if (filename == null)
        {
            if (other.filename != null) return false;
        }
        else if (!filename.equals(other.filename)) return false;
        if (newLength != other.newLength) return false;
        if (offset != other.offset) return false;
        if (origLength != other.origLength) return false;
        return true;
    }
}
