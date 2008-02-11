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

public class ASTEndSelectStmtNode extends InteriorNode
{
    ASTEndSelectStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTEndSelectStmtNode(this);
    }

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SELECT_STMT_689)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.END_SELECT_STMT_690)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.END_SELECT_STMT_691)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.END_SELECT_STMT_692)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SELECT_STMT_689)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.END_SELECT_STMT_690)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.END_SELECT_STMT_691)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.END_SELECT_STMT_692)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    public Token getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SELECT_STMT_690)
            return (Token)((ASTEndNameNode)getChild(2)).getEndName();
        else if (getProduction() == Production.END_SELECT_STMT_692)
            return (Token)((ASTEndNameNode)getChild(3)).getEndName();
        else
            return null;
    }

    public boolean hasEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SELECT_STMT_690)
            return ((ASTEndNameNode)getChild(2)).hasEndName();
        else if (getProduction() == Production.END_SELECT_STMT_692)
            return ((ASTEndNameNode)getChild(3)).hasEndName();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.END_SELECT_STMT_689 && index == 1)
            return false;
        else if (getProduction() == Production.END_SELECT_STMT_689 && index == 2)
            return false;
        else if (getProduction() == Production.END_SELECT_STMT_690 && index == 1)
            return false;
        else if (getProduction() == Production.END_SELECT_STMT_690 && index == 3)
            return false;
        else if (getProduction() == Production.END_SELECT_STMT_691 && index == 1)
            return false;
        else if (getProduction() == Production.END_SELECT_STMT_691 && index == 2)
            return false;
        else if (getProduction() == Production.END_SELECT_STMT_691 && index == 3)
            return false;
        else if (getProduction() == Production.END_SELECT_STMT_692 && index == 1)
            return false;
        else if (getProduction() == Production.END_SELECT_STMT_692 && index == 2)
            return false;
        else if (getProduction() == Production.END_SELECT_STMT_692 && index == 4)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.END_SELECT_STMT_689 && index == 0)
            return true;
        else if (getProduction() == Production.END_SELECT_STMT_690 && index == 0)
            return true;
        else if (getProduction() == Production.END_SELECT_STMT_690 && index == 2)
            return true;
        else if (getProduction() == Production.END_SELECT_STMT_691 && index == 0)
            return true;
        else if (getProduction() == Production.END_SELECT_STMT_692 && index == 0)
            return true;
        else if (getProduction() == Production.END_SELECT_STMT_692 && index == 3)
            return true;
        else
            return false;
    }
}
