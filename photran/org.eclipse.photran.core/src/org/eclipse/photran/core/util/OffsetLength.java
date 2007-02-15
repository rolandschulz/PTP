package org.eclipse.photran.core.util;

public final class OffsetLength
{
    private int offset = 0, length = 0;

    public OffsetLength(int offset, int length)
    {
        this.offset = offset;
        this.length = length;
    }
    
    public boolean contains(OffsetLength other)
    {
        return other != null
            && this.offset <= other.offset
            && other.getPositionPastEnd() <= this.getPositionPastEnd();
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

    public int getPositionPastEnd()
    {
        return offset + Math.max(length, 1);
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
