package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTMaskedElsewhereConstructNode extends InteriorNode
{
    ASTMaskedElsewhereConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTMaskedElsewhereConstructNode(this);
    }

    public ASTMaskedElsewhereStmtNode getMaskedElsewhereStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSEWHERE_CONSTRUCT_606)
            return (ASTMaskedElsewhereStmtNode)getChild(0);
        else
            return null;
    }

    public ASTWhereRangeNode getWhereRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSEWHERE_CONSTRUCT_606)
            return (ASTWhereRangeNode)getChild(1);
        else
            return null;
    }
}
