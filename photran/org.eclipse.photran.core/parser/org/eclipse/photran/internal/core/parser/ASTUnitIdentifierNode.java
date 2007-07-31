package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTUnitIdentifierNode extends InteriorNode
{
    ASTUnitIdentifierNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTUnitIdentifierNode(this);
    }

    public ASTUFExprNode getUFExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNIT_IDENTIFIER_737)
            return (ASTUFExprNode)getChild(0);
        else
            return null;
    }

    public Token getTAsterisk()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNIT_IDENTIFIER_738)
            return (Token)getChild(0);
        else
            return null;
    }
}
