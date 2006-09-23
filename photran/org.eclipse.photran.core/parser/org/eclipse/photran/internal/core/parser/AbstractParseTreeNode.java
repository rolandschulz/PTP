package org.eclipse.photran.internal.core.parser; import org.eclipse.photran.internal.core.lexer.*;

/**
 * Common superclass for <code>Token</code>s and <code>ParseTreeNode</code>s, the two types of
 * objects that can appear in a parse tree
 * 
 * @author joverbey
 */
public abstract class AbstractParseTreeNode
{
    public abstract void visitTopDownUsing(ASTVisitor visitor);
    
    public abstract void visitBottomUpUsing(ASTVisitor visitor);
    
    public abstract void visitUsing(ParseTreeVisitor visitor);

    public abstract void visitUsing(GenericParseTreeVisitor visitor);

    public abstract String toString(int indentLevel);

    protected ParseTreeNode parent = null;

    public ParseTreeNode getParent()
    {
        return parent;
    }

    protected String indent(int numSpaces)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < numSpaces; i++)
            sb.append(' ');
        return sb.toString();
    }
}
