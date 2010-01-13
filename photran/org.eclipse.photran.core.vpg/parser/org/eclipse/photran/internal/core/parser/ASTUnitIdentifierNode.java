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

import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings({ "unchecked", "unused" })
public class ASTUnitIdentifierNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hasAsterisk; // in ASTUnitIdentifierNode
    ASTUFExprNode expression; // in ASTUnitIdentifierNode

    public boolean hasAsterisk()
    {
        return this.hasAsterisk != null;
    }

    public void setHasAsterisk(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasAsterisk = newValue;
    }


    public ASTUFExprNode getExpression()
    {
        return this.expression;
    }

    public void setExpression(ASTUFExprNode newValue)
    {
        this.expression = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTUnitIdentifierNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 2;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hasAsterisk;
        case 1:  return this.expression;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hasAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.expression = (ASTUFExprNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

