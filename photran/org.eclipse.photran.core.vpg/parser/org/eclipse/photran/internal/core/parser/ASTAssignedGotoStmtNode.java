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

import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings({ "unchecked", "unused" })
public class ASTAssignedGotoStmtNode extends ASTNode implements IActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTAssignedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTGoto; // in ASTAssignedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTGo; // in ASTAssignedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTTo; // in ASTAssignedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token variableName; // in ASTAssignedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTAssignedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTAssignedGotoStmtNode
    IASTListNode<ASTLblRefListNode> lblRefList; // in ASTAssignedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTAssignedGotoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTAssignedGotoStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getVariableName()
    {
        return this.variableName;
    }

    public void setVariableName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.variableName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTLblRefListNode> getLblRefList()
    {
        return this.lblRefList;
    }

    public void setLblRefList(IASTListNode<ASTLblRefListNode> newValue)
    {
        this.lblRefList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAssignedGotoStmtNode(this);
        visitor.visitIActionStmt(this);
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
        case 0:  return this.label;
        case 1:  return this.hiddenTGoto;
        case 2:  return this.hiddenTGo;
        case 3:  return this.hiddenTTo;
        case 4:  return this.variableName;
        case 5:  return this.hiddenTComma;
        case 6:  return this.hiddenTLparen;
        case 7:  return this.lblRefList;
        case 8:  return this.hiddenTRparen;
        case 9:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTGoto = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTGo = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTTo = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.variableName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.lblRefList = (IASTListNode<ASTLblRefListNode>)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

