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

public class ASTFormatEditNode extends InteriorNode
{
    ASTFormatEditNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTFormatEditNode(this);
    }

    public ASTEditElementNode getEditElement()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_EDIT_876)
            return (ASTEditElementNode)getChild(0);
        else if (getProduction() == Production.FORMAT_EDIT_877)
            return (ASTEditElementNode)getChild(1);
        else if (getProduction() == Production.FORMAT_EDIT_880)
            return (ASTEditElementNode)getChild(1);
        else if (getProduction() == Production.FORMAT_EDIT_881)
            return (ASTEditElementNode)getChild(2);
        else
            return null;
    }

    public boolean hasEditElement()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_EDIT_876)
            return getChild(0) != null;
        else if (getProduction() == Production.FORMAT_EDIT_877)
            return getChild(1) != null;
        else if (getProduction() == Production.FORMAT_EDIT_880)
            return getChild(1) != null;
        else if (getProduction() == Production.FORMAT_EDIT_881)
            return getChild(2) != null;
        else
            return false;
    }

    public Token getIntConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_EDIT_877)
            return (Token)getChild(0);
        else if (getProduction() == Production.FORMAT_EDIT_881)
            return (Token)getChild(1);
        else
            return null;
    }

    public boolean hasIntConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_EDIT_877)
            return getChild(0) != null;
        else if (getProduction() == Production.FORMAT_EDIT_881)
            return getChild(1) != null;
        else
            return false;
    }

    public Token getHexConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_EDIT_878)
            return (Token)getChild(0);
        else
            return null;
    }

    public boolean hasHexConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_EDIT_878)
            return getChild(0) != null;
        else
            return false;
    }

    public Token getPConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_EDIT_879)
            return (Token)getChild(0);
        else if (getProduction() == Production.FORMAT_EDIT_880)
            return (Token)getChild(0);
        else if (getProduction() == Production.FORMAT_EDIT_881)
            return (Token)getChild(0);
        else
            return null;
    }

    public boolean hasPConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_EDIT_879)
            return getChild(0) != null;
        else if (getProduction() == Production.FORMAT_EDIT_880)
            return getChild(0) != null;
        else if (getProduction() == Production.FORMAT_EDIT_881)
            return getChild(0) != null;
        else
            return false;
    }
}
