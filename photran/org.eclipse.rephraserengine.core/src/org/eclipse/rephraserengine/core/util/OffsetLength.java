/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.util;


/**
 * An <b>offset</b> and a <b>length</b> (simply two integers, typically non-negative).
 * <p>
 * <code>OffsetLength</code>s are frequently used to store the location of a particular range of
 * text in a file.
 *
 * @author Jeff Overbey
 *
 * @since 1.0
 */
public final class OffsetLength
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Static Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** @return the offset of the first character beyond this region */
    public static int getPositionPastEnd(int offset, int length)
    {
        return offset + Math.max(length, 1);
    }

    /** @return true iff every character in the "other" region is also in "this" region */
    public static boolean contains(int thisOffset, int thisLength, int otherOffset, int otherLength)
    {
        return thisOffset <= otherOffset
            && getPositionPastEnd(otherOffset, otherLength) <= getPositionPastEnd(thisOffset, thisLength);
    }

    /** @return true iff every character in the "other" region is also in "this" region */
    public static boolean contains(int thisOffset, int thisLength, OffsetLength other)
    {
        return other != null && contains(thisOffset, thisLength, other.offset, other.length);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private int offset = 0, length = 0;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Creates an <code>OffsetLength</code> with the given offset and length */
    public OffsetLength(int offset, int length)
    {
        this.offset = offset;
        this.length = length;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Basic Accessor/Mutator Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** @return the offset */
    public int getOffset()
    {
        return offset;
    }

    /** Sets the offset */
    public void setOffset(int offset)
    {
        this.offset = Math.max(offset, 0);
    }

    /** @return the length */
    public int getLength()
    {
        return length;
    }

    /** Sets the length */
    public void setLength(int length)
    {
        this.length = Math.max(length, 0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Computed Accessor Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** @return the offset of the first character beyond this region */
   public int getPositionPastEnd()
    {
        return getPositionPastEnd(offset, length);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inquiry & Comparison Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

   /**
    * @return true iff the offset and length are both non-negative, i.e., this
    *         represents a plausible region of text
    * @since 2.0
    */
   public boolean isValid()
   {
       return this.offset >= 0 && this.length >= 0;
   }

   /** @return true iff every character in the "other" region is also in this region */
    public boolean contains(OffsetLength other)
    {
        return contains(this.offset, this.length, other);
    }

    /** @return true iff both the offset and length are equal to those of the supplied <code>OffsetLength</code> */
    public boolean equals(Object o)
    {
        if (o == null || !this.getClass().equals(o.getClass())) return false;

        OffsetLength other = (OffsetLength)o;
        return this.offset == other.offset && this.length == other.length;
    }

    public int hashCode()
    {
        return 19 * offset + length;
    }

    /** @return true iff this offset is greater than or equal to the target offset */
    public boolean isOnOrAfter(int targetOffset)
    {
        return this.offset >= targetOffset;
    }

    /** @return true iff this offset is less than the target offset */
    public boolean isBefore(int targetOffset)
    {
        return this.offset < targetOffset;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // toString
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String toString()
    {
        return "offset " + offset + ", length " + length;
    }
}
