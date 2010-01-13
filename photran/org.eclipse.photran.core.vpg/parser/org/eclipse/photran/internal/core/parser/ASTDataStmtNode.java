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
public class ASTDataStmtNode extends ASTNodeWithErrorRecoverySymbols implements IBlockDataBodyConstruct, IBodyConstruct, ICaseBodyConstruct, IDeclarationConstruct, IExecutionPartConstruct, IModuleBodyConstruct, IObsoleteExecutionPartConstruct, ISpecificationPartConstruct, ISpecificationStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTDataStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTData; // in ASTDataStmtNode
    IASTListNode<ASTDatalistNode> datalist; // in ASTDataStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTDataStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public IASTListNode<ASTDatalistNode> getDatalist()
    {
        return this.datalist;
    }

    public void setDatalist(IASTListNode<ASTDatalistNode> newValue)
    {
        this.datalist = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTDataStmtNode(this);
        visitor.visitIBlockDataBodyConstruct(this);
        visitor.visitIBodyConstruct(this);
        visitor.visitICaseBodyConstruct(this);
        visitor.visitIDeclarationConstruct(this);
        visitor.visitIExecutionPartConstruct(this);
        visitor.visitIModuleBodyConstruct(this);
        visitor.visitIObsoleteExecutionPartConstruct(this);
        visitor.visitISpecificationPartConstruct(this);
        visitor.visitISpecificationStmt(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 4;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.label;
        case 1:  return this.hiddenTData;
        case 2:  return this.datalist;
        case 3:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 1:  this.hiddenTData = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.datalist = (IASTListNode<ASTDatalistNode>)value; return;
        case 3:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

