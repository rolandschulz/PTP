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

public class ASTDimensionStmtNode extends InteriorNode
{
    ASTDimensionStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDimensionStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DIMENSION_STMT_339)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.DIMENSION_STMT_340)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTDimension()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DIMENSION_STMT_339)
            return (Token)getChild(1);
        else if (getProduction() == Production.DIMENSION_STMT_340)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DIMENSION_STMT_339)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTColon2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DIMENSION_STMT_339)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTArrayDeclaratorListNode getArrayDeclaratorList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DIMENSION_STMT_339)
            return (ASTArrayDeclaratorListNode)getChild(4);
        else if (getProduction() == Production.DIMENSION_STMT_340)
            return (ASTArrayDeclaratorListNode)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DIMENSION_STMT_339)
            return (Token)getChild(5);
        else if (getProduction() == Production.DIMENSION_STMT_340)
            return (Token)getChild(3);
        else
            return null;
    }
}
