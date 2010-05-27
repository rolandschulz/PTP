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
public class ASTSyncImagesStmtNode extends ASTNode implements IActionStmt
{
    org.eclipse.photran.internal.core.lexer.Token label; // in ASTSyncImagesStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSyncimages; // in ASTSyncImagesStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTSync; // in ASTSyncImagesStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTImages; // in ASTSyncImagesStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTLparen; // in ASTSyncImagesStmtNode
    ASTImageSetNode imageSet; // in ASTSyncImagesStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTComma; // in ASTSyncImagesStmtNode
    IASTListNode<ASTSyncStatNode> syncStatList; // in ASTSyncImagesStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTRparen; // in ASTSyncImagesStmtNode
    org.eclipse.photran.internal.core.lexer.Token hiddenTEos; // in ASTSyncImagesStmtNode

    public org.eclipse.photran.internal.core.lexer.Token getLabel()
    {
        return this.label;
    }

    public void setLabel(org.eclipse.photran.internal.core.lexer.Token newValue)
    {
        this.label = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public ASTImageSetNode getImageSet()
    {
        return this.imageSet;
    }

    public void setImageSet(ASTImageSetNode newValue)
    {
        this.imageSet = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    public IASTListNode<ASTSyncStatNode> getSyncStatList()
    {
        return this.syncStatList;
    }

    public void setSyncStatList(IASTListNode<ASTSyncStatNode> newValue)
    {
        this.syncStatList = newValue;
        if (newValue != null) newValue.setParent(this);
    }


    @Override
    public void accept(IASTVisitor visitor)
    {
        visitor.visitASTSyncImagesStmtNode(this);
        visitor.visitIActionStmt(this);
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
        case 0:  return this.label;
        case 1:  return this.hiddenTSyncimages;
        case 2:  return this.hiddenTSync;
        case 3:  return this.hiddenTImages;
        case 4:  return this.hiddenTLparen;
        case 5:  return this.imageSet;
        case 6:  return this.hiddenTComma;
        case 7:  return this.syncStatList;
        case 8:  return this.hiddenTRparen;
        case 9:  return this.hiddenTEos;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }

    @Override protected void setASTField(int index, IASTNode value)
    {
        switch (index)
        {
        case 0:  this.label = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 1:  this.hiddenTSyncimages = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 2:  this.hiddenTSync = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 3:  this.hiddenTImages = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 4:  this.hiddenTLparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 5:  this.imageSet = (ASTImageSetNode)value; if (value != null) value.setParent(this); return;
        case 6:  this.hiddenTComma = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 7:  this.syncStatList = (IASTListNode<ASTSyncStatNode>)value; if (value != null) value.setParent(this); return;
        case 8:  this.hiddenTRparen = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        case 9:  this.hiddenTEos = (org.eclipse.photran.internal.core.lexer.Token)value; if (value != null) value.setParent(this); return;
        default: throw new IllegalArgumentException("Invalid index");
        }
    }
}

