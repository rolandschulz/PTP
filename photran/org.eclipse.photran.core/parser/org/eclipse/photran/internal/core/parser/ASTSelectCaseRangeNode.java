package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSelectCaseRangeNode extends InteriorNode
{
    ASTSelectCaseRangeNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSelectCaseRangeNode(this);
    }

    public ASTSelectCaseBodyNode getSelectCaseBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_RANGE_676)
            return (ASTSelectCaseBodyNode)getChild(0);
        else
            return null;
    }

    public ASTEndSelectStmtNode getEndSelectStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_RANGE_676)
            return (ASTEndSelectStmtNode)getChild(1);
        else if (getProduction() == Production.SELECT_CASE_RANGE_677)
            return (ASTEndSelectStmtNode)getChild(0);
        else
            return null;
    }
}
