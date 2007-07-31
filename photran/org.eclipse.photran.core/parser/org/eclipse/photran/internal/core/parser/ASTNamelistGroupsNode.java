package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTNamelistGroupsNode extends InteriorNode
{
    protected int count = -1;

    ASTNamelistGroupsNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTNamelistGroupsNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTNamelistGroupsNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTNamelistGroupsNode recurseToIndex(int listIndex)
    {
        ASTNamelistGroupsNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTNamelistGroupsNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTNamelistGroupsNode(this);
    }

    public Token getTSlash(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTNamelistGroupsNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.NAMELIST_GROUPS_397)
            return (Token)node.getChild(0);
        else if (node.getProduction() == Production.NAMELIST_GROUPS_398)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.NAMELIST_GROUPS_399)
            return (Token)node.getChild(2);
        else
            return null;
    }

    public ASTNamelistGroupNameNode getNamelistGroupName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTNamelistGroupsNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.NAMELIST_GROUPS_397)
            return (ASTNamelistGroupNameNode)node.getChild(1);
        else if (node.getProduction() == Production.NAMELIST_GROUPS_398)
            return (ASTNamelistGroupNameNode)node.getChild(2);
        else if (node.getProduction() == Production.NAMELIST_GROUPS_399)
            return (ASTNamelistGroupNameNode)node.getChild(3);
        else
            return null;
    }

    public Token getTSlash2(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTNamelistGroupsNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.NAMELIST_GROUPS_397)
            return (Token)node.getChild(2);
        else if (node.getProduction() == Production.NAMELIST_GROUPS_398)
            return (Token)node.getChild(3);
        else if (node.getProduction() == Production.NAMELIST_GROUPS_399)
            return (Token)node.getChild(4);
        else
            return null;
    }

    public ASTNamelistGroupObjectNode getNamelistGroupObject(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTNamelistGroupsNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.NAMELIST_GROUPS_397)
            return (ASTNamelistGroupObjectNode)node.getChild(3);
        else if (node.getProduction() == Production.NAMELIST_GROUPS_398)
            return (ASTNamelistGroupObjectNode)node.getChild(4);
        else if (node.getProduction() == Production.NAMELIST_GROUPS_399)
            return (ASTNamelistGroupObjectNode)node.getChild(5);
        else if (node.getProduction() == Production.NAMELIST_GROUPS_400)
            return (ASTNamelistGroupObjectNode)node.getChild(2);
        else
            return null;
    }

    private ASTNamelistGroupsNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.NAMELIST_GROUPS_398)
            return (ASTNamelistGroupsNode)getChild(0);
        else if (getProduction() == Production.NAMELIST_GROUPS_399)
            return (ASTNamelistGroupsNode)getChild(0);
        else if (getProduction() == Production.NAMELIST_GROUPS_400)
            return (ASTNamelistGroupsNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTNamelistGroupsNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.NAMELIST_GROUPS_399)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.NAMELIST_GROUPS_400)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
