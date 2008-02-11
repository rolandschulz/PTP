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

public class ASTIfConstructNode extends InteriorNode implements IExecutableConstruct
{
    ASTIfConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitIExecutableConstruct(this);
        visitor.visitASTIfConstructNode(this);
    }

    public ASTIfThenStmtNode getIfThenStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_CONSTRUCT_654)
            return (ASTIfThenStmtNode)getChild(0);
        else if (getProduction() == Production.IF_CONSTRUCT_655)
            return (ASTIfThenStmtNode)getChild(0);
        else if (getProduction() == Production.IF_CONSTRUCT_656)
            return (ASTIfThenStmtNode)getChild(0);
        else if (getProduction() == Production.IF_CONSTRUCT_657)
            return (ASTIfThenStmtNode)getChild(0);
        else
            return null;
    }

    public ASTConditionalBodyNode getThenBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_CONSTRUCT_654)
            return (ASTConditionalBodyNode)getChild(1);
        else if (getProduction() == Production.IF_CONSTRUCT_655)
            return (ASTConditionalBodyNode)getChild(1);
        else if (getProduction() == Production.IF_CONSTRUCT_656)
            return (ASTConditionalBodyNode)getChild(1);
        else if (getProduction() == Production.IF_CONSTRUCT_657)
            return (ASTConditionalBodyNode)getChild(1);
        else
            return null;
    }

    public ASTElseIfPartsNode getElseIfParts()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_CONSTRUCT_654)
            return (ASTElseIfPartsNode)getChild(2);
        else if (getProduction() == Production.IF_CONSTRUCT_656)
            return (ASTElseIfPartsNode)getChild(2);
        else
            return null;
    }

    public boolean hasElseIfParts()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_CONSTRUCT_654)
            return getChild(2) != null;
        else if (getProduction() == Production.IF_CONSTRUCT_656)
            return getChild(2) != null;
        else
            return false;
    }

    public ASTElseStmtNode getElseStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_CONSTRUCT_654)
            return (ASTElseStmtNode)getChild(3);
        else if (getProduction() == Production.IF_CONSTRUCT_655)
            return (ASTElseStmtNode)getChild(2);
        else
            return null;
    }

    public boolean hasElseStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_CONSTRUCT_654)
            return getChild(3) != null;
        else if (getProduction() == Production.IF_CONSTRUCT_655)
            return getChild(2) != null;
        else
            return false;
    }

    public ASTConditionalBodyNode getElseBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_CONSTRUCT_654)
            return (ASTConditionalBodyNode)getChild(4);
        else if (getProduction() == Production.IF_CONSTRUCT_655)
            return (ASTConditionalBodyNode)getChild(3);
        else
            return null;
    }

    public boolean hasElseBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_CONSTRUCT_654)
            return getChild(4) != null;
        else if (getProduction() == Production.IF_CONSTRUCT_655)
            return getChild(3) != null;
        else
            return false;
    }

    public ASTEndIfStmtNode getEndIfStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_CONSTRUCT_654)
            return (ASTEndIfStmtNode)getChild(5);
        else if (getProduction() == Production.IF_CONSTRUCT_655)
            return (ASTEndIfStmtNode)getChild(4);
        else if (getProduction() == Production.IF_CONSTRUCT_656)
            return (ASTEndIfStmtNode)getChild(3);
        else if (getProduction() == Production.IF_CONSTRUCT_657)
            return (ASTEndIfStmtNode)getChild(2);
        else
            return null;
    }
}
