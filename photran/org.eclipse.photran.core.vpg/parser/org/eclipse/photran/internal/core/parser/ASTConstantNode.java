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

    public ASTNamedConstantUseNode getNamedConstantUse()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_111)
            return (ASTNamedConstantUseNode)getChild(0);
        else if (getProduction() == Production.CONSTANT_117)
            return (ASTNamedConstantUseNode)getChild(0);
        else
            return null;
    }

    public ASTUnsignedArithmeticConstantNode getUnsignedArithmeticConstant()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_112)
            return (ASTUnsignedArithmeticConstantNode)getChild(0);
        else if (getProduction() == Production.CONSTANT_113)
            return (ASTUnsignedArithmeticConstantNode)getChild(1);
        else if (getProduction() == Production.CONSTANT_114)
            return (ASTUnsignedArithmeticConstantNode)getChild(1);
        else
            return null;
    }

    public Token getTPlus()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_113)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTMinus()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_114)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTScon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_115)
            return (Token)getChild(0);
        else if (getProduction() == Production.CONSTANT_116)
            return (Token)getChild(2);
        else if (getProduction() == Production.CONSTANT_117)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTIcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_116)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTUnderscore()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_116)
            return (Token)getChild(1);
        else if (getProduction() == Production.CONSTANT_117)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTLogicalConstantNode getLogicalConstant()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_118)
            return (ASTLogicalConstantNode)getChild(0);
        else
            return null;
    }

    public ASTStructureConstructorNode getStructureConstructor()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_119)
            return (ASTStructureConstructorNode)getChild(0);
        else
            return null;
    }

    public ASTBozLiteralConstantNode getBozLiteralConstant()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_120)
            return (ASTBozLiteralConstantNode)getChild(0);
        else
            return null;
    }

    public Token getTHcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONSTANT_121)
            return (Token)getChild(0);
        else
            return null;
    }
}
