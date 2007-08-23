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

public class ASTAccessStmtNode extends InteriorNode
{
    ASTAccessStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAccessStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACCESS_STMT_324)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ACCESS_STMT_325)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ACCESS_STMT_326)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public ASTAccessSpecNode getAccessSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACCESS_STMT_324)
            return (ASTAccessSpecNode)getChild(1);
        else if (getProduction() == Production.ACCESS_STMT_325)
            return (ASTAccessSpecNode)getChild(1);
        else if (getProduction() == Production.ACCESS_STMT_326)
            return (ASTAccessSpecNode)getChild(1);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACCESS_STMT_324)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTColon2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACCESS_STMT_324)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTAccessIdListNode getAccessIdList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACCESS_STMT_324)
            return (ASTAccessIdListNode)getChild(4);
        else if (getProduction() == Production.ACCESS_STMT_325)
            return (ASTAccessIdListNode)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACCESS_STMT_324)
            return (Token)getChild(5);
        else if (getProduction() == Production.ACCESS_STMT_325)
            return (Token)getChild(3);
        else if (getProduction() == Production.ACCESS_STMT_326)
            return (Token)getChild(2);
        else
            return null;
    }
}
