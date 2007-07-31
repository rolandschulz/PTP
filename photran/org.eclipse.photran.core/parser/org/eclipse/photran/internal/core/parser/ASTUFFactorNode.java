package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTUFFactorNode extends InteriorNode
{
    ASTUFFactorNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTUFFactorNode(this);
    }

    public ASTUFPrimaryNode getUFPrimary()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFFACTOR_523)
            return (ASTUFPrimaryNode)getChild(0);
        else if (getProduction() == Production.UFFACTOR_524)
            return (ASTUFPrimaryNode)getChild(0);
        else
            return null;
    }

    public ASTPowerOpNode getPowerOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFFACTOR_524)
            return (ASTPowerOpNode)getChild(1);
        else
            return null;
    }

    public ASTUFFactorNode getUFFactor()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFFACTOR_524)
            return (ASTUFFactorNode)getChild(2);
        else
            return null;
    }
}
