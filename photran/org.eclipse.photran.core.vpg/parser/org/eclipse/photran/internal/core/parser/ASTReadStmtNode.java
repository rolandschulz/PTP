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

public class ASTReadStmtNode extends InteriorNode implements IActionStmt
{
    ASTReadStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitIActionStmt(this);
        visitor.visitASTReadStmtNode(this);
    }

    public ASTRdCtlSpecNode getRdCtlSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_765)
            return (ASTRdCtlSpecNode)getChild(2);
        else if (getProduction() == Production.READ_STMT_766)
            return (ASTRdCtlSpecNode)getChild(2);
        else
            return null;
    }

    public boolean hasRdCtlSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_765)
            return getChild(2) != null;
        else if (getProduction() == Production.READ_STMT_766)
            return getChild(2) != null;
        else
            return false;
    }

    public ASTInputItemListNode getInputItemList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_765)
            return (ASTInputItemListNode)getChild(3);
        else if (getProduction() == Production.READ_STMT_767)
            return (ASTInputItemListNode)getChild(4);
        else
            return null;
    }

    public boolean hasInputItemList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_765)
            return getChild(3) != null;
        else if (getProduction() == Production.READ_STMT_767)
            return getChild(4) != null;
        else
            return false;
    }

    public ASTRdFmtIdNode getRdFmtId()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_767)
            return (ASTRdFmtIdNode)getChild(2);
        else if (getProduction() == Production.READ_STMT_768)
            return (ASTRdFmtIdNode)getChild(2);
        else
            return null;
    }

    public boolean hasRdFmtId()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_767)
            return getChild(2) != null;
        else if (getProduction() == Production.READ_STMT_768)
            return getChild(2) != null;
        else
            return false;
    }

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_765)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.READ_STMT_766)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.READ_STMT_767)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.READ_STMT_768)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_765)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.READ_STMT_766)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.READ_STMT_767)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.READ_STMT_768)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.READ_STMT_765 && index == 1)
            return false;
        else if (getProduction() == Production.READ_STMT_765 && index == 4)
            return false;
        else if (getProduction() == Production.READ_STMT_766 && index == 1)
            return false;
        else if (getProduction() == Production.READ_STMT_766 && index == 3)
            return false;
        else if (getProduction() == Production.READ_STMT_767 && index == 1)
            return false;
        else if (getProduction() == Production.READ_STMT_767 && index == 3)
            return false;
        else if (getProduction() == Production.READ_STMT_767 && index == 5)
            return false;
        else if (getProduction() == Production.READ_STMT_768 && index == 1)
            return false;
        else if (getProduction() == Production.READ_STMT_768 && index == 3)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.READ_STMT_765 && index == 0)
            return true;
        else if (getProduction() == Production.READ_STMT_766 && index == 0)
            return true;
        else if (getProduction() == Production.READ_STMT_767 && index == 0)
            return true;
        else if (getProduction() == Production.READ_STMT_768 && index == 0)
            return true;
        else
            return false;
    }
}
