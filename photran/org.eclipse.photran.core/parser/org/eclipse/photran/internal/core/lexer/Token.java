package org.eclipse.photran.internal.core.lexer;

import java.io.PrintStream;

import org.eclipse.photran.core.util.OffsetLength;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.AbstractParseTreeNode;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.ParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.ParserSymbol;
import org.eclipse.photran.internal.core.parser.Terminal;

/**
 * Enumerates the terminal symbols in the grammar being parsed
 */
public class Token extends AbstractParseTreeNode implements ParserSymbol
{
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * The Terminal that this token is an instance of
     */
    protected Terminal terminal = null;

    /**
     * Whitespace and whitetext appearing before this token that should be associated with this token
     */
    protected String whiteBefore = "";

    /**
     * The token text
     */
    protected String text = "";

    /**
     * Whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    protected String whiteAfter = "";
    
    ///////////////////////////////////////////////////////////////////////////
    // Additional Fields - Not updated when refactoring
    ///////////////////////////////////////////////////////////////////////////
    
    protected int line = -1, col = -1, offset = -1, length = -1;
    protected Object binding = null;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////
    
    public Token(Terminal terminal, String whiteBefore, String tokenText, String whiteAfter)
    {
        this.terminal    = terminal;
        this.whiteBefore = whiteBefore == null ? "" : whiteBefore;
        this.text   = tokenText   == null ? "" : tokenText;
        this.whiteAfter  = whiteAfter  == null ? "" : whiteAfter;
    }
    
    public Token(Terminal terminal, String tokenText)
    {
        this(terminal, null, tokenText, null);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Accessor/Mutator Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the Terminal that this token is an instance of
     */
    public Terminal getTerminal() { return terminal; }

    /**
     * Sets the Terminal that this token is an instance of
     */
    public void setTerminal(Terminal value) { terminal = value; }

    /**
     * Returns the token text
     */
    public String getText() { return text; }

    /**
     * Sets the token text
     */
    public void setText(String value) { text = value == null ? "" : value; }

    /**
     * Returns whitespace and whitetext appearing before this token that should be associated with this token
     */
    public String getWhiteBefore() { return whiteBefore; }

    /**
     * Sets whitespace and whitetext appearing before this token that should be associated with this token
     */
    public void setWhiteBefore(String value) { whiteBefore = value == null ? "" : value; }

    /**
     * Returns whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    public String getWhiteAfter() { return whiteAfter; }

    /**
     * Sets whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    public void setWhiteAfter(String value) { whiteAfter = value == null ? "" : value; }

    public int getLine()
    {
        return line;
    }

    public void setLine(int line)
    {
        this.line = line;
    }

    public int getCol()
    {
        return col;
    }

    public void setCol(int col)
    {
        this.col = col;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }
    
    public Object getBinding()
    {
        return binding;
    }
    
    public void setBinding(Object binding)
    {
        this.binding = binding;
    }

    public boolean contains(OffsetLength other)
    {
        return OffsetLength.contains(offset, length, other);
    }
    
    public boolean isOnOrAfter(int targetOffset)
    {
        return offset >= targetOffset;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Visitor Support
    ///////////////////////////////////////////////////////////////////////////

    public void visitTopDownUsing(ASTVisitor visitor) { visitor.visitToken(this); }

    public void visitBottomUpUsing(ASTVisitor visitor) { visitor.visitToken(this); }

    public void visitUsing(ParseTreeVisitor visitor) { ; }

    public void visitUsing(GenericParseTreeVisitor visitor) { visitor.visitToken(this); }

    ///////////////////////////////////////////////////////////////////////////
    // Debugging Output
    ///////////////////////////////////////////////////////////////////////////
    
    public String toString(int numSpaces) { return indent(numSpaces) + getDescription() + "\n"; }

    /**
     * Returns a string describing the token
     */
    public String getDescription() { return terminal.getDescription() + ": \"" + text + "\""; }
    
    ///////////////////////////////////////////////////////////////////////////
    // Source Code Reproduction
    ///////////////////////////////////////////////////////////////////////////
    
    public void printOn(PrintStream out)
    {
        out.print(whiteBefore);
        out.print(text);
        out.print(whiteAfter);
    }
}
