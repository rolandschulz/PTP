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
public class ASTInquireSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTAsynchronouseq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRoundeq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTWriteeq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAccesseq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTDelimeq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTDecimaleq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRecleq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPositioneq; // in ASTInquireSpecNode
    IExpr reclExpr; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTNameeq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTStreameq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTExisteq; // in ASTInquireSpecNode
    ASTScalarVariableNode existVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFormattedeq; // in ASTInquireSpecNode
    ASTScalarVariableNode formattedVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIostateq; // in ASTInquireSpecNode
    ASTScalarVariableNode ioStatVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPoseq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFormeq; // in ASTInquireSpecNode
    ASTScalarVariableNode streamVar; // in ASTInquireSpecNode
    ASTScalarVariableNode roundExpr; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTNumbereq; // in ASTInquireSpecNode
    ASTScalarVariableNode asyncExpr; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTDirecteq; // in ASTInquireSpecNode
    ASTScalarVariableNode decimalExpr; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIomsgeq; // in ASTInquireSpecNode
    ASTScalarVariableNode iomsgExpr; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEncodingeq; // in ASTInquireSpecNode
    ASTScalarVariableNode encodingExpr; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSequentialeq; // in ASTInquireSpecNode
    ASTScalarVariableNode sequentialVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPendingeq; // in ASTInquireSpecNode
    ASTScalarVariableNode positionVar; // in ASTInquireSpecNode
    ASTScalarVariableNode directVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSizeeq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFileeq; // in ASTInquireSpecNode
    ASTScalarVariableNode numberVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTUnformattedeq; // in ASTInquireSpecNode
    ASTScalarVariableNode sizeVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTNextreceq; // in ASTInquireSpecNode
    ASTScalarVariableNode nextRecVar; // in ASTInquireSpecNode
    ASTCExprNode fileExpr; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTOpenedeq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTErreq; // in ASTInquireSpecNode
    ASTLblRefNode errVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSigneq; // in ASTInquireSpecNode
    ASTScalarVariableNode signExpr; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTBlankeq; // in ASTInquireSpecNode
    ASTScalarVariableNode blankVar; // in ASTInquireSpecNode
    ASTScalarVariableNode posVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIdeq; // in ASTInquireSpecNode
    ASTScalarVariableNode idVar; // in ASTInquireSpecNode
    ASTScalarVariableNode unformattedVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTActioneq; // in ASTInquireSpecNode
    ASTScalarVariableNode actionVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTUniteq; // in ASTInquireSpecNode
    ASTUnitIdentifierNode unitIdentifier; // in ASTInquireSpecNode
    ASTScalarVariableNode formVar; // in ASTInquireSpecNode
    ASTScalarVariableNode nameVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTReadeq; // in ASTInquireSpecNode
    ASTScalarVariableNode readVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTNamedeq; // in ASTInquireSpecNode
    ASTScalarVariableNode namedVar; // in ASTInquireSpecNode
    ASTScalarVariableNode pendingVar; // in ASTInquireSpecNode
    ASTScalarVariableNode delimVar; // in ASTInquireSpecNode
    ASTScalarVariableNode accessVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPadeq; // in ASTInquireSpecNode
    ASTScalarVariableNode padVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTReadwriteeq; // in ASTInquireSpecNode
    ASTScalarVariableNode readWriteVar; // in ASTInquireSpecNode
    ASTScalarVariableNode openedVar; // in ASTInquireSpecNode
    ASTScalarVariableNode writeVar; // in ASTInquireSpecNode

    public IExpr getReclExpr()
    {
        return this.reclExpr;
    }

    public void setReclExpr(IExpr newValue)
    {
        this.reclExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getExistVar()
    {
        return this.existVar;
    }

    public void setExistVar(ASTScalarVariableNode newValue)
    {
        this.existVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getFormattedVar()
    {
        return this.formattedVar;
    }

    public void setFormattedVar(ASTScalarVariableNode newValue)
    {
        this.formattedVar = newValue;
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


    public ASTScalarVariableNode getStreamVar()
    {
        return this.streamVar;
    }

    public void setStreamVar(ASTScalarVariableNode newValue)
    {
        this.streamVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getRoundExpr()
    {
        return this.roundExpr;
    }

    public void setRoundExpr(ASTScalarVariableNode newValue)
    {
        this.roundExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getAsyncExpr()
    {
        return this.asyncExpr;
    }

    public void setAsyncExpr(ASTScalarVariableNode newValue)
    {
        this.asyncExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getDecimalExpr()
    {
        return this.decimalExpr;
    }

    public void setDecimalExpr(ASTScalarVariableNode newValue)
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


    public ASTScalarVariableNode getEncodingExpr()
    {
        return this.encodingExpr;
    }

    public void setEncodingExpr(ASTScalarVariableNode newValue)
    {
        this.encodingExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getSequentialVar()
    {
        return this.sequentialVar;
    }

    public void setSequentialVar(ASTScalarVariableNode newValue)
    {
        this.sequentialVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getPositionVar()
    {
        return this.positionVar;
    }

    public void setPositionVar(ASTScalarVariableNode newValue)
    {
        this.positionVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getDirectVar()
    {
        return this.directVar;
    }

    public void setDirectVar(ASTScalarVariableNode newValue)
    {
        this.directVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getNumberVar()
    {
        return this.numberVar;
    }

    public void setNumberVar(ASTScalarVariableNode newValue)
    {
        this.numberVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getSizeVar()
    {
        return this.sizeVar;
    }

    public void setSizeVar(ASTScalarVariableNode newValue)
    {
        this.sizeVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getNextRecVar()
    {
        return this.nextRecVar;
    }

    public void setNextRecVar(ASTScalarVariableNode newValue)
    {
        this.nextRecVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCExprNode getFileExpr()
    {
        return this.fileExpr;
    }

    public void setFileExpr(ASTCExprNode newValue)
    {
        this.fileExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTLblRefNode getErrVar()
    {
        return this.errVar;
    }

    public void setErrVar(ASTLblRefNode newValue)
    {
        this.errVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getSignExpr()
    {
        return this.signExpr;
    }

    public void setSignExpr(ASTScalarVariableNode newValue)
    {
        this.signExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getBlankVar()
    {
        return this.blankVar;
    }

    public void setBlankVar(ASTScalarVariableNode newValue)
    {
        this.blankVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getPosVar()
    {
        return this.posVar;
    }

    public void setPosVar(ASTScalarVariableNode newValue)
    {
        this.posVar = newValue;
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


    public ASTScalarVariableNode getUnformattedVar()
    {
        return this.unformattedVar;
    }

    public void setUnformattedVar(ASTScalarVariableNode newValue)
    {
        this.unformattedVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getActionVar()
    {
        return this.actionVar;
    }

    public void setActionVar(ASTScalarVariableNode newValue)
    {
        this.actionVar = newValue;
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


    public ASTScalarVariableNode getFormVar()
    {
        return this.formVar;
    }

    public void setFormVar(ASTScalarVariableNode newValue)
    {
        this.formVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getNameVar()
    {
        return this.nameVar;
    }

    public void setNameVar(ASTScalarVariableNode newValue)
    {
        this.nameVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getReadVar()
    {
        return this.readVar;
    }

    public void setReadVar(ASTScalarVariableNode newValue)
    {
        this.readVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getNamedVar()
    {
        return this.namedVar;
    }

    public void setNamedVar(ASTScalarVariableNode newValue)
    {
        this.namedVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getPendingVar()
    {
        return this.pendingVar;
    }

    public void setPendingVar(ASTScalarVariableNode newValue)
    {
        this.pendingVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getDelimVar()
    {
        return this.delimVar;
    }

    public void setDelimVar(ASTScalarVariableNode newValue)
    {
        this.delimVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getAccessVar()
    {
        return this.accessVar;
    }

    public void setAccessVar(ASTScalarVariableNode newValue)
    {
        this.accessVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getPadVar()
    {
        return this.padVar;
    }

    public void setPadVar(ASTScalarVariableNode newValue)
    {
        this.padVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getReadWriteVar()
    {
        return this.readWriteVar;
    }

    public void setReadWriteVar(ASTScalarVariableNode newValue)
    {
        this.readWriteVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getOpenedVar()
    {
        return this.openedVar;
    }

    public void setOpenedVar(ASTScalarVariableNode newValue)
    {
        this.openedVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTScalarVariableNode getWriteVar()
    {
        return this.writeVar;
    }

    public void setWriteVar(ASTScalarVariableNode newValue)
    {
        this.writeVar = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTInquireSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 72;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTAsynchronouseq;
        case 1:  return this.hiddenTRoundeq;
        case 2:  return this.hiddenTWriteeq;
        case 3:  return this.hiddenTAccesseq;
        case 4:  return this.hiddenTDelimeq;
        case 5:  return this.hiddenTDecimaleq;
        case 6:  return this.hiddenTRecleq;
        case 7:  return this.hiddenTPositioneq;
        case 8:  return this.reclExpr;
        case 9:  return this.hiddenTNameeq;
        case 10: return this.hiddenTStreameq;
        case 11: return this.hiddenTExisteq;
        case 12: return this.existVar;
        case 13: return this.hiddenTFormattedeq;
        case 14: return this.formattedVar;
        case 15: return this.hiddenTIostateq;
        case 16: return this.ioStatVar;
        case 17: return this.hiddenTPoseq;
        case 18: return this.hiddenTFormeq;
        case 19: return this.streamVar;
        case 20: return this.roundExpr;
        case 21: return this.hiddenTNumbereq;
        case 22: return this.asyncExpr;
        case 23: return this.hiddenTDirecteq;
        case 24: return this.decimalExpr;
        case 25: return this.hiddenTIomsgeq;
        case 26: return this.iomsgExpr;
        case 27: return this.hiddenTEncodingeq;
        case 28: return this.encodingExpr;
        case 29: return this.hiddenTSequentialeq;
        case 30: return this.sequentialVar;
        case 31: return this.hiddenTPendingeq;
        case 32: return this.positionVar;
        case 33: return this.directVar;
        case 34: return this.hiddenTSizeeq;
        case 35: return this.hiddenTFileeq;
        case 36: return this.numberVar;
        case 37: return this.hiddenTUnformattedeq;
        case 38: return this.sizeVar;
        case 39: return this.hiddenTNextreceq;
        case 40: return this.nextRecVar;
        case 41: return this.fileExpr;
        case 42: return this.hiddenTOpenedeq;
        case 43: return this.hiddenTErreq;
        case 44: return this.errVar;
        case 45: return this.hiddenTSigneq;
        case 46: return this.signExpr;
        case 47: return this.hiddenTBlankeq;
        case 48: return this.blankVar;
        case 49: return this.posVar;
        case 50: return this.hiddenTIdeq;
        case 51: return this.idVar;
        case 52: return this.unformattedVar;
        case 53: return this.hiddenTActioneq;
        case 54: return this.actionVar;
        case 55: return this.hiddenTUniteq;
        case 56: return this.unitIdentifier;
        case 57: return this.formVar;
        case 58: return this.nameVar;
        case 59: return this.hiddenTReadeq;
        case 60: return this.readVar;
        case 61: return this.hiddenTNamedeq;
        case 62: return this.namedVar;
        case 63: return this.pendingVar;
        case 64: return this.delimVar;
        case 65: return this.accessVar;
        case 66: return this.hiddenTPadeq;
        case 67: return this.padVar;
        case 68: return this.hiddenTReadwriteeq;
        case 69: return this.readWriteVar;
        case 70: return this.openedVar;
        case 71: return this.writeVar;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTAsynchronouseq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTRoundeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTWriteeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTAccesseq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTDelimeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTDecimaleq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTRecleq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTPositioneq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.reclExpr = (IExpr)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTNameeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.hiddenTStreameq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 11: this.hiddenTExisteq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 12: this.existVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 13: this.hiddenTFormattedeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 14: this.formattedVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 15: this.hiddenTIostateq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 16: this.ioStatVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 17: this.hiddenTPoseq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 18: this.hiddenTFormeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 19: this.streamVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 20: this.roundExpr = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 21: this.hiddenTNumbereq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 22: this.asyncExpr = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 23: this.hiddenTDirecteq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 24: this.decimalExpr = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 25: this.hiddenTIomsgeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 26: this.iomsgExpr = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 27: this.hiddenTEncodingeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 28: this.encodingExpr = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 29: this.hiddenTSequentialeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 30: this.sequentialVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 31: this.hiddenTPendingeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 32: this.positionVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 33: this.directVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 34: this.hiddenTSizeeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 35: this.hiddenTFileeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 36: this.numberVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 37: this.hiddenTUnformattedeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 38: this.sizeVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 39: this.hiddenTNextreceq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 40: this.nextRecVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 41: this.fileExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 42: this.hiddenTOpenedeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 43: this.hiddenTErreq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 44: this.errVar = (ASTLblRefNode)value; if (value != null) value.setParent(this); return;
        case 45: this.hiddenTSigneq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 46: this.signExpr = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 47: this.hiddenTBlankeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 48: this.blankVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 49: this.posVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 50: this.hiddenTIdeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 51: this.idVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 52: this.unformattedVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 53: this.hiddenTActioneq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 54: this.actionVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 55: this.hiddenTUniteq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 56: this.unitIdentifier = (ASTUnitIdentifierNode)value; if (value != null) value.setParent(this); return;
        case 57: this.formVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 58: this.nameVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 59: this.hiddenTReadeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 60: this.readVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 61: this.hiddenTNamedeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 62: this.namedVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 63: this.pendingVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 64: this.delimVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 65: this.accessVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 66: this.hiddenTPadeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 67: this.padVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 68: this.hiddenTReadwriteeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 69: this.readWriteVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 70: this.openedVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 71: this.writeVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

