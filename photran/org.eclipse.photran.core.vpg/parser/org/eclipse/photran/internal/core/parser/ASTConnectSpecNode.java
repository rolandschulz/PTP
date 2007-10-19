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

public class ASTConnectSpecNode extends InteriorNode
{
    ASTConnectSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTConnectSpecNode(this);
    }

    public Token getTUniteq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_750)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_750)
            return (ASTUnitIdentifierNode)getChild(1);
        else
            return null;
    }

    public Token getTErreq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_751)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTLblRefNode getLblRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_751)
            return (ASTLblRefNode)getChild(1);
        else
            return null;
    }

    public Token getTFileeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_752)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTCExprNode getCExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_752)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_753)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_754)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_755)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_757)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_759)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_760)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_761)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_762)
            return (ASTCExprNode)getChild(1);
        else
            return null;
    }

    public Token getTStatuseq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_753)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTAccesseq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_754)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTFormeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_755)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTRecleq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_756)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_756)
            return (ASTExprNode)getChild(1);
        else
            return null;
    }

    public Token getTBlankeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_757)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTIostateq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_758)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTScalarVariableNode getScalarVariable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_758)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public Token getTPositioneq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_759)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTActioneq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_760)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTDelimeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_761)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTPadeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_762)
            return (Token)getChild(0);
        else
            return null;
    }
}
