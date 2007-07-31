package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTArrayAllocationListNode extends InteriorNode
{
    protected int count = -1;

    ASTArrayAllocationListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTArrayAllocationListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTArrayAllocationListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTArrayAllocationListNode recurseToIndex(int listIndex)
    {
        ASTArrayAllocationListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTArrayAllocationListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTArrayAllocationListNode(this);
    }

    public ASTArrayAllocationNode getArrayAllocation(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTArrayAllocationListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ARRAY_ALLOCATION_LIST_346)
            return (ASTArrayAllocationNode)node.getChild(0);
        else if (node.getProduction() == Production.ARRAY_ALLOCATION_LIST_347)
            return (ASTArrayAllocationNode)node.getChild(2);
        else
            return null;
    }

    private ASTArrayAllocationListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ARRAY_ALLOCATION_LIST_347)
            return (ASTArrayAllocationListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTArrayAllocationListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ARRAY_ALLOCATION_LIST_347)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
