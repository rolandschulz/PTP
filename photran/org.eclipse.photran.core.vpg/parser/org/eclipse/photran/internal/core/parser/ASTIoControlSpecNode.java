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

    public Token getTFmteq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_791)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTFormatIdentifierNode getFormatIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_791)
            return (ASTFormatIdentifierNode)getChild(1);
        else
            return null;
    }

    public Token getTUniteq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_792)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_792)
            return (ASTUnitIdentifierNode)getChild(1);
        else
            return null;
    }

    public Token getTReceq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_793)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_793)
            return (ASTExprNode)getChild(1);
        else
            return null;
    }

    public Token getTEndeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_794)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTLblRefNode getLblRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_794)
            return (ASTLblRefNode)getChild(1);
        else if (getProduction() == Production.IO_CONTROL_SPEC_795)
            return (ASTLblRefNode)getChild(1);
        else if (getProduction() == Production.IO_CONTROL_SPEC_800)
            return (ASTLblRefNode)getChild(1);
        else
            return null;
    }

    public Token getTErreq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_795)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTIostateq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_796)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTScalarVariableNode getScalarVariable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_796)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public Token getTNmleq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_797)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTNamelistGroupNameNode getNamelistGroupName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_797)
            return (ASTNamelistGroupNameNode)getChild(1);
        else
            return null;
    }

    public Token getTAdvanceeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_798)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTCExprNode getCExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_798)
            return (ASTCExprNode)getChild(1);
        else
            return null;
    }

    public Token getTSizeeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_799)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTVariableNode getVariable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_799)
            return (ASTVariableNode)getChild(1);
        else
            return null;
    }

    public Token getTEoreq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_800)
            return (Token)getChild(0);
        else
            return null;
    }
}
