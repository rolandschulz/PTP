package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTPointerObjectListNode extends InteriorNode
{
    protected int count = -1;

    ASTPointerObjectListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTPointerObjectListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTPointerObjectListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTPointerObjectListNode recurseToIndex(int listIndex)
    {
        ASTPointerObjectListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTPointerObjectListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTPointerObjectListNode(this);
    }

    public ASTPointerObjectNode getPointerObject(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPointerObjectListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POINTER_OBJECT_LIST_466)
            return (ASTPointerObjectNode)node.getChild(0);
        else if (node.getProduction() == Production.POINTER_OBJECT_LIST_467)
            return (ASTPointerObjectNode)node.getChild(2);
        else
            return null;
    }

    private ASTPointerObjectListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_OBJECT_LIST_467)
            return (ASTPointerObjectListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPointerObjectListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POINTER_OBJECT_LIST_467)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
