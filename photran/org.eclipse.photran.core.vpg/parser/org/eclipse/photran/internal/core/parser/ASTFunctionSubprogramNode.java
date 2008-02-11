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

public class ASTFunctionSubprogramNode extends ScopingNode implements IInternalSubprogram, IModuleSubprogram
{
    ASTFunctionSubprogramNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitIModuleSubprogram(this);
        visitor.visitIInternalSubprogram(this);
        visitor.visitASTFunctionSubprogramNode(this);
    }

    public ASTFunctionStmtNode getFunctionStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_SUBPROGRAM_17)
            return (ASTFunctionStmtNode)getChild(0);
        else
            return null;
    }

    public ASTBodyNode getBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_SUBPROGRAM_17)
            return (ASTBodyNode)((ASTFunctionRangeNode)getChild(1)).getBody();
        else
            return null;
    }

    public ASTEndFunctionStmtNode getEndFunctionStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_SUBPROGRAM_17)
            return (ASTEndFunctionStmtNode)((ASTFunctionRangeNode)getChild(1)).getEndFunctionStmt();
        else
            return null;
    }

    public ASTContainsStmtNode getContainsStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_SUBPROGRAM_17)
            return (ASTContainsStmtNode)((ASTFunctionRangeNode)getChild(1)).getContainsStmt();
        else
            return null;
    }

    public ASTInternalSubprogramsNode getInternalSubprograms()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_SUBPROGRAM_17)
            return (ASTInternalSubprogramsNode)((ASTFunctionRangeNode)getChild(1)).getInternalSubprograms();
        else
            return null;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.FUNCTION_SUBPROGRAM_17 && index == 1)
            return true;
        else
            return false;
    }
}
