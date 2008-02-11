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

public class ASTComponentAttrSpecNode extends InteriorNode
{
    ASTComponentAttrSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTComponentAttrSpecNode(this);
    }

    public boolean pointer()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_ATTR_SPEC_195)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean dimension()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_ATTR_SPEC_196)
            return getChild(0) != null;
        else
            return false;
    }

    public IComponentArraySpec getComponentArraySpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_ATTR_SPEC_196)
            return (IComponentArraySpec)getChild(2);
        else
            return null;
    }

    public boolean allocatable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_ATTR_SPEC_197)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTAccessSpecNode getAccessSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_ATTR_SPEC_198)
            return (ASTAccessSpecNode)getChild(0);
        else
            return null;
    }

    public boolean hasAccessSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_ATTR_SPEC_198)
            return getChild(0) != null;
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.COMPONENT_ATTR_SPEC_196 && index == 1)
            return false;
        else if (getProduction() == Production.COMPONENT_ATTR_SPEC_196 && index == 3)
            return false;
        else
            return true;
    }
}
