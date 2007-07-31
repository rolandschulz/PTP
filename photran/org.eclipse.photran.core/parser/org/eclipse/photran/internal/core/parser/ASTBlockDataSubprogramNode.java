package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTBlockDataSubprogramNode extends InteriorNode
{
    ASTBlockDataSubprogramNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTBlockDataSubprogramNode(this);
    }

    public ASTBlockDataStmtNode getBlockDataStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BLOCK_DATA_SUBPROGRAM_32)
            return (ASTBlockDataStmtNode)getChild(0);
        else if (getProduction() == Production.BLOCK_DATA_SUBPROGRAM_33)
            return (ASTBlockDataStmtNode)getChild(0);
        else
            return null;
    }

    public ASTBlockDataBodyNode getBlockDataBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BLOCK_DATA_SUBPROGRAM_32)
            return (ASTBlockDataBodyNode)getChild(1);
        else
            return null;
    }

    public ASTEndBlockDataStmtNode getEndBlockDataStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BLOCK_DATA_SUBPROGRAM_32)
            return (ASTEndBlockDataStmtNode)getChild(2);
        else if (getProduction() == Production.BLOCK_DATA_SUBPROGRAM_33)
            return (ASTEndBlockDataStmtNode)getChild(1);
        else
            return null;
    }
}
