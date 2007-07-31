package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTElsewhereConstructNode extends InteriorNode
{
    ASTElsewhereConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTElsewhereConstructNode(this);
    }

    public ASTElsewhereStmtNode getElsewhereStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSEWHERE_CONSTRUCT_607)
            return (ASTElsewhereStmtNode)getChild(0);
        else
            return null;
    }

    public ASTElsewherePartNode getElsewherePart()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSEWHERE_CONSTRUCT_607)
            return (ASTElsewherePartNode)getChild(1);
        else
            return null;
    }
}
