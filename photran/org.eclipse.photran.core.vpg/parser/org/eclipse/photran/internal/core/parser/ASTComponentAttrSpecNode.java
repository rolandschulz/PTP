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

public class ASTComponentAttrSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token allocatable; // in ASTComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token dimension; // in ASTComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token pointer; // in ASTComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTComponentAttrSpecNode
    ASTComponentArraySpecNode componentArraySpec; // in ASTComponentAttrSpecNode
    ASTAccessSpecNode accessSpec; // in ASTComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTComponentAttrSpecNode

    public boolean allocatable()
    {
        return this.allocatable != null;
    }

    public void setAllocatable(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.allocatable = newValue;
    }


    public boolean dimension()
    {
        return this.dimension != null;
    }

    public void setDimension(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.dimension = newValue;
    }


    public boolean pointer()
    {
        return this.pointer != null;
    }

    public void setPointer(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.pointer = newValue;
    }


    public ASTComponentArraySpecNode getComponentArraySpec()
    {
        return this.componentArraySpec;
    }

    public void setComponentArraySpec(ASTComponentArraySpecNode newValue)
    {
        this.componentArraySpec = newValue;
    }


    public ASTAccessSpecNode getAccessSpec()
    {
        return this.accessSpec;
    }

    public void setAccessSpec(ASTAccessSpecNode newValue)
    {
        this.accessSpec = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTComponentAttrSpecNode(this);
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
        case 0:  return this.allocatable;
        case 1:  return this.dimension;
        case 2:  return this.pointer;
        case 3:  return this.hiddenTLparen;
        case 4:  return this.componentArraySpec;
        case 5:  return this.accessSpec;
        case 6:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.allocatable = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.dimension = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.pointer = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.componentArraySpec = (ASTComponentArraySpecNode)value; return;
        case 5:  this.accessSpec = (ASTAccessSpecNode)value; return;
        case 6:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

