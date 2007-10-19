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

    public ASTLengthSelectorNode getLengthSelector()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_277)
            return (ASTLengthSelectorNode)getChild(0);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_278)
            return (Token)getChild(0);
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return (Token)getChild(0);
        else if (getProduction() == Production.CHAR_SELECTOR_280)
            return (Token)getChild(0);
        else if (getProduction() == Production.CHAR_SELECTOR_281)
            return (Token)getChild(0);
        else if (getProduction() == Production.CHAR_SELECTOR_282)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTLeneq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_278)
            return (Token)getChild(1);
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return (Token)getChild(1);
        else if (getProduction() == Production.CHAR_SELECTOR_281)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTCharLenParamValueNode getCharLenParamValue()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_278)
            return (ASTCharLenParamValueNode)getChild(2);
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return (ASTCharLenParamValueNode)getChild(2);
        else if (getProduction() == Production.CHAR_SELECTOR_281)
            return (ASTCharLenParamValueNode)getChild(2);
        else if (getProduction() == Production.CHAR_SELECTOR_282)
            return (ASTCharLenParamValueNode)getChild(1);
        else
            return null;
    }

    public Token getTComma()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_278)
            return (Token)getChild(3);
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTKindeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_278)
            return (Token)getChild(4);
        else if (getProduction() == Production.CHAR_SELECTOR_280)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_278)
            return (ASTExprNode)getChild(5);
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return (ASTExprNode)getChild(4);
        else if (getProduction() == Production.CHAR_SELECTOR_280)
            return (ASTExprNode)getChild(2);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_278)
            return (Token)getChild(6);
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return (Token)getChild(5);
        else if (getProduction() == Production.CHAR_SELECTOR_280)
            return (Token)getChild(3);
        else if (getProduction() == Production.CHAR_SELECTOR_281)
            return (Token)getChild(3);
        else if (getProduction() == Production.CHAR_SELECTOR_282)
            return (Token)getChild(2);
        else
            return null;
    }
}
