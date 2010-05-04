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
public class ASTScalarVariableNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token variableName; // in ASTScalarVariableNode
    IASTListNode<ASTStructureComponentNode> structureComponent; // in ASTScalarVariableNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTScalarVariableNode
    IASTListNode<ASTSectionSubscriptNode> sectionSubscriptList; // in ASTScalarVariableNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTScalarVariableNode
    ASTImageSelectorNode imageSelector; // in ASTScalarVariableNode

    public org.eclipse.photran.internal.core.lexer.Token getVariableName()
    {
        return this.variableName;
    }

    public void setVariableName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.variableName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTStructureComponentNode> getStructureComponent()
    {
        return this.structureComponent;
    }

    public void setStructureComponent(IASTListNode<ASTStructureComponentNode> newValue)
    {
        this.structureComponent = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTSectionSubscriptNode> getSectionSubscriptList()
    {
        return this.sectionSubscriptList;
    }

    public void setSectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.sectionSubscriptList = newValue;
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


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTScalarVariableNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 6;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.variableName;
        case 1:  return this.structureComponent;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.sectionSubscriptList;
        case 4:  return this.hiddenTRparen;
        case 5:  return this.imageSelector;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.variableName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.structureComponent = (IASTListNode<ASTStructureComponentNode>)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.sectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.imageSelector = (ASTImageSelectorNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

