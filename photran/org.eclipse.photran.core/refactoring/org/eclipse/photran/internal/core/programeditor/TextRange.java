package org.eclipse.photran.internal.core.programeditor;

import org.eclipse.photran.internal.core.f95parser.IPresentationBlock;

/**
 * Represents a range of positions within a document.
 * 
 * @author joverbey
 */
public final class TextRange
{
    private int firstLine, firstCol;

    private int lastLine, lastCol;

    public TextRange(int firstLine, int firstCol, int lastLine, int lastCol)
    {
        this.firstLine = firstLine;
        this.firstCol = firstCol;
        this.lastLine = lastLine;
        this.lastCol = lastCol;
    }

    public TextRange(IPresentationBlock firstBlock, IPresentationBlock lastBlock)
    {
        this.firstLine = firstBlock.getStartLine();
        this.firstCol = firstBlock.getStartCol();
        this.lastLine = lastBlock.getEndLine();
        this.lastCol = lastBlock.getEndCol();
    }

    public int getFirstCol()
    {
        return firstCol;
    }

    public int getFirstLine()
    {
        return firstLine;
    }

    public int getLastCol()
    {
        return lastCol;
    }

    public int getLastLine()
    {
        return lastLine;
    }
}
