package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTComlistNode extends InteriorNode
{
    protected int count = -1;

    ASTComlistNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTComlistNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTComlistNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTComlistNode recurseToIndex(int listIndex)
    {
        ASTComlistNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTComlistNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTComlistNode(this);
    }

    public ASTCommonBlockObjectNode getCommonBlockObject(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTComlistNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.COMLIST_410)
            return (ASTCommonBlockObjectNode)node.getChild(0);
        else if (node.getProduction() == Production.COMLIST_411)
            return (ASTCommonBlockObjectNode)node.getChild(1);
        else if (node.getProduction() == Production.COMLIST_412)
            return (ASTCommonBlockObjectNode)node.getChild(2);
        else if (node.getProduction() == Production.COMLIST_413)
            return (ASTCommonBlockObjectNode)node.getChild(2);
        else if (node.getProduction() == Production.COMLIST_414)
            return (ASTCommonBlockObjectNode)node.getChild(3);
        else
            return null;
    }

    public ASTComblockNode getComblock(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTComlistNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.COMLIST_411)
            return (ASTComblockNode)node.getChild(0);
        else if (node.getProduction() == Production.COMLIST_413)
            return (ASTComblockNode)node.getChild(1);
        else if (node.getProduction() == Production.COMLIST_414)
            return (ASTComblockNode)node.getChild(2);
        else
            return null;
    }

    private ASTComlistNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMLIST_412)
            return (ASTComlistNode)getChild(0);
        else if (getProduction() == Production.COMLIST_413)
            return (ASTComlistNode)getChild(0);
        else if (getProduction() == Production.COMLIST_414)
            return (ASTComlistNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTComlistNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.COMLIST_412)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.COMLIST_414)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
