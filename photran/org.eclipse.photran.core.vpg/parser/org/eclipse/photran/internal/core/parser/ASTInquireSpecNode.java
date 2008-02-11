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

public class ASTInquireSpecNode extends InteriorNode
{
    ASTInquireSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTInquireSpecNode(this);
    }

    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_841)
            return (ASTUnitIdentifierNode)getChild(1);
        else
            return null;
    }

    public boolean hasUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_841)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getFileExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_842)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasFileExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_842)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTLblRefNode getErrVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_843)
            return (ASTLblRefNode)getChild(1);
        else
            return null;
    }

    public boolean hasErrVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_843)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getIoStatVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_844)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasIoStatVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_844)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getExistVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_845)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasExistVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_845)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getOpenedVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_846)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasOpenedVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_846)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getNumberVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_847)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasNumberVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_847)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getNamedVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_848)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasNamedVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_848)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getNameVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_849)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasNameVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_849)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getAccessVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_850)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasAccessVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_850)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getSequentialVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_851)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasSequentialVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_851)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getDirectVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_852)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasDirectVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_852)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getFormVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_853)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasFormVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_853)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getFormattedVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_854)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasFormattedVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_854)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getUnformattedVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_855)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasUnformattedVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_855)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTExpressionNode getReclExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_856)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public boolean hasReclExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_856)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getNextRecVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_857)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasNextRecVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_857)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getBlankVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_858)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasBlankVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_858)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getPositionVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_859)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasPositionVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_859)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getActionVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_860)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasActionVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_860)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getReadVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_861)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasReadVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_861)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getWriteVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_862)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasWriteVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_862)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getReadWriteVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_863)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasReadWriteVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_863)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getDelimVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_864)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasDelimVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_864)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTScalarVariableNode getPadVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_865)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public boolean hasPadVar()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_865)
            return getChild(1) != null;
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.INQUIRE_SPEC_841 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_842 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_843 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_844 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_845 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_846 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_847 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_848 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_849 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_850 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_851 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_852 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_853 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_854 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_855 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_856 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_857 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_858 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_859 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_860 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_861 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_862 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_863 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_864 && index == 0)
            return false;
        else if (getProduction() == Production.INQUIRE_SPEC_865 && index == 0)
            return false;
        else
            return true;
    }
}
