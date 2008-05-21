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

public class ASTVariableNode extends ASTNode implements IDataStmtObject, IInputItem
{
    org.eclipse.photran.internal.core.lexer.Token stringConst; // in ASTVariableNode
    IASTListNode<ASTDataRefNode> dataRef; // in ASTVariableNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTVariableNode
    IASTListNode<ASTSectionSubscriptNode> sectionSubscriptList; // in ASTVariableNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTVariableNode
    ASTSubstringRangeNode substringRange; // in ASTVariableNode

    public org.eclipse.photran.internal.core.lexer.Token getStringConst()
    {
        return this.stringConst;
    }

    public void setStringConst(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.stringConst = newValue;
    }


    public IASTListNode<ASTDataRefNode> getDataRef()
    {
        return this.dataRef;
    }

    public void setDataRef(IASTListNode<ASTDataRefNode> newValue)
    {
        this.dataRef = newValue;
    }


    public IASTListNode<ASTSectionSubscriptNode> getSectionSubscriptList()
    {
        return this.sectionSubscriptList;
    }

    public void setSectionSubscriptList(IASTListNode<ASTSectionSubscriptNode> newValue)
    {
        this.sectionSubscriptList = newValue;
    }


    public ASTSubstringRangeNode getSubstringRange()
    {
        return this.substringRange;
    }

    public void setSubstringRange(ASTSubstringRangeNode newValue)
    {
        this.substringRange = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTVariableNode(this);
        visitor.visitIDataStmtObject(this);
        visitor.visitIInputItem(this);
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
        case 0:  return this.stringConst;
        case 1:  return this.dataRef;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.sectionSubscriptList;
        case 4:  return this.hiddenTRparen;
        case 5:  return this.substringRange;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.stringConst = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.dataRef = (IASTListNode<ASTDataRefNode>)value;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.sectionSubscriptList = (IASTListNode<ASTSectionSubscriptNode>)value;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 5:  this.substringRange = (ASTSubstringRangeNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

