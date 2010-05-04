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
public class ASTIoControlSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTEoreq; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPoseq; // in ASTIoControlSpecNode
    ASTCExprNode posExpr; // in ASTIoControlSpecNode
    ASTLblRefNode eorLbl; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTErreq; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIostateq; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAdvanceeq; // in ASTIoControlSpecNode
    ASTCExprNode advanceExpr; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAsynchronouseq; // in ASTIoControlSpecNode
    ASTCExprNode asyncExpr; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSigneq; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEndeq; // in ASTIoControlSpecNode
    ASTLblRefNode endExpr; // in ASTIoControlSpecNode
    ASTLblRefNode errLbl; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIomsgeq; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTUniteq; // in ASTIoControlSpecNode
    ASTUnitIdentifierNode unitIdentifier; // in ASTIoControlSpecNode
    ASTScalarVariableNode ioStatVar; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTNmleq; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRoundeq; // in ASTIoControlSpecNode
    ASTCExprNode roundExpr; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTDecimaleq; // in ASTIoControlSpecNode
    ASTCExprNode decimalExpr; // in ASTIoControlSpecNode
    ASTScalarVariableNode iomsgExpr; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFmteq; // in ASTIoControlSpecNode
    ASTFormatIdentifierNode formatIdentifier; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTReceq; // in ASTIoControlSpecNode
    IExpr recExpr; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSizeeq; // in ASTIoControlSpecNode
    ASTVariableNode sizeVar; // in ASTIoControlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIdeq; // in ASTIoControlSpecNode
    ASTScalarVariableNode idVar; // in ASTIoControlSpecNode
    ASTCExprNode signExpr; // in ASTIoControlSpecNode
    ASTNamelistGroupNameNode namelistGroupName; // in ASTIoControlSpecNode

    public ASTCExprNode getPosExpr()
    {
        return this.posExpr;
    }

    public void setPosExpr(ASTCExprNode newValue)
    {
        this.posExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTLblRefNode getEorLbl()
    {
        return this.eorLbl;
    }

    public void setEorLbl(ASTLblRefNode newValue)
    {
        this.eorLbl = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCExprNode getAdvanceExpr()
    {
        return this.advanceExpr;
    }

    public void setAdvanceExpr(ASTCExprNode newValue)
    {
        this.advanceExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCExprNode getAsyncExpr()
    {
        return this.asyncExpr;
    }

    public void setAsyncExpr(ASTCExprNode newValue)
    {
        this.asyncExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTLblRefNode getEndExpr()
    {
        return this.endExpr;
    }

    public void setEndExpr(ASTLblRefNode newValue)
    {
        this.endExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTLblRefNode getErrLbl()
    {
        return this.errLbl;
    }

    public void setErrLbl(ASTLblRefNode newValue)
    {
        this.errLbl = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        return this.unitIdentifier;
    }

    public void setUnitIdentifier(ASTUnitIdentifierNode newValue)
    {
        this.unitIdentifier = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getIoStatVar()
    {
        return this.ioStatVar;
    }

    public void setIoStatVar(ASTScalarVariableNode newValue)
    {
        this.ioStatVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCExprNode getRoundExpr()
    {
        return this.roundExpr;
    }

    public void setRoundExpr(ASTCExprNode newValue)
    {
        this.roundExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCExprNode getDecimalExpr()
    {
        return this.decimalExpr;
    }

    public void setDecimalExpr(ASTCExprNode newValue)
    {
        this.decimalExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getIomsgExpr()
    {
        return this.iomsgExpr;
    }

    public void setIomsgExpr(ASTScalarVariableNode newValue)
    {
        this.iomsgExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTFormatIdentifierNode getFormatIdentifier()
    {
        return this.formatIdentifier;
    }

    public void setFormatIdentifier(ASTFormatIdentifierNode newValue)
    {
        this.formatIdentifier = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getRecExpr()
    {
        return this.recExpr;
    }

    public void setRecExpr(IExpr newValue)
    {
        this.recExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTVariableNode getSizeVar()
    {
        return this.sizeVar;
    }

    public void setSizeVar(ASTVariableNode newValue)
    {
        this.sizeVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getIdVar()
    {
        return this.idVar;
    }

    public void setIdVar(ASTScalarVariableNode newValue)
    {
        this.idVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCExprNode getSignExpr()
    {
        return this.signExpr;
    }

    public void setSignExpr(ASTCExprNode newValue)
    {
        this.signExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTNamelistGroupNameNode getNamelistGroupName()
    {
        return this.namelistGroupName;
    }

    public void setNamelistGroupName(ASTNamelistGroupNameNode newValue)
    {
        this.namelistGroupName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTIoControlSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 34;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTEoreq;
        case 1:  return this.hiddenTPoseq;
        case 2:  return this.posExpr;
        case 3:  return this.eorLbl;
        case 4:  return this.hiddenTErreq;
        case 5:  return this.hiddenTIostateq;
        case 6:  return this.hiddenTAdvanceeq;
        case 7:  return this.advanceExpr;
        case 8:  return this.hiddenTAsynchronouseq;
        case 9:  return this.asyncExpr;
        case 10: return this.hiddenTSigneq;
        case 11: return this.hiddenTEndeq;
        case 12: return this.endExpr;
        case 13: return this.errLbl;
        case 14: return this.hiddenTIomsgeq;
        case 15: return this.hiddenTUniteq;
        case 16: return this.unitIdentifier;
        case 17: return this.ioStatVar;
        case 18: return this.hiddenTNmleq;
        case 19: return this.hiddenTRoundeq;
        case 20: return this.roundExpr;
        case 21: return this.hiddenTDecimaleq;
        case 22: return this.decimalExpr;
        case 23: return this.iomsgExpr;
        case 24: return this.hiddenTFmteq;
        case 25: return this.formatIdentifier;
        case 26: return this.hiddenTReceq;
        case 27: return this.recExpr;
        case 28: return this.hiddenTSizeeq;
        case 29: return this.sizeVar;
        case 30: return this.hiddenTIdeq;
        case 31: return this.idVar;
        case 32: return this.signExpr;
        case 33: return this.namelistGroupName;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTEoreq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTPoseq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.posExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.eorLbl = (ASTLblRefNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTErreq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTIostateq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTAdvanceeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.advanceExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTAsynchronouseq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.asyncExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 10: this.hiddenTSigneq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 11: this.hiddenTEndeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 12: this.endExpr = (ASTLblRefNode)value; if (value != null) value.setParent(this); return;
        case 13: this.errLbl = (ASTLblRefNode)value; if (value != null) value.setParent(this); return;
        case 14: this.hiddenTIomsgeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 15: this.hiddenTUniteq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 16: this.unitIdentifier = (ASTUnitIdentifierNode)value; if (value != null) value.setParent(this); return;
        case 17: this.ioStatVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 18: this.hiddenTNmleq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 19: this.hiddenTRoundeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 20: this.roundExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 21: this.hiddenTDecimaleq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 22: this.decimalExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 23: this.iomsgExpr = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 24: this.hiddenTFmteq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 25: this.formatIdentifier = (ASTFormatIdentifierNode)value; if (value != null) value.setParent(this); return;
        case 26: this.hiddenTReceq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 27: this.recExpr = (IExpr)value; if (value != null) value.setParent(this); return;
        case 28: this.hiddenTSizeeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 29: this.sizeVar = (ASTVariableNode)value; if (value != null) value.setParent(this); return;
        case 30: this.hiddenTIdeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 31: this.idVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 32: this.signExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 33: this.namelistGroupName = (ASTNamelistGroupNameNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

