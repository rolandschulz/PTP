package org.eclipse.photran.internal.core.parser; import org.eclipse.photran.internal.core.lexer.*;

/**
 * Represents a Visitor for a parse tree, albeit one that cares more about whether it's visiting a
 * token versus an internal node than exactly what type of internal nodes it's stepping through.
 * 
 * See also <code>ParseTreeVisitor</code>, which is more appropriate when the types of the
 * internal nodes are what is important.
 * 
 * @author joverbey
 */
public abstract class GenericParseTreeVisitor
{
    public void visitParseTreeNode(ParseTreeNode node)
    {
        ;
    }

    public void visitToken(Token token)
    {
        ;
    }

    public void preparingToVisitChildrenOf(ParseTreeNode node)
    {
        ;
    }

    public void doneVisitingChildrenOf(ParseTreeNode node)
    {
        ;
    }
}
