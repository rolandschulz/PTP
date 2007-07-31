package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTLevel3ExprNode extends InteriorNode
{
    ASTLevel3ExprNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTLevel3ExprNode(this);
    }

    public ASTLevel2ExprNode getLevel2Expr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LEVEL_3_EXPR_536)
            return (ASTLevel2ExprNode)getChild(0);
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return (ASTLevel2ExprNode)getChild(2);
        else
            return null;
    }

    public ASTLevel3ExprNode getLevel3Expr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LEVEL_3_EXPR_537)
            return (ASTLevel3ExprNode)getChild(0);
        else
            return null;
    }

    public ASTConcatOpNode getConcatOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LEVEL_3_EXPR_537)
            return (ASTConcatOpNode)getChild(1);
        else
            return null;
    }
}
