package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTDeferredShapeSpecListNode extends InteriorNode
{
    protected int count = -1;

    ASTDeferredShapeSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTDeferredShapeSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTDeferredShapeSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTDeferredShapeSpecListNode recurseToIndex(int listIndex)
    {
        ASTDeferredShapeSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTDeferredShapeSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDeferredShapeSpecListNode(this);
    }

    public ASTDeferredShapeSpecNode getDeferredShapeSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDeferredShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DEFERRED_SHAPE_SPEC_LIST_307)
            return (ASTDeferredShapeSpecNode)node.getChild(0);
        else if (node.getProduction() == Production.DEFERRED_SHAPE_SPEC_LIST_308)
            return (ASTDeferredShapeSpecNode)node.getChild(2);
        else
            return null;
    }

    private ASTDeferredShapeSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFERRED_SHAPE_SPEC_LIST_308)
            return (ASTDeferredShapeSpecListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDeferredShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DEFERRED_SHAPE_SPEC_LIST_308)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
