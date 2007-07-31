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

public class ASTInquireStmtNode extends InteriorNode
{
    ASTInquireStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTInquireStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_STMT_835)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.INQUIRE_STMT_836)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTInquire()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_STMT_835)
            return (Token)getChild(1);
        else if (getProduction() == Production.INQUIRE_STMT_836)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_STMT_835)
            return (Token)getChild(2);
        else if (getProduction() == Production.INQUIRE_STMT_836)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTInquireSpecListNode getInquireSpecList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_STMT_835)
            return (ASTInquireSpecListNode)getChild(3);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_STMT_835)
            return (Token)getChild(4);
        else if (getProduction() == Production.INQUIRE_STMT_836)
            return (Token)getChild(5);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_STMT_835)
            return (Token)getChild(5);
        else if (getProduction() == Production.INQUIRE_STMT_836)
            return (Token)getChild(7);
        else
            return null;
    }

    public Token getTIolengtheq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_STMT_836)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTScalarVariableNode getScalarVariable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_STMT_836)
            return (ASTScalarVariableNode)getChild(4);
        else
            return null;
    }

    public ASTOutputItemListNode getOutputItemList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_STMT_836)
            return (ASTOutputItemListNode)getChild(6);
        else
            return null;
    }
}
