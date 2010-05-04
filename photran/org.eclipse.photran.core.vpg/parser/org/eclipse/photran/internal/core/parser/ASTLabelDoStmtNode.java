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
public class ASTLabelDoStmtNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTLabelDoStmtNode
    org.eclipse.photran.internal.core.lexer.Token name; // in ASTLabelDoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTLabelDoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTDo; // in ASTLabelDoStmtNode
    ASTLblRefNode lblRef; // in ASTLabelDoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTLabelDoStmtNode
    ASTLoopControlNode loopControl; // in ASTLabelDoStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTLabelDoStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getName()
    {
        return this.name;
    }

    public void setName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.name = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTLblRefNode getLblRef()
    {
        return this.lblRef;
    }

    public void setLblRef(ASTLblRefNode newValue)
    {
        this.lblRef = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTLoopControlNode getLoopControl()
    {
        return this.loopControl;
    }

    public void setLoopControl(ASTLoopControlNode newValue)
    {
        this.loopControl = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTLabelDoStmtNode(this);
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
        case 1:  return this.name;
        case 2:  return this.hiddenTColon;
        case 3:  return this.hiddenTDo;
        case 4:  return this.lblRef;
        case 5:  return this.hiddenTComma;
        case 6:  return this.loopControl;
        case 7:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.name = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTDo = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.lblRef = (ASTLblRefNode)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.loopControl = (ASTLoopControlNode)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

