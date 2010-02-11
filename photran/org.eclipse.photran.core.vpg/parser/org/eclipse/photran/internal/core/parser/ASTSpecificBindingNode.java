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
public class ASTSpecificBindingNode extends ASTNode implements IProcBindingStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTSpecificBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTProcedure; // in ASTSpecificBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTSpecificBindingNode
    org.eclipse.photran.internal.core.lexer.Token interfaceName; // in ASTSpecificBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTSpecificBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTSpecificBindingNode
    IASTListNode<ASTBindingAttrNode> bindingAttrList; // in ASTSpecificBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon; // in ASTSpecificBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTColon2; // in ASTSpecificBindingNode
    org.eclipse.photran.internal.core.lexer.Token bindingName; // in ASTSpecificBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEqgreaterthan; // in ASTSpecificBindingNode
    org.eclipse.photran.internal.core.lexer.Token procedureName; // in ASTSpecificBindingNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTSpecificBindingNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getInterfaceName()
    {
        return this.interfaceName;
    }

    public void setInterfaceName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.interfaceName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTBindingAttrNode> getBindingAttrList()
    {
        return this.bindingAttrList;
    }

    public void setBindingAttrList(IASTListNode<ASTBindingAttrNode> newValue)
    {
        this.bindingAttrList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getBindingName()
    {
        return this.bindingName;
    }

    public void setBindingName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.bindingName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public org.eclipse.photran.internal.core.lexer.Token getProcedureName()
    {
        return this.procedureName;
    }

    public void setProcedureName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.procedureName = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSpecificBindingNode(this);
        visitor.visitIProcBindingStmt(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 13;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTProcedure;
        case 2:  return this.hiddenTLparen;
        case 3:  return this.interfaceName;
        case 4:  return this.hiddenTRparen;
        case 5:  return this.hiddenTComma;
        case 6:  return this.bindingAttrList;
        case 7:  return this.hiddenTColon;
        case 8:  return this.hiddenTColon2;
        case 9:  return this.bindingName;
        case 10: return this.hiddenTEqgreaterthan;
        case 11: return this.procedureName;
        case 12: return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTProcedure = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.interfaceName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 6:  this.bindingAttrList = (IASTListNode<ASTBindingAttrNode>)value; if (value != null) value.setParent(this); return;
        case 7:  this.hiddenTColon = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTColon2 = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.bindingName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 10: this.hiddenTEqgreaterthan = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 11: this.procedureName = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 12: this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

