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

public class ASTEndSelectStmtNode extends InteriorNode
{
    ASTEndSelectStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTEndSelectStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SELECT_STMT_688)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_SELECT_STMT_689)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_SELECT_STMT_690)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_SELECT_STMT_691)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTEndselect()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SELECT_STMT_688)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_SELECT_STMT_689)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SELECT_STMT_688)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_SELECT_STMT_689)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_SELECT_STMT_690)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_SELECT_STMT_691)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTEndNameNode getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SELECT_STMT_689)
            return (ASTEndNameNode)getChild(2);
        else if (getProduction() == Production.END_SELECT_STMT_691)
            return (ASTEndNameNode)getChild(3);
        else
            return null;
    }

    public Token getTEnd()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SELECT_STMT_690)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_SELECT_STMT_691)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTSelect()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SELECT_STMT_690)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_SELECT_STMT_691)
            return (Token)getChild(2);
        else
            return null;
    }
}
