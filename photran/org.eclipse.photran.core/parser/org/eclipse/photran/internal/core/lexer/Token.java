package org.eclipse.photran.internal.core.lexer;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.photran.core.util.ObjectMap;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.AbstractParseTreeNode;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.ParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.ParserSymbol;
import org.eclipse.photran.internal.core.parser.Terminal;

/**
 * Enumerates the terminal symbols in the grammar being parsed
 */
public class Token extends AbstractParseTreeNode implements ParserSymbol, IAdaptable
{
    /**
     * The Terminal that this token is an instance of
     */
    protected Terminal terminal = null;

    /**
     * The token text
     */
    protected String text = "";

    /**
     * Whitespace and whitetext appearing before this token that should be associated with this token
     */
    protected String whiteBefore = "";

    /**
     * Whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    protected String whiteAfter = "";
    
    protected Map/*<Class, Object>*/ adapters = new ObjectMap/*<Class, Object>*/();

    public Token()
    {
    }

    public Token(Terminal terminal, String whiteBefore, String tokenText, String whiteAfter)
    {
        this.terminal    = terminal;
        this.whiteBefore = whiteBefore == null ? "" : whiteBefore;
        this.text   = tokenText   == null ? "" : tokenText;
        this.whiteAfter  = whiteAfter  == null ? "" : whiteAfter;
    }

    /**
     * Returns a string describing the token
     */
    public String getDescription() { return terminal.getDescription() + ": \"" + text + "\""; }

    /**
     * See AbstractParseTreeNode
     */
    public void visitUsing(ParseTreeVisitor visitor) { ; }

    /**
     * See AbstractParseTreeNode
     */
    public void visitUsing(GenericParseTreeVisitor visitor) { visitor.visitToken(this); }

    /**
     * See AbstractParseTreeNode
     */
    public void visitTopDownUsing(ASTVisitor visitor) { visitor.visitToken(this); }

    /**
     * See AbstractParseTreeNode
     */
    public void visitBottomUpUsing(ASTVisitor visitor) { visitor.visitToken(this); }

    /**
     * See AbstractParseTreeNode
     */
    public String toString(int numSpaces) { return indent(numSpaces) + getDescription() + "\n"; }
    
    public String toString() { return getDescription(); }
    
    ///////////////////////////////////////////////////////////////////////////
    // Accessor/mutator methods
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
    
    public Object getAdapter(Class adapter)
    {
        return adapters.get(adapter);
    }
    
    public void setAdapter(Class adapterClass, Object adaptedObject)
    {
        adapters.put(adapterClass, adaptedObject);
    }

    public Map/*<Class, Object>*/ getAllAdapters()
    {
        return adapters;
    }

    public String getCompleteText()
    {
        return whiteBefore + text + whiteAfter;
    }
}
