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

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTExecutionPartConstructNode extends InteriorNode
{
    ASTExecutionPartConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTExecutionPartConstructNode(this);
    }

    public ASTExecutableConstructNode getExecutableConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTION_PART_CONSTRUCT_48)
            return (ASTExecutableConstructNode)getChild(0);
        else
            return null;
    }

    public ASTFormatStmtNode getFormatStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTION_PART_CONSTRUCT_49)
            return (ASTFormatStmtNode)getChild(0);
        else
            return null;
    }

    public ASTEntryStmtNode getEntryStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTION_PART_CONSTRUCT_50)
            return (ASTEntryStmtNode)getChild(0);
        else
            return null;
    }

    public ASTDataStmtNode getDataStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTION_PART_CONSTRUCT_47)
            return (ASTDataStmtNode)getChild(0, 0);
        else
            return null;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.EXECUTION_PART_CONSTRUCT_47 && index == 0)
            return true;
        else
            return false;
    }
}
