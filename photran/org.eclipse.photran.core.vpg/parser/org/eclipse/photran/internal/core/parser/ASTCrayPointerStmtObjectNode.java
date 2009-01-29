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

public class ASTCrayPointerStmtObjectNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTCrayPointerStmtObjectNode
    org.eclipse.photran.internal.core.lexer.Token pointerName; // in ASTCrayPointerStmtObjectNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTCrayPointerStmtObjectNode
    ASTTargetObjectNode targetObject; // in ASTCrayPointerStmtObjectNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTCrayPointerStmtObjectNode

    public org.eclipse.photran.internal.core.lexer.Token getPointerName()
    {
        return this.pointerName;
    }

    public void setPointerName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.pointerName = newValue;
    }


    public ASTTargetObjectNode getTargetObject()
    {
        return this.targetObject;
    }

    public void setTargetObject(ASTTargetObjectNode newValue)
    {
        this.targetObject = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCrayPointerStmtObjectNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 5;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTLparen;
        case 1:  return this.pointerName;
        case 2:  return this.hiddenTComma;
        case 3:  return this.targetObject;
        case 4:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.pointerName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.targetObject = (ASTTargetObjectNode)value; return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

