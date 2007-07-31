package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTActionStmtNode extends InteriorNode
{
    ASTActionStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTActionStmtNode(this);
    }

    public ASTAllocateStmtNode getAllocateStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_83)
            return (ASTAllocateStmtNode)getChild(0);
        else
            return null;
    }

    public ASTAssignmentStmtNode getAssignmentStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_84)
            return (ASTAssignmentStmtNode)getChild(0);
        else
            return null;
    }

    public ASTBackspaceStmtNode getBackspaceStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_85)
            return (ASTBackspaceStmtNode)getChild(0);
        else
            return null;
    }

    public ASTCallStmtNode getCallStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_86)
            return (ASTCallStmtNode)getChild(0);
        else
            return null;
    }

    public ASTCloseStmtNode getCloseStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_87)
            return (ASTCloseStmtNode)getChild(0);
        else
            return null;
    }

    public ASTContinueStmtNode getContinueStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_88)
            return (ASTContinueStmtNode)getChild(0);
        else
            return null;
    }

    public ASTCycleStmtNode getCycleStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_89)
            return (ASTCycleStmtNode)getChild(0);
        else
            return null;
    }

    public ASTDeallocateStmtNode getDeallocateStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_90)
            return (ASTDeallocateStmtNode)getChild(0);
        else
            return null;
    }

    public ASTEndfileStmtNode getEndfileStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_91)
            return (ASTEndfileStmtNode)getChild(0);
        else
            return null;
    }

    public ASTExitStmtNode getExitStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_92)
            return (ASTExitStmtNode)getChild(0);
        else
            return null;
    }

    public ASTForallStmtNode getForallStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_93)
            return (ASTForallStmtNode)getChild(0);
        else
            return null;
    }

    public ASTGotoStmtNode getGotoStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_94)
            return (ASTGotoStmtNode)getChild(0);
        else
            return null;
    }

    public ASTIfStmtNode getIfStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_95)
            return (ASTIfStmtNode)getChild(0);
        else
            return null;
    }

    public ASTInquireStmtNode getInquireStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_96)
            return (ASTInquireStmtNode)getChild(0);
        else
            return null;
    }

    public ASTNullifyStmtNode getNullifyStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_97)
            return (ASTNullifyStmtNode)getChild(0);
        else
            return null;
    }

    public ASTOpenStmtNode getOpenStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_98)
            return (ASTOpenStmtNode)getChild(0);
        else
            return null;
    }

    public ASTPointerAssignmentStmtNode getPointerAssignmentStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_99)
            return (ASTPointerAssignmentStmtNode)getChild(0);
        else
            return null;
    }

    public ASTPrintStmtNode getPrintStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_100)
            return (ASTPrintStmtNode)getChild(0);
        else
            return null;
    }

    public ASTReadStmtNode getReadStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_101)
            return (ASTReadStmtNode)getChild(0);
        else
            return null;
    }

    public ASTReturnStmtNode getReturnStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_102)
            return (ASTReturnStmtNode)getChild(0);
        else
            return null;
    }

    public ASTRewindStmtNode getRewindStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_103)
            return (ASTRewindStmtNode)getChild(0);
        else
            return null;
    }

    public ASTStopStmtNode getStopStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_104)
            return (ASTStopStmtNode)getChild(0);
        else
            return null;
    }

    public ASTWhereStmtNode getWhereStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_105)
            return (ASTWhereStmtNode)getChild(0);
        else
            return null;
    }

    public ASTWriteStmtNode getWriteStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_106)
            return (ASTWriteStmtNode)getChild(0);
        else
            return null;
    }

    public ASTAssignStmtNode getAssignStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_1043)
            return (ASTAssignStmtNode)getChild(0);
        else
            return null;
    }

    public ASTAssignedGotoStmtNode getAssignedGotoStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_1044)
            return (ASTAssignedGotoStmtNode)getChild(0);
        else
            return null;
    }

    public ASTPauseStmtNode getPauseStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_1045)
            return (ASTPauseStmtNode)getChild(0);
        else
            return null;
    }

    public ASTStmtFunctionStmtNode getStmtFunctionStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_82)
            return (ASTStmtFunctionStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTArithmeticIfStmtNode getArithmeticIfStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_82)
            return (ASTArithmeticIfStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTComputedGotoStmtNode getComputedGotoStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACTION_STMT_82)
            return (ASTComputedGotoStmtNode)getChild(0, 0);
        else
            return null;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.ACTION_STMT_82 && index == 0)
            return true;
        else
            return false;
    }
}
