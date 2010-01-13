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
public class ASTIoControlSpecListNode extends ASTNode
{
    ASTUnitIdentifierNode unitIdentifier; // in ASTIoControlSpecListNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTIoControlSpecListNode
    ASTIoControlSpecNode ioControlSpec; // in ASTIoControlSpecListNode
    ASTFormatIdentifierNode formatIdentifier; // in ASTIoControlSpecListNode

    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        return this.unitIdentifier;
    }

    public void setUnitIdentifier(ASTUnitIdentifierNode newValue)
    {
        this.unitIdentifier = newValue;
    }


    public ASTIoControlSpecNode getIoControlSpec()
    {
        return this.ioControlSpec;
    }

    public void setIoControlSpec(ASTIoControlSpecNode newValue)
    {
        this.ioControlSpec = newValue;
    }


    public ASTFormatIdentifierNode getFormatIdentifier()
    {
        return this.formatIdentifier;
    }

    public void setFormatIdentifier(ASTFormatIdentifierNode newValue)
    {
        this.formatIdentifier = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTIoControlSpecListNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 4;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.unitIdentifier;
        case 1:  return this.hiddenTComma;
        case 2:  return this.ioControlSpec;
        case 3:  return this.formatIdentifier;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.unitIdentifier = (ASTUnitIdentifierNode)value; return;
        case 1:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.ioControlSpec = (ASTIoControlSpecNode)value; return;
        case 3:  this.formatIdentifier = (ASTFormatIdentifierNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

