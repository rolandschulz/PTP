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

public class ASTComponentDefStmtNode extends InteriorNode implements IDerivedTypeBodyConstruct
{
    ASTComponentDefStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitIDerivedTypeBodyConstruct(this);
        visitor.visitASTComponentDefStmtNode(this);
    }

    public ASTTypeSpecNode getTypeSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_DEF_STMT_190)
            return (ASTTypeSpecNode)getChild(1);
        else if (getProduction() == Production.COMPONENT_DEF_STMT_191)
            return (ASTTypeSpecNode)getChild(1);
        else if (getProduction() == Production.COMPONENT_DEF_STMT_192)
            return (ASTTypeSpecNode)getChild(1);
        else
            return null;
    }

    public ASTComponentAttrSpecListNode getComponentAttrSpecList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_DEF_STMT_190)
            return (ASTComponentAttrSpecListNode)getChild(3);
        else
            return null;
    }

    public ASTComponentDeclListNode getComponentDeclList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_DEF_STMT_190)
            return (ASTComponentDeclListNode)getChild(6);
        else if (getProduction() == Production.COMPONENT_DEF_STMT_191)
            return (ASTComponentDeclListNode)getChild(4);
        else if (getProduction() == Production.COMPONENT_DEF_STMT_192)
            return (ASTComponentDeclListNode)getChild(2);
        else
            return null;
    }

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_DEF_STMT_190)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.COMPONENT_DEF_STMT_191)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.COMPONENT_DEF_STMT_192)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMPONENT_DEF_STMT_190)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.COMPONENT_DEF_STMT_191)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.COMPONENT_DEF_STMT_192)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.COMPONENT_DEF_STMT_190 && index == 2)
            return false;
        else if (getProduction() == Production.COMPONENT_DEF_STMT_190 && index == 4)
            return false;
        else if (getProduction() == Production.COMPONENT_DEF_STMT_190 && index == 5)
            return false;
        else if (getProduction() == Production.COMPONENT_DEF_STMT_190 && index == 7)
            return false;
        else if (getProduction() == Production.COMPONENT_DEF_STMT_191 && index == 2)
            return false;
        else if (getProduction() == Production.COMPONENT_DEF_STMT_191 && index == 3)
            return false;
        else if (getProduction() == Production.COMPONENT_DEF_STMT_191 && index == 5)
            return false;
        else if (getProduction() == Production.COMPONENT_DEF_STMT_192 && index == 3)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.COMPONENT_DEF_STMT_190 && index == 0)
            return true;
        else if (getProduction() == Production.COMPONENT_DEF_STMT_191 && index == 0)
            return true;
        else if (getProduction() == Production.COMPONENT_DEF_STMT_192 && index == 0)
            return true;
        else
            return false;
    }
}
