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

public class ASTPositionSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTErreq; // in ASTPositionSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIostateq; // in ASTPositionSpecNode
    ASTScalarVariableNode ioStatVar; // in ASTPositionSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTUniteq; // in ASTPositionSpecNode
    ASTUnitIdentifierNode unitIdentifier; // in ASTPositionSpecNode
    ASTLblRefNode errLbl; // in ASTPositionSpecNode

    public ASTScalarVariableNode getIoStatVar()
    {
        return this.ioStatVar;
    }

    public void setIoStatVar(ASTScalarVariableNode newValue)
    {
        this.ioStatVar = newValue;
    }


    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        return this.unitIdentifier;
    }

    public void setUnitIdentifier(ASTUnitIdentifierNode newValue)
    {
        this.unitIdentifier = newValue;
    }


    public ASTLblRefNode getErrLbl()
    {
        return this.errLbl;
    }

    public void setErrLbl(ASTLblRefNode newValue)
    {
        this.errLbl = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTPositionSpecNode(this);
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
        case 0:  return this.hiddenTErreq;
        case 1:  return this.hiddenTIostateq;
        case 2:  return this.ioStatVar;
        case 3:  return this.hiddenTUniteq;
        case 4:  return this.unitIdentifier;
        case 5:  return this.errLbl;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTErreq = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.hiddenTIostateq = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.ioStatVar = (ASTScalarVariableNode)value;
        case 3:  this.hiddenTUniteq = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 4:  this.unitIdentifier = (ASTUnitIdentifierNode)value;
        case 5:  this.errLbl = (ASTLblRefNode)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

