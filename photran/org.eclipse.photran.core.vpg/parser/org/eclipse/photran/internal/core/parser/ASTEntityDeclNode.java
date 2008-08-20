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

public class ASTEntityDeclNode extends ASTNode
{
    ASTObjectNameNode objectName; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenAsterisk2; // in ASTEntityDeclNode
    ASTCharLengthNode initialCharLength; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenLparen2; // in ASTEntityDeclNode
    ASTArraySpecNode arraySpec; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenRparen2; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTEntityDeclNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTAsterisk; // in ASTEntityDeclNode
    ASTCharLengthNode charLength; // in ASTEntityDeclNode
    ASTInitializationNode initialization; // in ASTEntityDeclNode

    public ASTObjectNameNode getObjectName()
    {
        return this.objectName;
    }

    public void setObjectName(ASTObjectNameNode newValue)
    {
        this.objectName = newValue;
    }


    public ASTCharLengthNode getInitialCharLength()
    {
        return this.initialCharLength;
    }

    public void setInitialCharLength(ASTCharLengthNode newValue)
    {
        this.initialCharLength = newValue;
    }


    public ASTArraySpecNode getArraySpec()
    {
        return this.arraySpec;
    }

    public void setArraySpec(ASTArraySpecNode newValue)
    {
        this.arraySpec = newValue;
    }


    public ASTCharLengthNode getCharLength()
    {
        return this.charLength;
    }

    public void setCharLength(ASTCharLengthNode newValue)
    {
        this.charLength = newValue;
    }


    public ASTInitializationNode getInitialization()
    {
        return this.initialization;
    }

    public void setInitialization(ASTInitializationNode newValue)
    {
        this.initialization = newValue;
    }


    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTEntityDeclNode(this);
        visitor.visitASTNode(this);
    }

    @Override protected int getNumASTFields()
    {
        return 11;
    }

    @Override protected IASTNode getASTField(int index)
    {
        switch (index)
        {
        case 0:  return this.objectName;
        case 1:  return this.hiddenTLparen;
        case 2:  return this.hiddenAsterisk2;
        case 3:  return this.initialCharLength;
        case 4:  return this.hiddenLparen2;
        case 5:  return this.arraySpec;
        case 6:  return this.hiddenRparen2;
        case 7:  return this.hiddenTRparen;
        case 8:  return this.hiddenTAsterisk;
        case 9:  return this.charLength;
        case 10: return this.initialization;
        default: return null;
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.objectName = (ASTObjectNameNode)value; return;
        case 1:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 2:  this.hiddenAsterisk2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 3:  this.initialCharLength = (ASTCharLengthNode)value; return;
        case 4:  this.hiddenLparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 5:  this.arraySpec = (ASTArraySpecNode)value; return;
        case 6:  this.hiddenRparen2 = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 7:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 8:  this.hiddenTAsterisk = (org.eclipse.photran.internal.core.lexer.Token)value; return;
        case 9:  this.charLength = (ASTCharLengthNode)value; return;
        case 10: this.initialization = (ASTInitializationNode)value; return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

