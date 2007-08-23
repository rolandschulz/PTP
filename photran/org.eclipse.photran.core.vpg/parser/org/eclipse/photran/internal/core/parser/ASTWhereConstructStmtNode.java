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

public class ASTWhereConstructStmtNode extends InteriorNode
{
    ASTWhereConstructStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTWhereConstructStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_CONSTRUCT_STMT_612)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.WHERE_CONSTRUCT_STMT_613)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_CONSTRUCT_STMT_612)
            return (ASTNameNode)getChild(1);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_CONSTRUCT_STMT_612)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTWhere()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_CONSTRUCT_STMT_612)
            return (Token)getChild(3);
        else if (getProduction() == Production.WHERE_CONSTRUCT_STMT_613)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_CONSTRUCT_STMT_612)
            return (Token)getChild(4);
        else if (getProduction() == Production.WHERE_CONSTRUCT_STMT_613)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTMaskExprNode getMaskExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_CONSTRUCT_STMT_612)
            return (ASTMaskExprNode)getChild(5);
        else if (getProduction() == Production.WHERE_CONSTRUCT_STMT_613)
            return (ASTMaskExprNode)getChild(3);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_CONSTRUCT_STMT_612)
            return (Token)getChild(6);
        else if (getProduction() == Production.WHERE_CONSTRUCT_STMT_613)
            return (Token)getChild(4);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_CONSTRUCT_STMT_612)
            return (Token)getChild(7);
        else if (getProduction() == Production.WHERE_CONSTRUCT_STMT_613)
            return (Token)getChild(5);
        else
            return null;
    }
}
