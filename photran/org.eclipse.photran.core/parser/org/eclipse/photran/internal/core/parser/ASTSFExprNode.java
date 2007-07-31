package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSFExprNode extends InteriorNode
{
    ASTSFExprNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSFExprNode(this);
    }

    public ASTSFTermNode getSFTerm()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_580)
            return (ASTSFTermNode)getChild(0);
        else
            return null;
    }

    public ASTSignNode getSign()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_581)
            return (ASTSignNode)getChild(0);
        else
            return null;
    }

    public ASTAddOperandNode getAddOperand()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_581)
            return (ASTAddOperandNode)getChild(1);
        else if (getProduction() == Production.SFEXPR_582)
            return (ASTAddOperandNode)getChild(2);
        else
            return null;
    }

    public ASTSFExprNode getSFExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_582)
            return (ASTSFExprNode)getChild(0);
        else
            return null;
    }

    public ASTAddOpNode getAddOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_582)
            return (ASTAddOpNode)getChild(1);
        else
            return null;
    }
}
