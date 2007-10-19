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

public class ASTSFExprListNode extends InteriorNode
{
    ASTSFExprListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTSFExprListNode(this);
    }

    public ASTSFExprNode getSFExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_LIST_554)
            return (ASTSFExprNode)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_555)
            return (ASTSFExprNode)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_573)
            return (ASTSFExprNode)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_574)
            return (ASTSFExprNode)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_575)
            return (ASTSFExprNode)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_579)
            return (ASTSFExprNode)getChild(2);
        else if (getProduction() == Production.SFEXPR_LIST_580)
            return (ASTSFExprNode)getChild(2);
        else if (getProduction() == Production.SFEXPR_LIST_581)
            return (ASTSFExprNode)getChild(2);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_LIST_554)
            return (Token)getChild(1);
        else if (getProduction() == Production.SFEXPR_LIST_555)
            return (Token)getChild(1);
        else if (getProduction() == Production.SFEXPR_LIST_556)
            return (Token)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_557)
            return (Token)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_571)
            return (Token)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_572)
            return (Token)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_574)
            return (Token)getChild(1);
        else if (getProduction() == Production.SFEXPR_LIST_575)
            return (Token)getChild(1);
        else if (getProduction() == Production.SFEXPR_LIST_577)
            return (Token)getChild(2);
        else if (getProduction() == Production.SFEXPR_LIST_578)
            return (Token)getChild(2);
        else if (getProduction() == Production.SFEXPR_LIST_580)
            return (Token)getChild(3);
        else if (getProduction() == Production.SFEXPR_LIST_581)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_LIST_554)
            return (ASTExprNode)getChild(2);
        else if (getProduction() == Production.SFEXPR_LIST_555)
            return (ASTExprNode)getChild(3);
        else if (getProduction() == Production.SFEXPR_LIST_556)
            return (ASTExprNode)getChild(1);
        else if (getProduction() == Production.SFEXPR_LIST_557)
            return (ASTExprNode)getChild(2);
        else if (getProduction() == Production.SFEXPR_LIST_572)
            return (ASTExprNode)getChild(1);
        else if (getProduction() == Production.SFEXPR_LIST_575)
            return (ASTExprNode)getChild(2);
        else if (getProduction() == Production.SFEXPR_LIST_578)
            return (ASTExprNode)getChild(3);
        else if (getProduction() == Production.SFEXPR_LIST_581)
            return (ASTExprNode)getChild(4);
        else
            return null;
    }

    public Token getTColon2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_LIST_554)
            return (Token)getChild(3);
        else if (getProduction() == Production.SFEXPR_LIST_555)
            return (Token)getChild(2);
        else if (getProduction() == Production.SFEXPR_LIST_556)
            return (Token)getChild(2);
        else if (getProduction() == Production.SFEXPR_LIST_557)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTExprNode getExpr2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_LIST_554)
            return (ASTExprNode)getChild(4);
        else if (getProduction() == Production.SFEXPR_LIST_556)
            return (ASTExprNode)getChild(3);
        else
            return null;
    }

    public ASTSFExprListNode getSFExprList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_LIST_576)
            return (ASTSFExprListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_LIST_576)
            return (Token)getChild(1);
        else if (getProduction() == Production.SFEXPR_LIST_577)
            return (Token)getChild(1);
        else if (getProduction() == Production.SFEXPR_LIST_578)
            return (Token)getChild(1);
        else if (getProduction() == Production.SFEXPR_LIST_579)
            return (Token)getChild(1);
        else if (getProduction() == Production.SFEXPR_LIST_580)
            return (Token)getChild(1);
        else if (getProduction() == Production.SFEXPR_LIST_581)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTSectionSubscriptNode getSectionSubscript()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_LIST_576)
            return (ASTSectionSubscriptNode)getChild(2);
        else
            return null;
    }

    public ASTSFDummyArgNameListNode getSFDummyArgNameList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_LIST_577)
            return (ASTSFDummyArgNameListNode)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_578)
            return (ASTSFDummyArgNameListNode)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_579)
            return (ASTSFDummyArgNameListNode)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_580)
            return (ASTSFDummyArgNameListNode)getChild(0);
        else if (getProduction() == Production.SFEXPR_LIST_581)
            return (ASTSFDummyArgNameListNode)getChild(0);
        else
            return null;
    }
}
