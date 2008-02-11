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

public class ASTIoControlSpecNode extends InteriorNode
{
    ASTIoControlSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTIoControlSpecNode(this);
    }

    public ASTFormatIdentifierNode getFormatIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_792)
            return (ASTFormatIdentifierNode)getChild(1);
        else
            return null;
    }

    public boolean hasFormatIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_792)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_793)
            return (ASTUnitIdentifierNode)getChild(1);
        else
            return null;
    }

    public boolean hasUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_793)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getRecExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_794)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasRecExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_794)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTLblRefNode getEndExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_795)
            return (ASTLblRefNode)getChild(1);
        else
            return null;
    }

    public boolean hasEndExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_795)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTLblRefNode getErrLbl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_796)
            return (ASTLblRefNode)getChild(1);
        else
            return null;
    }

    public boolean hasErrLbl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_796)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getIoStatVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_797)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasIoStatVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_797)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTNamelistGroupNameNode getNamelistGroupName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_798)
            return (ASTNamelistGroupNameNode)getChild(1);
        else
            return null;
    }

    public boolean hasNamelistGroupName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_798)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getAdvanceExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_799)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasAdvanceExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_799)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTVariableNode getSizeVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_800)
            return (ASTVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasSizeVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_800)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTLblRefNode getEorLbl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_801)
            return (ASTLblRefNode)getChild(1);
        else
            return null;
    }

    public boolean hasEorLbl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_801)
            return getChild(1) != null;
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.IO_CONTROL_SPEC_792 && index == 0)
            return false;
        else if (getProduction() == Production.IO_CONTROL_SPEC_793 && index == 0)
            return false;
        else if (getProduction() == Production.IO_CONTROL_SPEC_794 && index == 0)
            return false;
        else if (getProduction() == Production.IO_CONTROL_SPEC_795 && index == 0)
            return false;
        else if (getProduction() == Production.IO_CONTROL_SPEC_796 && index == 0)
            return false;
        else if (getProduction() == Production.IO_CONTROL_SPEC_797 && index == 0)
            return false;
        else if (getProduction() == Production.IO_CONTROL_SPEC_798 && index == 0)
            return false;
        else if (getProduction() == Production.IO_CONTROL_SPEC_799 && index == 0)
            return false;
        else if (getProduction() == Production.IO_CONTROL_SPEC_800 && index == 0)
            return false;
        else if (getProduction() == Production.IO_CONTROL_SPEC_801 && index == 0)
            return false;
        else
            return true;
    }
}
