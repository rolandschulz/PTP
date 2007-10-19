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

public class ASTSubscriptTripletNode extends InteriorNode
{
    ASTSubscriptTripletNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTSubscriptTripletNode(this);
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBSCRIPT_TRIPLET_448)
            return (Token)getChild(0);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_449)
            return (Token)getChild(0);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_450)
            return (Token)getChild(1);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_451)
            return (Token)getChild(1);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_452)
            return (Token)getChild(1);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_453)
            return (Token)getChild(1);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_454)
            return (Token)getChild(0);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_455)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBSCRIPT_TRIPLET_449)
            return (ASTExprNode)getChild(1);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_450)
            return (ASTExprNode)getChild(0);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_451)
            return (ASTExprNode)getChild(0);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_452)
            return (ASTExprNode)getChild(0);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_453)
            return (ASTExprNode)getChild(0);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_454)
            return (ASTExprNode)getChild(1);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_455)
            return (ASTExprNode)getChild(2);
        else
            return null;
    }

    public ASTExprNode getExpr2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBSCRIPT_TRIPLET_451)
            return (ASTExprNode)getChild(2);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_452)
            return (ASTExprNode)getChild(2);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_453)
            return (ASTExprNode)getChild(3);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_454)
            return (ASTExprNode)getChild(3);
        else
            return null;
    }

    public Token getTColon2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBSCRIPT_TRIPLET_452)
            return (Token)getChild(3);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_453)
            return (Token)getChild(2);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_454)
            return (Token)getChild(2);
        else if (getProduction() == Production.SUBSCRIPT_TRIPLET_455)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTExprNode getExpr3()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBSCRIPT_TRIPLET_452)
            return (ASTExprNode)getChild(4);
        else
            return null;
    }
}
