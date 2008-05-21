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

public class ASTDataStmtValueNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hasConstIntKind; // in ASTDataStmtValueNode
    ASTNamedConstantUseNode namedConstKind; // in ASTDataStmtValueNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAsterisk; // in ASTDataStmtValueNode
    ASTConstantNode constant; // in ASTDataStmtValueNode
    org.eclipse.photran.internal.core.lexer.Token isNull; // in ASTDataStmtValueNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTDataStmtValueNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTDataStmtValueNode

    public boolean hasConstIntKind()
    {
        return this.hasConstIntKind != null;
    }

    public void setHasConstIntKind(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasConstIntKind = newValue;
    }


    public ASTNamedConstantUseNode getNamedConstKind()
    {
        return this.namedConstKind;
    }

    public void setNamedConstKind(ASTNamedConstantUseNode newValue)
    {
        this.namedConstKind = newValue;
    }


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
        visitor.visitASTDataStmtValueNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 7;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hasConstIntKind;
        case 1:  return this.namedConstKind;
        case 2:  return this.hiddenTAsterisk;
        case 3:  return this.constant;
        case 4:  return this.isNull;
        case 5:  return this.hiddenTLparen;
        case 6:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hasConstIntKind = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.namedConstKind = (ASTNamedConstantUseNode)value;
        case 2:  this.hiddenTAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.constant = (ASTConstantNode)value;
        case 4:  this.isNull = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 5:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

