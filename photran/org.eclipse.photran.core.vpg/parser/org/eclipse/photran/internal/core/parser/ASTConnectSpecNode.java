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

public class ASTConnectSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTUniteq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTDelimeq; // in ASTConnectSpecNode
    ASTCExprNode delimExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTActioneq; // in ASTConnectSpecNode
    ASTCExprNode actionExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAccesseq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFileeq; // in ASTConnectSpecNode
    ASTCExprNode fileExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTErreq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTStatuseq; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPadeq; // in ASTConnectSpecNode
    ASTCExprNode accessExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPositioneq; // in ASTConnectSpecNode
    ASTCExprNode positionExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIostateq; // in ASTConnectSpecNode
    ASTScalarVariableNode ioStatVar; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFormeq; // in ASTConnectSpecNode
    ASTCExprNode formExpr; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTBlankeq; // in ASTConnectSpecNode
    ASTCExprNode blankExpr; // in ASTConnectSpecNode
    ASTCExprNode statusExpr; // in ASTConnectSpecNode
    ASTCExprNode padExpr; // in ASTConnectSpecNode
    ASTUnitIdentifierNode unitIdentifier; // in ASTConnectSpecNode
    ASTLblRefNode errLbl; // in ASTConnectSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRecleq; // in ASTConnectSpecNode
    IExpr reclExpr; // in ASTConnectSpecNode

    public ASTCExprNode getDelimExpr()
    {
        return this.delimExpr;
    }

    public void setDelimExpr(ASTCExprNode newValue)
    {
        this.delimExpr = newValue;
    }


    public ASTCExprNode getActionExpr()
    {
        return this.actionExpr;
    }

    public void setActionExpr(ASTCExprNode newValue)
    {
        this.actionExpr = newValue;
    }


    public ASTCExprNode getFileExpr()
    {
        return this.fileExpr;
    }

    public void setFileExpr(ASTCExprNode newValue)
    {
        this.fileExpr = newValue;
    }


    public ASTCExprNode getAccessExpr()
    {
        return this.accessExpr;
    }

    public void setAccessExpr(ASTCExprNode newValue)
    {
        this.accessExpr = newValue;
    }


    public ASTCExprNode getPositionExpr()
    {
        return this.positionExpr;
    }

    public void setPositionExpr(ASTCExprNode newValue)
    {
        this.positionExpr = newValue;
    }


    public ASTScalarVariableNode getIoStatVar()
    {
        return this.ioStatVar;
    }

    public void setIoStatVar(ASTScalarVariableNode newValue)
    {
        this.ioStatVar = newValue;
    }


    public ASTCExprNode getFormExpr()
    {
        return this.formExpr;
    }

    public void setFormExpr(ASTCExprNode newValue)
    {
        this.formExpr = newValue;
    }


    public ASTCExprNode getBlankExpr()
    {
        return this.blankExpr;
    }

    public void setBlankExpr(ASTCExprNode newValue)
    {
        this.blankExpr = newValue;
    }


    public ASTCExprNode getStatusExpr()
    {
        return this.statusExpr;
    }

    public void setStatusExpr(ASTCExprNode newValue)
    {
        this.statusExpr = newValue;
    }


    public ASTCExprNode getPadExpr()
    {
        return this.padExpr;
    }

    public void setPadExpr(ASTCExprNode newValue)
    {
        this.padExpr = newValue;
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


    public IExpr getReclExpr()
    {
        return this.reclExpr;
    }

    public void setReclExpr(IExpr newValue)
    {
        this.reclExpr = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTConnectSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 26;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTUniteq;
        case 1:  return this.hiddenTDelimeq;
        case 2:  return this.delimExpr;
        case 3:  return this.hiddenTActioneq;
        case 4:  return this.actionExpr;
        case 5:  return this.hiddenTAccesseq;
        case 6:  return this.hiddenTFileeq;
        case 7:  return this.fileExpr;
        case 8:  return this.hiddenTErreq;
        case 9:  return this.hiddenTStatuseq;
        case 10: return this.hiddenTPadeq;
        case 11: return this.accessExpr;
        case 12: return this.hiddenTPositioneq;
        case 13: return this.positionExpr;
        case 14: return this.hiddenTIostateq;
        case 15: return this.ioStatVar;
        case 16: return this.hiddenTFormeq;
        case 17: return this.formExpr;
        case 18: return this.hiddenTBlankeq;
        case 19: return this.blankExpr;
        case 20: return this.statusExpr;
        case 21: return this.padExpr;
        case 22: return this.unitIdentifier;
        case 23: return this.errLbl;
        case 24: return this.hiddenTRecleq;
        case 25: return this.reclExpr;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTUniteq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTDelimeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.delimExpr = (ASTCExprNode)value; return;
        case 3:  this.hiddenTActioneq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.actionExpr = (ASTCExprNode)value; return;
        case 5:  this.hiddenTAccesseq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.hiddenTFileeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.fileExpr = (ASTCExprNode)value; return;
        case 8:  this.hiddenTErreq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 9:  this.hiddenTStatuseq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 10: this.hiddenTPadeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 11: this.accessExpr = (ASTCExprNode)value; return;
        case 12: this.hiddenTPositioneq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 13: this.positionExpr = (ASTCExprNode)value; return;
        case 14: this.hiddenTIostateq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 15: this.ioStatVar = (ASTScalarVariableNode)value; return;
        case 16: this.hiddenTFormeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 17: this.formExpr = (ASTCExprNode)value; return;
        case 18: this.hiddenTBlankeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 19: this.blankExpr = (ASTCExprNode)value; return;
        case 20: this.statusExpr = (ASTCExprNode)value; return;
        case 21: this.padExpr = (ASTCExprNode)value; return;
        case 22: this.unitIdentifier = (ASTUnitIdentifierNode)value; return;
        case 23: this.errLbl = (ASTLblRefNode)value; return;
        case 24: this.hiddenTRecleq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 25: this.reclExpr = (IExpr)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

