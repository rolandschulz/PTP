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

public class ASTDataStmtValueNode extends InteriorNode
{
    ASTDataStmtValueNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDataStmtValueNode(this);
    }

    public ASTDataStmtConstantNode getDataStmtConstant()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_VALUE_386)
            return (ASTDataStmtConstantNode)getChild(0);
        else if (getProduction() == Production.DATA_STMT_VALUE_387)
            return (ASTDataStmtConstantNode)getChild(2);
        else if (getProduction() == Production.DATA_STMT_VALUE_388)
            return (ASTDataStmtConstantNode)getChild(2);
        else
            return null;
    }

    public Token getTIcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_VALUE_387)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTAsterisk()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_VALUE_387)
            return (Token)getChild(1);
        else if (getProduction() == Production.DATA_STMT_VALUE_388)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTNamedConstantUseNode getNamedConstantUse()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_VALUE_388)
            return (ASTNamedConstantUseNode)getChild(0);
        else
            return null;
    }
}
