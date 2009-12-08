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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

public class ASTComponentDeclNode extends ASTNode
{
    ASTComponentNameNode componentName; // in ASTComponentDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTComponentDeclNode
    ASTComponentArraySpecNode componentArraySpec; // in ASTComponentDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTComponentDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLbracket; // in ASTComponentDeclNode
    ASTCoarraySpecNode coarraySpec; // in ASTComponentDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRbracket; // in ASTComponentDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAsterisk; // in ASTComponentDeclNode
    ASTCharLengthNode charLength; // in ASTComponentDeclNode
    ASTComponentInitializationNode componentInitialization; // in ASTComponentDeclNode

    public ASTComponentNameNode getComponentName()
    {
        return this.componentName;
    }

    public void setComponentName(ASTComponentNameNode newValue)
    {
        this.componentName = newValue;
    }


    public ASTComponentArraySpecNode getComponentArraySpec()
    {
        return this.componentArraySpec;
    }

    public void setComponentArraySpec(ASTComponentArraySpecNode newValue)
    {
        this.componentArraySpec = newValue;
    }


    public ASTCoarraySpecNode getCoarraySpec()
    {
        return this.coarraySpec;
    }

    public void setCoarraySpec(ASTCoarraySpecNode newValue)
    {
        this.coarraySpec = newValue;
    }


    public ASTCharLengthNode getCharLength()
    {
        return this.charLength;
    }

    public void setCharLength(ASTCharLengthNode newValue)
    {
        this.charLength = newValue;
    }


    public ASTComponentInitializationNode getComponentInitialization()
    {
        return this.componentInitialization;
    }

    public void setComponentInitialization(ASTComponentInitializationNode newValue)
    {
        this.componentInitialization = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTComponentDeclNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 10;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.componentName;
        case 1:  return this.hiddenTLparen;
        case 2:  return this.componentArraySpec;
        case 3:  return this.hiddenTRparen;
        case 4:  return this.hiddenTLbracket;
        case 5:  return this.coarraySpec;
        case 6:  return this.hiddenTRbracket;
        case 7:  return this.hiddenTAsterisk;
        case 8:  return this.charLength;
        case 9:  return this.componentInitialization;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.componentName = (ASTComponentNameNode)value; return;
        case 1:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.componentArraySpec = (ASTComponentArraySpecNode)value; return;
        case 3:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 4:  this.hiddenTLbracket = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.coarraySpec = (ASTCoarraySpecNode)value; return;
        case 6:  this.hiddenTRbracket = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hiddenTAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.charLength = (ASTCharLengthNode)value; return;
        case 9:  this.componentInitialization = (ASTComponentInitializationNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

