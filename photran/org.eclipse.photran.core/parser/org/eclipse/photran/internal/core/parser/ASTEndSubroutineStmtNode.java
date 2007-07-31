package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTEndSubroutineStmtNode extends InteriorNode
{
    ASTEndSubroutineStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTEndSubroutineStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SUBROUTINE_STMT_1004)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1005)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1006)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1007)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1008)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTEnd()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SUBROUTINE_STMT_1004)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1007)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1008)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SUBROUTINE_STMT_1004)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1005)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1006)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1007)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1008)
            return (Token)getChild(4);
        else
            return null;
    }

    public Token getTEndsubroutine()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SUBROUTINE_STMT_1005)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1006)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTEndNameNode getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SUBROUTINE_STMT_1006)
            return (ASTEndNameNode)getChild(2);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1008)
            return (ASTEndNameNode)getChild(3);
        else
            return null;
    }

    public Token getTSubroutine()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SUBROUTINE_STMT_1007)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1008)
            return (Token)getChild(2);
        else
            return null;
    }
}
