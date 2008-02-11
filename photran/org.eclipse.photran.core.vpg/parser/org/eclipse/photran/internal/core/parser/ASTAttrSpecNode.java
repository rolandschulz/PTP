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

public class ASTAttrSpecNode extends InteriorNode
{
    ASTAttrSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTAttrSpecNode(this);
    }

    public ASTAccessSpecNode getAccessSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_247)
            return (ASTAccessSpecNode)getChild(0);
        else
            return null;
    }

    public boolean hasAccessSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_247)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isParameter()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_248)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isAllocatable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_249)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isDimension()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_250)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTArraySpecNode getArraySpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_250)
            return (ASTArraySpecNode)getChild(2);
        else
            return null;
    }

    public boolean isExternal()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_251)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isIntent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_252)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTIntentSpecNode getIntentSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_252)
            return (ASTIntentSpecNode)getChild(2);
        else
            return null;
    }

    public boolean isIntrinsic()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_253)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isOptional()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_254)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isPointer()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_255)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isSave()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_256)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean isTarget()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_257)
            return getChild(0) != null;
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.ATTR_SPEC_250 && index == 1)
            return false;
        else if (getProduction() == Production.ATTR_SPEC_250 && index == 3)
            return false;
        else if (getProduction() == Production.ATTR_SPEC_252 && index == 1)
            return false;
        else if (getProduction() == Production.ATTR_SPEC_252 && index == 3)
            return false;
        else
            return true;
    }
}
