package org.eclipse.photran.core.util;

import org.eclipse.photran.internal.core.lexer.Token;

public final class OffsetLength
{
    private int offset = 0, length = 0;

    public OffsetLength(int offset, int length)
    {
        this.offset = offset;
        this.length = length;
    }
    
    public static boolean contains(int offset, int length, int otherOffset, int otherLength)
    {
        return offset <= otherOffset
            && getPositionPastEnd(otherOffset, otherLength) <= getPositionPastEnd(offset, length);
    }
    
    public static boolean contains(int offset, int length, OffsetLength other)
    {
        return other != null && contains(offset, length, other.offset, other.length);
    }
    
    public boolean contains(OffsetLength other)
    {
        return contains(this.offset, this.length, other);
    }
    
    public boolean containsFileRange(Token token)
    {
        return contains(this.offset, this.length, token.getFileOffset(), token.getLength());
    }
    
    public boolean equals(OffsetLength other)
    {
        return this.offset == other.offset && this.length == other.length;
    }

    public boolean isOnOrAfter(int targetOffset)
    {
        return this.offset >= targetOffset;
    }

    public boolean isBefore(int targetOffset)
    {
        return this.offset < targetOffset;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = Math.max(offset, 0);
    }

    public int getLength()
    {
        return length;
    }

    public static int getPositionPastEnd(int offset, int length)
    {
        return offset + Math.max(length, 1);
    }

    public int getPositionPastEnd()
    {
        return getPositionPastEnd(offset, length);
    }

    public void setLength(int length)
    {
        this.length = Math.max(length, 0);
    }
    
    public String toString()
    {
        return "offset " + offset + ", length " + length;
    }
}
