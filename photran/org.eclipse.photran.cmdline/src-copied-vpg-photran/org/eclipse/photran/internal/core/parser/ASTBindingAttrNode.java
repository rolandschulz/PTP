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

public class ASTBindingAttrNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token isNoPass; // in ASTBindingAttrNode
    ASTAccessSpecNode accessSpec; // in ASTBindingAttrNode
    org.eclipse.photran.internal.core.lexer.Token isPass; // in ASTBindingAttrNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTBindingAttrNode
    org.eclipse.photran.internal.core.lexer.Token argName; // in ASTBindingAttrNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTBindingAttrNode
    org.eclipse.photran.internal.core.lexer.Token isDeferred; // in ASTBindingAttrNode
    org.eclipse.photran.internal.core.lexer.Token isNonOverridable; // in ASTBindingAttrNode

    public boolean isNoPass()
    {
        return this.isNoPass != null;
    }

    public void setIsNoPass(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isNoPass = newValue;
    }


    public ASTAccessSpecNode getAccessSpec()
    {
        return this.accessSpec;
    }

    public void setAccessSpec(ASTAccessSpecNode newValue)
    {
        this.accessSpec = newValue;
    }


    public boolean isPass()
    {
        return this.isPass != null;
    }

    public void setIsPass(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isPass = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getArgName()
    {
        return this.argName;
    }

    public void setArgName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.argName = newValue;
    }


    public boolean isDeferred()
    {
        return this.isDeferred != null;
    }

    public void setIsDeferred(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isDeferred = newValue;
    }


    public boolean isNonOverridable()
    {
        return this.isNonOverridable != null;
    }

    public void setIsNonOverridable(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isNonOverridable = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTBindingAttrNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 8;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.isNoPass;
        case 1:  return this.accessSpec;
        case 2:  return this.isPass;
        case 3:  return this.hiddenTLparen;
        case 4:  return this.argName;
        case 5:  return this.hiddenTRparen;
        case 6:  return this.isDeferred;
        case 7:  return this.isNonOverridable;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isNoPass = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.accessSpec = (ASTAccessSpecNode)value; return;
        case 2:  this.isPass = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.argName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.isDeferred = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.isNonOverridable = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

