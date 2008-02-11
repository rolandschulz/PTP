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

public class ASTCharSelectorNode extends InteriorNode
{
    ASTCharSelectorNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTCharSelectorNode(this);
    }

    public ASTExpressionNode getKindExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_276)
            return (ASTExpressionNode)getChild(5);
        else if (getProduction() == Production.CHAR_SELECTOR_277)
            return (ASTExpressionNode)getChild(4);
        else if (getProduction() == Production.CHAR_SELECTOR_278)
            return (ASTExpressionNode)getChild(2);
        else
            return null;
    }

    public boolean hasKindExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_276)
            return getChild(5) != null;
        else if (getProduction() == Production.CHAR_SELECTOR_277)
            return getChild(4) != null;
        else if (getProduction() == Production.CHAR_SELECTOR_278)
            return getChild(2) != null;
        else
            return false;
    }

    public Token getConstIntLength()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_275)
            return (Token)((ASTCharLengthNode)getChild(1)).getConstIntLength();
        else
            return null;
    }

    public boolean hasConstIntLength()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_275)
            return ((ASTCharLengthNode)getChild(1)).hasConstIntLength();
        else
            return false;
    }

    public ASTExpressionNode getLengthExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_275)
            return (ASTExpressionNode)((ASTCharLengthNode)getChild(1)).getLengthExpr();
        else if (getProduction() == Production.CHAR_SELECTOR_276)
            return (ASTExpressionNode)((ASTCharLenParamValueNode)getChild(2)).getLengthExpr();
        else if (getProduction() == Production.CHAR_SELECTOR_277)
            return (ASTExpressionNode)((ASTCharLenParamValueNode)getChild(2)).getLengthExpr();
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return (ASTExpressionNode)((ASTCharLenParamValueNode)getChild(2)).getLengthExpr();
        else if (getProduction() == Production.CHAR_SELECTOR_280)
            return (ASTExpressionNode)((ASTCharLenParamValueNode)getChild(1)).getLengthExpr();
        else
            return null;
    }

    public boolean hasLengthExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_275)
            return ((ASTCharLengthNode)getChild(1)).hasLengthExpr();
        else if (getProduction() == Production.CHAR_SELECTOR_276)
            return ((ASTCharLenParamValueNode)getChild(2)).hasLengthExpr();
        else if (getProduction() == Production.CHAR_SELECTOR_277)
            return ((ASTCharLenParamValueNode)getChild(2)).hasLengthExpr();
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return ((ASTCharLenParamValueNode)getChild(2)).hasLengthExpr();
        else if (getProduction() == Production.CHAR_SELECTOR_280)
            return ((ASTCharLenParamValueNode)getChild(1)).hasLengthExpr();
        else
            return false;
    }

    public boolean isAssumedLength()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_275)
            return ((ASTCharLengthNode)getChild(1)).isAssumedLength();
        else if (getProduction() == Production.CHAR_SELECTOR_276)
            return ((ASTCharLenParamValueNode)getChild(2)).isAssumedLength();
        else if (getProduction() == Production.CHAR_SELECTOR_277)
            return ((ASTCharLenParamValueNode)getChild(2)).isAssumedLength();
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return ((ASTCharLenParamValueNode)getChild(2)).isAssumedLength();
        else if (getProduction() == Production.CHAR_SELECTOR_280)
            return ((ASTCharLenParamValueNode)getChild(1)).isAssumedLength();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.CHAR_SELECTOR_275 && index == 0)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_276 && index == 0)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_276 && index == 1)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_276 && index == 3)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_276 && index == 4)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_276 && index == 6)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_277 && index == 0)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_277 && index == 1)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_277 && index == 3)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_277 && index == 5)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_278 && index == 0)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_278 && index == 1)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_278 && index == 3)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_279 && index == 0)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_279 && index == 1)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_279 && index == 3)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_280 && index == 0)
            return false;
        else if (getProduction() == Production.CHAR_SELECTOR_280 && index == 2)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.CHAR_SELECTOR_275 && index == 1)
            return true;
        else if (getProduction() == Production.CHAR_SELECTOR_276 && index == 2)
            return true;
        else if (getProduction() == Production.CHAR_SELECTOR_277 && index == 2)
            return true;
        else if (getProduction() == Production.CHAR_SELECTOR_279 && index == 2)
            return true;
        else if (getProduction() == Production.CHAR_SELECTOR_280 && index == 1)
            return true;
        else
            return false;
    }
}
