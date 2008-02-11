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

public class ASTTypeSpecNode extends InteriorNode
{
    ASTTypeSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTTypeSpecNode(this);
    }

    public boolean isInteger()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_234)
            return getChild(0) != null;
        else if (getProduction() == Production.TYPE_SPEC_240)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isReal()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_235)
            return getChild(0) != null;
        else if (getProduction() == Production.TYPE_SPEC_241)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isDouble()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_236)
            return getChild(0) != null;
        else if (getProduction() == Production.TYPE_SPEC_242)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isComplex()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_237)
            return getChild(0) != null;
        else if (getProduction() == Production.TYPE_SPEC_243)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isLogical()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_238)
            return getChild(0) != null;
        else if (getProduction() == Production.TYPE_SPEC_245)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isCharacter()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_239)
            return getChild(0) != null;
        else if (getProduction() == Production.TYPE_SPEC_244)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTKindSelectorNode getKindSelector()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_240)
            return (ASTKindSelectorNode)getChild(1);
        else if (getProduction() == Production.TYPE_SPEC_241)
            return (ASTKindSelectorNode)getChild(1);
        else if (getProduction() == Production.TYPE_SPEC_243)
            return (ASTKindSelectorNode)getChild(1);
        else if (getProduction() == Production.TYPE_SPEC_245)
            return (ASTKindSelectorNode)getChild(1);
        else
            return null;
    }

    public boolean hasKindSelector()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_240)
            return getChild(1) != null;
        else if (getProduction() == Production.TYPE_SPEC_241)
            return getChild(1) != null;
        else if (getProduction() == Production.TYPE_SPEC_243)
            return getChild(1) != null;
        else if (getProduction() == Production.TYPE_SPEC_245)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTCharSelectorNode getCharSelector()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_244)
            return (ASTCharSelectorNode)getChild(1);
        else
            return null;
    }

    public boolean hasCharSelector()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_244)
            return getChild(1) != null;
        else
            return false;
    }

    public boolean isDerivedType()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_246)
            return getChild(0) != null;
        else
            return false;
    }

    public Token getTypeName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_246)
            return (Token)((ASTTypeNameNode)getChild(2)).getTypeName();
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.TYPE_SPEC_242 && index == 1)
            return false;
        else if (getProduction() == Production.TYPE_SPEC_246 && index == 1)
            return false;
        else if (getProduction() == Production.TYPE_SPEC_246 && index == 3)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.TYPE_SPEC_246 && index == 2)
            return true;
        else
            return false;
    }
}
