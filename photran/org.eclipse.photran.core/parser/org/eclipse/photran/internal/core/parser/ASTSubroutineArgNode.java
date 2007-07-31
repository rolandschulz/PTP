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

public class ASTSubroutineArgNode extends InteriorNode
{
    ASTSubroutineArgNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSubroutineArgNode(this);
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_969)
            return (ASTExprNode)getChild(0);
        else if (getProduction() == Production.SUBROUTINE_ARG_971)
            return (ASTExprNode)getChild(2);
        else
            return null;
    }

    public Token getTAsterisk()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_970)
            return (Token)getChild(0);
        else if (getProduction() == Production.SUBROUTINE_ARG_972)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTLblRefNode getLblRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_970)
            return (ASTLblRefNode)getChild(1);
        else if (getProduction() == Production.SUBROUTINE_ARG_972)
            return (ASTLblRefNode)getChild(3);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_971)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.SUBROUTINE_ARG_972)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.SUBROUTINE_ARG_974)
            return (ASTNameNode)getChild(0);
        else
            return null;
    }

    public Token getTEquals()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_971)
            return (Token)getChild(1);
        else if (getProduction() == Production.SUBROUTINE_ARG_972)
            return (Token)getChild(1);
        else if (getProduction() == Production.SUBROUTINE_ARG_974)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTHcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_973)
            return (Token)getChild(0);
        else if (getProduction() == Production.SUBROUTINE_ARG_974)
            return (Token)getChild(2);
        else
            return null;
    }
}
