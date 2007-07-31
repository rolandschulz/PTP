package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTExecutableConstructNode extends InteriorNode
{
    ASTExecutableConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTExecutableConstructNode(this);
    }

    public ASTCaseConstructNode getCaseConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_76)
            return (ASTCaseConstructNode)getChild(0);
        else
            return null;
    }

    public ASTDoConstructNode getDoConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_77)
            return (ASTDoConstructNode)getChild(0);
        else
            return null;
    }

    public ASTForallConstructNode getForallConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_78)
            return (ASTForallConstructNode)getChild(0);
        else
            return null;
    }

    public ASTIfConstructNode getIfConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_79)
            return (ASTIfConstructNode)getChild(0);
        else
            return null;
    }

    public ASTWhereConstructNode getWhereConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_80)
            return (ASTWhereConstructNode)getChild(0);
        else
            return null;
    }

    public ASTEndDoStmtNode getEndDoStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_81)
            return (ASTEndDoStmtNode)getChild(0);
        else
            return null;
    }

    public ASTAllocateStmtNode getAllocateStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTAllocateStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTAssignmentStmtNode getAssignmentStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTAssignmentStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTBackspaceStmtNode getBackspaceStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTBackspaceStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTCallStmtNode getCallStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTCallStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTCloseStmtNode getCloseStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTCloseStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTContinueStmtNode getContinueStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTContinueStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTCycleStmtNode getCycleStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTCycleStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTDeallocateStmtNode getDeallocateStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTDeallocateStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTEndfileStmtNode getEndfileStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTEndfileStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTExitStmtNode getExitStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTExitStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTForallStmtNode getForallStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTForallStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTGotoStmtNode getGotoStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTGotoStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTIfStmtNode getIfStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTIfStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTInquireStmtNode getInquireStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTInquireStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTNullifyStmtNode getNullifyStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTNullifyStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTOpenStmtNode getOpenStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTOpenStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTPointerAssignmentStmtNode getPointerAssignmentStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTPointerAssignmentStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTPrintStmtNode getPrintStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTPrintStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTReadStmtNode getReadStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTReadStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTReturnStmtNode getReturnStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTReturnStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTRewindStmtNode getRewindStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTRewindStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTStopStmtNode getStopStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTStopStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTWhereStmtNode getWhereStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTWhereStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTWriteStmtNode getWriteStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTWriteStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTAssignStmtNode getAssignStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTAssignStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTAssignedGotoStmtNode getAssignedGotoStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTAssignedGotoStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTPauseStmtNode getPauseStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTPauseStmtNode)getChild(0, 0);
        else
            return null;
    }

    public ASTStmtFunctionStmtNode getStmtFunctionStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTStmtFunctionStmtNode)getChild(0, 0, 0);
        else
            return null;
    }

    public ASTArithmeticIfStmtNode getArithmeticIfStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTArithmeticIfStmtNode)getChild(0, 0, 0);
        else
            return null;
    }

    public ASTComputedGotoStmtNode getComputedGotoStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTComputedGotoStmtNode)getChild(0, 0, 0);
        else
            return null;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75 && index == 0)
            return true;
        else
            return false;
    }
}
