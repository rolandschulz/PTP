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

public class ASTInterfaceStmtNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTInterfaceStmtNode
    org.eclipse.photran.internal.core.lexer.Token isAbstract; // in ASTInterfaceStmtNode
    org.eclipse.photran.internal.core.lexer.Token interfaceToken; // in ASTInterfaceStmtNode
    ASTGenericNameNode genericName; // in ASTInterfaceStmtNode
    ASTGenericSpecNode genericSpec; // in ASTInterfaceStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTInterfaceStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public boolean isAbstract()
    {
        return this.isAbstract != null;
    }

    public void setIsAbstract(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.isAbstract = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getInterfaceToken()
    {
        return this.interfaceToken;
    }

    public void setInterfaceToken(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.interfaceToken = newValue;
    }


    public ASTGenericNameNode getGenericName()
    {
        return this.genericName;
    }

    public void setGenericName(ASTGenericNameNode newValue)
    {
        this.genericName = newValue;
    }


    public ASTGenericSpecNode getGenericSpec()
    {
        return this.genericSpec;
    }

    public void setGenericSpec(ASTGenericSpecNode newValue)
    {
        this.genericSpec = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTInterfaceStmtNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 6;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.isAbstract;
        case 2:  return this.interfaceToken;
        case 3:  return this.genericName;
        case 4:  return this.genericSpec;
        case 5:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.isAbstract = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.interfaceToken = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.genericName = (ASTGenericNameNode)value; return;
        case 4:  this.genericSpec = (ASTGenericSpecNode)value; return;
        case 5:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

