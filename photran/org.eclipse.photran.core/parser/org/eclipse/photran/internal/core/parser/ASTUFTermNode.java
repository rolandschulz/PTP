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

public class ASTUFTermNode extends InteriorNode
{
    ASTUFTermNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTUFTermNode(this);
    }

    public ASTUFFactorNode getUFFactor()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFTERM_527)
            return (ASTUFFactorNode)getChild(0);
        else if (getProduction() == Production.UFTERM_528)
            return (ASTUFFactorNode)getChild(2);
        else
            return null;
    }

    public ASTUFTermNode getUFTerm()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFTERM_528)
            return (ASTUFTermNode)getChild(0);
        else if (getProduction() == Production.UFTERM_529)
            return (ASTUFTermNode)getChild(0);
        else
            return null;
    }

    public ASTMultOpNode getMultOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFTERM_528)
            return (ASTMultOpNode)getChild(1);
        else
            return null;
    }

    public ASTConcatOpNode getConcatOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFTERM_529)
            return (ASTConcatOpNode)getChild(1);
        else
            return null;
    }

    public ASTUFPrimaryNode getUFPrimary()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFTERM_529)
            return (ASTUFPrimaryNode)getChild(2);
        else
            return null;
    }
}
