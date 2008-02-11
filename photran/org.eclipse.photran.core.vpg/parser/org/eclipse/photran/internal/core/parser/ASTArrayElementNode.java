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

public class ASTArrayElementNode extends InteriorNode implements IDataIDoObject
{
    ASTArrayElementNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitIDataIDoObject(this);
        visitor.visitASTArrayElementNode(this);
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ARRAY_ELEMENT_439)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.ARRAY_ELEMENT_440)
            return (ASTSectionSubscriptListNode)getChild(2);
        else
            return null;
    }

    public ASTStructureComponentNode getStructureComponent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ARRAY_ELEMENT_440)
            return (ASTStructureComponentNode)getChild(0);
        else
            return null;
    }

    public boolean hasStructureComponent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ARRAY_ELEMENT_440)
            return getChild(0) != null;
        else
            return false;
    }

    public Token getVariableName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ARRAY_ELEMENT_439)
            return (Token)((ASTVariableNameNode)getChild(0)).getVariableName();
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.ARRAY_ELEMENT_439 && index == 1)
            return false;
        else if (getProduction() == Production.ARRAY_ELEMENT_439 && index == 3)
            return false;
        else if (getProduction() == Production.ARRAY_ELEMENT_440 && index == 1)
            return false;
        else if (getProduction() == Production.ARRAY_ELEMENT_440 && index == 3)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.ARRAY_ELEMENT_439 && index == 0)
            return true;
        else
            return false;
    }
}
