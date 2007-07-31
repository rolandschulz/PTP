package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

class ASTSubroutineRangeNode extends InteriorNode
{
    ASTSubroutineRangeNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    public ASTBodyNode getBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_RANGE_22)
            return (ASTBodyNode)getChild(0);
        else
            return null;
    }

    public ASTEndSubroutineStmtNode getEndSubroutineStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_RANGE_22)
            return (ASTEndSubroutineStmtNode)getChild(1);
        else if (getProduction() == Production.SUBROUTINE_RANGE_23)
            return (ASTEndSubroutineStmtNode)getChild(0);
        else if (getProduction() == Production.SUBROUTINE_RANGE_24)
            return (ASTEndSubroutineStmtNode)getChild(1);
        else
            return null;
    }

    public ASTBodyPlusInternalsNode getBodyPlusInternals()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_RANGE_24)
            return (ASTBodyPlusInternalsNode)getChild(0);
        else
            return null;
    }
}
