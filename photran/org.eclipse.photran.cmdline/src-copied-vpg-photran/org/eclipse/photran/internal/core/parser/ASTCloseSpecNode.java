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

public class ASTCloseSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTUniteq; // in ASTCloseSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTStatuseq; // in ASTCloseSpecNode
    ASTCExprNode statusExpr; // in ASTCloseSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIostateq; // in ASTCloseSpecNode
    ASTUnitIdentifierNode unitIdentifier; // in ASTCloseSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTErreq; // in ASTCloseSpecNode
    ASTLblRefNode errLbl; // in ASTCloseSpecNode
    ASTScalarVariableNode ioStatVar; // in ASTCloseSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIomsgeq; // in ASTCloseSpecNode
    ASTScalarVariableNode ioMsgVar; // in ASTCloseSpecNode

    public ASTCExprNode getStatusExpr()
    {
        return this.statusExpr;
    }

    public void setStatusExpr(ASTCExprNode newValue)
    {
        this.statusExpr = newValue;
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


    public ASTScalarVariableNode getIoStatVar()
    {
        return this.ioStatVar;
    }

    public void setIoStatVar(ASTScalarVariableNode newValue)
    {
        this.ioStatVar = newValue;
    }


    public ASTScalarVariableNode getIoMsgVar()
    {
        return this.ioMsgVar;
    }

    public void setIoMsgVar(ASTScalarVariableNode newValue)
    {
        this.ioMsgVar = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTCloseSpecNode(this);
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
        case 0:  return this.hiddenTUniteq;
        case 1:  return this.hiddenTStatuseq;
        case 2:  return this.statusExpr;
        case 3:  return this.hiddenTIostateq;
        case 4:  return this.unitIdentifier;
        case 5:  return this.hiddenTErreq;
        case 6:  return this.errLbl;
        case 7:  return this.ioStatVar;
        case 8:  return this.hiddenTIomsgeq;
        case 9:  return this.ioMsgVar;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTUniteq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTStatuseq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.statusExpr = (ASTCExprNode)value; return;
        case 3:  this.hiddenTIostateq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.unitIdentifier = (ASTUnitIdentifierNode)value; return;
        case 5:  this.hiddenTErreq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.errLbl = (ASTLblRefNode)value; return;
        case 7:  this.ioStatVar = (ASTScalarVariableNode)value; return;
        case 8:  this.hiddenTIomsgeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 9:  this.ioMsgVar = (ASTScalarVariableNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

