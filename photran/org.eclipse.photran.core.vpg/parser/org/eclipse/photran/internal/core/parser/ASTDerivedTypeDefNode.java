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
public class ASTDerivedTypeDefNode extends ScopingNode implements IDeclarationConstruct
{
    ASTDerivedTypeStmtNode derivedTypeStmt; // in ASTDerivedTypeDefNode
    ASTTypeParamDefStmtNode typeParamDefStmt; // in ASTDerivedTypeDefNode
    IASTListNode<IDerivedTypeBodyConstruct> derivedTypeBody; // in ASTDerivedTypeDefNode
    ASTTypeBoundProcedurePartNode typeBoundProcedurePart; // in ASTDerivedTypeDefNode
    ASTEndTypeStmtNode endTypeStmt; // in ASTDerivedTypeDefNode

    public ASTDerivedTypeStmtNode getDerivedTypeStmt()
    {
        return this.derivedTypeStmt;
    }

    public void setDerivedTypeStmt(ASTDerivedTypeStmtNode newValue)
    {
        this.derivedTypeStmt = newValue;
    }


    public ASTTypeParamDefStmtNode getTypeParamDefStmt()
    {
        return this.typeParamDefStmt;
    }

    public void setTypeParamDefStmt(ASTTypeParamDefStmtNode newValue)
    {
        this.typeParamDefStmt = newValue;
    }


    public IASTListNode<IDerivedTypeBodyConstruct> getDerivedTypeBody()
    {
        return this.derivedTypeBody;
    }

    public void setDerivedTypeBody(IASTListNode<IDerivedTypeBodyConstruct> newValue)
    {
        this.derivedTypeBody = newValue;
    }


    public ASTTypeBoundProcedurePartNode getTypeBoundProcedurePart()
    {
        return this.typeBoundProcedurePart;
    }

    public void setTypeBoundProcedurePart(ASTTypeBoundProcedurePartNode newValue)
    {
        this.typeBoundProcedurePart = newValue;
    }


    public ASTEndTypeStmtNode getEndTypeStmt()
    {
        return this.endTypeStmt;
    }

    public void setEndTypeStmt(ASTEndTypeStmtNode newValue)
    {
        this.endTypeStmt = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTDerivedTypeDefNode(this);
        visitor.visitIDeclarationConstruct(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 5;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.derivedTypeStmt;
        case 1:  return this.typeParamDefStmt;
        case 2:  return this.derivedTypeBody;
        case 3:  return this.typeBoundProcedurePart;
        case 4:  return this.endTypeStmt;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.derivedTypeStmt = (ASTDerivedTypeStmtNode)value; return;
        case 1:  this.typeParamDefStmt = (ASTTypeParamDefStmtNode)value; return;
        case 2:  this.derivedTypeBody = (IASTListNode<IDerivedTypeBodyConstruct>)value; return;
        case 3:  this.typeBoundProcedurePart = (ASTTypeBoundProcedurePartNode)value; return;
        case 4:  this.endTypeStmt = (ASTEndTypeStmtNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

