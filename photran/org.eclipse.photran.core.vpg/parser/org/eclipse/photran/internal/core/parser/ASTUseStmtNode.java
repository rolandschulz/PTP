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

public class ASTUseStmtNode extends InteriorNode implements ISpecificationPartConstruct
{
    ASTUseStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitISpecificationPartConstruct(this);
        visitor.visitASTUseStmtNode(this);
    }

    public Token getUseToken()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_901)
            return (Token)getChild(1);
        else if (getProduction() == Production.USE_STMT_902)
            return (Token)getChild(1);
        else if (getProduction() == Production.USE_STMT_903)
            return (Token)getChild(1);
        else if (getProduction() == Production.USE_STMT_904)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTRenameListNode getRenameList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_902)
            return (ASTRenameListNode)getChild(4);
        else
            return null;
    }

    public boolean hasRenameList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_902)
            return getChild(4) != null;
        else
            return false;
    }

    public ASTOnlyListNode getOnlyList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_904)
            return (ASTOnlyListNode)getChild(6);
        else
            return null;
    }

    public boolean hasOnlyList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_904)
            return getChild(6) != null;
        else
            return false;
    }

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_901)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.USE_STMT_902)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.USE_STMT_903)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.USE_STMT_904)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_901)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.USE_STMT_902)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.USE_STMT_903)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.USE_STMT_904)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    public Token getModuleName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.USE_STMT_901)
            return (Token)((ASTNameNode)getChild(2)).getName();
        else if (getProduction() == Production.USE_STMT_902)
            return (Token)((ASTNameNode)getChild(2)).getName();
        else if (getProduction() == Production.USE_STMT_903)
            return (Token)((ASTNameNode)getChild(2)).getName();
        else if (getProduction() == Production.USE_STMT_904)
            return (Token)((ASTNameNode)getChild(2)).getName();
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.USE_STMT_901 && index == 3)
            return false;
        else if (getProduction() == Production.USE_STMT_902 && index == 3)
            return false;
        else if (getProduction() == Production.USE_STMT_902 && index == 5)
            return false;
        else if (getProduction() == Production.USE_STMT_903 && index == 3)
            return false;
        else if (getProduction() == Production.USE_STMT_903 && index == 4)
            return false;
        else if (getProduction() == Production.USE_STMT_903 && index == 5)
            return false;
        else if (getProduction() == Production.USE_STMT_903 && index == 6)
            return false;
        else if (getProduction() == Production.USE_STMT_904 && index == 3)
            return false;
        else if (getProduction() == Production.USE_STMT_904 && index == 4)
            return false;
        else if (getProduction() == Production.USE_STMT_904 && index == 5)
            return false;
        else if (getProduction() == Production.USE_STMT_904 && index == 7)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.USE_STMT_901 && index == 0)
            return true;
        else if (getProduction() == Production.USE_STMT_901 && index == 2)
            return true;
        else if (getProduction() == Production.USE_STMT_902 && index == 0)
            return true;
        else if (getProduction() == Production.USE_STMT_902 && index == 2)
            return true;
        else if (getProduction() == Production.USE_STMT_903 && index == 0)
            return true;
        else if (getProduction() == Production.USE_STMT_903 && index == 2)
            return true;
        else if (getProduction() == Production.USE_STMT_904 && index == 0)
            return true;
        else if (getProduction() == Production.USE_STMT_904 && index == 2)
            return true;
        else
            return false;
    }
}
