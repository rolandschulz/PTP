package org.eclipse.photran.internal.core.parser; import org.eclipse.photran.internal.core.lexer.*;

import java.lang.ref.WeakReference;

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

    protected WeakReference/*<ParseTreeNode>*/ parentRef = null;

    public ParseTreeNode getParent()
    {
        return parentRef == null ? null : (ParseTreeNode)parentRef.get();
    }

    protected String indent(int numSpaces)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < numSpaces; i++)
            sb.append(' ');
        return sb.toString();
    }
}
