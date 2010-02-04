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
public class ASTTypeAttrSpecNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token isAbstract; // in ASTTypeAttrSpecNode
    ASTAccessSpecNode accessSpec; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isExtends; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token isBind; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token parentTypeName; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token language; // in ASTTypeAttrSpecNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTTypeAttrSpecNode

    public boolean isAbstract()
    {
        return this.isAbstract != null;
    }

    public void setIsAbstract(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isAbstract = newValue;
    }


    public ASTAccessSpecNode getAccessSpec()
    {
        return this.accessSpec;
    }

    public void setAccessSpec(ASTAccessSpecNode newValue)
    {
        this.accessSpec = newValue;
    }


    public boolean isExtends()
    {
        return this.isExtends != null;
    }

    public void setIsExtends(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isExtends = newValue;
    }


    public boolean isBind()
    {
        return this.isBind != null;
    }

    public void setIsBind(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isBind = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getParentTypeName()
    {
        return this.parentTypeName;
    }

    public void setParentTypeName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.parentTypeName = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getLanguage()
    {
        return this.language;
    }

    public void setLanguage(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.language = newValue;
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
        case 0:  return this.isAbstract;
        case 1:  return this.accessSpec;
        case 2:  return this.isExtends;
        case 3:  return this.isBind;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.parentTypeName;
        case 6:  return this.language;
        case 7:  return this.hiddenTRparen;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.isAbstract = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.accessSpec = (ASTAccessSpecNode)value; return;
        case 2:  this.isExtends = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.isBind = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.parentTypeName = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 6:  this.language = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

