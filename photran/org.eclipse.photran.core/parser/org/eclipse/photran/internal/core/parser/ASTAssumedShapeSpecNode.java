package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTAssumedShapeSpecNode extends InteriorNode
{
    ASTAssumedShapeSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAssumedShapeSpecNode(this);
    }

    public ASTLowerBoundNode getLowerBound()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSUMED_SHAPE_SPEC_305)
            return (ASTLowerBoundNode)getChild(0);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSUMED_SHAPE_SPEC_305)
            return (Token)getChild(1);
        else if (getProduction() == Production.ASSUMED_SHAPE_SPEC_306)
            return (Token)getChild(0);
        else
            return null;
    }
}
