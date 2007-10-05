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

public class ASTComponentDeclNode extends InteriorNode
{
    ASTComponentDeclNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTComponentDeclNode(this);
    }

    public ASTComponentNameNode getComponentName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_DECL_200)
            return (ASTComponentNameNode)getChild(0);
        else if (getProduction() == Production.COMPONENT_DECL_201)
            return (ASTComponentNameNode)getChild(0);
        else if (getProduction() == Production.COMPONENT_DECL_202)
            return (ASTComponentNameNode)getChild(0);
        else if (getProduction() == Production.COMPONENT_DECL_203)
            return (ASTComponentNameNode)getChild(0);
        else if (getProduction() == Production.COMPONENT_DECL_204)
            return (ASTComponentNameNode)getChild(0);
        else if (getProduction() == Production.COMPONENT_DECL_205)
            return (ASTComponentNameNode)getChild(0);
        else if (getProduction() == Production.COMPONENT_DECL_206)
            return (ASTComponentNameNode)getChild(0);
        else if (getProduction() == Production.COMPONENT_DECL_207)
            return (ASTComponentNameNode)getChild(0);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_DECL_200)
            return (Token)getChild(1);
        else if (getProduction() == Production.COMPONENT_DECL_201)
            return (Token)getChild(1);
        else if (getProduction() == Production.COMPONENT_DECL_204)
            return (Token)getChild(1);
        else if (getProduction() == Production.COMPONENT_DECL_205)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTComponentArraySpecNode getComponentArraySpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_DECL_200)
            return (ASTComponentArraySpecNode)getChild(2);
        else if (getProduction() == Production.COMPONENT_DECL_201)
            return (ASTComponentArraySpecNode)getChild(2);
        else if (getProduction() == Production.COMPONENT_DECL_204)
            return (ASTComponentArraySpecNode)getChild(2);
        else if (getProduction() == Production.COMPONENT_DECL_205)
            return (ASTComponentArraySpecNode)getChild(2);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_DECL_200)
            return (Token)getChild(3);
        else if (getProduction() == Production.COMPONENT_DECL_201)
            return (Token)getChild(3);
        else if (getProduction() == Production.COMPONENT_DECL_204)
            return (Token)getChild(3);
        else if (getProduction() == Production.COMPONENT_DECL_205)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTAsterisk()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_DECL_200)
            return (Token)getChild(4);
        else if (getProduction() == Production.COMPONENT_DECL_202)
            return (Token)getChild(1);
        else if (getProduction() == Production.COMPONENT_DECL_204)
            return (Token)getChild(4);
        else if (getProduction() == Production.COMPONENT_DECL_206)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTCharLengthNode getCharLength()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_DECL_200)
            return (ASTCharLengthNode)getChild(5);
        else if (getProduction() == Production.COMPONENT_DECL_202)
            return (ASTCharLengthNode)getChild(2);
        else if (getProduction() == Production.COMPONENT_DECL_204)
            return (ASTCharLengthNode)getChild(5);
        else if (getProduction() == Production.COMPONENT_DECL_206)
            return (ASTCharLengthNode)getChild(2);
        else
            return null;
    }

    public ASTComponentInitializationNode getComponentInitialization()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_DECL_200)
            return (ASTComponentInitializationNode)getChild(6);
        else if (getProduction() == Production.COMPONENT_DECL_201)
            return (ASTComponentInitializationNode)getChild(4);
        else if (getProduction() == Production.COMPONENT_DECL_202)
            return (ASTComponentInitializationNode)getChild(3);
        else if (getProduction() == Production.COMPONENT_DECL_203)
            return (ASTComponentInitializationNode)getChild(1);
        else
            return null;
    }
}
