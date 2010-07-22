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
public class ASTTypeGuardStmtNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token isType; // in ASTTypeGuardStmtNode
    org.eclipse.photran.internal.core.lexer.Token isDefault; // in ASTTypeGuardStmtNode
    org.eclipse.photran.internal.core.lexer.Token isClass; // in ASTTypeGuardStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIs; // in ASTTypeGuardStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTTypeGuardStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTDefault; // in ASTTypeGuardStmtNode
    ASTTypeSpecNode typeSpecNoPrefix; // in ASTTypeGuardStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTTypeGuardStmtNode
    org.eclipse.photran.internal.core.lexer.Token selectConstructName; // in ASTTypeGuardStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTTypeGuardStmtNode

    public boolean isType()
    {
        return this.isType != null;
    }

    public void setIsType(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isType = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isDefault()
    {
        return this.isDefault != null;
    }

    public void setIsDefault(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isDefault = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isClass()
    {
        return this.isClass != null;
    }

    public void setIsClass(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isClass = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTTypeSpecNode getTypeSpecNoPrefix()
    {
        return this.typeSpecNoPrefix;
    }

    public void setTypeSpecNoPrefix(ASTTypeSpecNode newValue)
    {
        this.typeSpecNoPrefix = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getSelectConstructName()
    {
        return this.selectConstructName;
    }

    public void setSelectConstructName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.selectConstructName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTTypeGuardStmtNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 10;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.isType;
        case 1:  return this.isDefault;
        case 2:  return this.isClass;
        case 3:  return this.hiddenTIs;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.hiddenTDefault;
        case 6:  return this.typeSpecNoPrefix;
        case 7:  return this.hiddenTRparen;
        case 8:  return this.selectConstructName;
        case 9:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isType = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.isDefault = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.isClass = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTIs = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTDefault = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.typeSpecNoPrefix = (ASTTypeSpecNode)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.selectConstructName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

