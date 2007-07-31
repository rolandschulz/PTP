package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSubroutineSubprogramNode extends InteriorNode
{
    ASTSubroutineSubprogramNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSubroutineSubprogramNode(this);
    }

    public ASTSubroutineStmtNode getSubroutineStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_SUBPROGRAM_21)
            return (ASTSubroutineStmtNode)getChild(0);
        else
            return null;
    }

    public ASTBodyNode getBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_SUBPROGRAM_21)
            return (ASTBodyNode)getChild(1, 0);
        else
            return null;
    }

    public ASTEndSubroutineStmtNode getEndSubroutineStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_SUBPROGRAM_21)
            return (ASTEndSubroutineStmtNode)getChild(1, 1);
        else if (getProduction() == Production.SUBROUTINE_SUBPROGRAM_21)
            return (ASTEndSubroutineStmtNode)getChild(1, 0);
        else if (getProduction() == Production.SUBROUTINE_SUBPROGRAM_21)
            return (ASTEndSubroutineStmtNode)getChild(1, 1);
        else
            return null;
    }

    public ASTBodyPlusInternalsNode getBodyPlusInternals()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_SUBPROGRAM_21)
            return (ASTBodyPlusInternalsNode)getChild(1, 0);
        else
            return null;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.SUBROUTINE_SUBPROGRAM_21 && index == 1)
            return true;
        else
            return false;
    }
}
