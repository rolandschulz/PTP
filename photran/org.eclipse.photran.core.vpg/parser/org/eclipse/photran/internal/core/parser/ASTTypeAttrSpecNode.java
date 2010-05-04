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
public class ASTTypeAttrSpecNode extends ASTNode
{
    ASTAccessSpecNode accessSpec; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isExtends; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isAbstract; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isBind; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token parentTypeName; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token language; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTTypeAttrSpecNode

    public ASTAccessSpecNode getAccessSpec()
    {
        return this.accessSpec;
    }

    public void setAccessSpec(ASTAccessSpecNode newValue)
    {
        this.accessSpec = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isExtends()
    {
        return this.isExtends != null;
    }

    public void setIsExtends(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isExtends = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isAbstract()
    {
        return this.isAbstract != null;
    }

    public void setIsAbstract(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isAbstract = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public boolean isBind()
    {
        return this.isBind != null;
    }

    public void setIsBind(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isBind = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getParentTypeName()
    {
        return this.parentTypeName;
    }

    public void setParentTypeName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.parentTypeName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getLanguage()
    {
        return this.language;
    }

    public void setLanguage(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.language = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTTypeAttrSpecNode(this);
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
        case 0:  return this.accessSpec;
        case 1:  return this.isExtends;
        case 2:  return this.isAbstract;
        case 3:  return this.isBind;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.parentTypeName;
        case 6:  return this.language;
        case 7:  return this.hiddenTRparen;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.accessSpec = (ASTAccessSpecNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.isExtends = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.isAbstract = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.isBind = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.parentTypeName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.language = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

