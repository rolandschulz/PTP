/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;

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
        
    @Override public InteriorNode getASTParent()
    {
        InteriorNode actualParent = super.getParent();
        
        // If a node has been pulled up in an ACST, its physical parent in
        // the CST is not its logical parent in the ACST
        if (actualParent != null && actualParent.childIsPulledUp(actualParent.findChild(this)))
            return actualParent.getParent();
        else 
            return actualParent;
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
            return (ASTAllocateStmtNode)((ASTActionStmtNode)getChild(0)).getAllocateStmt();
        else
            return null;
    }

    public ASTAssignmentStmtNode getAssignmentStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTAssignmentStmtNode)((ASTActionStmtNode)getChild(0)).getAssignmentStmt();
        else
            return null;
    }

    public ASTBackspaceStmtNode getBackspaceStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTBackspaceStmtNode)((ASTActionStmtNode)getChild(0)).getBackspaceStmt();
        else
            return null;
    }

    public ASTCallStmtNode getCallStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTCallStmtNode)((ASTActionStmtNode)getChild(0)).getCallStmt();
        else
            return null;
    }

    public ASTCloseStmtNode getCloseStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTCloseStmtNode)((ASTActionStmtNode)getChild(0)).getCloseStmt();
        else
            return null;
    }

    public ASTContinueStmtNode getContinueStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTContinueStmtNode)((ASTActionStmtNode)getChild(0)).getContinueStmt();
        else
            return null;
    }

    public ASTCycleStmtNode getCycleStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTCycleStmtNode)((ASTActionStmtNode)getChild(0)).getCycleStmt();
        else
            return null;
    }

    public ASTDeallocateStmtNode getDeallocateStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTDeallocateStmtNode)((ASTActionStmtNode)getChild(0)).getDeallocateStmt();
        else
            return null;
    }

    public ASTEndfileStmtNode getEndfileStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTEndfileStmtNode)((ASTActionStmtNode)getChild(0)).getEndfileStmt();
        else
            return null;
    }

    public ASTExitStmtNode getExitStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTExitStmtNode)((ASTActionStmtNode)getChild(0)).getExitStmt();
        else
            return null;
    }

    public ASTForallStmtNode getForallStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTForallStmtNode)((ASTActionStmtNode)getChild(0)).getForallStmt();
        else
            return null;
    }

    public ASTGotoStmtNode getGotoStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTGotoStmtNode)((ASTActionStmtNode)getChild(0)).getGotoStmt();
        else
            return null;
    }

    public ASTIfStmtNode getIfStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTIfStmtNode)((ASTActionStmtNode)getChild(0)).getIfStmt();
        else
            return null;
    }

    public ASTInquireStmtNode getInquireStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTInquireStmtNode)((ASTActionStmtNode)getChild(0)).getInquireStmt();
        else
            return null;
    }

    public ASTNullifyStmtNode getNullifyStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTNullifyStmtNode)((ASTActionStmtNode)getChild(0)).getNullifyStmt();
        else
            return null;
    }

    public ASTOpenStmtNode getOpenStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTOpenStmtNode)((ASTActionStmtNode)getChild(0)).getOpenStmt();
        else
            return null;
    }

    public ASTPointerAssignmentStmtNode getPointerAssignmentStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTPointerAssignmentStmtNode)((ASTActionStmtNode)getChild(0)).getPointerAssignmentStmt();
        else
            return null;
    }

    public ASTPrintStmtNode getPrintStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTPrintStmtNode)((ASTActionStmtNode)getChild(0)).getPrintStmt();
        else
            return null;
    }

    public ASTReadStmtNode getReadStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTReadStmtNode)((ASTActionStmtNode)getChild(0)).getReadStmt();
        else
            return null;
    }

    public ASTReturnStmtNode getReturnStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTReturnStmtNode)((ASTActionStmtNode)getChild(0)).getReturnStmt();
        else
            return null;
    }

    public ASTRewindStmtNode getRewindStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTRewindStmtNode)((ASTActionStmtNode)getChild(0)).getRewindStmt();
        else
            return null;
    }

    public ASTStopStmtNode getStopStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTStopStmtNode)((ASTActionStmtNode)getChild(0)).getStopStmt();
        else
            return null;
    }

    public ASTWhereStmtNode getWhereStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTWhereStmtNode)((ASTActionStmtNode)getChild(0)).getWhereStmt();
        else
            return null;
    }

    public ASTWriteStmtNode getWriteStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTWriteStmtNode)((ASTActionStmtNode)getChild(0)).getWriteStmt();
        else
            return null;
    }

    public ASTAssignStmtNode getAssignStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTAssignStmtNode)((ASTActionStmtNode)getChild(0)).getAssignStmt();
        else
            return null;
    }

    public ASTAssignedGotoStmtNode getAssignedGotoStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTAssignedGotoStmtNode)((ASTActionStmtNode)getChild(0)).getAssignedGotoStmt();
        else
            return null;
    }

    public ASTPauseStmtNode getPauseStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTPauseStmtNode)((ASTActionStmtNode)getChild(0)).getPauseStmt();
        else
            return null;
    }

    public ASTStmtFunctionStmtNode getStmtFunctionStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTStmtFunctionStmtNode)((ASTActionStmtNode)getChild(0)).getStmtFunctionStmt();
        else
            return null;
    }

    public ASTArithmeticIfStmtNode getArithmeticIfStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTArithmeticIfStmtNode)((ASTActionStmtNode)getChild(0)).getArithmeticIfStmt();
        else
            return null;
    }

    public ASTComputedGotoStmtNode getComputedGotoStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_CONSTRUCT_75)
            return (ASTComputedGotoStmtNode)((ASTActionStmtNode)getChild(0)).getComputedGotoStmt();
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
