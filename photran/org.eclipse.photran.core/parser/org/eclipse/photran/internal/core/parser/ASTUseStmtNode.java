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

public class ASTUseStmtNode extends InteriorNode
{
    ASTUseStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTUseStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_900)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.USE_STMT_901)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.USE_STMT_902)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.USE_STMT_903)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTUse()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_900)
            return (Token)getChild(1);
        else if (getProduction() == Production.USE_STMT_901)
            return (Token)getChild(1);
        else if (getProduction() == Production.USE_STMT_902)
            return (Token)getChild(1);
        else if (getProduction() == Production.USE_STMT_903)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_900)
            return (ASTNameNode)getChild(2);
        else if (getProduction() == Production.USE_STMT_901)
            return (ASTNameNode)getChild(2);
        else if (getProduction() == Production.USE_STMT_902)
            return (ASTNameNode)getChild(2);
        else if (getProduction() == Production.USE_STMT_903)
            return (ASTNameNode)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_900)
            return (Token)getChild(3);
        else if (getProduction() == Production.USE_STMT_901)
            return (Token)getChild(5);
        else if (getProduction() == Production.USE_STMT_902)
            return (Token)getChild(6);
        else if (getProduction() == Production.USE_STMT_903)
            return (Token)getChild(7);
        else
            return null;
    }

    public Token getTComma()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_901)
            return (Token)getChild(3);
        else if (getProduction() == Production.USE_STMT_902)
            return (Token)getChild(3);
        else if (getProduction() == Production.USE_STMT_903)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTRenameListNode getRenameList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_901)
            return (ASTRenameListNode)getChild(4);
        else
            return null;
    }

    public Token getTOnly()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_902)
            return (Token)getChild(4);
        else if (getProduction() == Production.USE_STMT_903)
            return (Token)getChild(4);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_902)
            return (Token)getChild(5);
        else if (getProduction() == Production.USE_STMT_903)
            return (Token)getChild(5);
        else
            return null;
    }

    public ASTOnlyListNode getOnlyList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_903)
            return (ASTOnlyListNode)getChild(6);
        else
            return null;
    }
}
