package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

class ASTObsoleteExecutionPartConstructNode extends InteriorNode
{
    ASTObsoleteExecutionPartConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    public ASTDataStmtNode getDataStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OBSOLETE_EXECUTION_PART_CONSTRUCT_51)
            return (ASTDataStmtNode)getChild(0);
        else
            return null;
    }
}
