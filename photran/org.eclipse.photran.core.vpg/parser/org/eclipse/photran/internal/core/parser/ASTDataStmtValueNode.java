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

public class ASTDataStmtValueNode extends InteriorNode
{
    ASTDataStmtValueNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTDataStmtValueNode(this);
    }

    public boolean hasConstIntKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_VALUE_386)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTNamedConstantUseNode getNamedConstKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_VALUE_387)
            return (ASTNamedConstantUseNode)getChild(0);
        else
            return null;
    }

    public boolean hasNamedConstKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_VALUE_387)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTConstantNode getConstant()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_VALUE_385)
            return (ASTConstantNode)((ASTDataStmtConstantNode)getChild(0)).getConstant();
        else if (getProduction() == Production.DATA_STMT_VALUE_386)
            return (ASTConstantNode)((ASTDataStmtConstantNode)getChild(2)).getConstant();
        else if (getProduction() == Production.DATA_STMT_VALUE_387)
            return (ASTConstantNode)((ASTDataStmtConstantNode)getChild(2)).getConstant();
        else
            return null;
    }

    public boolean hasConstant()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_VALUE_385)
            return ((ASTDataStmtConstantNode)getChild(0)).hasConstant();
        else if (getProduction() == Production.DATA_STMT_VALUE_386)
            return ((ASTDataStmtConstantNode)getChild(2)).hasConstant();
        else if (getProduction() == Production.DATA_STMT_VALUE_387)
            return ((ASTDataStmtConstantNode)getChild(2)).hasConstant();
        else
            return false;
    }

    public boolean isNull()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_VALUE_385)
            return ((ASTDataStmtConstantNode)getChild(0)).isNull();
        else if (getProduction() == Production.DATA_STMT_VALUE_386)
            return ((ASTDataStmtConstantNode)getChild(2)).isNull();
        else if (getProduction() == Production.DATA_STMT_VALUE_387)
            return ((ASTDataStmtConstantNode)getChild(2)).isNull();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.DATA_STMT_VALUE_386 && index == 1)
            return false;
        else if (getProduction() == Production.DATA_STMT_VALUE_387 && index == 1)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.DATA_STMT_VALUE_385 && index == 0)
            return true;
        else if (getProduction() == Production.DATA_STMT_VALUE_386 && index == 2)
            return true;
        else if (getProduction() == Production.DATA_STMT_VALUE_387 && index == 2)
            return true;
        else
            return false;
    }
}
