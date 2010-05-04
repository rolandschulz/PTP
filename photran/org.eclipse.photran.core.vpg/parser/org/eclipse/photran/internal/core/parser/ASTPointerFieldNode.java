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
public class ASTPointerFieldNode extends ASTNode
{
    ASTFieldSelectorNode fieldSelector; // in ASTPointerFieldNode
    ASTNameNode name; // in ASTPointerFieldNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTPointerFieldNode
    IASTListNode<ASTSFExprListNode> SFExprList; // in ASTPointerFieldNode
    IASTListNode<ASTSFDummyArgNameListNode> SFDummyArgNameList; // in ASTPointerFieldNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTPointerFieldNode
    ASTImageSelectorNode imageSelector; // in ASTPointerFieldNode
    org.eclipse.photran.internal.core.lexer.Token hasDerivedTypeComponentRef; // in ASTPointerFieldNode
    ASTNameNode componentName; // in ASTPointerFieldNode

    public ASTFieldSelectorNode getFieldSelector()
    {
        return this.fieldSelector;
    }

    public void setFieldSelector(ASTFieldSelectorNode newValue)
    {
        this.fieldSelector = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTNameNode getName()
    {
        return this.name;
    }

    public void setName(ASTNameNode newValue)
    {
        this.name = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTSFExprListNode> getSFExprList()
    {
        return this.SFExprList;
    }

    public void setSFExprList(IASTListNode<ASTSFExprListNode> newValue)
    {
        this.SFExprList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTSFDummyArgNameListNode> getSFDummyArgNameList()
    {
        return this.SFDummyArgNameList;
    }

    public void setSFDummyArgNameList(IASTListNode<ASTSFDummyArgNameListNode> newValue)
    {
        this.SFDummyArgNameList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTImageSelectorNode getImageSelector()
    {
        return this.imageSelector;
    }

    public void setImageSelector(ASTImageSelectorNode newValue)
    {
        this.imageSelector = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean hasDerivedTypeComponentRef()
    {
        return this.hasDerivedTypeComponentRef != null;
    }

    public void setHasDerivedTypeComponentRef(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.hasDerivedTypeComponentRef = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTNameNode getComponentName()
    {
        return this.componentName;
    }

    public void setComponentName(ASTNameNode newValue)
    {
        this.componentName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTPointerFieldNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 9;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.fieldSelector;
        case 1:  return this.name;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.SFExprList;
        case 4:  return this.SFDummyArgNameList;
        case 5:  return this.hiddenTRparen;
        case 6:  return this.imageSelector;
        case 7:  return this.hasDerivedTypeComponentRef;
        case 8:  return this.componentName;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.fieldSelector = (ASTFieldSelectorNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.name = (ASTNameNode)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.SFExprList = (IASTListNode<ASTSFExprListNode>)value; if (value != null) value.setParent(this); return;
        case 4:  this.SFDummyArgNameList = (IASTListNode<ASTSFDummyArgNameListNode>)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.imageSelector = (ASTImageSelectorNode)value; if (value != null) value.setParent(this); return;
        case 7:  this.hasDerivedTypeComponentRef = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.componentName = (ASTNameNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

