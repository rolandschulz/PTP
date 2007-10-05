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

public class ASTEntityDeclNode extends InteriorNode
{
    ASTEntityDeclNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTEntityDeclNode(this);
    }

    public ASTObjectNameNode getObjectName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTITY_DECL_260)
            return (ASTObjectNameNode)getChild(0);
        else if (getProduction() == Production.ENTITY_DECL_261)
            return (ASTObjectNameNode)getChild(0);
        else if (getProduction() == Production.ENTITY_DECL_262)
            return (ASTObjectNameNode)getChild(0);
        else if (getProduction() == Production.ENTITY_DECL_263)
            return (ASTObjectNameNode)getChild(0);
        else if (getProduction() == Production.ENTITY_DECL_264)
            return (ASTObjectNameNode)getChild(0);
        else if (getProduction() == Production.ENTITY_DECL_265)
            return (ASTObjectNameNode)getChild(0);
        else if (getProduction() == Production.ENTITY_DECL_266)
            return (ASTObjectNameNode)getChild(0);
        else if (getProduction() == Production.ENTITY_DECL_267)
            return (ASTObjectNameNode)getChild(0);
        else
            return null;
    }

    public ASTInitializationNode getInitialization()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTITY_DECL_261)
            return (ASTInitializationNode)getChild(1);
        else if (getProduction() == Production.ENTITY_DECL_263)
            return (ASTInitializationNode)getChild(3);
        else if (getProduction() == Production.ENTITY_DECL_265)
            return (ASTInitializationNode)getChild(4);
        else if (getProduction() == Production.ENTITY_DECL_267)
            return (ASTInitializationNode)getChild(6);
        else
            return null;
    }

    public Token getTAsterisk()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTITY_DECL_262)
            return (Token)getChild(1);
        else if (getProduction() == Production.ENTITY_DECL_263)
            return (Token)getChild(1);
        else if (getProduction() == Production.ENTITY_DECL_266)
            return (Token)getChild(4);
        else if (getProduction() == Production.ENTITY_DECL_267)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTCharLengthNode getCharLength()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTITY_DECL_262)
            return (ASTCharLengthNode)getChild(2);
        else if (getProduction() == Production.ENTITY_DECL_263)
            return (ASTCharLengthNode)getChild(2);
        else if (getProduction() == Production.ENTITY_DECL_266)
            return (ASTCharLengthNode)getChild(5);
        else if (getProduction() == Production.ENTITY_DECL_267)
            return (ASTCharLengthNode)getChild(5);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTITY_DECL_264)
            return (Token)getChild(1);
        else if (getProduction() == Production.ENTITY_DECL_265)
            return (Token)getChild(1);
        else if (getProduction() == Production.ENTITY_DECL_266)
            return (Token)getChild(1);
        else if (getProduction() == Production.ENTITY_DECL_267)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTArraySpecNode getArraySpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTITY_DECL_264)
            return (ASTArraySpecNode)getChild(2);
        else if (getProduction() == Production.ENTITY_DECL_265)
            return (ASTArraySpecNode)getChild(2);
        else if (getProduction() == Production.ENTITY_DECL_266)
            return (ASTArraySpecNode)getChild(2);
        else if (getProduction() == Production.ENTITY_DECL_267)
            return (ASTArraySpecNode)getChild(2);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTITY_DECL_264)
            return (Token)getChild(3);
        else if (getProduction() == Production.ENTITY_DECL_265)
            return (Token)getChild(3);
        else if (getProduction() == Production.ENTITY_DECL_266)
            return (Token)getChild(3);
        else if (getProduction() == Production.ENTITY_DECL_267)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTInvalidEntityDeclNode getInvalidEntityDecl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTITY_DECL_268)
            return (ASTInvalidEntityDeclNode)getChild(0);
        else
            return null;
    }
}
