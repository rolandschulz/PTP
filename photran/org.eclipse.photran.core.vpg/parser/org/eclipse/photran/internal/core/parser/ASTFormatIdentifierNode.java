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

import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings("all")
public class ASTFormatIdentifierNode extends ASTNode
{
    ASTCExprNode formatExpr; // in ASTFormatIdentifierNode
    org.eclipse.photran.internal.core.lexer.Token formatIsAsterisk; // in ASTFormatIdentifierNode
    ASTLblRefNode formatLbl; // in ASTFormatIdentifierNode

    public ASTCExprNode getFormatExpr()
    {
        return this.formatExpr;
    }

    public void setFormatExpr(ASTCExprNode newValue)
    {
        this.formatExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean formatIsAsterisk()
    {
        return this.formatIsAsterisk != null;
    }

    public void setFormatIsAsterisk(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.formatIsAsterisk = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTLblRefNode getFormatLbl()
    {
        return this.formatLbl;
    }

    public void setFormatLbl(ASTLblRefNode newValue)
    {
        this.formatLbl = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTFormatIdentifierNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 3;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.formatExpr;
        case 1:  return this.formatIsAsterisk;
        case 2:  return this.formatLbl;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.formatExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.formatIsAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.formatLbl = (ASTLblRefNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

