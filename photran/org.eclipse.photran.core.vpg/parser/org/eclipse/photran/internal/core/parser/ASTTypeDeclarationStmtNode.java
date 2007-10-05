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

public class ASTTypeDeclarationStmtNode extends InteriorNode
{
    ASTTypeDeclarationStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTTypeDeclarationStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_DECLARATION_STMT_229)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.TYPE_DECLARATION_STMT_230)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.TYPE_DECLARATION_STMT_231)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public ASTTypeSpecNode getTypeSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_DECLARATION_STMT_229)
            return (ASTTypeSpecNode)getChild(1);
        else if (getProduction() == Production.TYPE_DECLARATION_STMT_230)
            return (ASTTypeSpecNode)getChild(1);
        else if (getProduction() == Production.TYPE_DECLARATION_STMT_231)
            return (ASTTypeSpecNode)getChild(1);
        else
            return null;
    }

    public ASTAttrSpecSeqNode getAttrSpecSeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_DECLARATION_STMT_229)
            return (ASTAttrSpecSeqNode)getChild(2);
        else
            return null;
    }

    public ASTEntityDeclListNode getEntityDeclList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_DECLARATION_STMT_229)
            return (ASTEntityDeclListNode)getChild(5);
        else if (getProduction() == Production.TYPE_DECLARATION_STMT_230)
            return (ASTEntityDeclListNode)getChild(4);
        else if (getProduction() == Production.TYPE_DECLARATION_STMT_231)
            return (ASTEntityDeclListNode)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_DECLARATION_STMT_229)
            return (Token)getChild(6);
        else if (getProduction() == Production.TYPE_DECLARATION_STMT_230)
            return (Token)getChild(5);
        else if (getProduction() == Production.TYPE_DECLARATION_STMT_231)
            return (Token)getChild(3);
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.TYPE_DECLARATION_STMT_229 && index == 3)
            return false;
        else if (getProduction() == Production.TYPE_DECLARATION_STMT_229 && index == 4)
            return false;
        else if (getProduction() == Production.TYPE_DECLARATION_STMT_230 && index == 2)
            return false;
        else if (getProduction() == Production.TYPE_DECLARATION_STMT_230 && index == 3)
            return false;
        else
            return true;
    }
}
