package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTElseStmtNode extends InteriorNode
{
    ASTElseStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTElseStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_STMT_668)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ELSE_STMT_669)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTElse()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_STMT_668)
            return (Token)getChild(1);
        else if (getProduction() == Production.ELSE_STMT_669)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_STMT_668)
            return (Token)getChild(2);
        else if (getProduction() == Production.ELSE_STMT_669)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTEndNameNode getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_STMT_669)
            return (ASTEndNameNode)getChild(2);
        else
            return null;
    }
}
