package org.eclipse.photran.internal.core.programeditor;

import org.eclipse.photran.internal.core.f95parser.IPresentationBlock;

/**
 * Utility methods for <code>IPresentationBlock</code>s. Comparisons are done based on line and
 * column information, not offsets.
 * 
 * TODO-Jeff: Move comesBefore into IPresentationBlock after Token class is not generated anymore
 * 
 * @author joverbey
 */
public class PresBlockUtil
{
    public static IPresentationBlock whicheverComesFirst(IPresentationBlock a, IPresentationBlock b)
    {
        if (a == null)
            return b;
        else if (b == null)
            return a;
        else
            return comesBefore(a, b) ? a : b;
    }
    
    public static boolean comesBefore(IPresentationBlock a, IPresentationBlock b)
    {
        return comesBefore(a, b.getStartLine(), b.getStartCol());
    }
    
    public static boolean comesBefore(IPresentationBlock blk, int line, int col)
    {
        if (blk.getStartLine() < line)
            return true;
        else if (blk.getStartLine() == line && blk.getStartCol() < col)
            return true;
        else
            return false;
    }
    
    public static boolean comesAfter(IPresentationBlock blk, int line, int col)
    {
        return !comesBefore(blk, line, col+1);
    }
    
    public static boolean comesOnOrAfter(IPresentationBlock blk, int line, int col)
    {
        if (blk.getStartLine() == line && blk.getStartCol() == col)
            return true;
        else
            return comesAfter(blk, line, col);
    }
}
