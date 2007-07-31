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

public class ASTRdIoCtlSpecListNode extends InteriorNode
{
    protected int count = -1;

    ASTRdIoCtlSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTRdIoCtlSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTRdIoCtlSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTRdIoCtlSpecListNode recurseToIndex(int listIndex)
    {
        ASTRdIoCtlSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTRdIoCtlSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTRdIoCtlSpecListNode(this);
    }

    public ASTUnitIdentifierNode getUnitIdentifier(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTRdIoCtlSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.RD_IO_CTL_SPEC_LIST_772)
            return (ASTUnitIdentifierNode)node.getChild(0);
        else if (node.getProduction() == Production.RD_IO_CTL_SPEC_LIST_773)
            return (ASTUnitIdentifierNode)node.getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTRdIoCtlSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.RD_IO_CTL_SPEC_LIST_772)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.RD_IO_CTL_SPEC_LIST_773)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.RD_IO_CTL_SPEC_LIST_775)
            return (Token)node.getChild(1);
        else
            return null;
    }

    public ASTIoControlSpecNode getIoControlSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTRdIoCtlSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.RD_IO_CTL_SPEC_LIST_772)
            return (ASTIoControlSpecNode)node.getChild(2);
        else if (node.getProduction() == Production.RD_IO_CTL_SPEC_LIST_774)
            return (ASTIoControlSpecNode)node.getChild(0);
        else if (node.getProduction() == Production.RD_IO_CTL_SPEC_LIST_775)
            return (ASTIoControlSpecNode)node.getChild(2);
        else
            return null;
    }

    public ASTFormatIdentifierNode getFormatIdentifier(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTRdIoCtlSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.RD_IO_CTL_SPEC_LIST_773)
            return (ASTFormatIdentifierNode)node.getChild(2);
        else
            return null;
    }

    private ASTRdIoCtlSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RD_IO_CTL_SPEC_LIST_775)
            return (ASTRdIoCtlSpecListNode)getChild(0);
        else
            return null;
    }
}
