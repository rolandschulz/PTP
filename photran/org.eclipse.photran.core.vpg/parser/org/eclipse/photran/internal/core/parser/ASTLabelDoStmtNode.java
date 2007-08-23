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

import org.eclipse.photran.internal.core.lexer.Token;
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
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTLabelDoStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_702)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_703)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_704)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_705)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_706)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_707)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_708)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.LABEL_DO_STMT_709)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTDo()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_702)
            return (Token)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_703)
            return (Token)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_704)
            return (Token)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_705)
            return (Token)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_706)
            return (Token)getChild(3);
        else if (getProduction() == Production.LABEL_DO_STMT_707)
            return (Token)getChild(3);
        else if (getProduction() == Production.LABEL_DO_STMT_708)
            return (Token)getChild(3);
        else if (getProduction() == Production.LABEL_DO_STMT_709)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTLblRefNode getLblRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_702)
            return (ASTLblRefNode)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_703)
            return (ASTLblRefNode)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_706)
            return (ASTLblRefNode)getChild(4);
        else if (getProduction() == Production.LABEL_DO_STMT_707)
            return (ASTLblRefNode)getChild(4);
        else
            return null;
    }

    public ASTCommaLoopControlNode getCommaLoopControl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_702)
            return (ASTCommaLoopControlNode)getChild(3);
        else if (getProduction() == Production.LABEL_DO_STMT_704)
            return (ASTCommaLoopControlNode)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_706)
            return (ASTCommaLoopControlNode)getChild(5);
        else if (getProduction() == Production.LABEL_DO_STMT_708)
            return (ASTCommaLoopControlNode)getChild(4);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_702)
            return (Token)getChild(4);
        else if (getProduction() == Production.LABEL_DO_STMT_703)
            return (Token)getChild(3);
        else if (getProduction() == Production.LABEL_DO_STMT_704)
            return (Token)getChild(3);
        else if (getProduction() == Production.LABEL_DO_STMT_705)
            return (Token)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_706)
            return (Token)getChild(6);
        else if (getProduction() == Production.LABEL_DO_STMT_707)
            return (Token)getChild(5);
        else if (getProduction() == Production.LABEL_DO_STMT_708)
            return (Token)getChild(5);
        else if (getProduction() == Production.LABEL_DO_STMT_709)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_706)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_707)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_708)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.LABEL_DO_STMT_709)
            return (ASTNameNode)getChild(1);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LABEL_DO_STMT_706)
            return (Token)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_707)
            return (Token)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_708)
            return (Token)getChild(2);
        else if (getProduction() == Production.LABEL_DO_STMT_709)
            return (Token)getChild(2);
        else
            return null;
    }
}
