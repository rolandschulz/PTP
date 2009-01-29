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

public class ASTInquireSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTWriteeq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAccesseq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTDelimeq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRecleq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTPositioneq; // in ASTInquireSpecNode
    IExpr reclExpr; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTNameeq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTExisteq; // in ASTInquireSpecNode
    ASTScalarVariableNode existVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFormattedeq; // in ASTInquireSpecNode
    ASTScalarVariableNode formattedVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTIostateq; // in ASTInquireSpecNode
    ASTScalarVariableNode ioStatVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFormeq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTNumbereq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTDirecteq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSequentialeq; // in ASTInquireSpecNode
    ASTScalarVariableNode sequentialVar; // in ASTInquireSpecNode
    ASTScalarVariableNode positionVar; // in ASTInquireSpecNode
    ASTScalarVariableNode directVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTFileeq; // in ASTInquireSpecNode
    ASTScalarVariableNode numberVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTUnformattedeq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTNextreceq; // in ASTInquireSpecNode
    ASTScalarVariableNode nextRecVar; // in ASTInquireSpecNode
    ASTCExprNode fileExpr; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTOpenedeq; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTErreq; // in ASTInquireSpecNode
    ASTLblRefNode errVar; // in ASTInquireSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTBlankeq; // in ASTInquireSpecNode
    ASTScalarVariableNode blankVar; // in ASTInquireSpecNode
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
    }


    public ASTScalarVariableNode getExistVar()
    {
        return this.existVar;
    }

    public void setExistVar(ASTScalarVariableNode newValue)
    {
        this.existVar = newValue;
    }


    public ASTScalarVariableNode getFormattedVar()
    {
        return this.formattedVar;
    }

    public void setFormattedVar(ASTScalarVariableNode newValue)
    {
        this.formattedVar = newValue;
    }


    public ASTScalarVariableNode getIoStatVar()
    {
        return this.ioStatVar;
    }

    public void setIoStatVar(ASTScalarVariableNode newValue)
    {
        this.ioStatVar = newValue;
    }


    public ASTScalarVariableNode getSequentialVar()
    {
        return this.sequentialVar;
    }

    public void setSequentialVar(ASTScalarVariableNode newValue)
    {
        this.sequentialVar = newValue;
    }


    public ASTScalarVariableNode getPositionVar()
    {
        return this.positionVar;
    }

    public void setPositionVar(ASTScalarVariableNode newValue)
    {
        this.positionVar = newValue;
    }


    public ASTScalarVariableNode getDirectVar()
    {
        return this.directVar;
    }

    public void setDirectVar(ASTScalarVariableNode newValue)
    {
        this.directVar = newValue;
    }


    public ASTScalarVariableNode getNumberVar()
    {
        return this.numberVar;
    }

    public void setNumberVar(ASTScalarVariableNode newValue)
    {
        this.numberVar = newValue;
    }


    public ASTScalarVariableNode getNextRecVar()
    {
        return this.nextRecVar;
    }

    public void setNextRecVar(ASTScalarVariableNode newValue)
    {
        this.nextRecVar = newValue;
    }


    public ASTCExprNode getFileExpr()
    {
        return this.fileExpr;
    }

    public void setFileExpr(ASTCExprNode newValue)
    {
        this.fileExpr = newValue;
    }


    public ASTLblRefNode getErrVar()
    {
        return this.errVar;
    }

    public void setErrVar(ASTLblRefNode newValue)
    {
        this.errVar = newValue;
    }


    public ASTScalarVariableNode getBlankVar()
    {
        return this.blankVar;
    }

    public void setBlankVar(ASTScalarVariableNode newValue)
    {
        this.blankVar = newValue;
    }


    public ASTScalarVariableNode getUnformattedVar()
    {
        return this.unformattedVar;
    }

    public void setUnformattedVar(ASTScalarVariableNode newValue)
    {
        this.unformattedVar = newValue;
    }


    public ASTScalarVariableNode getActionVar()
    {
        return this.actionVar;
    }

    public void setActionVar(ASTScalarVariableNode newValue)
    {
        this.actionVar = newValue;
    }


    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        return this.unitIdentifier;
    }

    public void setUnitIdentifier(ASTUnitIdentifierNode newValue)
    {
        this.unitIdentifier = newValue;
    }


    public ASTScalarVariableNode getFormVar()
    {
        return this.formVar;
    }

    public void setFormVar(ASTScalarVariableNode newValue)
    {
        this.formVar = newValue;
    }


    public ASTScalarVariableNode getNameVar()
    {
        return this.nameVar;
    }

    public void setNameVar(ASTScalarVariableNode newValue)
    {
        this.nameVar = newValue;
    }


    public ASTScalarVariableNode getReadVar()
    {
        return this.readVar;
    }

    public void setReadVar(ASTScalarVariableNode newValue)
    {
        this.readVar = newValue;
    }


    public ASTScalarVariableNode getNamedVar()
    {
        return this.namedVar;
    }

    public void setNamedVar(ASTScalarVariableNode newValue)
    {
        this.namedVar = newValue;
    }


    public ASTScalarVariableNode getDelimVar()
    {
        return this.delimVar;
    }

    public void setDelimVar(ASTScalarVariableNode newValue)
    {
        this.delimVar = newValue;
    }


    public ASTScalarVariableNode getAccessVar()
    {
        return this.accessVar;
    }

    public void setAccessVar(ASTScalarVariableNode newValue)
    {
        this.accessVar = newValue;
    }


    public ASTScalarVariableNode getPadVar()
    {
        return this.padVar;
    }

    public void setPadVar(ASTScalarVariableNode newValue)
    {
        this.padVar = newValue;
    }


    public ASTScalarVariableNode getReadWriteVar()
    {
        return this.readWriteVar;
    }

    public void setReadWriteVar(ASTScalarVariableNode newValue)
    {
        this.readWriteVar = newValue;
    }


    public ASTScalarVariableNode getOpenedVar()
    {
        return this.openedVar;
    }

    public void setOpenedVar(ASTScalarVariableNode newValue)
    {
        this.openedVar = newValue;
    }


    public ASTScalarVariableNode getWriteVar()
    {
        return this.writeVar;
    }

    public void setWriteVar(ASTScalarVariableNode newValue)
    {
        this.writeVar = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTInquireSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 50;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTWriteeq;
        case 1:  return this.hiddenTAccesseq;
        case 2:  return this.hiddenTDelimeq;
        case 3:  return this.hiddenTRecleq;
        case 4:  return this.hiddenTPositioneq;
        case 5:  return this.reclExpr;
        case 6:  return this.hiddenTNameeq;
        case 7:  return this.hiddenTExisteq;
        case 8:  return this.existVar;
        case 9:  return this.hiddenTFormattedeq;
        case 10: return this.formattedVar;
        case 11: return this.hiddenTIostateq;
        case 12: return this.ioStatVar;
        case 13: return this.hiddenTFormeq;
        case 14: return this.hiddenTNumbereq;
        case 15: return this.hiddenTDirecteq;
        case 16: return this.hiddenTSequentialeq;
        case 17: return this.sequentialVar;
        case 18: return this.positionVar;
        case 19: return this.directVar;
        case 20: return this.hiddenTFileeq;
        case 21: return this.numberVar;
        case 22: return this.hiddenTUnformattedeq;
        case 23: return this.hiddenTNextreceq;
        case 24: return this.nextRecVar;
        case 25: return this.fileExpr;
        case 26: return this.hiddenTOpenedeq;
        case 27: return this.hiddenTErreq;
        case 28: return this.errVar;
        case 29: return this.hiddenTBlankeq;
        case 30: return this.blankVar;
        case 31: return this.unformattedVar;
        case 32: return this.hiddenTActioneq;
        case 33: return this.actionVar;
        case 34: return this.hiddenTUniteq;
        case 35: return this.unitIdentifier;
        case 36: return this.formVar;
        case 37: return this.nameVar;
        case 38: return this.hiddenTReadeq;
        case 39: return this.readVar;
        case 40: return this.hiddenTNamedeq;
        case 41: return this.namedVar;
        case 42: return this.delimVar;
        case 43: return this.accessVar;
        case 44: return this.hiddenTPadeq;
        case 45: return this.padVar;
        case 46: return this.hiddenTReadwriteeq;
        case 47: return this.readWriteVar;
        case 48: return this.openedVar;
        case 49: return this.writeVar;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTWriteeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTAccesseq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenTDelimeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTRecleq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hiddenTPositioneq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.reclExpr = (IExpr)value; return;
        case 6:  this.hiddenTNameeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hiddenTExisteq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.existVar = (ASTScalarVariableNode)value; return;
        case 9:  this.hiddenTFormattedeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 10: this.formattedVar = (ASTScalarVariableNode)value; return;
        case 11: this.hiddenTIostateq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 12: this.ioStatVar = (ASTScalarVariableNode)value; return;
        case 13: this.hiddenTFormeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 14: this.hiddenTNumbereq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 15: this.hiddenTDirecteq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 16: this.hiddenTSequentialeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 17: this.sequentialVar = (ASTScalarVariableNode)value; return;
        case 18: this.positionVar = (ASTScalarVariableNode)value; return;
        case 19: this.directVar = (ASTScalarVariableNode)value; return;
        case 20: this.hiddenTFileeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 21: this.numberVar = (ASTScalarVariableNode)value; return;
        case 22: this.hiddenTUnformattedeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 23: this.hiddenTNextreceq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 24: this.nextRecVar = (ASTScalarVariableNode)value; return;
        case 25: this.fileExpr = (ASTCExprNode)value; return;
        case 26: this.hiddenTOpenedeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 27: this.hiddenTErreq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 28: this.errVar = (ASTLblRefNode)value; return;
        case 29: this.hiddenTBlankeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 30: this.blankVar = (ASTScalarVariableNode)value; return;
        case 31: this.unformattedVar = (ASTScalarVariableNode)value; return;
        case 32: this.hiddenTActioneq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 33: this.actionVar = (ASTScalarVariableNode)value; return;
        case 34: this.hiddenTUniteq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 35: this.unitIdentifier = (ASTUnitIdentifierNode)value; return;
        case 36: this.formVar = (ASTScalarVariableNode)value; return;
        case 37: this.nameVar = (ASTScalarVariableNode)value; return;
        case 38: this.hiddenTReadeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 39: this.readVar = (ASTScalarVariableNode)value; return;
        case 40: this.hiddenTNamedeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 41: this.namedVar = (ASTScalarVariableNode)value; return;
        case 42: this.delimVar = (ASTScalarVariableNode)value; return;
        case 43: this.accessVar = (ASTScalarVariableNode)value; return;
        case 44: this.hiddenTPadeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 45: this.padVar = (ASTScalarVariableNode)value; return;
        case 46: this.hiddenTReadwriteeq = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 47: this.readWriteVar = (ASTScalarVariableNode)value; return;
        case 48: this.openedVar = (ASTScalarVariableNode)value; return;
        case 49: this.writeVar = (ASTScalarVariableNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

