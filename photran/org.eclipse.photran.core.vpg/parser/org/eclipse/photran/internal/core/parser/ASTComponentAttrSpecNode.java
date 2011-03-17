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
public class ASTComponentAttrSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token contiguous; // in ASTComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token codimension; // in ASTComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLbracket; // in ASTComponentAttrSpecNode
    ASTCoarraySpecNode coarraySpec; // in ASTComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRbracket; // in ASTComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token pointer; // in ASTComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token dimension; // in ASTComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTComponentAttrSpecNode
    ASTComponentArraySpecNode componentArraySpec; // in ASTComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token allocatable; // in ASTComponentAttrSpecNode
    ASTAccessSpecNode accessSpec; // in ASTComponentAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTComponentAttrSpecNode

    public boolean contiguous()
    {
        return this.contiguous != null;
    }

    public void setContiguous(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.contiguous = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean codimension()
    {
        return this.codimension != null;
    }

    public void setCodimension(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.codimension = newValue;
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


    public boolean pointer()
    {
        return this.pointer != null;
    }

    public void setPointer(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.pointer = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean dimension()
    {
        return this.dimension != null;
    }

    public void setDimension(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.dimension = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTComponentArraySpecNode getComponentArraySpec()
    {
        return this.componentArraySpec;
    }

    public void setComponentArraySpec(ASTComponentArraySpecNode newValue)
    {
        this.componentArraySpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean allocatable()
    {
        return this.allocatable != null;
    }

    public void setAllocatable(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.allocatable = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTAccessSpecNode getAccessSpec()
    {
        return this.accessSpec;
    }

    public void setAccessSpec(ASTAccessSpecNode newValue)
    {
        this.accessSpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTComponentAttrSpecNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 12;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.contiguous;
        case 1:  return this.codimension;
        case 2:  return this.hiddenTLbracket;
        case 3:  return this.coarraySpec;
        case 4:  return this.hiddenTRbracket;
        case 5:  return this.pointer;
        case 6:  return this.dimension;
        case 7:  return this.hiddenTLparen;
        case 8:  return this.componentArraySpec;
        case 9:  return this.allocatable;
        case 10: return this.accessSpec;
        case 11: return this.hiddenTRparen;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.contiguous = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.codimension = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLbracket = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.coarraySpec = (ASTCoarraySpecNode)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRbracket = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.pointer = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.dimension = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.componentArraySpec = (ASTComponentArraySpecNode)value; if (value != null) value.setParent(this); return;
        case 9:  this.allocatable = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.accessSpec = (ASTAccessSpecNode)value; if (value != null) value.setParent(this); return;
        case 11: this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

