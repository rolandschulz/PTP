package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTAllocateObjectNode extends InteriorNode
{
    protected int count = -1;

    ASTAllocateObjectNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTAllocateObjectNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTAllocateObjectNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTAllocateObjectNode recurseToIndex(int listIndex)
    {
        ASTAllocateObjectNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTAllocateObjectNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAllocateObjectNode(this);
    }

    public ASTVariableNameNode getVariableName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAllocateObjectNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ALLOCATE_OBJECT_463)
            return (ASTVariableNameNode)node.getChild(0);
        else
            return null;
    }

    private ASTAllocateObjectNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ALLOCATE_OBJECT_464)
            return (ASTAllocateObjectNode)getChild(0);
        else
            return null;
    }

    public ASTFieldSelectorNode getFieldSelector(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAllocateObjectNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ALLOCATE_OBJECT_464)
            return (ASTFieldSelectorNode)node.getChild(1);
        else
            return null;
    }
}
