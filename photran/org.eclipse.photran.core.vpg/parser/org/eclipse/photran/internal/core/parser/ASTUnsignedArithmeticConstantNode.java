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

public class ASTUnsignedArithmeticConstantNode extends InteriorNode
{
    ASTUnsignedArithmeticConstantNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTUnsignedArithmeticConstantNode(this);
    }

    public Token getIntConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_163)
            return (Token)getChild(0);
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_167)
            return (Token)getChild(0);
        else
            return null;
    }

    public boolean hasIntConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_163)
            return getChild(0) != null;
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_167)
            return getChild(0) != null;
        else
            return false;
    }

    public Token getRealConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_164)
            return (Token)getChild(0);
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_168)
            return (Token)getChild(0);
        else
            return null;
    }

    public boolean hasRealConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_164)
            return getChild(0) != null;
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_168)
            return getChild(0) != null;
        else
            return false;
    }

    public Token getDblConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_165)
            return (Token)getChild(0);
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_169)
            return (Token)getChild(0);
        else
            return null;
    }

    public boolean hasDblConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_165)
            return getChild(0) != null;
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_169)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTComplexConstNode getComplexConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_166)
            return (ASTComplexConstNode)getChild(0);
        else
            return null;
    }

    public boolean hasComplexConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_166)
            return getChild(0) != null;
        else
            return false;
    }

    public Token getUnsignedArithConstIntKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_167)
            return (Token)((ASTKindParamNode)getChild(2)).getIntKind();
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_168)
            return (Token)((ASTKindParamNode)getChild(2)).getIntKind();
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_169)
            return (Token)((ASTKindParamNode)getChild(2)).getIntKind();
        else
            return null;
    }

    public boolean hasUnsignedArithConstIntKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_167)
            return ((ASTKindParamNode)getChild(2)).hasIntKind();
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_168)
            return ((ASTKindParamNode)getChild(2)).hasIntKind();
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_169)
            return ((ASTKindParamNode)getChild(2)).hasIntKind();
        else
            return false;
    }

    public ASTNamedConstantUseNode getUnsignedArithConstNamedConstKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_167)
            return (ASTNamedConstantUseNode)((ASTKindParamNode)getChild(2)).getNamedConstKind();
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_168)
            return (ASTNamedConstantUseNode)((ASTKindParamNode)getChild(2)).getNamedConstKind();
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_169)
            return (ASTNamedConstantUseNode)((ASTKindParamNode)getChild(2)).getNamedConstKind();
        else
            return null;
    }

    public boolean hasUnsignedArithConstNamedConstKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_167)
            return ((ASTKindParamNode)getChild(2)).hasNamedConstKind();
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_168)
            return ((ASTKindParamNode)getChild(2)).hasNamedConstKind();
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_169)
            return ((ASTKindParamNode)getChild(2)).hasNamedConstKind();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_167 && index == 1)
            return false;
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_168 && index == 1)
            return false;
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_169 && index == 1)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_167 && index == 2)
            return true;
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_168 && index == 2)
            return true;
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_169 && index == 2)
            return true;
        else
            return false;
    }
}
