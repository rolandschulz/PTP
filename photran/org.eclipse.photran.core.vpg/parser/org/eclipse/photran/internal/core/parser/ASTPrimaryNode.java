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

public class ASTPrimaryNode extends InteriorNode
{
    ASTPrimaryNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTPrimaryNode(this);
    }

    public ASTLogicalConstantNode getLogicalConstant()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_478)
            return (ASTLogicalConstantNode)getChild(0);
        else
            return null;
    }

    public Token getTScon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_479)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTUnsignedArithmeticConstantNode getUnsignedArithmeticConstant()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_480)
            return (ASTUnsignedArithmeticConstantNode)getChild(0);
        else
            return null;
    }

    public ASTArrayConstructorNode getArrayConstructor()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_481)
            return (ASTArrayConstructorNode)getChild(0);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_482)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_483)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_484)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_485)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_486)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_487)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_488)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_489)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_490)
            return (ASTNameNode)getChild(0);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_483)
            return (Token)getChild(1);
        else if (getProduction() == Production.PRIMARY_484)
            return (Token)getChild(1);
        else if (getProduction() == Production.PRIMARY_486)
            return (Token)getChild(3);
        else if (getProduction() == Production.PRIMARY_487)
            return (Token)getChild(3);
        else if (getProduction() == Production.PRIMARY_488)
            return (Token)getChild(1);
        else if (getProduction() == Production.PRIMARY_489)
            return (Token)getChild(1);
        else if (getProduction() == Production.PRIMARY_490)
            return (Token)getChild(1);
        else if (getProduction() == Production.PRIMARY_494)
            return (Token)getChild(3);
        else if (getProduction() == Production.PRIMARY_495)
            return (Token)getChild(3);
        else if (getProduction() == Production.PRIMARY_496)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_483)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_484)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_486)
            return (ASTSectionSubscriptListNode)getChild(4);
        else if (getProduction() == Production.PRIMARY_487)
            return (ASTSectionSubscriptListNode)getChild(4);
        else if (getProduction() == Production.PRIMARY_488)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_489)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_490)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_494)
            return (ASTSectionSubscriptListNode)getChild(4);
        else if (getProduction() == Production.PRIMARY_495)
            return (ASTSectionSubscriptListNode)getChild(4);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_483)
            return (Token)getChild(3);
        else if (getProduction() == Production.PRIMARY_484)
            return (Token)getChild(3);
        else if (getProduction() == Production.PRIMARY_486)
            return (Token)getChild(5);
        else if (getProduction() == Production.PRIMARY_487)
            return (Token)getChild(5);
        else if (getProduction() == Production.PRIMARY_488)
            return (Token)getChild(3);
        else if (getProduction() == Production.PRIMARY_489)
            return (Token)getChild(3);
        else if (getProduction() == Production.PRIMARY_490)
            return (Token)getChild(3);
        else if (getProduction() == Production.PRIMARY_494)
            return (Token)getChild(5);
        else if (getProduction() == Production.PRIMARY_495)
            return (Token)getChild(5);
        else if (getProduction() == Production.PRIMARY_496)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTSubstringRangeNode getSubstringRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_484)
            return (ASTSubstringRangeNode)getChild(4);
        else if (getProduction() == Production.PRIMARY_487)
            return (ASTSubstringRangeNode)getChild(6);
        else if (getProduction() == Production.PRIMARY_490)
            return (ASTSubstringRangeNode)getChild(9);
        else if (getProduction() == Production.PRIMARY_492)
            return (ASTSubstringRangeNode)getChild(1);
        else if (getProduction() == Production.PRIMARY_495)
            return (ASTSubstringRangeNode)getChild(6);
        else
            return null;
    }

    public Token getTPercent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_485)
            return (Token)getChild(1);
        else if (getProduction() == Production.PRIMARY_486)
            return (Token)getChild(1);
        else if (getProduction() == Production.PRIMARY_487)
            return (Token)getChild(1);
        else if (getProduction() == Production.PRIMARY_488)
            return (Token)getChild(4);
        else if (getProduction() == Production.PRIMARY_489)
            return (Token)getChild(4);
        else if (getProduction() == Production.PRIMARY_490)
            return (Token)getChild(4);
        else if (getProduction() == Production.PRIMARY_493)
            return (Token)getChild(1);
        else if (getProduction() == Production.PRIMARY_494)
            return (Token)getChild(1);
        else if (getProduction() == Production.PRIMARY_495)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTDataRefNode getDataRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_485)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_486)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_487)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_488)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.PRIMARY_489)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.PRIMARY_490)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.PRIMARY_493)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_494)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_495)
            return (ASTDataRefNode)getChild(2);
        else
            return null;
    }

    public Token getTLparen2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_489)
            return (Token)getChild(6);
        else if (getProduction() == Production.PRIMARY_490)
            return (Token)getChild(6);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_489)
            return (ASTSectionSubscriptListNode)getChild(7);
        else if (getProduction() == Production.PRIMARY_490)
            return (ASTSectionSubscriptListNode)getChild(7);
        else
            return null;
    }

    public Token getTRparen2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_489)
            return (Token)getChild(8);
        else if (getProduction() == Production.PRIMARY_490)
            return (Token)getChild(8);
        else
            return null;
    }

    public ASTFunctionReferenceNode getFunctionReference()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_491)
            return (ASTFunctionReferenceNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_492)
            return (ASTFunctionReferenceNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_493)
            return (ASTFunctionReferenceNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_494)
            return (ASTFunctionReferenceNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_495)
            return (ASTFunctionReferenceNode)getChild(0);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_496)
            return (ASTExprNode)getChild(1);
        else
            return null;
    }

    public ASTSubstrConstNode getSubstrConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_497)
            return (ASTSubstrConstNode)getChild(0);
        else
            return null;
    }
}
