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
public class ASTTypeDeclarationStmtNode extends ASTNode implements IDeclarationConstruct
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTTypeDeclarationStmtNode
    ASTTypeSpecNode typeSpec; // in ASTTypeDeclarationStmtNode
    IASTListNode<ASTAttrSpecSeqNode> attrSpecSeq; // in ASTTypeDeclarationStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTTypeDeclarationStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTTypeDeclarationStmtNode
    IASTListNode<ASTEntityDeclNode> entityDeclList; // in ASTTypeDeclarationStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTTypeDeclarationStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTTypeSpecNode getTypeSpec()
    {
        return this.typeSpec;
    }

    public void setTypeSpec(ASTTypeSpecNode newValue)
    {
        this.typeSpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTAttrSpecSeqNode> getAttrSpecSeq()
    {
        return this.attrSpecSeq;
    }

    public void setAttrSpecSeq(IASTListNode<ASTAttrSpecSeqNode> newValue)
    {
        this.attrSpecSeq = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTEntityDeclNode> getEntityDeclList()
    {
        return this.entityDeclList;
    }

    public void setEntityDeclList(IASTListNode<ASTEntityDeclNode> newValue)
    {
        this.entityDeclList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTTypeDeclarationStmtNode(this);
        visitor.visitIDeclarationConstruct(this);
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
        case 0:  return this.label;
        case 1:  return this.typeSpec;
        case 2:  return this.attrSpecSeq;
        case 3:  return this.hiddenTColon;
        case 4:  return this.hiddenTColon2;
        case 5:  return this.entityDeclList;
        case 6:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.typeSpec = (ASTTypeSpecNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.attrSpecSeq = (IASTListNode<ASTAttrSpecSeqNode>)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.entityDeclList = (IASTListNode<ASTEntityDeclNode>)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

