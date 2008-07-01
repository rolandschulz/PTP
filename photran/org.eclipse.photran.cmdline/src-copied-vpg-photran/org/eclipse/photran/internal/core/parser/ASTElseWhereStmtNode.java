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

public class ASTElseWhereStmtNode extends ASTNode
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTElseWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTElse; // in ASTElseWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTWhere; // in ASTElseWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTElsewhere; // in ASTElseWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token endName; // in ASTElseWhereStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTElseWhereStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
    }


    public org.eclipse.photran.internal.core.lexer.Token getEndName()
    {
        return this.endName;
    }

    public void setEndName(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.endName = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTElseWhereStmtNode(this);
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
        case 1:  return this.hiddenTElse;
        case 2:  return this.hiddenTWhere;
        case 3:  return this.hiddenTElsewhere;
        case 4:  return this.endName;
        case 5:  return this.hiddenTEos;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 1:  this.hiddenTElse = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 2:  this.hiddenTWhere = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 3:  this.hiddenTElsewhere = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 4:  this.endName = (org.eclipse.photran.internal.core.lexer.Token)value;
        case 5:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

