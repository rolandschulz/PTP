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

import java.util.List;

import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.parser.Parser.CSTNode;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;
import org.eclipse.photran.internal.core.parser.Parser.Production;

public class ASTMainProgramNode extends ScopingNode
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

        if (getProduction() == Production.MAIN_PROGRAM_8 && ((InteriorNode)getChild(0)).getProduction() == Production.MAIN_RANGE_10)
            return (ASTEndProgramStmtNode)getChild(0, 1);
        else if (getProduction() == Production.MAIN_PROGRAM_8 && ((InteriorNode)getChild(0)).getProduction() == Production.MAIN_RANGE_11)
            return (ASTEndProgramStmtNode)getChild(0, 1);
        else if (getProduction() == Production.MAIN_PROGRAM_8 && ((InteriorNode)getChild(0)).getProduction() == Production.MAIN_RANGE_12)
            return (ASTEndProgramStmtNode)getChild(0, 0);
        else if (getProduction() == Production.MAIN_PROGRAM_9 && ((InteriorNode)getChild(1)).getProduction() == Production.MAIN_RANGE_10)
            return (ASTEndProgramStmtNode)getChild(1, 1);
        else if (getProduction() == Production.MAIN_PROGRAM_9 && ((InteriorNode)getChild(1)).getProduction() == Production.MAIN_RANGE_11)
            return (ASTEndProgramStmtNode)getChild(1, 1);
        else if (getProduction() == Production.MAIN_PROGRAM_9 && ((InteriorNode)getChild(1)).getProduction() == Production.MAIN_RANGE_12)
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
