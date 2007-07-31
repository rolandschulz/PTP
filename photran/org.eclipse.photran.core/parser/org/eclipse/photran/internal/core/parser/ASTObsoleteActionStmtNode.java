package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

class ASTObsoleteActionStmtNode extends InteriorNode
{
    ASTObsoleteActionStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    public ASTStmtFunctionStmtNode getStmtFunctionStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OBSOLETE_ACTION_STMT_107)
            return (ASTStmtFunctionStmtNode)getChild(0);
        else
            return null;
    }

    public ASTArithmeticIfStmtNode getArithmeticIfStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OBSOLETE_ACTION_STMT_108)
            return (ASTArithmeticIfStmtNode)getChild(0);
        else
            return null;
    }

    public ASTComputedGotoStmtNode getComputedGotoStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OBSOLETE_ACTION_STMT_109)
            return (ASTComputedGotoStmtNode)getChild(0);
        else
            return null;
    }
}
