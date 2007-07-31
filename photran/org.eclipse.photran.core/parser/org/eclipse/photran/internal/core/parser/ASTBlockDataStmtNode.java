package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTBlockDataStmtNode extends InteriorNode
{
    ASTBlockDataStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTBlockDataStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BLOCK_DATA_STMT_912)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.BLOCK_DATA_STMT_913)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTBlockdata()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BLOCK_DATA_STMT_912)
            return (Token)getChild(1);
        else if (getProduction() == Production.BLOCK_DATA_STMT_913)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTBlockDataNameNode getBlockDataName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BLOCK_DATA_STMT_912)
            return (ASTBlockDataNameNode)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BLOCK_DATA_STMT_912)
            return (Token)getChild(3);
        else if (getProduction() == Production.BLOCK_DATA_STMT_913)
            return (Token)getChild(2);
        else
            return null;
    }
}
