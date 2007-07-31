package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSavedCommonBlockNode extends InteriorNode
{
    ASTSavedCommonBlockNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSavedCommonBlockNode(this);
    }

    public Token getTSlash()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SAVED_COMMON_BLOCK_338)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTCommonBlockNameNode getCommonBlockName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SAVED_COMMON_BLOCK_338)
            return (ASTCommonBlockNameNode)getChild(1);
        else
            return null;
    }

    public Token getTSlash2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SAVED_COMMON_BLOCK_338)
            return (Token)getChild(2);
        else
            return null;
    }
}
