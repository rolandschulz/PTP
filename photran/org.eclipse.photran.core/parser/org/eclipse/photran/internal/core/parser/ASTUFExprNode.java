package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTUFExprNode extends InteriorNode
{
    ASTUFExprNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTUFExprNode(this);
    }

    public ASTUFTermNode getUFTerm()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFEXPR_533)
            return (ASTUFTermNode)getChild(0);
        else if (getProduction() == Production.UFEXPR_534)
            return (ASTUFTermNode)getChild(1);
        else if (getProduction() == Production.UFEXPR_535)
            return (ASTUFTermNode)getChild(2);
        else
            return null;
    }

    public ASTSignNode getSign()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFEXPR_534)
            return (ASTSignNode)getChild(0);
        else
            return null;
    }

    public ASTUFExprNode getUFExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFEXPR_535)
            return (ASTUFExprNode)getChild(0);
        else
            return null;
    }

    public ASTAddOpNode getAddOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFEXPR_535)
            return (ASTAddOpNode)getChild(1);
        else
            return null;
    }
}
