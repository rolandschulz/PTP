package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTOrOperandNode extends InteriorNode
{
    ASTOrOperandNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTOrOperandNode(this);
    }

    public ASTAndOperandNode getAndOperand()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OR_OPERAND_544)
            return (ASTAndOperandNode)getChild(0);
        else if (getProduction() == Production.OR_OPERAND_545)
            return (ASTAndOperandNode)getChild(2);
        else
            return null;
    }

    public ASTOrOperandNode getOrOperand()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OR_OPERAND_545)
            return (ASTOrOperandNode)getChild(0);
        else
            return null;
    }

    public ASTAndOpNode getAndOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OR_OPERAND_545)
            return (ASTAndOpNode)getChild(1);
        else
            return null;
    }
}
