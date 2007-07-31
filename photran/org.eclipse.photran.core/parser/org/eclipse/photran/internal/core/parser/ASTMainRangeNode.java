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

class ASTMainRangeNode extends InteriorNode
{
    ASTMainRangeNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    public ASTBodyNode getBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MAIN_RANGE_10)
            return (ASTBodyNode)getChild(0);
        else
            return null;
    }

    public ASTEndProgramStmtNode getEndProgramStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MAIN_RANGE_10)
            return (ASTEndProgramStmtNode)getChild(1);
        else if (getProduction() == Production.MAIN_RANGE_11)
            return (ASTEndProgramStmtNode)getChild(1);
        else if (getProduction() == Production.MAIN_RANGE_12)
            return (ASTEndProgramStmtNode)getChild(0);
        else
            return null;
    }

    public ASTBodyPlusInternalsNode getBodyPlusInternals()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MAIN_RANGE_11)
            return (ASTBodyPlusInternalsNode)getChild(0);
        else
            return null;
    }
}
