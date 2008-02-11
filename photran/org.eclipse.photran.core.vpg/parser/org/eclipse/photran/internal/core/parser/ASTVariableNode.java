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

public class ASTVariableNode extends InteriorNode implements IDataStmtObject, IInputItem
{
    ASTVariableNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitIDataStmtObject(this);
        visitor.visitIInputItem(this);
        visitor.visitASTVariableNode(this);
    }

    public ASTDataRefNode getDataRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_419)
            return (ASTDataRefNode)getChild(0);
        else if (getProduction() == Production.VARIABLE_420)
            return (ASTDataRefNode)getChild(0);
        else if (getProduction() == Production.VARIABLE_421)
            return (ASTDataRefNode)getChild(0);
        else
            return null;
    }

    public boolean hasDataRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_419)
            return getChild(0) != null;
        else if (getProduction() == Production.VARIABLE_420)
            return getChild(0) != null;
        else if (getProduction() == Production.VARIABLE_421)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_420)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.VARIABLE_421)
            return (ASTSectionSubscriptListNode)getChild(2);
        else
            return null;
    }

    public boolean hasSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_420)
            return getChild(2) != null;
        else if (getProduction() == Production.VARIABLE_421)
            return getChild(2) != null;
        else
            return false;
    }

    public ASTSubstringRangeNode getSubstringRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_421)
            return (ASTSubstringRangeNode)getChild(4);
        else if (getProduction() == Production.VARIABLE_422)
            return (ASTSubstringRangeNode)((ASTSubstrConstNode)getChild(0)).getSubstringRange();
        else
            return null;
    }

    public boolean hasSubstringRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_421)
            return getChild(4) != null;
        else if (getProduction() == Production.VARIABLE_422)
            return ((ASTSubstrConstNode)getChild(0)).hasSubstringRange();
        else
            return false;
    }

    public Token getStringConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_422)
            return (Token)((ASTSubstrConstNode)getChild(0)).getStringConst();
        else
            return null;
    }

    public boolean hasStringConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_422)
            return ((ASTSubstrConstNode)getChild(0)).hasStringConst();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.VARIABLE_420 && index == 1)
            return false;
        else if (getProduction() == Production.VARIABLE_420 && index == 3)
            return false;
        else if (getProduction() == Production.VARIABLE_421 && index == 1)
            return false;
        else if (getProduction() == Production.VARIABLE_421 && index == 3)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.VARIABLE_422 && index == 0)
            return true;
        else
            return false;
    }
}
