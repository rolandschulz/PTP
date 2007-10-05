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

public class ASTSubroutineStmtNode extends InteriorNodeWithErrorRecoverySymbols
{
    ASTSubroutineStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production, discardedSymbols);
         
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
        visitor.visitASTSubroutineStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_STMT_995)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.SUBROUTINE_STMT_996)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.SUBROUTINE_STMT_997)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.SUBROUTINE_STMT_ERROR_2)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public ASTSubroutinePrefixNode getSubroutinePrefix()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_STMT_995)
            return (ASTSubroutinePrefixNode)getChild(1);
        else if (getProduction() == Production.SUBROUTINE_STMT_996)
            return (ASTSubroutinePrefixNode)getChild(1);
        else if (getProduction() == Production.SUBROUTINE_STMT_997)
            return (ASTSubroutinePrefixNode)getChild(1);
        else if (getProduction() == Production.SUBROUTINE_STMT_ERROR_2)
            return (ASTSubroutinePrefixNode)getChild(1);
        else
            return null;
    }

    public ASTSubroutineNameNode getSubroutineName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_STMT_995)
            return (ASTSubroutineNameNode)getChild(2);
        else if (getProduction() == Production.SUBROUTINE_STMT_996)
            return (ASTSubroutineNameNode)getChild(2);
        else if (getProduction() == Production.SUBROUTINE_STMT_997)
            return (ASTSubroutineNameNode)getChild(2);
        else if (getProduction() == Production.SUBROUTINE_STMT_ERROR_2)
            return (ASTSubroutineNameNode)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_STMT_995)
            return (Token)getChild(3);
        else if (getProduction() == Production.SUBROUTINE_STMT_996)
            return (Token)getChild(5);
        else if (getProduction() == Production.SUBROUTINE_STMT_997)
            return (Token)getChild(6);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_STMT_996)
            return (Token)getChild(3);
        else if (getProduction() == Production.SUBROUTINE_STMT_997)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_STMT_996)
            return (Token)getChild(4);
        else if (getProduction() == Production.SUBROUTINE_STMT_997)
            return (Token)getChild(5);
        else
            return null;
    }

    public ASTSubroutineParsNode getSubroutinePars()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_STMT_997)
            return (ASTSubroutineParsNode)getChild(4);
        else
            return null;
    }
}
