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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;

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
        
    @Override public InteriorNode getASTParent()
    {
        InteriorNode actualParent = super.getParent();
        
        // If a node has been pulled up in an ACST, its physical parent in
        // the CST is not its logical parent in the ACST
        if (actualParent != null && actualParent.childIsPulledUp(actualParent.findChild(this)))
            return actualParent.getParent();
        else 
            return actualParent;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSubroutineArgNode(this);
    }

    public ASTExpressionNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_972)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.SUBROUTINE_ARG_974)
            return (ASTExpressionNode)getChild(2);
        else
            return null;
    }

    public boolean hasExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_972)
            return getChild(0) != null;
        else if (getProduction() == Production.SUBROUTINE_ARG_974)
            return getChild(2) != null;
        else
            return false;
    }

    public Token getHollerith()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_976)
            return (Token)getChild(0);
        else if (getProduction() == Production.SUBROUTINE_ARG_977)
            return (Token)getChild(2);
        else
            return null;
    }

    public boolean hasHollerith()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_976)
            return getChild(0) != null;
        else if (getProduction() == Production.SUBROUTINE_ARG_977)
            return getChild(2) != null;
        else
            return false;
    }

    public Token getAsteriskLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_973)
            return (Token)((ASTLblRefNode)getChild(1)).getLabel();
        else if (getProduction() == Production.SUBROUTINE_ARG_975)
            return (Token)((ASTLblRefNode)getChild(3)).getLabel();
        else
            return null;
    }

    public boolean hasAsteriskLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_973)
            return ((ASTLblRefNode)getChild(1)).hasLabel();
        else if (getProduction() == Production.SUBROUTINE_ARG_975)
            return ((ASTLblRefNode)getChild(3)).hasLabel();
        else
            return false;
    }

    public Token getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_ARG_974)
            return (Token)((ASTNameNode)getChild(0)).getName();
        else if (getProduction() == Production.SUBROUTINE_ARG_975)
            return (Token)((ASTNameNode)getChild(0)).getName();
        else if (getProduction() == Production.SUBROUTINE_ARG_977)
            return (Token)((ASTNameNode)getChild(0)).getName();
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.SUBROUTINE_ARG_973 && index == 0)
            return false;
        else if (getProduction() == Production.SUBROUTINE_ARG_974 && index == 1)
            return false;
        else if (getProduction() == Production.SUBROUTINE_ARG_975 && index == 1)
            return false;
        else if (getProduction() == Production.SUBROUTINE_ARG_975 && index == 2)
            return false;
        else if (getProduction() == Production.SUBROUTINE_ARG_977 && index == 1)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.SUBROUTINE_ARG_973 && index == 1)
            return true;
        else if (getProduction() == Production.SUBROUTINE_ARG_974 && index == 0)
            return true;
        else if (getProduction() == Production.SUBROUTINE_ARG_975 && index == 0)
            return true;
        else if (getProduction() == Production.SUBROUTINE_ARG_975 && index == 3)
            return true;
        else if (getProduction() == Production.SUBROUTINE_ARG_977 && index == 0)
            return true;
        else
            return false;
    }
}
