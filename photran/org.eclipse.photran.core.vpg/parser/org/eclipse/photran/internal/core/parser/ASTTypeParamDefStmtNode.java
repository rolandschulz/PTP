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

public class ASTTypeParamDefStmtNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTTypeParamDefStmtNode
    ASTTypeSpecNode typeSpec; // in ASTTypeParamDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTTypeParamDefStmtNode
    ASTTypeParamAttrSpecNode typeParamAttrSpec; // in ASTTypeParamDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTTypeParamDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTTypeParamDefStmtNode
    ASTTypeParamDeclListNode typeParamDeclList; // in ASTTypeParamDefStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTTypeParamDefStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public ASTTypeSpecNode getTypeSpec()
    {
        return this.typeSpec;
    }

    public void setTypeSpec(ASTTypeSpecNode newValue)
    {
        this.typeSpec = newValue;
    }


    public ASTTypeParamAttrSpecNode getTypeParamAttrSpec()
    {
        return this.typeParamAttrSpec;
    }

    public void setTypeParamAttrSpec(ASTTypeParamAttrSpecNode newValue)
    {
        this.typeParamAttrSpec = newValue;
    }


    public ASTTypeParamDeclListNode getTypeParamDeclList()
    {
        return this.typeParamDeclList;
    }

    public void setTypeParamDeclList(ASTTypeParamDeclListNode newValue)
    {
        this.typeParamDeclList = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTTypeParamDefStmtNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 8;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.typeSpec;
        case 2:  return this.hiddenTComma;
        case 3:  return this.typeParamAttrSpec;
        case 4:  return this.hiddenTColon;
        case 5:  return this.hiddenTColon2;
        case 6:  return this.typeParamDeclList;
        case 7:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.typeSpec = (ASTTypeSpecNode)value; return;
        case 2:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.typeParamAttrSpec = (ASTTypeParamAttrSpecNode)value; return;
        case 4:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.typeParamDeclList = (ASTTypeParamDeclListNode)value; return;
        case 7:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

