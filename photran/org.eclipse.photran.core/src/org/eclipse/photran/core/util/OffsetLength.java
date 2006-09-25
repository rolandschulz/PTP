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
        return this.offset <= other.offset
            && other.end() <= this.end();
    }
    
    protected int end()
    {
        return offset + length;
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

    public void setLength(int length)
    {
        this.length = Math.max(length, 0);
    }
    
    public String toString()
    {
        return "offset " + offset + ", length " + length;
    }
}
