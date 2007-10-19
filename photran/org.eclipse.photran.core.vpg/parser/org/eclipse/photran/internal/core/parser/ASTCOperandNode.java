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

public class ASTCOperandNode extends InteriorNode
{
    ASTCOperandNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTCOperandNode(this);
    }

    public Token getTScon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_500)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_501)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_502)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_503)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_504)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_505)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_506)
            return (ASTNameNode)getChild(0);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_502)
            return (Token)getChild(1);
        else if (getProduction() == Production.COPERAND_504)
            return (Token)getChild(3);
        else if (getProduction() == Production.COPERAND_505)
            return (Token)getChild(1);
        else if (getProduction() == Production.COPERAND_506)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_502)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.COPERAND_504)
            return (ASTSectionSubscriptListNode)getChild(4);
        else if (getProduction() == Production.COPERAND_505)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.COPERAND_506)
            return (ASTSectionSubscriptListNode)getChild(2);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_502)
            return (Token)getChild(3);
        else if (getProduction() == Production.COPERAND_504)
            return (Token)getChild(5);
        else if (getProduction() == Production.COPERAND_505)
            return (Token)getChild(3);
        else if (getProduction() == Production.COPERAND_506)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTPercent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_503)
            return (Token)getChild(1);
        else if (getProduction() == Production.COPERAND_504)
            return (Token)getChild(1);
        else if (getProduction() == Production.COPERAND_505)
            return (Token)getChild(4);
        else if (getProduction() == Production.COPERAND_506)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTDataRefNode getDataRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_503)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.COPERAND_504)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.COPERAND_505)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.COPERAND_506)
            return (ASTDataRefNode)getChild(5);
        else
            return null;
    }

    public Token getTLparen2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_506)
            return (Token)getChild(6);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_506)
            return (ASTSectionSubscriptListNode)getChild(7);
        else
            return null;
    }

    public Token getTRparen2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_506)
            return (Token)getChild(8);
        else
            return null;
    }

    public ASTFunctionReferenceNode getFunctionReference()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_507)
            return (ASTFunctionReferenceNode)getChild(0);
        else
            return null;
    }
}
