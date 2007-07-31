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

public class ASTAttrSpecNode extends InteriorNode
{
    ASTAttrSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAttrSpecNode(this);
    }

    public Token getTParameter()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_247)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTAccessSpecNode getAccessSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_248)
            return (ASTAccessSpecNode)getChild(0);
        else
            return null;
    }

    public Token getTAllocatable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_249)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTDimension()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_250)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_250)
            return (Token)getChild(1);
        else if (getProduction() == Production.ATTR_SPEC_252)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTArraySpecNode getArraySpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_250)
            return (ASTArraySpecNode)getChild(2);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_250)
            return (Token)getChild(3);
        else if (getProduction() == Production.ATTR_SPEC_252)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTExternal()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_251)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTIntent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_252)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTIntentSpecNode getIntentSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_252)
            return (ASTIntentSpecNode)getChild(2);
        else
            return null;
    }

    public Token getTIntrinsic()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_253)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTOptional()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_254)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTPointer()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_255)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTSave()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_256)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTTarget()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_257)
            return (Token)getChild(0);
        else
            return null;
    }
}
