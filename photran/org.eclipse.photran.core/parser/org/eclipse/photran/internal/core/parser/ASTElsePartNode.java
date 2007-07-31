package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTElsePartNode extends InteriorNode
{
    ASTElsePartNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTElsePartNode(this);
    }

    public ASTEndIfStmtNode getEndIfStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_PART_658)
            return (ASTEndIfStmtNode)getChild(0);
        else if (getProduction() == Production.ELSE_PART_659)
            return (ASTEndIfStmtNode)getChild(1);
        else
            return null;
    }

    public ASTConditionalBodyNode getConditionalBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_PART_659)
            return (ASTConditionalBodyNode)getChild(0);
        else
            return null;
    }
}
