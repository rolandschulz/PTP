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

public class ASTInterfaceBlockNode extends ScopingNode implements IDeclarationConstruct
{
    ASTInterfaceBlockNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitIDeclarationConstruct(this);
        visitor.visitASTInterfaceBlockNode(this);
    }

    public ASTInterfaceStmtNode getInterfaceStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_BLOCK_926)
            return (ASTInterfaceStmtNode)getChild(0);
        else
            return null;
    }

    public ASTInterfaceBlockBodyNode getInterfaceBlockBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_BLOCK_926)
            return (ASTInterfaceBlockBodyNode)((ASTInterfaceRangeNode)getChild(1)).getInterfaceBlockBody();
        else
            return null;
    }

    public ASTEndInterfaceStmtNode getEndInterfaceStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_BLOCK_926)
            return (ASTEndInterfaceStmtNode)((ASTInterfaceRangeNode)getChild(1)).getEndInterfaceStmt();
        else
            return null;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.INTERFACE_BLOCK_926 && index == 1)
            return true;
        else
            return false;
    }
}
