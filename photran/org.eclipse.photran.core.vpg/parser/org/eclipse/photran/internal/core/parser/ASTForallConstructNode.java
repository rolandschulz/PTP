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

public class ASTForallConstructNode extends InteriorNode
{
    ASTForallConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTForallConstructNode(this);
    }

    public ASTForallConstructStmtNode getForallConstructStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_626)
            return (ASTForallConstructStmtNode)getChild(0);
        else if (getProduction() == Production.FORALL_CONSTRUCT_627)
            return (ASTForallConstructStmtNode)getChild(0);
        else
            return null;
    }

    public ASTEndForallStmtNode getEndForallStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_626)
            return (ASTEndForallStmtNode)getChild(1);
        else if (getProduction() == Production.FORALL_CONSTRUCT_627)
            return (ASTEndForallStmtNode)getChild(2);
        else
            return null;
    }

    public ASTForallBodyNode getForallBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_627)
            return (ASTForallBodyNode)getChild(1);
        else
            return null;
    }
}
