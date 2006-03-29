package org.eclipse.photran.internal.core.f95modelparser;


/**
 * Enumerates the terminal symbols in the grammar being parsed
 */
public class Token implements ParserSymbol, IPresentationBlock
{
    /**
     * The Terminal that this token is an instance of
     */
    private Terminal terminal;

    /**
     * Returns the Terminal that this token is an instance of
     */
    public Terminal getTerminal() { return terminal; }

    /**
     * Sets the Terminal that this token is an instance of
     */
    public void setTerminal(Terminal value) { terminal = value; }

    /**
     * The filename in which the token occurred
     */
    private String filename;

    /**
     * Returns the filename in which the token occurred
     */
    public String getFilename() { return filename; }

    /**
     * Sets the filename in which the token occurred
     */
    public void setFilename(String value) { filename = value; }

    /**
     * The line number on which the token starts (1=first line, 2=second, etc.)
     */
    private int startLine;

    /**
     * Returns the line number on which the token starts (1=first line, 2=second, etc.)
     */
    public int getStartLine() { return startLine; }

    /**
     * Sets the line number on which the token starts (1=first line, 2=second, etc.)
     */
    public void setStartLine(int value) { startLine = value; }

    /**
     * The column number on which the token starts (1=first column, 2=second, etc.)
     */
    private int startCol;

    /**
     * Returns the column number on which the token starts (1=first column, 2=second, etc.)
     */
    public int getStartCol() { return startCol; }

    /**
     * Sets the column number on which the token starts (1=first column, 2=second, etc.)
     */
    public void setStartCol(int value) { startCol = value; }

    /**
     * The line number on which the token ends (1=first line, 2=second, etc.)
     */
    private int endLine;

    /**
     * Returns the line number on which the token ends (1=first line, 2=second, etc.)
     */
    public int getEndLine() { return endLine; }

    /**
     * Sets the line number on which the token ends (1=first line, 2=second, etc.)
     */
    public void setEndLine(int value) { endLine = value; }

    /**
     * The column number on which the token ends (1=first column, 2=second, etc.)
     */
    private int endCol;

    /**
     * Returns the column number on which the token ends (1=first column, 2=second, etc.)
     */
    public int getEndCol() { return endCol; }

    /**
     * Sets the column number on which the token ends (1=first column, 2=second, etc.)
     */
    public void setEndCol(int value) { endCol = value; }

    /**
     * The start of the token, as a character offset in the file
     * 0=first character, 1=second, etc.
     */
    private int offset;

    /**
     * Returns the start of the token, as a character offset in the file
     * 0=first character, 1=second, etc.
     */
    public int getOffset() { return offset; }

    /**
     * Sets the start of the token, as a character offset in the file
     * 0=first character, 1=second, etc.
     */
    public void setOffset(int value) { offset = value; }

    /**
     * The length of the token text, in characters
     */
    private int length;

    /**
     * Returns the length of the token text, in characters
     */
    public int getLength() { return length; }

    /**
     * Sets the length of the token text, in characters
     */
    public void setLength(int value) { length = value; }

    /**
     * The token text
     */
    private String text;

    /**
     * Returns the token text
     */
    public String getText() { return text; }

    /**
     * Sets the token text
     */
    public void setText(String value) { text = value; }

    /**
     * Returns a string describing the token
     */
    public String getDescription() { return terminal.getDescription() + ": \"" + text + "\""; }
    
    public String toString() { return getDescription(); }

    /**
     * See AbstractParseTreeNode
     */
	public void visitUsing(IPresentationBlockVisitor v)
	{
		v.visitToken(this);
	}
}
