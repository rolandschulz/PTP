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

public class ASTUnsignedArithmeticConstantNode extends InteriorNode
{
    ASTUnsignedArithmeticConstantNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTUnsignedArithmeticConstantNode(this);
    }

    public Token getTIcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_162)
            return (Token)getChild(0);
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_166)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTRcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_163)
            return (Token)getChild(0);
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_167)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTDcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_164)
            return (Token)getChild(0);
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_168)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTComplexConstNode getComplexConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_165)
            return (ASTComplexConstNode)getChild(0);
        else
            return null;
    }

    public Token getTUnderscore()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_166)
            return (Token)getChild(1);
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_167)
            return (Token)getChild(1);
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_168)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTKindParamNode getKindParam()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_166)
            return (ASTKindParamNode)getChild(2);
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_167)
            return (ASTKindParamNode)getChild(2);
        else if (getProduction() == Production.UNSIGNED_ARITHMETIC_CONSTANT_168)
            return (ASTKindParamNode)getChild(2);
        else
            return null;
    }
}
