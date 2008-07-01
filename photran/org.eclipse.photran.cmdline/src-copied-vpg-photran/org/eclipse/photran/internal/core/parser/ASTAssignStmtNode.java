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

public class ASTAssignStmtNode extends ASTNode implements IActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTAssignStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAssign; // in ASTAssignStmtNode
    ASTLblRefNode assignedLblRef; // in ASTAssignStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTTo; // in ASTAssignStmtNode
    org.eclipse.photran.internal.core.lexer.Token variableName; // in ASTAssignStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTAssignStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public ASTLblRefNode getAssignedLblRef()
    {
        return this.assignedLblRef;
    }

    public void setAssignedLblRef(ASTLblRefNode newValue)
    {
        this.assignedLblRef = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getVariableName()
    {
        return this.variableName;
    }

    public void setVariableName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.variableName = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTAssignStmtNode(this);
        visitor.visitIActionStmt(this);
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
        case 0:  return this.label;
        case 1:  return this.hiddenTAssign;
        case 2:  return this.assignedLblRef;
        case 3:  return this.hiddenTTo;
        case 4:  return this.variableName;
        case 5:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.hiddenTAssign = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.assignedLblRef = (ASTLblRefNode)value;
        case 3:  this.hiddenTTo = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 4:  this.variableName = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 5:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

