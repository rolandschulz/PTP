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

public class ASTSpecificationStmtNode extends InteriorNode
{
    ASTSpecificationStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTSpecificationStmtNode(this);
    }

    public ASTAccessStmtNode getAccessStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_61)
            return (ASTAccessStmtNode)getChild(0);
        else
            return null;
    }

    public ASTAllocatableStmtNode getAllocatableStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_62)
            return (ASTAllocatableStmtNode)getChild(0);
        else
            return null;
    }

    public ASTCommonStmtNode getCommonStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_63)
            return (ASTCommonStmtNode)getChild(0);
        else
            return null;
    }

    public ASTDataStmtNode getDataStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_64)
            return (ASTDataStmtNode)getChild(0);
        else
            return null;
    }

    public ASTDimensionStmtNode getDimensionStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_65)
            return (ASTDimensionStmtNode)getChild(0);
        else
            return null;
    }

    public ASTEquivalenceStmtNode getEquivalenceStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_66)
            return (ASTEquivalenceStmtNode)getChild(0);
        else
            return null;
    }

    public ASTExternalStmtNode getExternalStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_67)
            return (ASTExternalStmtNode)getChild(0);
        else
            return null;
    }

    public ASTIntentStmtNode getIntentStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_68)
            return (ASTIntentStmtNode)getChild(0);
        else
            return null;
    }

    public ASTIntrinsicStmtNode getIntrinsicStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_69)
            return (ASTIntrinsicStmtNode)getChild(0);
        else
            return null;
    }

    public ASTNamelistStmtNode getNamelistStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_70)
            return (ASTNamelistStmtNode)getChild(0);
        else
            return null;
    }

    public ASTOptionalStmtNode getOptionalStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_71)
            return (ASTOptionalStmtNode)getChild(0);
        else
            return null;
    }

    public ASTPointerStmtNode getPointerStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_72)
            return (ASTPointerStmtNode)getChild(0);
        else
            return null;
    }

    public ASTSaveStmtNode getSaveStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_73)
            return (ASTSaveStmtNode)getChild(0);
        else
            return null;
    }

    public ASTTargetStmtNode getTargetStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SPECIFICATION_STMT_74)
            return (ASTTargetStmtNode)getChild(0);
        else
            return null;
    }
}
