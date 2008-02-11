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

public class ASTMainProgramNode extends ScopingNode
{
    ASTMainProgramNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
            return (ASTBodyNode)((ASTMainRangeNode)getChild(0)).getBody();
        else if (getProduction() == Production.MAIN_PROGRAM_9)
            return (ASTBodyNode)((ASTMainRangeNode)getChild(1)).getBody();
        else
            return null;
    }

    public ASTEndProgramStmtNode getEndProgramStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MAIN_PROGRAM_8)
            return (ASTEndProgramStmtNode)((ASTMainRangeNode)getChild(0)).getEndProgramStmt();
        else if (getProduction() == Production.MAIN_PROGRAM_9)
            return (ASTEndProgramStmtNode)((ASTMainRangeNode)getChild(1)).getEndProgramStmt();
        else
            return null;
    }

    public ASTContainsStmtNode getContainsStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MAIN_PROGRAM_8)
            return (ASTContainsStmtNode)((ASTMainRangeNode)getChild(0)).getContainsStmt();
        else if (getProduction() == Production.MAIN_PROGRAM_9)
            return (ASTContainsStmtNode)((ASTMainRangeNode)getChild(1)).getContainsStmt();
        else
            return null;
    }

    public ASTInternalSubprogramsNode getInternalSubprograms()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MAIN_PROGRAM_8)
            return (ASTInternalSubprogramsNode)((ASTMainRangeNode)getChild(0)).getInternalSubprograms();
        else if (getProduction() == Production.MAIN_PROGRAM_9)
            return (ASTInternalSubprogramsNode)((ASTMainRangeNode)getChild(1)).getInternalSubprograms();
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
