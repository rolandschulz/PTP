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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;

public class ASTDataStmtConstantNode extends ASTNode
{
    ASTConstantNode constant; // in ASTDataStmtConstantNode
    org.eclipse.photran.internal.core.lexer.Token isNull; // in ASTDataStmtConstantNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTDataStmtConstantNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTDataStmtConstantNode

    public ASTConstantNode getConstant()
    {
        return this.constant;
    }

    public void setConstant(ASTConstantNode newValue)
    {
        this.constant = newValue;
    }


    public boolean isNull()
    {
        return this.isNull != null;
    }

    public void setIsNull(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isNull = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTDataStmtConstantNode(this);
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
        case 0:  return this.constant;
        case 1:  return this.isNull;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.constant = (ASTConstantNode)value; return;
        case 1:  this.isNull = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

