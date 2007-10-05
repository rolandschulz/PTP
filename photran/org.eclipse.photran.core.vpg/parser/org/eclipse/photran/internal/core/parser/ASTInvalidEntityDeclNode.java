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

public class ASTInvalidEntityDeclNode extends InteriorNode
{
    ASTInvalidEntityDeclNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTInvalidEntityDeclNode(this);
    }

    public ASTObjectNameNode getObjectName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INVALID_ENTITY_DECL_269)
            return (ASTObjectNameNode)getChild(0);
        else if (getProduction() == Production.INVALID_ENTITY_DECL_270)
            return (ASTObjectNameNode)getChild(0);
        else
            return null;
    }

    public Token getTAsterisk()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INVALID_ENTITY_DECL_269)
            return (Token)getChild(1);
        else if (getProduction() == Production.INVALID_ENTITY_DECL_270)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTCharLengthNode getCharLength()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INVALID_ENTITY_DECL_269)
            return (ASTCharLengthNode)getChild(2);
        else if (getProduction() == Production.INVALID_ENTITY_DECL_270)
            return (ASTCharLengthNode)getChild(2);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INVALID_ENTITY_DECL_269)
            return (Token)getChild(3);
        else if (getProduction() == Production.INVALID_ENTITY_DECL_270)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTArraySpecNode getArraySpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INVALID_ENTITY_DECL_269)
            return (ASTArraySpecNode)getChild(4);
        else if (getProduction() == Production.INVALID_ENTITY_DECL_270)
            return (ASTArraySpecNode)getChild(4);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INVALID_ENTITY_DECL_269)
            return (Token)getChild(5);
        else if (getProduction() == Production.INVALID_ENTITY_DECL_270)
            return (Token)getChild(5);
        else
            return null;
    }

    public ASTInitializationNode getInitialization()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INVALID_ENTITY_DECL_270)
            return (ASTInitializationNode)getChild(6);
        else
            return null;
    }
}
