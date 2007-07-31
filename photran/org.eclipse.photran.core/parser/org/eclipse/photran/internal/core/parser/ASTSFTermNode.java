package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSFTermNode extends InteriorNode
{
    ASTSFTermNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSFTermNode(this);
    }

    public ASTSFFactorNode getSFFactor()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFTERM_583)
            return (ASTSFFactorNode)getChild(0);
        else
            return null;
    }

    public ASTSFTermNode getSFTerm()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFTERM_584)
            return (ASTSFTermNode)getChild(0);
        else
            return null;
    }

    public ASTMultOpNode getMultOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFTERM_584)
            return (ASTMultOpNode)getChild(1);
        else
            return null;
    }

    public ASTMultOperandNode getMultOperand()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFTERM_584)
            return (ASTMultOperandNode)getChild(2);
        else
            return null;
    }
}
