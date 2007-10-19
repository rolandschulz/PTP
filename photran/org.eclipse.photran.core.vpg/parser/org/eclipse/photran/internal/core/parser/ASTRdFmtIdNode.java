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

public class ASTRdFmtIdNode extends InteriorNode
{
    ASTRdFmtIdNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTRdFmtIdNode(this);
    }

    public ASTLblRefNode getLblRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_783)
            return (ASTLblRefNode)getChild(0);
        else
            return null;
    }

    public Token getTAsterisk()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_784)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTCOperandNode getCOperand()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_785)
            return (ASTCOperandNode)getChild(0);
        else if (getProduction() == Production.RD_FMT_ID_786)
            return (ASTCOperandNode)getChild(0);
        else
            return null;
    }

    public ASTConcatOpNode getConcatOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_786)
            return (ASTConcatOpNode)getChild(1);
        else if (getProduction() == Production.RD_FMT_ID_787)
            return (ASTConcatOpNode)getChild(1);
        else
            return null;
    }

    public ASTCPrimaryNode getCPrimary()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_786)
            return (ASTCPrimaryNode)getChild(2);
        else if (getProduction() == Production.RD_FMT_ID_787)
            return (ASTCPrimaryNode)getChild(2);
        else
            return null;
    }

    public ASTRdFmtIdExprNode getRdFmtIdExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_FMT_ID_787)
            return (ASTRdFmtIdExprNode)getChild(0);
        else
            return null;
    }
}
