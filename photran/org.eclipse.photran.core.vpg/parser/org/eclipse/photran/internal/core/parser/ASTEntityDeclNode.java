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
public class ASTEntityDeclNode extends ASTNode
{
    ASTObjectNameNode objectName; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenAsterisk2; // in ASTEntityDeclNode
    ASTCharLengthNode initialCharLength; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenLparen2; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSlash; // in ASTEntityDeclNode
    ASTLogicalConstNode logicalConstant; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSlash2; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTEntityDeclNode
    ASTArraySpecNode arraySpec; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLbracket; // in ASTEntityDeclNode
    ASTCoarraySpecNode coarraySpec; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRbracket; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAsterisk; // in ASTEntityDeclNode
    ASTCharLengthNode charLength; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenRparen2; // in ASTEntityDeclNode
    ASTInitializationNode initialization; // in ASTEntityDeclNode

    public ASTObjectNameNode getObjectName()
    {
        return this.objectName;
    }

    public void setObjectName(ASTObjectNameNode newValue)
    {
        this.objectName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCharLengthNode getInitialCharLength()
    {
        return this.initialCharLength;
    }

    public void setInitialCharLength(ASTCharLengthNode newValue)
    {
        this.initialCharLength = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTLogicalConstNode getLogicalConstant()
    {
        return this.logicalConstant;
    }

    public void setLogicalConstant(ASTLogicalConstNode newValue)
    {
        this.logicalConstant = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTArraySpecNode getArraySpec()
    {
        return this.arraySpec;
    }

    public void setArraySpec(ASTArraySpecNode newValue)
    {
        this.arraySpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCoarraySpecNode getCoarraySpec()
    {
        return this.coarraySpec;
    }

    public void setCoarraySpec(ASTCoarraySpecNode newValue)
    {
        this.coarraySpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCharLengthNode getCharLength()
    {
        return this.charLength;
    }

    public void setCharLength(ASTCharLengthNode newValue)
    {
        this.charLength = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTInitializationNode getInitialization()
    {
        return this.initialization;
    }

    public void setInitialization(ASTInitializationNode newValue)
    {
        this.initialization = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTEntityDeclNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 17;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.objectName;
        case 1:  return this.hiddenAsterisk2;
        case 2:  return this.initialCharLength;
        case 3:  return this.hiddenLparen2;
        case 4:  return this.hiddenTSlash;
        case 5:  return this.logicalConstant;
        case 6:  return this.hiddenTSlash2;
        case 7:  return this.hiddenTLparen;
        case 8:  return this.arraySpec;
        case 9:  return this.hiddenTRparen;
        case 10: return this.hiddenTLbracket;
        case 11: return this.coarraySpec;
        case 12: return this.hiddenTRbracket;
        case 13: return this.hiddenTAsterisk;
        case 14: return this.charLength;
        case 15: return this.hiddenRparen2;
        case 16: return this.initialization;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.objectName = (ASTObjectNameNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenAsterisk2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.initialCharLength = (ASTCharLengthNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTSlash = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.logicalConstant = (ASTLogicalConstNode)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTSlash2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.arraySpec = (ASTArraySpecNode)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.hiddenTLbracket = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 11: this.coarraySpec = (ASTCoarraySpecNode)value; if (value != null) value.setParent(this); return;
        case 12: this.hiddenTRbracket = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 13: this.hiddenTAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 14: this.charLength = (ASTCharLengthNode)value; if (value != null) value.setParent(this); return;
        case 15: this.hiddenRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 16: this.initialization = (ASTInitializationNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

