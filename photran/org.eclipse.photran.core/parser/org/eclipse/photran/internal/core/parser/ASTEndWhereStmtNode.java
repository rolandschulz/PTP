package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTEndWhereStmtNode extends InteriorNode
{
    ASTEndWhereStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTEndWhereStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_WHERE_STMT_622)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_WHERE_STMT_623)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_WHERE_STMT_624)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_WHERE_STMT_625)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTEndwhere()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_WHERE_STMT_622)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_WHERE_STMT_623)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_WHERE_STMT_622)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_WHERE_STMT_623)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_WHERE_STMT_624)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_WHERE_STMT_625)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTEndNameNode getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_WHERE_STMT_623)
            return (ASTEndNameNode)getChild(2);
        else if (getProduction() == Production.END_WHERE_STMT_625)
            return (ASTEndNameNode)getChild(3);
        else
            return null;
    }

    public Token getTEnd()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_WHERE_STMT_624)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_WHERE_STMT_625)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTWhere()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_WHERE_STMT_624)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_WHERE_STMT_625)
            return (Token)getChild(2);
        else
            return null;
    }
}
