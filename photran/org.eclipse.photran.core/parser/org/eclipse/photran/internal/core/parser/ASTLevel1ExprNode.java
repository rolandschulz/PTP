package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTLevel1ExprNode extends InteriorNode
{
    ASTLevel1ExprNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTLevel1ExprNode(this);
    }

    public ASTPrimaryNode getPrimary()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LEVEL_1_EXPR_519)
            return (ASTPrimaryNode)getChild(0);
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return (ASTPrimaryNode)getChild(1);
        else
            return null;
    }

    public ASTDefinedUnaryOpNode getDefinedUnaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LEVEL_1_EXPR_520)
            return (ASTDefinedUnaryOpNode)getChild(0);
        else
            return null;
    }
}
