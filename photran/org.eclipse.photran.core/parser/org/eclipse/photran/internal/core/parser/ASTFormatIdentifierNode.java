package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTFormatIdentifierNode extends InteriorNode
{
    ASTFormatIdentifierNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTFormatIdentifierNode(this);
    }

    public ASTLblRefNode getLblRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_IDENTIFIER_801)
            return (ASTLblRefNode)getChild(0);
        else
            return null;
    }

    public ASTCExprNode getCExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_IDENTIFIER_802)
            return (ASTCExprNode)getChild(0);
        else
            return null;
    }

    public Token getTAsterisk()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_IDENTIFIER_803)
            return (Token)getChild(0);
        else
            return null;
    }
}
