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

public class ASTRdCtlSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTRdCtlSpecNode
    IASTListNode<ASTRdIoCtlSpecListNode> rdIoCtlSpecList; // in ASTRdCtlSpecNode
    ASTUFExprNode readUnitExpr; // in ASTRdCtlSpecNode
    org.eclipse.photran.internal.core.lexer.Token readUnitIsAsterisk; // in ASTRdCtlSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTRdCtlSpecNode

    public IASTListNode<ASTRdIoCtlSpecListNode> getRdIoCtlSpecList()
    {
        return this.rdIoCtlSpecList;
    }

    public void setRdIoCtlSpecList(IASTListNode<ASTRdIoCtlSpecListNode> newValue)
    {
        this.rdIoCtlSpecList = newValue;
    }


    public ASTUFExprNode getReadUnitExpr()
    {
        return this.readUnitExpr;
    }

    public void setReadUnitExpr(ASTUFExprNode newValue)
    {
        this.readUnitExpr = newValue;
    }


    public boolean readUnitIsAsterisk()
    {
        return this.readUnitIsAsterisk != null;
    }

    public void setReadUnitIsAsterisk(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.readUnitIsAsterisk = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTRdCtlSpecNode(this);
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
        case 0:  return this.hiddenTLparen;
        case 1:  return this.rdIoCtlSpecList;
        case 2:  return this.readUnitExpr;
        case 3:  return this.readUnitIsAsterisk;
        case 4:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.rdIoCtlSpecList = (IASTListNode<ASTRdIoCtlSpecListNode>)value;
        case 2:  this.readUnitExpr = (ASTUFExprNode)value;
        case 3:  this.readUnitIsAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

