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

public class ASTTypeParamDeclListNode extends ASTNode
{
    ASTTypeParamDeclListNode typeParamDeclList; // in ASTTypeParamDeclListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTTypeParamDeclListNode
    ASTTypeParamDeclNode typeParamDecl; // in ASTTypeParamDeclListNode

    public ASTTypeParamDeclListNode getTypeParamDeclList()
    {
        return this.typeParamDeclList;
    }

    public void setTypeParamDeclList(ASTTypeParamDeclListNode newValue)
    {
        this.typeParamDeclList = newValue;
    }


    public ASTTypeParamDeclNode getTypeParamDecl()
    {
        return this.typeParamDecl;
    }

    public void setTypeParamDecl(ASTTypeParamDeclNode newValue)
    {
        this.typeParamDecl = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTTypeParamDeclListNode(this);
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
        case 0:  return this.typeParamDeclList;
        case 1:  return this.hiddenTComma;
        case 2:  return this.typeParamDecl;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.typeParamDeclList = (ASTTypeParamDeclListNode)value; return;
        case 1:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.typeParamDecl = (ASTTypeParamDeclNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

