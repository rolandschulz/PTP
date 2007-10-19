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

public class ASTEndSubroutineStmtNode extends InteriorNode
{
    ASTEndSubroutineStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTEndSubroutineStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SUBROUTINE_STMT_1011)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1012)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1013)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1014)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1015)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTEnd()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SUBROUTINE_STMT_1011)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1014)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1015)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SUBROUTINE_STMT_1011)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1012)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1013)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1014)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1015)
            return (Token)getChild(4);
        else
            return null;
    }

    public Token getTEndsubroutine()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SUBROUTINE_STMT_1012)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1013)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTEndNameNode getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SUBROUTINE_STMT_1013)
            return (ASTEndNameNode)getChild(2);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1015)
            return (ASTEndNameNode)getChild(3);
        else
            return null;
    }

    public Token getTSubroutine()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_SUBROUTINE_STMT_1014)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_SUBROUTINE_STMT_1015)
            return (Token)getChild(2);
        else
            return null;
    }
}
