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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

public class ASTArrayElementNode extends ASTNode implements IDataIDoObject
{
    IASTListNode<ASTStructureComponentNode> structureComponent; // in ASTArrayElementNode
    org.eclipse.photran.internal.core.lexer.Token variableName; // in ASTArrayElementNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTArrayElementNode
    IASTListNode<ASTSectionSubscriptNode> sectionSubscriptList; // in ASTArrayElementNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTArrayElementNode
    ASTImageSelectorNode imageSelector; // in ASTArrayElementNode

    public IASTListNode<ASTStructureComponentNode> getStructureComponent()
    {
        return this.structureComponent;
    }

    public void setStructureComponent(IASTListNode<ASTStructureComponentNode> newValue)
    {
        this.structureComponent = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getVariableName()
    {
        return this.variableName;
    }

    public void setVariableName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.variableName = newValue;
    }


    public IASTListNode<ASTSectionSubscriptNode> getSectionSubscriptList()
    {
        return this.sectionSubscriptList;
    }

    public void setSectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.sectionSubscriptList = newValue;
    }


    public ASTImageSelectorNode getImageSelector()
    {
        return this.imageSelector;
    }

    public void setImageSelector(ASTImageSelectorNode newValue)
    {
        this.imageSelector = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTArrayElementNode(this);
        visitor.visitIDataIDoObject(this);
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
        case 0:  return this.structureComponent;
        case 1:  return this.variableName;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.sectionSubscriptList;
        case 4:  return this.hiddenTRparen;
        case 5:  return this.imageSelector;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.structureComponent = (IASTListNode<ASTStructureComponentNode>)value; return;
        case 1:  this.variableName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.sectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value; return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.imageSelector = (ASTImageSelectorNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

