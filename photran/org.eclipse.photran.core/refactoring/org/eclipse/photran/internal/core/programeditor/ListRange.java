package org.eclipse.photran.internal.core.programeditor;

/**
 * Represents a range of indices, e.g., in a linked list.
 * 
 * lastIndex < firstIndex indicates an empty range.
 * 
 * @author joverbey
 */
public final class ListRange
{
    private int firstIndex;

    private int lastIndex;

    public ListRange(int firstIndex, int lastIndex)
    {
        this.firstIndex = firstIndex;
        this.lastIndex = lastIndex;
    }
    
    public boolean isEmpty()
    {
        return lastIndex < firstIndex;
    }

    public int getFirstIndex()
    {
        return firstIndex;
    }

    public int getLastIndex()
    {
        return lastIndex;
    }
}
