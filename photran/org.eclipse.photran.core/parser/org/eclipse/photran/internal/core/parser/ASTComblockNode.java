package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTComblockNode extends InteriorNode
{
    ASTComblockNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTComblockNode(this);
    }

    public Token getTSlash()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMBLOCK_415)
            return (Token)getChild(0);
        else if (getProduction() == Production.COMBLOCK_416)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTSlash2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMBLOCK_415)
            return (Token)getChild(1);
        else if (getProduction() == Production.COMBLOCK_416)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTCommonBlockNameNode getCommonBlockName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMBLOCK_416)
            return (ASTCommonBlockNameNode)getChild(1);
        else
            return null;
    }
}
