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

public class ASTIoControlSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTAdvanceeq; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSizeeq; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTUniteq; // in ASTIoControlSpecNode
    ASTUnitIdentifierNode unitIdentifier; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTReceq; // in ASTIoControlSpecNode
    ASTVariableNode sizeVar; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEoreq; // in ASTIoControlSpecNode
    ASTLblRefNode eorLbl; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTNmleq; // in ASTIoControlSpecNode
    ASTCExprNode advanceExpr; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFmteq; // in ASTIoControlSpecNode
    ASTFormatIdentifierNode formatIdentifier; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTErreq; // in ASTIoControlSpecNode
    ASTLblRefNode errLbl; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIostateq; // in ASTIoControlSpecNode
    ASTScalarVariableNode ioStatVar; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEndeq; // in ASTIoControlSpecNode
    ASTLblRefNode endExpr; // in ASTIoControlSpecNode
    ASTNamelistGroupNameNode namelistGroupName; // in ASTIoControlSpecNode
    ASTExprNode recExpr; // in ASTIoControlSpecNode

    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        return this.unitIdentifier;
    }

    public void setUnitIdentifier(ASTUnitIdentifierNode newValue)
    {
        this.unitIdentifier = newValue;
    }


    public ASTVariableNode getSizeVar()
    {
        return this.sizeVar;
    }

    public void setSizeVar(ASTVariableNode newValue)
    {
        this.sizeVar = newValue;
    }


    public ASTLblRefNode getEorLbl()
    {
        return this.eorLbl;
    }

    public void setEorLbl(ASTLblRefNode newValue)
    {
        this.eorLbl = newValue;
    }


    public ASTCExprNode getAdvanceExpr()
    {
        return this.advanceExpr;
    }

    public void setAdvanceExpr(ASTCExprNode newValue)
    {
        this.advanceExpr = newValue;
    }


    public ASTFormatIdentifierNode getFormatIdentifier()
    {
        return this.formatIdentifier;
    }

    public void setFormatIdentifier(ASTFormatIdentifierNode newValue)
    {
        this.formatIdentifier = newValue;
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


    public ASTLblRefNode getEndExpr()
    {
        return this.endExpr;
    }

    public void setEndExpr(ASTLblRefNode newValue)
    {
        this.endExpr = newValue;
    }


    public ASTNamelistGroupNameNode getNamelistGroupName()
    {
        return this.namelistGroupName;
    }

    public void setNamelistGroupName(ASTNamelistGroupNameNode newValue)
    {
        this.namelistGroupName = newValue;
    }


    public ASTExprNode getRecExpr()
    {
        return this.recExpr;
    }

    public void setRecExpr(ASTExprNode newValue)
    {
        this.recExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTIoControlSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 20;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTAdvanceeq;
        case 1:  return this.hiddenTSizeeq;
        case 2:  return this.hiddenTUniteq;
        case 3:  return this.unitIdentifier;
        case 4:  return this.hiddenTReceq;
        case 5:  return this.sizeVar;
        case 6:  return this.hiddenTEoreq;
        case 7:  return this.eorLbl;
        case 8:  return this.hiddenTNmleq;
        case 9:  return this.advanceExpr;
        case 10: return this.hiddenTFmteq;
        case 11: return this.formatIdentifier;
        case 12: return this.hiddenTErreq;
        case 13: return this.errLbl;
        case 14: return this.hiddenTIostateq;
        case 15: return this.ioStatVar;
        case 16: return this.hiddenTEndeq;
        case 17: return this.endExpr;
        case 18: return this.namelistGroupName;
        case 19: return this.recExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTAdvanceeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTSizeeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTUniteq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.unitIdentifier = (ASTUnitIdentifierNode)value; return;
        case 4:  this.hiddenTReceq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.sizeVar = (ASTVariableNode)value; return;
        case 6:  this.hiddenTEoreq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.eorLbl = (ASTLblRefNode)value; return;
        case 8:  this.hiddenTNmleq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 9:  this.advanceExpr = (ASTCExprNode)value; return;
        case 10: this.hiddenTFmteq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 11: this.formatIdentifier = (ASTFormatIdentifierNode)value; return;
        case 12: this.hiddenTErreq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 13: this.errLbl = (ASTLblRefNode)value; return;
        case 14: this.hiddenTIostateq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 15: this.ioStatVar = (ASTScalarVariableNode)value; return;
        case 16: this.hiddenTEndeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 17: this.endExpr = (ASTLblRefNode)value; return;
        case 18: this.namelistGroupName = (ASTNamelistGroupNameNode)value; return;
        case 19: this.recExpr = (ASTExprNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

