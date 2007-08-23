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

public class ASTProgramUnitNode extends InteriorNode
{
    ASTProgramUnitNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTProgramUnitNode(this);
    }

    public ASTMainProgramNode getMainProgram()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PROGRAM_UNIT_3)
            return (ASTMainProgramNode)getChild(0);
        else
            return null;
    }

    public ASTFunctionSubprogramNode getFunctionSubprogram()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PROGRAM_UNIT_4)
            return (ASTFunctionSubprogramNode)getChild(0);
        else
            return null;
    }

    public ASTSubroutineSubprogramNode getSubroutineSubprogram()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PROGRAM_UNIT_5)
            return (ASTSubroutineSubprogramNode)getChild(0);
        else
            return null;
    }

    public ASTModuleNode getModule()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PROGRAM_UNIT_6)
            return (ASTModuleNode)getChild(0);
        else
            return null;
    }

    public ASTBlockDataSubprogramNode getBlockDataSubprogram()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PROGRAM_UNIT_7)
            return (ASTBlockDataSubprogramNode)getChild(0);
        else
            return null;
    }
}
