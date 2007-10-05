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

public class ASTSpecificationPartConstructNode extends InteriorNode
{
    ASTSpecificationPartConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTSpecificationPartConstructNode(this);
    }

    public ASTUseStmtNode getUseStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_PART_CONSTRUCT_37)
            return (ASTUseStmtNode)getChild(0);
        else
            return null;
    }

    public ASTImplicitStmtNode getImplicitStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_PART_CONSTRUCT_38)
            return (ASTImplicitStmtNode)getChild(0);
        else
            return null;
    }

    public ASTParameterStmtNode getParameterStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_PART_CONSTRUCT_39)
            return (ASTParameterStmtNode)getChild(0);
        else
            return null;
    }

    public ASTFormatStmtNode getFormatStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_PART_CONSTRUCT_40)
            return (ASTFormatStmtNode)getChild(0);
        else
            return null;
    }

    public ASTEntryStmtNode getEntryStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_PART_CONSTRUCT_41)
            return (ASTEntryStmtNode)getChild(0);
        else
            return null;
    }

    public ASTDerivedTypeDefNode getDerivedTypeDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_PART_CONSTRUCT_42)
            return (ASTDerivedTypeDefNode)((ASTDeclarationConstructNode)getChild(0)).getDerivedTypeDef();
        else
            return null;
    }

    public ASTInterfaceBlockNode getInterfaceBlock()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_PART_CONSTRUCT_42)
            return (ASTInterfaceBlockNode)((ASTDeclarationConstructNode)getChild(0)).getInterfaceBlock();
        else
            return null;
    }

    public ASTTypeDeclarationStmtNode getTypeDeclarationStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_PART_CONSTRUCT_42)
            return (ASTTypeDeclarationStmtNode)((ASTDeclarationConstructNode)getChild(0)).getTypeDeclarationStmt();
        else
            return null;
    }

    public ASTSpecificationStmtNode getSpecificationStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_PART_CONSTRUCT_42)
            return (ASTSpecificationStmtNode)((ASTDeclarationConstructNode)getChild(0)).getSpecificationStmt();
        else
            return null;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.SPECIFICATION_PART_CONSTRUCT_42 && index == 0)
            return true;
        else
            return false;
    }
}
