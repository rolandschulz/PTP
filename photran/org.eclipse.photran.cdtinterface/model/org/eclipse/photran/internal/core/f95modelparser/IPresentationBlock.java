package org.eclipse.photran.internal.core.f95modelparser;

/**
 * Common super-interface for <code>Token</code>s and <code>NonTreeToken</code>s, essentially,
 * anything that will be printed by the <code>SourcePrinter</code> when source code is reproduced
 * from a parse tree and <code>Presentation</code>.
 * 
 * @author joverbey
 */
public interface IPresentationBlock
{
    /**
     * Returns the filename in which the token occurred
     */
    public abstract String getFilename();

    /**
     * Sets the filename in which the token occurred
     */
    public abstract void setFilename(String value);

    /**
     * Returns the line number on which the token starts (1=first line, 2=second, etc.)
     */
    public abstract int getStartLine();

    /**
     * Sets the line number on which the token starts (1=first line, 2=second, etc.)
     */
    public abstract void setStartLine(int value);

    /**
     * Returns the column number on which the token starts (1=first column, 2=second, etc.)
     */
    public abstract int getStartCol();

    /**
     * Sets the column number on which the token starts (1=first column, 2=second, etc.)
     */
    public abstract void setStartCol(int value);

    /**
     * Returns the line number on which the token ends (1=first line, 2=second, etc.)
     */
    public abstract int getEndLine();

    /**
     * Sets the line number on which the token ends (1=first line, 2=second, etc.)
     */
    public abstract void setEndLine(int value);

    /**
     * Returns the column number on which the token ends (1=first column, 2=second, etc.)
     */
    public abstract int getEndCol();

    /**
     * Sets the column number on which the token ends (1=first column, 2=second, etc.)
     */
    public abstract void setEndCol(int value);

    /**
     * Returns the start of the token, as a character offset in the file 0=first character,
     * 1=second, etc.
     */
    public abstract int getOffset();

    /**
     * Sets the start of the token, as a character offset in the file 0=first character, 1=second,
     * etc.
     */
    public abstract void setOffset(int value);

    /**
     * Returns the length of the token text, in characters
     */
    public abstract int getLength();

    /**
     * Sets the length of the token text, in characters
     */
    public abstract void setLength(int value);

    /**
     * Returns the token text
     */
    public abstract String getText();

    /**
     * Sets the token text
     */
    public abstract void setText(String value);
    
    /**
     * Double-dispatch method for visiting all types of <code>IPresentationBlocks</code>
     */
    public abstract void visitUsing(IPresentationBlockVisitor v);
}