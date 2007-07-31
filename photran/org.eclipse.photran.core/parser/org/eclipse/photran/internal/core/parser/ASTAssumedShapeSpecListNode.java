package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTAssumedShapeSpecListNode extends InteriorNode
{
    protected int count = -1;

    ASTAssumedShapeSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTAssumedShapeSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTAssumedShapeSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTAssumedShapeSpecListNode recurseToIndex(int listIndex)
    {
        ASTAssumedShapeSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTAssumedShapeSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAssumedShapeSpecListNode(this);
    }

    public ASTLowerBoundNode getLowerBound(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAssumedShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_296)
            return (ASTLowerBoundNode)node.getChild(0);
        else if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_297)
            return (ASTLowerBoundNode)node.getChild(2);
        else
            return null;
    }

    public Token getTColon(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAssumedShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_296)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_297)
            return (Token)node.getChild(3);
        else
            return null;
    }

    public ASTDeferredShapeSpecListNode getDeferredShapeSpecList(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAssumedShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_297)
            return (ASTDeferredShapeSpecListNode)node.getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAssumedShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_297)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_298)
            return (Token)node.getChild(1);
        else
            return null;
    }

    private ASTAssumedShapeSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_298)
            return (ASTAssumedShapeSpecListNode)getChild(0);
        else
            return null;
    }

    public ASTAssumedShapeSpecNode getAssumedShapeSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAssumedShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_298)
            return (ASTAssumedShapeSpecNode)node.getChild(2);
        else
            return null;
    }
}
