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

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTIoControlSpecListNode extends InteriorNode
{
    protected int count = -1;

    ASTIoControlSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTIoControlSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTIoControlSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTIoControlSpecListNode recurseToIndex(int listIndex)
    {
        ASTIoControlSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTIoControlSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTIoControlSpecListNode(this);
    }

    public ASTUnitIdentifierNode getUnitIdentifier(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTIoControlSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.IO_CONTROL_SPEC_LIST_786)
            return (ASTUnitIdentifierNode)node.getChild(0);
        else if (node.getProduction() == Production.IO_CONTROL_SPEC_LIST_787)
            return (ASTUnitIdentifierNode)node.getChild(0);
        else if (node.getProduction() == Production.IO_CONTROL_SPEC_LIST_788)
            return (ASTUnitIdentifierNode)node.getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTIoControlSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.IO_CONTROL_SPEC_LIST_787)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.IO_CONTROL_SPEC_LIST_788)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.IO_CONTROL_SPEC_LIST_790)
            return (Token)node.getChild(1);
        else
            return null;
    }

    public ASTFormatIdentifierNode getFormatIdentifier(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTIoControlSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.IO_CONTROL_SPEC_LIST_787)
            return (ASTFormatIdentifierNode)node.getChild(2);
        else
            return null;
    }

    public ASTIoControlSpecNode getIoControlSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTIoControlSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.IO_CONTROL_SPEC_LIST_788)
            return (ASTIoControlSpecNode)node.getChild(2);
        else if (node.getProduction() == Production.IO_CONTROL_SPEC_LIST_789)
            return (ASTIoControlSpecNode)node.getChild(0);
        else if (node.getProduction() == Production.IO_CONTROL_SPEC_LIST_790)
            return (ASTIoControlSpecNode)node.getChild(2);
        else
            return null;
    }

    private ASTIoControlSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IO_CONTROL_SPEC_LIST_790)
            return (ASTIoControlSpecListNode)getChild(0);
        else
            return null;
    }
}
