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

public class ASTIntentSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token isIntentInOut; // in ASTIntentSpecNode
    org.eclipse.photran.internal.core.lexer.Token isIntentOut; // in ASTIntentSpecNode
    org.eclipse.photran.internal.core.lexer.Token isIntentIn; // in ASTIntentSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTOut; // in ASTIntentSpecNode

    public boolean isIntentInOut()
    {
        return this.isIntentInOut != null;
    }

    public void setIsIntentInOut(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isIntentInOut = newValue;
    }


    public boolean isIntentOut()
    {
        return this.isIntentOut != null;
    }

    public void setIsIntentOut(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isIntentOut = newValue;
    }


    public boolean isIntentIn()
    {
        return this.isIntentIn != null;
    }

    public void setIsIntentIn(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isIntentIn = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTIntentSpecNode(this);
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
        case 0:  return this.isIntentInOut;
        case 1:  return this.isIntentOut;
        case 2:  return this.isIntentIn;
        case 3:  return this.hiddenTOut;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isIntentInOut = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.isIntentOut = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.isIntentIn = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTOut = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

