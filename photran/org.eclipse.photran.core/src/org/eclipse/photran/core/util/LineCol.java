package org.eclipse.photran.core.util;

public final class LineCol
{
    private int line = 0, col = 0;

    public LineCol(int line, int col)
    {
        this.line = line;
        this.col = col;
    }

    public int getLine()
    {
        return line;
    }

    public void setLine(int line)
    {
        this.line = Math.max(line, 0);
    }

    public int getCol()
    {
        return col;
    }

    public void setCol(int col)
    {
        this.col = Math.max(col, 0);
    }
    
    public String toString()
    {
        return "line " + line + ", column " + col;
    }
}
