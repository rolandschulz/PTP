package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTAndOperandNode extends InteriorNode
{
    ASTAndOperandNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAndOperandNode(this);
    }

    public ASTLevel4ExprNode getLevel4Expr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AND_OPERAND_542)
            return (ASTLevel4ExprNode)getChild(0);
        else if (getProduction() == Production.AND_OPERAND_543)
            return (ASTLevel4ExprNode)getChild(1);
        else
            return null;
    }

    public ASTNotOpNode getNotOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AND_OPERAND_543)
            return (ASTNotOpNode)getChild(0);
        else
            return null;
    }
}
