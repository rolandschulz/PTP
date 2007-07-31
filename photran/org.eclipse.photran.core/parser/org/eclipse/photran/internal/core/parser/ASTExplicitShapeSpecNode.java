package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTExplicitShapeSpecNode extends InteriorNode
{
    ASTExplicitShapeSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTExplicitShapeSpecNode(this);
    }

    public ASTLowerBoundNode getLowerBound()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXPLICIT_SHAPE_SPEC_301)
            return (ASTLowerBoundNode)getChild(0);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXPLICIT_SHAPE_SPEC_301)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTUpperBoundNode getUpperBound()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXPLICIT_SHAPE_SPEC_301)
            return (ASTUpperBoundNode)getChild(2);
        else if (getProduction() == Production.EXPLICIT_SHAPE_SPEC_302)
            return (ASTUpperBoundNode)getChild(0);
        else
            return null;
    }
}
