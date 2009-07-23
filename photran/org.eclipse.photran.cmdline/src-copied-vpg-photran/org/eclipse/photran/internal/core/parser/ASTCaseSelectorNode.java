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

import java.io.PrintStream;
import java.util.Iterator;

import java.util.List;

import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

public class ASTCaseSelectorNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hasDefaultSelector; // in ASTCaseSelectorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTCaseSelectorNode
    IASTListNode<ASTCaseValueRangeNode> caseValueRangeListSelector; // in ASTCaseSelectorNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTCaseSelectorNode

    public boolean hasDefaultSelector()
    {
        return this.hasDefaultSelector != null;
    }

    public void setHasDefaultSelector(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasDefaultSelector = newValue;
    }


    public IASTListNode<ASTCaseValueRangeNode> getCaseValueRangeListSelector()
    {
        return this.caseValueRangeListSelector;
    }

    public void setCaseValueRangeListSelector(IASTListNode<ASTCaseValueRangeNode> newValue)
    {
        this.caseValueRangeListSelector = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCaseSelectorNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 4;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hasDefaultSelector;
        case 1:  return this.hiddenTLparen;
        case 2:  return this.caseValueRangeListSelector;
        case 3:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hasDefaultSelector = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.caseValueRangeListSelector = (IASTListNode<ASTCaseValueRangeNode>)value; return;
        case 3:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

