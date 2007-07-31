package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTElseIfConstructNode extends InteriorNode
{
    ASTElseIfConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTElseIfConstructNode(this);
    }

    public ASTElseIfStmtNode getElseIfStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_IF_CONSTRUCT_656)
            return (ASTElseIfStmtNode)getChild(0);
        else
            return null;
    }

    public ASTThenPartNode getThenPart()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_IF_CONSTRUCT_656)
            return (ASTThenPartNode)getChild(1);
        else
            return null;
    }
}
