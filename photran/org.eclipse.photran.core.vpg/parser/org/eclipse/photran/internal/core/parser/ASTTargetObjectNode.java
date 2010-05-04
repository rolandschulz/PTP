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
public class ASTTargetObjectNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token targetName; // in ASTTargetObjectNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTTargetObjectNode
    ASTArraySpecNode arraySpec; // in ASTTargetObjectNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTTargetObjectNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLbracket; // in ASTTargetObjectNode
    ASTCoarraySpecNode coarraySpec; // in ASTTargetObjectNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRbracket; // in ASTTargetObjectNode

    public org.eclipse.photran.internal.core.lexer.Token getTargetName()
    {
        return this.targetName;
    }

    public void setTargetName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.targetName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTArraySpecNode getArraySpec()
    {
        return this.arraySpec;
    }

    public void setArraySpec(ASTArraySpecNode newValue)
    {
        this.arraySpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTCoarraySpecNode getCoarraySpec()
    {
        return this.coarraySpec;
    }

    public void setCoarraySpec(ASTCoarraySpecNode newValue)
    {
        this.coarraySpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTTargetObjectNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 7;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.targetName;
        case 1:  return this.hiddenTLparen;
        case 2:  return this.arraySpec;
        case 3:  return this.hiddenTRparen;
        case 4:  return this.hiddenTLbracket;
        case 5:  return this.coarraySpec;
        case 6:  return this.hiddenTRbracket;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.targetName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.arraySpec = (ASTArraySpecNode)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTLbracket = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.coarraySpec = (ASTCoarraySpecNode)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTRbracket = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

