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

public class ASTLabelDoStmtNode extends InteriorNode
{
    ASTLabelDoStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTLabelDoStmtNode(this);
    }

    public ASTLblRefNode getLblRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_703)
            return (ASTLblRefNode)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_704)
            return (ASTLblRefNode)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_707)
            return (ASTLblRefNode)getChild(4);
        else if (getProduction() == Production.LABEL_DO_STMT_708)
            return (ASTLblRefNode)getChild(4);
        else
            return null;
    }

    public boolean hasLblRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_703)
            return getChild(2) != null;
        else if (getProduction() == Production.LABEL_DO_STMT_704)
            return getChild(2) != null;
        else if (getProduction() == Production.LABEL_DO_STMT_707)
            return getChild(4) != null;
        else if (getProduction() == Production.LABEL_DO_STMT_708)
            return getChild(4) != null;
        else
            return false;
    }

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_703)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_704)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_705)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_706)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_707)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_708)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_709)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_710)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_703)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_704)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_705)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_706)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_707)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_708)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_709)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.LABEL_DO_STMT_710)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    public ASTLoopControlNode getLoopControl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_703)
            return (ASTLoopControlNode)((ASTCommaLoopControlNode)getChild(3)).getLoopControl();
        else if (getProduction() == Production.LABEL_DO_STMT_705)
            return (ASTLoopControlNode)((ASTCommaLoopControlNode)getChild(2)).getLoopControl();
        else if (getProduction() == Production.LABEL_DO_STMT_707)
            return (ASTLoopControlNode)((ASTCommaLoopControlNode)getChild(5)).getLoopControl();
        else if (getProduction() == Production.LABEL_DO_STMT_709)
            return (ASTLoopControlNode)((ASTCommaLoopControlNode)getChild(4)).getLoopControl();
        else
            return null;
    }

    public boolean hasLoopControl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_703)
            return ((ASTCommaLoopControlNode)getChild(3)).hasLoopControl();
        else if (getProduction() == Production.LABEL_DO_STMT_705)
            return ((ASTCommaLoopControlNode)getChild(2)).hasLoopControl();
        else if (getProduction() == Production.LABEL_DO_STMT_707)
            return ((ASTCommaLoopControlNode)getChild(5)).hasLoopControl();
        else if (getProduction() == Production.LABEL_DO_STMT_709)
            return ((ASTCommaLoopControlNode)getChild(4)).hasLoopControl();
        else
            return false;
    }

    public Token getDoConstructName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_707)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.LABEL_DO_STMT_708)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.LABEL_DO_STMT_709)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else if (getProduction() == Production.LABEL_DO_STMT_710)
            return (Token)((ASTNameNode)getChild(1)).getName();
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.LABEL_DO_STMT_703 && index == 1)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_703 && index == 4)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_704 && index == 1)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_704 && index == 3)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_705 && index == 1)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_705 && index == 3)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_706 && index == 1)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_706 && index == 2)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_707 && index == 2)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_707 && index == 3)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_707 && index == 6)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_708 && index == 2)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_708 && index == 3)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_708 && index == 5)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_709 && index == 2)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_709 && index == 3)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_709 && index == 5)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_710 && index == 2)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_710 && index == 3)
            return false;
        else if (getProduction() == Production.LABEL_DO_STMT_710 && index == 4)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.LABEL_DO_STMT_703 && index == 0)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_703 && index == 3)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_704 && index == 0)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_705 && index == 0)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_705 && index == 2)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_706 && index == 0)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_707 && index == 0)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_707 && index == 1)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_707 && index == 5)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_708 && index == 0)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_708 && index == 1)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_709 && index == 0)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_709 && index == 1)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_709 && index == 4)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_710 && index == 0)
            return true;
        else if (getProduction() == Production.LABEL_DO_STMT_710 && index == 1)
            return true;
        else
            return false;
    }
}
