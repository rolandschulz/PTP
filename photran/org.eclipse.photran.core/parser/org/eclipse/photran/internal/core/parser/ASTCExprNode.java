package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTCExprNode extends InteriorNode
{
    ASTCExprNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTCExprNode(this);
    }

    public ASTCPrimaryNode getCPrimary()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_538)
            return (ASTCPrimaryNode)getChild(0);
        else if (getProduction() == Production.CEXPR_539)
            return (ASTCPrimaryNode)getChild(2);
        else
            return null;
    }

    public ASTCExprNode getCExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return (ASTCExprNode)getChild(0);
        else
            return null;
    }

    public ASTConcatOpNode getConcatOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return (ASTConcatOpNode)getChild(1);
        else
            return null;
    }
}
