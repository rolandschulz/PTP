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

public class ASTStmtFunctionRangeNode extends InteriorNode
{
    ASTStmtFunctionRangeNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTStmtFunctionRangeNode(this);
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.STMT_FUNCTION_RANGE_1015)
            return (Token)getChild(0);
        else if (getProduction() == Production.STMT_FUNCTION_RANGE_1016)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.STMT_FUNCTION_RANGE_1015)
            return (Token)getChild(1);
        else if (getProduction() == Production.STMT_FUNCTION_RANGE_1016)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTEquals()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.STMT_FUNCTION_RANGE_1015)
            return (Token)getChild(2);
        else if (getProduction() == Production.STMT_FUNCTION_RANGE_1016)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.STMT_FUNCTION_RANGE_1015)
            return (ASTExprNode)getChild(3);
        else if (getProduction() == Production.STMT_FUNCTION_RANGE_1016)
            return (ASTExprNode)getChild(4);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.STMT_FUNCTION_RANGE_1015)
            return (Token)getChild(4);
        else if (getProduction() == Production.STMT_FUNCTION_RANGE_1016)
            return (Token)getChild(5);
        else
            return null;
    }

    public ASTSFDummyArgNameListNode getSFDummyArgNameList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.STMT_FUNCTION_RANGE_1016)
            return (ASTSFDummyArgNameListNode)getChild(1);
        else
            return null;
    }
}
