package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

class ASTFunctionRangeNode extends InteriorNode
{
    ASTFunctionRangeNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    public ASTBodyNode getBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_RANGE_18)
            return (ASTBodyNode)getChild(0);
        else
            return null;
    }

    public ASTEndFunctionStmtNode getEndFunctionStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_RANGE_18)
            return (ASTEndFunctionStmtNode)getChild(1);
        else if (getProduction() == Production.FUNCTION_RANGE_19)
            return (ASTEndFunctionStmtNode)getChild(0);
        else if (getProduction() == Production.FUNCTION_RANGE_20)
            return (ASTEndFunctionStmtNode)getChild(1);
        else
            return null;
    }

    public ASTBodyPlusInternalsNode getBodyPlusInternals()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_RANGE_20)
            return (ASTBodyPlusInternalsNode)getChild(0);
        else
            return null;
    }
}
