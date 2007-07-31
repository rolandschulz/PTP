package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTCaseConstructNode extends InteriorNode
{
    ASTCaseConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTCaseConstructNode(this);
    }

    public ASTSelectCaseStmtNode getSelectCaseStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CASE_CONSTRUCT_675)
            return (ASTSelectCaseStmtNode)getChild(0);
        else
            return null;
    }

    public ASTSelectCaseRangeNode getSelectCaseRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CASE_CONSTRUCT_675)
            return (ASTSelectCaseRangeNode)getChild(1);
        else
            return null;
    }
}
