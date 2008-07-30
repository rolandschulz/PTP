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

public class ASTProgramStmtNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTProgramStmtNode
    org.eclipse.photran.internal.core.lexer.Token programToken; // in ASTProgramStmtNode
    ASTProgramNameNode programName; // in ASTProgramStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTProgramStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getProgramToken()
    {
        return this.programToken;
    }

    public void setProgramToken(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.programToken = newValue;
    }


    public ASTProgramNameNode getProgramName()
    {
        return this.programName;
    }

    public void setProgramName(ASTProgramNameNode newValue)
    {
        this.programName = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTProgramStmtNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 4;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.programToken;
        case 2:  return this.programName;
        case 3:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.programToken = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.programName = (ASTProgramNameNode)value;
        case 3:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

