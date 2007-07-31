package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTExprNode extends InteriorNode
{
    ASTExprNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTExprNode(this);
    }

    public ASTLevel5ExprNode getLevel5Expr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXPR_550)
            return (ASTLevel5ExprNode)getChild(0);
        else if (getProduction() == Production.EXPR_551)
            return (ASTLevel5ExprNode)getChild(2);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXPR_551)
            return (ASTExprNode)getChild(0);
        else
            return null;
    }

    public ASTDefinedBinaryOpNode getDefinedBinaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXPR_551)
            return (ASTDefinedBinaryOpNode)getChild(1);
        else
            return null;
    }
}
