package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTMainProgramNode extends InteriorNode
{
    ASTMainProgramNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTMainProgramNode(this);
    }

    public ASTProgramStmtNode getProgramStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MAIN_PROGRAM_9)
            return (ASTProgramStmtNode)getChild(0);
        else
            return null;
    }

    public ASTBodyNode getBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MAIN_PROGRAM_8)
            return (ASTBodyNode)getChild(0, 0);
        else if (getProduction() == Production.MAIN_PROGRAM_9)
            return (ASTBodyNode)getChild(1, 0);
        else
            return null;
    }

    public ASTEndProgramStmtNode getEndProgramStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MAIN_PROGRAM_8)
            return (ASTEndProgramStmtNode)getChild(0, 1);
        else if (getProduction() == Production.MAIN_PROGRAM_8)
            return (ASTEndProgramStmtNode)getChild(0, 1);
        else if (getProduction() == Production.MAIN_PROGRAM_8)
            return (ASTEndProgramStmtNode)getChild(0, 0);
        else if (getProduction() == Production.MAIN_PROGRAM_9)
            return (ASTEndProgramStmtNode)getChild(1, 1);
        else if (getProduction() == Production.MAIN_PROGRAM_9)
            return (ASTEndProgramStmtNode)getChild(1, 1);
        else if (getProduction() == Production.MAIN_PROGRAM_9)
            return (ASTEndProgramStmtNode)getChild(1, 0);
        else
            return null;
    }

    public ASTBodyPlusInternalsNode getBodyPlusInternals()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MAIN_PROGRAM_8)
            return (ASTBodyPlusInternalsNode)getChild(0, 0);
        else if (getProduction() == Production.MAIN_PROGRAM_9)
            return (ASTBodyPlusInternalsNode)getChild(1, 0);
        else
            return null;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.MAIN_PROGRAM_8 && index == 0)
            return true;
        else if (getProduction() == Production.MAIN_PROGRAM_9 && index == 1)
            return true;
        else
            return false;
    }
}
