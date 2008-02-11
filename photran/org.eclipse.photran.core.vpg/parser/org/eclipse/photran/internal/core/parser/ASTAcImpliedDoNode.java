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

public class ASTAcImpliedDoNode extends InteriorNode
{
    ASTAcImpliedDoNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTAcImpliedDoNode(this);
    }

    public ASTExpressionNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AC_IMPLIED_DO_225)
            return (ASTExpressionNode)getChild(1);
        else if (getProduction() == Production.AC_IMPLIED_DO_226)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AC_IMPLIED_DO_225)
            return getChild(1) != null;
        else if (getProduction() == Production.AC_IMPLIED_DO_226)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTImpliedDoVariableNode getImpliedDoVariable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AC_IMPLIED_DO_225)
            return (ASTImpliedDoVariableNode)getChild(3);
        else if (getProduction() == Production.AC_IMPLIED_DO_226)
            return (ASTImpliedDoVariableNode)getChild(3);
        else if (getProduction() == Production.AC_IMPLIED_DO_227)
            return (ASTImpliedDoVariableNode)getChild(3);
        else if (getProduction() == Production.AC_IMPLIED_DO_228)
            return (ASTImpliedDoVariableNode)getChild(3);
        else
            return null;
    }

    public ASTExpressionNode getLb()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AC_IMPLIED_DO_225)
            return (ASTExpressionNode)getChild(5);
        else if (getProduction() == Production.AC_IMPLIED_DO_226)
            return (ASTExpressionNode)getChild(5);
        else if (getProduction() == Production.AC_IMPLIED_DO_227)
            return (ASTExpressionNode)getChild(5);
        else if (getProduction() == Production.AC_IMPLIED_DO_228)
            return (ASTExpressionNode)getChild(5);
        else
            return null;
    }

    public ASTExpressionNode getUb()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AC_IMPLIED_DO_225)
            return (ASTExpressionNode)getChild(7);
        else if (getProduction() == Production.AC_IMPLIED_DO_226)
            return (ASTExpressionNode)getChild(7);
        else if (getProduction() == Production.AC_IMPLIED_DO_227)
            return (ASTExpressionNode)getChild(7);
        else if (getProduction() == Production.AC_IMPLIED_DO_228)
            return (ASTExpressionNode)getChild(7);
        else
            return null;
    }

    public ASTExpressionNode getStep()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AC_IMPLIED_DO_226)
            return (ASTExpressionNode)getChild(9);
        else if (getProduction() == Production.AC_IMPLIED_DO_228)
            return (ASTExpressionNode)getChild(9);
        else
            return null;
    }

    public ASTAcImpliedDoNode getNestedImpliedDo()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AC_IMPLIED_DO_227)
            return (ASTAcImpliedDoNode)getChild(1);
        else if (getProduction() == Production.AC_IMPLIED_DO_228)
            return (ASTAcImpliedDoNode)getChild(1);
        else
            return null;
    }

    public boolean hasNestedImpliedDo()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AC_IMPLIED_DO_227)
            return getChild(1) != null;
        else if (getProduction() == Production.AC_IMPLIED_DO_228)
            return getChild(1) != null;
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.AC_IMPLIED_DO_225 && index == 0)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_225 && index == 2)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_225 && index == 4)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_225 && index == 6)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_225 && index == 8)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_226 && index == 0)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_226 && index == 2)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_226 && index == 4)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_226 && index == 6)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_226 && index == 8)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_226 && index == 10)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_227 && index == 0)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_227 && index == 2)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_227 && index == 4)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_227 && index == 6)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_227 && index == 8)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_228 && index == 0)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_228 && index == 2)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_228 && index == 4)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_228 && index == 6)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_228 && index == 8)
            return false;
        else if (getProduction() == Production.AC_IMPLIED_DO_228 && index == 10)
            return false;
        else
            return true;
    }
}
