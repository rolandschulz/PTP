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

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_709)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_710)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_711)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_712)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_713)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_714)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_715)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_716)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTDo()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_709)
            return (Token)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_710)
            return (Token)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_711)
            return (Token)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_712)
            return (Token)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_713)
            return (Token)getChild(3);
        else if (getProduction() == Production.LABEL_DO_STMT_714)
            return (Token)getChild(3);
        else if (getProduction() == Production.LABEL_DO_STMT_715)
            return (Token)getChild(3);
        else if (getProduction() == Production.LABEL_DO_STMT_716)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTLblRefNode getLblRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_709)
            return (ASTLblRefNode)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_710)
            return (ASTLblRefNode)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_713)
            return (ASTLblRefNode)getChild(4);
        else if (getProduction() == Production.LABEL_DO_STMT_714)
            return (ASTLblRefNode)getChild(4);
        else
            return null;
    }

    public ASTCommaLoopControlNode getCommaLoopControl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_709)
            return (ASTCommaLoopControlNode)getChild(3);
        else if (getProduction() == Production.LABEL_DO_STMT_711)
            return (ASTCommaLoopControlNode)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_713)
            return (ASTCommaLoopControlNode)getChild(5);
        else if (getProduction() == Production.LABEL_DO_STMT_715)
            return (ASTCommaLoopControlNode)getChild(4);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_709)
            return (Token)getChild(4);
        else if (getProduction() == Production.LABEL_DO_STMT_710)
            return (Token)getChild(3);
        else if (getProduction() == Production.LABEL_DO_STMT_711)
            return (Token)getChild(3);
        else if (getProduction() == Production.LABEL_DO_STMT_712)
            return (Token)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_713)
            return (Token)getChild(6);
        else if (getProduction() == Production.LABEL_DO_STMT_714)
            return (Token)getChild(5);
        else if (getProduction() == Production.LABEL_DO_STMT_715)
            return (Token)getChild(5);
        else if (getProduction() == Production.LABEL_DO_STMT_716)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_713)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_714)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_715)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_716)
            return (ASTNameNode)getChild(1);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_713)
            return (Token)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_714)
            return (Token)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_715)
            return (Token)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_716)
            return (Token)getChild(2);
        else
            return null;
    }
}
