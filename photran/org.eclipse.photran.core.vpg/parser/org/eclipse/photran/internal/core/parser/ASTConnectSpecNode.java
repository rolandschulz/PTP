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

public class ASTConnectSpecNode extends InteriorNode
{
    ASTConnectSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTConnectSpecNode(this);
    }

    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_744)
            return (ASTUnitIdentifierNode)getChild(1);
        else
            return null;
    }

    public boolean hasUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_744)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTLblRefNode getErrLbl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_745)
            return (ASTLblRefNode)getChild(1);
        else
            return null;
    }

    public boolean hasErrLbl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_745)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getFileExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_746)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasFileExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_746)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getStatusExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_747)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasStatusExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_747)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getAccessExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_748)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasAccessExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_748)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getFormExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_749)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasFormExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_749)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getReclExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_750)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasReclExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_750)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getBlankExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_751)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasBlankExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_751)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getIoStatVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_752)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasIoStatVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_752)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getPositionExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_753)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasPositionExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_753)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getActionExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_754)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasActionExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_754)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getDelimExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_755)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasDelimExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_755)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getPadExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_756)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasPadExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_756)
            return getChild(1) != null;
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.CONNECT_SPEC_744 && index == 0)
            return false;
        else if (getProduction() == Production.CONNECT_SPEC_745 && index == 0)
            return false;
        else if (getProduction() == Production.CONNECT_SPEC_746 && index == 0)
            return false;
        else if (getProduction() == Production.CONNECT_SPEC_747 && index == 0)
            return false;
        else if (getProduction() == Production.CONNECT_SPEC_748 && index == 0)
            return false;
        else if (getProduction() == Production.CONNECT_SPEC_749 && index == 0)
            return false;
        else if (getProduction() == Production.CONNECT_SPEC_750 && index == 0)
            return false;
        else if (getProduction() == Production.CONNECT_SPEC_751 && index == 0)
            return false;
        else if (getProduction() == Production.CONNECT_SPEC_752 && index == 0)
            return false;
        else if (getProduction() == Production.CONNECT_SPEC_753 && index == 0)
            return false;
        else if (getProduction() == Production.CONNECT_SPEC_754 && index == 0)
            return false;
        else if (getProduction() == Production.CONNECT_SPEC_755 && index == 0)
            return false;
        else if (getProduction() == Production.CONNECT_SPEC_756 && index == 0)
            return false;
        else
            return true;
    }
}
