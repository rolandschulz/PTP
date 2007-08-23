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

public class ASTForallStmtNode extends InteriorNode
{
    ASTForallStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTForallStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_647)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.FORALL_STMT_648)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTForall()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_647)
            return (Token)getChild(1);
        else if (getProduction() == Production.FORALL_STMT_648)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTForallHeaderNode getForallHeader()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_647)
            return (ASTForallHeaderNode)getChild(2);
        else if (getProduction() == Production.FORALL_STMT_648)
            return (ASTForallHeaderNode)getChild(2);
        else
            return null;
    }

    public ASTAssignmentStmtNode getAssignmentStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_647)
            return (ASTAssignmentStmtNode)getChild(3);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_647)
            return (Token)getChild(4);
        else if (getProduction() == Production.FORALL_STMT_648)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTPointerAssignmentStmtNode getPointerAssignmentStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_STMT_648)
            return (ASTPointerAssignmentStmtNode)getChild(3);
        else
            return null;
    }
}
