package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTOnlyNode extends InteriorNode
{
    ASTOnlyNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTOnlyNode(this);
    }

    public ASTGenericSpecNode getGenericSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ONLY_909)
            return (ASTGenericSpecNode)getChild(0);
        else
            return null;
    }

    public Token getTIdent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ONLY_910)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTEqgreaterthan()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ONLY_910)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTUseNameNode getUseName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ONLY_910)
            return (ASTUseNameNode)getChild(2);
        else if (getProduction() == Production.ONLY_911)
            return (ASTUseNameNode)getChild(0);
        else
            return null;
    }
}
