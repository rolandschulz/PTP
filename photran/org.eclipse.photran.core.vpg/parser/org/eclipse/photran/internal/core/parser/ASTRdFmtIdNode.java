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

public class ASTRdFmtIdNode extends InteriorNode
{
    ASTRdFmtIdNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTRdFmtIdNode(this);
    }

    public boolean formatIsAsterisk()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_778)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTPrimaryNode getPrimary1()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_779)
            return (ASTPrimaryNode)getChild(0);
        else if (getProduction() == Production.RD_FMT_ID_780)
            return (ASTPrimaryNode)getChild(0);
        else
            return null;
    }

    public boolean hasPrimary1()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_779)
            return getChild(0) != null;
        else if (getProduction() == Production.RD_FMT_ID_780)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTPrimaryNode getPrimary2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return (ASTPrimaryNode)getChild(2);
        else if (getProduction() == Production.RD_FMT_ID_781)
            return (ASTPrimaryNode)getChild(2);
        else
            return null;
    }

    public boolean hasPrimary2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return getChild(2) != null;
        else if (getProduction() == Production.RD_FMT_ID_781)
            return getChild(2) != null;
        else
            return false;
    }

    public Token getFormatLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_777)
            return (Token)((ASTLblRefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasFormatLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_777)
            return ((ASTLblRefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    public boolean hasPowerOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else
            return false;
    }

    public boolean hasTimesOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else
            return false;
    }

    public boolean hasDivideOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else
            return false;
    }

    public boolean hasPlusOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else
            return false;
    }

    public boolean hasMinusOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else
            return false;
    }

    public boolean hasConcatOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else
            return false;
    }

    public boolean hasEqOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else
            return false;
    }

    public boolean hasNeOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else
            return false;
    }

    public boolean hasLtOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else
            return false;
    }

    public boolean hasLeOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else
            return false;
    }

    public boolean hasGtOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else
            return false;
    }

    public boolean hasGeOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else
            return false;
    }

    public boolean hasEqEqOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else
            return false;
    }

    public boolean hasSlashEqOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else
            return false;
    }

    public boolean hasNotOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else
            return false;
    }

    public boolean hasAndOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else
            return false;
    }

    public boolean hasOrOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else
            return false;
    }

    public boolean hasEqvOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else
            return false;
    }

    public boolean hasNeqvOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else
            return false;
    }

    public Token getCustomDefinedOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else
            return null;
    }

    public boolean hasCustomDefinedOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else
            return false;
    }

    public Token getDefinedUnaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else
            return null;
    }

    public boolean hasDefinedUnaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else
            return false;
    }

    public Token getDefinedBinaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else
            return null;
    }

    public boolean hasDefinedBinaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_780)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.RD_FMT_ID_781)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else
            return false;
    }

    public ASTExpressionNode getFormatIdExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_781)
            return (ASTExpressionNode)((ASTRdFmtIdExprNode)getChild(0)).getFormatIdExpr();
        else
            return null;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.RD_FMT_ID_777 && index == 0)
            return true;
        else if (getProduction() == Production.RD_FMT_ID_780 && index == 1)
            return true;
        else if (getProduction() == Production.RD_FMT_ID_781 && index == 0)
            return true;
        else if (getProduction() == Production.RD_FMT_ID_781 && index == 1)
            return true;
        else
            return false;
    }
}
