package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTElsewherePartNode extends InteriorNode
{
    ASTElsewherePartNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTElsewherePartNode(this);
    }

    public ASTEndWhereStmtNode getEndWhereStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSEWHERE_PART_608)
            return (ASTEndWhereStmtNode)getChild(0);
        else if (getProduction() == Production.ELSEWHERE_PART_609)
            return (ASTEndWhereStmtNode)getChild(1);
        else
            return null;
    }

    public ASTWhereBodyConstructBlockNode getWhereBodyConstructBlock()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSEWHERE_PART_609)
            return (ASTWhereBodyConstructBlockNode)getChild(0);
        else
            return null;
    }
}
