package org.eclipse.photran.internal.core.parser; import java.io.PrintStream;


/**
 * Common superclass for <code>Token</code>s and <code>ParseTreeNode</code>s, the two types of
 * objects that can appear in a parse tree
 * 
 * @author joverbey
 */
public abstract class AbstractParseTreeNode
{
    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////
    
    protected static final int INDENT_SIZE = 4;
    
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////
    
    protected ParseTreeNode parent = null;

    ///////////////////////////////////////////////////////////////////////////
    // Accessor/Mutator Methods
    ///////////////////////////////////////////////////////////////////////////

    public ParseTreeNode getParent()
    {
        return parent;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Visitor Support
    ///////////////////////////////////////////////////////////////////////////
    
    public abstract void visitTopDownUsing(ASTVisitor visitor);
    
    public abstract void visitBottomUpUsing(ASTVisitor visitor);
    
    public abstract void visitUsing(ParseTreeVisitor visitor);

    public abstract void visitUsing(GenericParseTreeVisitor visitor);

    ///////////////////////////////////////////////////////////////////////////
    // Debugging Output
    ///////////////////////////////////////////////////////////////////////////
    
    public String toString()
    {
        return toString(0);
    }
    
    public abstract String toString(int indentLevel);

    protected String indent(int numSpaces)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < numSpaces; i++)
            sb.append(' ');
        return sb.toString();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Source Code Reproduction
    ///////////////////////////////////////////////////////////////////////////
    
    public abstract String printOn(PrintStream out, String currentPreprocessorDirective);
}
