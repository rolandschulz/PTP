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
public class ASTInterfaceBlockNode extends ScopingNode implements IDeclarationConstruct
{
    ASTInterfaceStmtNode interfaceStmt; // in ASTInterfaceBlockNode
    IASTListNode<IInterfaceSpecification> interfaceBlockBody; // in ASTInterfaceBlockNode
    ASTEndInterfaceStmtNode endInterfaceStmt; // in ASTInterfaceBlockNode

    public ASTInterfaceStmtNode getInterfaceStmt()
    {
        return this.interfaceStmt;
    }

    public void setInterfaceStmt(ASTInterfaceStmtNode newValue)
    {
        this.interfaceStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<IInterfaceSpecification> getInterfaceBlockBody()
    {
        return this.interfaceBlockBody;
    }

    public void setInterfaceBlockBody(IASTListNode<IInterfaceSpecification> newValue)
    {
        this.interfaceBlockBody = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTEndInterfaceStmtNode getEndInterfaceStmt()
    {
        return this.endInterfaceStmt;
    }

    public void setEndInterfaceStmt(ASTEndInterfaceStmtNode newValue)
    {
        this.endInterfaceStmt = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTInterfaceBlockNode(this);
        visitor.visitIDeclarationConstruct(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 3;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.interfaceStmt;
        case 1:  return this.interfaceBlockBody;
        case 2:  return this.endInterfaceStmt;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.interfaceStmt = (ASTInterfaceStmtNode)value; if (value != null) value.setParent(this); return;
        case 1:  this.interfaceBlockBody = (IASTListNode<IInterfaceSpecification>)value; if (value != null) value.setParent(this); return;
        case 2:  this.endInterfaceStmt = (ASTEndInterfaceStmtNode)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

