package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTWhereConstructNode extends InteriorNode
{
    ASTWhereConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTWhereConstructNode(this);
    }

    public ASTWhereConstructStmtNode getWhereConstructStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_CONSTRUCT_599)
            return (ASTWhereConstructStmtNode)getChild(0);
        else
            return null;
    }

    public ASTWhereRangeNode getWhereRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_CONSTRUCT_599)
            return (ASTWhereRangeNode)getChild(1);
        else
            return null;
    }
}
