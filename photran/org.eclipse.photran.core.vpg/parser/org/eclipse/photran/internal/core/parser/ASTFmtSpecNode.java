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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;

import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTFmtSpecNode extends InteriorNode
{
    protected int count = -1;

    ASTFmtSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
        
    @Override public InteriorNode getASTParent()
    {
        // This is a recursive node in a list, so its logical parent node
        // is the parent of the first node in the list
    
        InteriorNode parent = super.getParent();
        InteriorNode grandparent = parent == null ? null : parent.getParent();
        InteriorNode logicalParent = parent;
        
        while (parent != null && grandparent != null
               && parent instanceof ASTFmtSpecNode
               && grandparent instanceof ASTFmtSpecNode
               && ((ASTFmtSpecNode)grandparent).getRecursiveNode() == parent)
        {
            logicalParent = grandparent;
            parent = grandparent;
            grandparent = grandparent.getParent() == null ? null : grandparent.getParent();
        }
        
        InteriorNode logicalGrandparent = logicalParent.getParent();
        
        // If a node has been pulled up in an ACST, its physical parent in
        // the CST is not its logical parent in the ACST
        if (logicalGrandparent != null && logicalGrandparent.childIsPulledUp(logicalGrandparent.findChild(logicalParent)))
            return logicalParent.getASTParent();
        else 
            return logicalParent;
    }

    /**
     * @return the number of ASTFmtSpecNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTFmtSpecNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTFmtSpecNode recurseToIndex(int listIndex)
    {
        ASTFmtSpecNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTFmtSpecNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTFmtSpecNode(this);
    }

    public ASTFormatEditNode getFormatEdit(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTFmtSpecNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.FMT_SPEC_868)
            return (ASTFormatEditNode)node.getChild(0);
        else if (node.getProduction() == Production.FMT_SPEC_870)
            return (ASTFormatEditNode)node.getChild(1);
        else if (node.getProduction() == Production.FMT_SPEC_872)
            return (ASTFormatEditNode)node.getChild(2);
        else if (node.getProduction() == Production.FMT_SPEC_873)
            return (ASTFormatEditNode)node.getChild(2);
        else if (node.getProduction() == Production.FMT_SPEC_875)
            return (ASTFormatEditNode)node.getChild(3);
        else
            return null;
    }

    public boolean hasFormatEdit(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTFmtSpecNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.FMT_SPEC_868)
            return node.getChild(0) != null;
        else if (node.getProduction() == Production.FMT_SPEC_870)
            return node.getChild(1) != null;
        else if (node.getProduction() == Production.FMT_SPEC_872)
            return node.getChild(2) != null;
        else if (node.getProduction() == Production.FMT_SPEC_873)
            return node.getChild(2) != null;
        else if (node.getProduction() == Production.FMT_SPEC_875)
            return node.getChild(3) != null;
        else
            return false;
    }

    private ASTFmtSpecNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FMT_SPEC_871)
            return (ASTFmtSpecNode)getChild(0);
        else if (getProduction() == Production.FMT_SPEC_872)
            return (ASTFmtSpecNode)getChild(0);
        else if (getProduction() == Production.FMT_SPEC_873)
            return (ASTFmtSpecNode)getChild(0);
        else if (getProduction() == Production.FMT_SPEC_874)
            return (ASTFmtSpecNode)getChild(0);
        else if (getProduction() == Production.FMT_SPEC_875)
            return (ASTFmtSpecNode)getChild(0);
        else
            return null;
    }

    public boolean slashFormatSep(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTFmtSpecNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.FMT_SPEC_869)
            return ((ASTFormatsepNode)node.getChild(0)).slashFormatSep();
        else if (node.getProduction() == Production.FMT_SPEC_870)
            return ((ASTFormatsepNode)node.getChild(0)).slashFormatSep();
        else if (node.getProduction() == Production.FMT_SPEC_871)
            return ((ASTFormatsepNode)node.getChild(1)).slashFormatSep();
        else if (node.getProduction() == Production.FMT_SPEC_872)
            return ((ASTFormatsepNode)node.getChild(1)).slashFormatSep();
        else if (node.getProduction() == Production.FMT_SPEC_874)
            return ((ASTFormatsepNode)node.getChild(2)).slashFormatSep();
        else if (node.getProduction() == Production.FMT_SPEC_875)
            return ((ASTFormatsepNode)node.getChild(2)).slashFormatSep();
        else
            return false;
    }

    public boolean colonFormatSep(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTFmtSpecNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.FMT_SPEC_869)
            return ((ASTFormatsepNode)node.getChild(0)).colonFormatSep();
        else if (node.getProduction() == Production.FMT_SPEC_870)
            return ((ASTFormatsepNode)node.getChild(0)).colonFormatSep();
        else if (node.getProduction() == Production.FMT_SPEC_871)
            return ((ASTFormatsepNode)node.getChild(1)).colonFormatSep();
        else if (node.getProduction() == Production.FMT_SPEC_872)
            return ((ASTFormatsepNode)node.getChild(1)).colonFormatSep();
        else if (node.getProduction() == Production.FMT_SPEC_874)
            return ((ASTFormatsepNode)node.getChild(2)).colonFormatSep();
        else if (node.getProduction() == Production.FMT_SPEC_875)
            return ((ASTFormatsepNode)node.getChild(2)).colonFormatSep();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.FMT_SPEC_873 && index == 1)
            return false;
        else if (getProduction() == Production.FMT_SPEC_874 && index == 1)
            return false;
        else if (getProduction() == Production.FMT_SPEC_875 && index == 1)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.FMT_SPEC_869 && index == 0)
            return true;
        else if (getProduction() == Production.FMT_SPEC_870 && index == 0)
            return true;
        else if (getProduction() == Production.FMT_SPEC_871 && index == 1)
            return true;
        else if (getProduction() == Production.FMT_SPEC_872 && index == 1)
            return true;
        else if (getProduction() == Production.FMT_SPEC_874 && index == 2)
            return true;
        else if (getProduction() == Production.FMT_SPEC_875 && index == 2)
            return true;
        else
            return false;
    }
}
