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

public class ASTConstantNode extends InteriorNode
{
    ASTConstantNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTConstantNode(this);
    }

    public ASTNamedConstantUseNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_112)
            return (ASTNamedConstantUseNode)getChild(0);
        else
            return null;
    }

    public boolean hasName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_112)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTUnsignedArithmeticConstantNode getUnsignedArithmeticConstant()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_113)
            return (ASTUnsignedArithmeticConstantNode)getChild(0);
        else if (getProduction() == Production.CONSTANT_114)
            return (ASTUnsignedArithmeticConstantNode)getChild(1);
        else if (getProduction() == Production.CONSTANT_115)
            return (ASTUnsignedArithmeticConstantNode)getChild(1);
        else
            return null;
    }

    public boolean hasUnsignedArithmeticConstant()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_113)
            return getChild(0) != null;
        else if (getProduction() == Production.CONSTANT_114)
            return getChild(1) != null;
        else if (getProduction() == Production.CONSTANT_115)
            return getChild(1) != null;
        else
            return false;
    }

    public boolean hasPlus()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_114)
            return getChild(0) != null;
        else
            return false;
    }

    public boolean hasMinus()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_115)
            return getChild(0) != null;
        else
            return false;
    }

    public Token getStringConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_116)
            return (Token)getChild(0);
        else if (getProduction() == Production.CONSTANT_117)
            return (Token)getChild(2);
        else if (getProduction() == Production.CONSTANT_118)
            return (Token)getChild(2);
        else
            return null;
    }

    public boolean hasStringConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_116)
            return getChild(0) != null;
        else if (getProduction() == Production.CONSTANT_117)
            return getChild(2) != null;
        else if (getProduction() == Production.CONSTANT_118)
            return getChild(2) != null;
        else
            return false;
    }

    public boolean hasIntKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_117)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTNamedConstantUseNode getNamedConstantKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_118)
            return (ASTNamedConstantUseNode)getChild(0);
        else
            return null;
    }

    public boolean hasNamedConstantKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_118)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTLogicalConstantNode getLogicalConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_119)
            return (ASTLogicalConstantNode)getChild(0);
        else
            return null;
    }

    public boolean hasLogicalConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_119)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTStructureConstructorNode getStructureConstructor()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_120)
            return (ASTStructureConstructorNode)getChild(0);
        else
            return null;
    }

    public boolean hasStructureConstructor()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_120)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTBozLiteralConstantNode getBozLiteralConstant()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_121)
            return (ASTBozLiteralConstantNode)getChild(0);
        else
            return null;
    }

    public boolean hasBozLiteralConstant()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_121)
            return getChild(0) != null;
        else
            return false;
    }

    public Token getHollerithConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_122)
            return (Token)getChild(0);
        else
            return null;
    }

    public boolean hasHollerithConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_122)
            return getChild(0) != null;
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.CONSTANT_117 && index == 1)
            return false;
        else if (getProduction() == Production.CONSTANT_118 && index == 1)
            return false;
        else
            return true;
    }
}
