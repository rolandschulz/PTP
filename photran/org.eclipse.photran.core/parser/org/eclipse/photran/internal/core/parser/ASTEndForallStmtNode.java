package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTEndForallStmtNode extends InteriorNode
{
    ASTEndForallStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTEndForallStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_FORALL_STMT_643)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_FORALL_STMT_644)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_FORALL_STMT_645)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_FORALL_STMT_646)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTEnd()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_FORALL_STMT_643)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_FORALL_STMT_644)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTForall()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_FORALL_STMT_643)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_FORALL_STMT_644)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_FORALL_STMT_643)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_FORALL_STMT_644)
            return (Token)getChild(4);
        else if (getProduction() == Production.END_FORALL_STMT_645)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_FORALL_STMT_646)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTEndNameNode getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_FORALL_STMT_644)
            return (ASTEndNameNode)getChild(3);
        else if (getProduction() == Production.END_FORALL_STMT_646)
            return (ASTEndNameNode)getChild(2);
        else
            return null;
    }

    public Token getTEndforall()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_FORALL_STMT_645)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_FORALL_STMT_646)
            return (Token)getChild(1);
        else
            return null;
    }
}
