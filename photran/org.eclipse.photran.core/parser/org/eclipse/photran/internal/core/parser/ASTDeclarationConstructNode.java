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

class ASTDeclarationConstructNode extends InteriorNode
{
    ASTDeclarationConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    public ASTDerivedTypeDefNode getDerivedTypeDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DECLARATION_CONSTRUCT_43)
            return (ASTDerivedTypeDefNode)getChild(0);
        else
            return null;
    }

    public ASTInterfaceBlockNode getInterfaceBlock()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DECLARATION_CONSTRUCT_44)
            return (ASTInterfaceBlockNode)getChild(0);
        else
            return null;
    }

    public ASTTypeDeclarationStmtNode getTypeDeclarationStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DECLARATION_CONSTRUCT_45)
            return (ASTTypeDeclarationStmtNode)getChild(0);
        else
            return null;
    }

    public ASTSpecificationStmtNode getSpecificationStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DECLARATION_CONSTRUCT_46)
            return (ASTSpecificationStmtNode)getChild(0);
        else
            return null;
    }
}
