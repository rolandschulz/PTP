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

public class ASTConnectSpecListNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTConnectSpecListNode
    ASTConnectSpecNode connectSpec; // in ASTConnectSpecListNode
    ASTUnitIdentifierNode unitIdentifier; // in ASTConnectSpecListNode

    public ASTConnectSpecNode getConnectSpec()
    {
        return this.connectSpec;
    }

    public void setConnectSpec(ASTConnectSpecNode newValue)
    {
        this.connectSpec = newValue;
    }


    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        return this.unitIdentifier;
    }

    public void setUnitIdentifier(ASTUnitIdentifierNode newValue)
    {
        this.unitIdentifier = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTConnectSpecListNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 3;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.hiddenTComma;
        case 1:  return this.connectSpec;
        case 2:  return this.unitIdentifier;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.connectSpec = (ASTConnectSpecNode)value; return;
        case 2:  this.unitIdentifier = (ASTUnitIdentifierNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

