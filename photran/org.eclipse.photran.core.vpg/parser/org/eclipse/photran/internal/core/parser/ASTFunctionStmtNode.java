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

public class ASTFunctionStmtNode extends InteriorNodeWithErrorRecoverySymbols
{
    ASTFunctionStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production, discardedSymbols);
         
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
        visitor.visitASTFunctionStmtNode(this);
    }

    public ASTFunctionNameNode getFunctionName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_978)
            return (ASTFunctionNameNode)getChild(2);
        else if (getProduction() == Production.FUNCTION_STMT_979)
            return (ASTFunctionNameNode)getChild(2);
        else if (getProduction() == Production.FUNCTION_STMT_980)
            return (ASTFunctionNameNode)getChild(2);
        else if (getProduction() == Production.FUNCTION_STMT_981)
            return (ASTFunctionNameNode)getChild(2);
        else if (getProduction() == Production.FUNCTION_STMT_ERROR_17)
            return (ASTFunctionNameNode)getChild(2);
        else
            return null;
    }

    public boolean hasResultClause()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_979)
            return getChild(5) != null;
        else if (getProduction() == Production.FUNCTION_STMT_981)
            return getChild(6) != null;
        else
            return false;
    }

    public ASTFunctionParsNode getFunctionPars()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_980)
            return (ASTFunctionParsNode)getChild(4);
        else if (getProduction() == Production.FUNCTION_STMT_981)
            return (ASTFunctionParsNode)getChild(4);
        else
            return null;
    }

    public boolean hasFunctionPars()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_980)
            return getChild(4) != null;
        else if (getProduction() == Production.FUNCTION_STMT_981)
            return getChild(4) != null;
        else
            return false;
    }

    public Token getLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_978)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.FUNCTION_STMT_979)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.FUNCTION_STMT_980)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.FUNCTION_STMT_981)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else if (getProduction() == Production.FUNCTION_STMT_ERROR_17)
            return (Token)((ASTLblDefNode)getChild(0)).getLabel();
        else
            return null;
    }

    public boolean hasLabel()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_978)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.FUNCTION_STMT_979)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.FUNCTION_STMT_980)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.FUNCTION_STMT_981)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else if (getProduction() == Production.FUNCTION_STMT_ERROR_17)
            return ((ASTLblDefNode)getChild(0)).hasLabel();
        else
            return false;
    }

    public ASTPrefixSpecListNode getPrefixSpecList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_978)
            return (ASTPrefixSpecListNode)((ASTFunctionPrefixNode)getChild(1)).getPrefixSpecList();
        else if (getProduction() == Production.FUNCTION_STMT_979)
            return (ASTPrefixSpecListNode)((ASTFunctionPrefixNode)getChild(1)).getPrefixSpecList();
        else if (getProduction() == Production.FUNCTION_STMT_980)
            return (ASTPrefixSpecListNode)((ASTFunctionPrefixNode)getChild(1)).getPrefixSpecList();
        else if (getProduction() == Production.FUNCTION_STMT_981)
            return (ASTPrefixSpecListNode)((ASTFunctionPrefixNode)getChild(1)).getPrefixSpecList();
        else if (getProduction() == Production.FUNCTION_STMT_ERROR_17)
            return (ASTPrefixSpecListNode)((ASTFunctionPrefixNode)getChild(1)).getPrefixSpecList();
        else
            return null;
    }

    public boolean hasPrefixSpecList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_978)
            return ((ASTFunctionPrefixNode)getChild(1)).hasPrefixSpecList();
        else if (getProduction() == Production.FUNCTION_STMT_979)
            return ((ASTFunctionPrefixNode)getChild(1)).hasPrefixSpecList();
        else if (getProduction() == Production.FUNCTION_STMT_980)
            return ((ASTFunctionPrefixNode)getChild(1)).hasPrefixSpecList();
        else if (getProduction() == Production.FUNCTION_STMT_981)
            return ((ASTFunctionPrefixNode)getChild(1)).hasPrefixSpecList();
        else if (getProduction() == Production.FUNCTION_STMT_ERROR_17)
            return ((ASTFunctionPrefixNode)getChild(1)).hasPrefixSpecList();
        else
            return false;
    }

    public Token getResultName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_STMT_979)
            return (Token)((ASTNameNode)getChild(7)).getName();
        else if (getProduction() == Production.FUNCTION_STMT_981)
            return (Token)((ASTNameNode)getChild(8)).getName();
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.FUNCTION_STMT_978 && index == 3)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_978 && index == 4)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_978 && index == 5)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_979 && index == 3)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_979 && index == 4)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_979 && index == 6)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_979 && index == 8)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_979 && index == 9)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_980 && index == 3)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_980 && index == 5)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_980 && index == 6)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_981 && index == 3)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_981 && index == 5)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_981 && index == 7)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_981 && index == 9)
            return false;
        else if (getProduction() == Production.FUNCTION_STMT_981 && index == 10)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.FUNCTION_STMT_978 && index == 0)
            return true;
        else if (getProduction() == Production.FUNCTION_STMT_978 && index == 1)
            return true;
        else if (getProduction() == Production.FUNCTION_STMT_979 && index == 0)
            return true;
        else if (getProduction() == Production.FUNCTION_STMT_979 && index == 1)
            return true;
        else if (getProduction() == Production.FUNCTION_STMT_979 && index == 7)
            return true;
        else if (getProduction() == Production.FUNCTION_STMT_980 && index == 0)
            return true;
        else if (getProduction() == Production.FUNCTION_STMT_980 && index == 1)
            return true;
        else if (getProduction() == Production.FUNCTION_STMT_981 && index == 0)
            return true;
        else if (getProduction() == Production.FUNCTION_STMT_981 && index == 1)
            return true;
        else if (getProduction() == Production.FUNCTION_STMT_981 && index == 8)
            return true;
        else if (getProduction() == Production.FUNCTION_STMT_ERROR_17 && index == 0)
            return true;
        else if (getProduction() == Production.FUNCTION_STMT_ERROR_17 && index == 1)
            return true;
        else
            return false;
    }
}
