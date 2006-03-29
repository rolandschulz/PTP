package org.eclipse.photran.internal.core.f95modelparser;


/**
 * Represents a comment, line continuation, or other entity that is
 * in the source code but is not included in the parse tree.
 * 
 * @author joverbey
 */
public class NonTreeToken implements IPresentationBlock
{
    private String filename;

    private int offset, length;
    
    private int startLine, endLine;

    private int startCol, endCol;

    private String text;

    public NonTreeToken(String filename, int offset, int row, int col, String text)
    {
        this.text = text;
        this.filename = filename;
        this.offset = offset;
        this.length = text.length();
        this.startLine = row;
        this.startCol = col;
        this.endLine = row + countNumberOfNewLinesIn(text);
        determineEndCol();
    }

    private int countNumberOfNewLinesIn(String string)
    {
        int count = 0;
        int lastOccurrence = -1, nextOccurrence;
        
        for (nextOccurrence = string.indexOf('\n', lastOccurrence+1);
            nextOccurrence > 0;
            lastOccurrence = nextOccurrence, nextOccurrence = string.indexOf('\n', lastOccurrence+1))
            count++;
        
        return count;
    }

    private void determineEndCol()
    {
        endCol = startCol;
        for (int i = 0; i < text.length(); i++)
            if (text.charAt(i) == '\n')
                endCol = 0;
            else
                endCol++;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String value)
    {
        this.filename = value;
    }

    public int getStartLine()
    {
        return this.startLine;
    }

    public void setStartLine(int value)
    {
        this.startLine = value;
    }

    public int getStartCol()
    {
        return this.startCol;
    }

    public void setStartCol(int value)
    {
        this.startCol = value;
    }

    public int getEndLine()
    {
        return this.endLine;
    }

    public void setEndLine(int value)
    {
        this.endLine = value;
    }

    public int getEndCol()
    {
        return this.endCol;
    }

    public void setEndCol(int value)
    {
        this.endCol = value;
    }

    public int getOffset()
    {
        return this.offset;
    }

    public void setOffset(int value)
    {
        this.offset = value;
    }

    public int getLength()
    {
        return this.length;
    }

    public void setLength(int value)
    {
        this.length = value;
    }

    public String getText()
    {
        return this.text;
    }

    public void setText(String value)
    {
        this.text = value;
    }

	public void visitUsing(IPresentationBlockVisitor v)
	{
		v.visitNonTreeToken(this);
	}
}
