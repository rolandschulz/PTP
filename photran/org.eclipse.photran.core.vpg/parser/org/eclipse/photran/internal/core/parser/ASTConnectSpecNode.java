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

import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;
import org.eclipse.photran.internal.core.lexer.Token;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings({ "unchecked", "unused" })
public class ASTConnectSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTEncodingeq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPadeq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTBlankeq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFileeq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRoundeq; // in ASTConnectSpecNode
    ASTCExprNode roundExpr; // in ASTConnectSpecNode
    ASTCExprNode encodingExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTDecimaleq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRecleq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTStatuseq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSigneq; // in ASTConnectSpecNode
    ASTCExprNode signExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPositioneq; // in ASTConnectSpecNode
    ASTCExprNode positionExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTActioneq; // in ASTConnectSpecNode
    ASTCExprNode actionExpr; // in ASTConnectSpecNode
    ASTCExprNode decimalExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTConverteq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAccesseq; // in ASTConnectSpecNode
    ASTCExprNode accessExpr; // in ASTConnectSpecNode
    ASTCExprNode convertExpr; // in ASTConnectSpecNode
    ASTCExprNode fileExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTDelimeq; // in ASTConnectSpecNode
    ASTCExprNode delimExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIostateq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTUniteq; // in ASTConnectSpecNode
    ASTCExprNode statusExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIomsgeq; // in ASTConnectSpecNode
    ASTScalarVariableNode iomsgExpr; // in ASTConnectSpecNode
    ASTCExprNode blankExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFormeq; // in ASTConnectSpecNode
    ASTCExprNode padExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAsynchronouseq; // in ASTConnectSpecNode
    ASTCExprNode asyncExpr; // in ASTConnectSpecNode
    ASTScalarVariableNode ioStatVar; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTErreq; // in ASTConnectSpecNode
    ASTUnitIdentifierNode unitIdentifier; // in ASTConnectSpecNode
    ASTLblRefNode errLbl; // in ASTConnectSpecNode
    ASTCExprNode formExpr; // in ASTConnectSpecNode
    IExpr reclExpr; // in ASTConnectSpecNode

    public ASTCExprNode getRoundExpr()
    {
        return this.roundExpr;
    }

    public void setRoundExpr(ASTCExprNode newValue)
    {
        this.roundExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCExprNode getEncodingExpr()
    {
        return this.encodingExpr;
    }

    public void setEncodingExpr(ASTCExprNode newValue)
    {
        this.encodingExpr = newValue;
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


    public ASTCExprNode getPositionExpr()
    {
        return this.positionExpr;
    }

    public void setPositionExpr(ASTCExprNode newValue)
    {
        this.positionExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCExprNode getActionExpr()
    {
        return this.actionExpr;
    }

    public void setActionExpr(ASTCExprNode newValue)
    {
        this.actionExpr = newValue;
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


    public ASTCExprNode getAccessExpr()
    {
        return this.accessExpr;
    }

    public void setAccessExpr(ASTCExprNode newValue)
    {
        this.accessExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCExprNode getConvertExpr()
    {
        return this.convertExpr;
    }

    public void setConvertExpr(ASTCExprNode newValue)
    {
        this.convertExpr = newValue;
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


    public ASTCExprNode getDelimExpr()
    {
        return this.delimExpr;
    }

    public void setDelimExpr(ASTCExprNode newValue)
    {
        this.delimExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCExprNode getStatusExpr()
    {
        return this.statusExpr;
    }

    public void setStatusExpr(ASTCExprNode newValue)
    {
        this.statusExpr = newValue;
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


    public ASTCExprNode getBlankExpr()
    {
        return this.blankExpr;
    }

    public void setBlankExpr(ASTCExprNode newValue)
    {
        this.blankExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCExprNode getPadExpr()
    {
        return this.padExpr;
    }

    public void setPadExpr(ASTCExprNode newValue)
    {
        this.padExpr = newValue;
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


    public ASTScalarVariableNode getIoStatVar()
    {
        return this.ioStatVar;
    }

    public void setIoStatVar(ASTScalarVariableNode newValue)
    {
        this.ioStatVar = newValue;
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


    public ASTLblRefNode getErrLbl()
    {
        return this.errLbl;
    }

    public void setErrLbl(ASTLblRefNode newValue)
    {
        this.errLbl = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCExprNode getFormExpr()
    {
        return this.formExpr;
    }

    public void setFormExpr(ASTCExprNode newValue)
    {
        this.formExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IExpr getReclExpr()
    {
        return this.reclExpr;
    }

    public void setReclExpr(IExpr newValue)
    {
        this.reclExpr = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTConnectSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 40;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTEncodingeq;
        case 1:  return this.hiddenTPadeq;
        case 2:  return this.hiddenTBlankeq;
        case 3:  return this.hiddenTFileeq;
        case 4:  return this.hiddenTRoundeq;
        case 5:  return this.roundExpr;
        case 6:  return this.encodingExpr;
        case 7:  return this.hiddenTDecimaleq;
        case 8:  return this.hiddenTRecleq;
        case 9:  return this.hiddenTStatuseq;
        case 10: return this.hiddenTSigneq;
        case 11: return this.signExpr;
        case 12: return this.hiddenTPositioneq;
        case 13: return this.positionExpr;
        case 14: return this.hiddenTActioneq;
        case 15: return this.actionExpr;
        case 16: return this.decimalExpr;
        case 17: return this.hiddenTConverteq;
        case 18: return this.hiddenTAccesseq;
        case 19: return this.accessExpr;
        case 20: return this.convertExpr;
        case 21: return this.fileExpr;
        case 22: return this.hiddenTDelimeq;
        case 23: return this.delimExpr;
        case 24: return this.hiddenTIostateq;
        case 25: return this.hiddenTUniteq;
        case 26: return this.statusExpr;
        case 27: return this.hiddenTIomsgeq;
        case 28: return this.iomsgExpr;
        case 29: return this.blankExpr;
        case 30: return this.hiddenTFormeq;
        case 31: return this.padExpr;
        case 32: return this.hiddenTAsynchronouseq;
        case 33: return this.asyncExpr;
        case 34: return this.ioStatVar;
        case 35: return this.hiddenTErreq;
        case 36: return this.unitIdentifier;
        case 37: return this.errLbl;
        case 38: return this.formExpr;
        case 39: return this.reclExpr;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTEncodingeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTPadeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTBlankeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTFileeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRoundeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.roundExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 6:  this.encodingExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTDecimaleq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTRecleq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTStatuseq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.hiddenTSigneq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 11: this.signExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 12: this.hiddenTPositioneq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 13: this.positionExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 14: this.hiddenTActioneq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 15: this.actionExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 16: this.decimalExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 17: this.hiddenTConverteq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 18: this.hiddenTAccesseq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 19: this.accessExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 20: this.convertExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 21: this.fileExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 22: this.hiddenTDelimeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 23: this.delimExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 24: this.hiddenTIostateq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 25: this.hiddenTUniteq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 26: this.statusExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 27: this.hiddenTIomsgeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 28: this.iomsgExpr = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 29: this.blankExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 30: this.hiddenTFormeq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 31: this.padExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 32: this.hiddenTAsynchronouseq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 33: this.asyncExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 34: this.ioStatVar = (ASTScalarVariableNode)value; if (value != null) value.setParent(this); return;
        case 35: this.hiddenTErreq = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 36: this.unitIdentifier = (ASTUnitIdentifierNode)value; if (value != null) value.setParent(this); return;
        case 37: this.errLbl = (ASTLblRefNode)value; if (value != null) value.setParent(this); return;
        case 38: this.formExpr = (ASTCExprNode)value; if (value != null) value.setParent(this); return;
        case 39: this.reclExpr = (IExpr)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

