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

public class ASTPrivateSequenceStmtNode extends ASTNode implements IDerivedTypeBodyConstruct
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTPrivateSequenceStmtNode
    org.eclipse.photran.internal.core.lexer.Token sequenceToken; // in ASTPrivateSequenceStmtNode
    org.eclipse.photran.internal.core.lexer.Token privateToken; // in ASTPrivateSequenceStmtNode
    org.eclipse.photran.internal.core.lexer.Token isSequence; // in ASTPrivateSequenceStmtNode
    org.eclipse.photran.internal.core.lexer.Token isPrivate; // in ASTPrivateSequenceStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getSequenceToken()
    {
        return this.sequenceToken;
    }

    public void setSequenceToken(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.sequenceToken = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getPrivateToken()
    {
        return this.privateToken;
    }

    public void setPrivateToken(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.privateToken = newValue;
    }


    public boolean isSequence()
    {
        return this.isSequence != null;
    }

    public void setIsSequence(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isSequence = newValue;
    }


    public boolean isPrivate()
    {
        return this.isPrivate != null;
    }

    public void setIsPrivate(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isPrivate = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTPrivateSequenceStmtNode(this);
        visitor.visitIDerivedTypeBodyConstruct(this);
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
        case 0:  return this.label;
        case 1:  return this.sequenceToken;
        case 2:  return this.privateToken;
        case 3:  return this.isSequence;
        case 4:  return this.isPrivate;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.sequenceToken = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.privateToken = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.isSequence = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 4:  this.isPrivate = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

