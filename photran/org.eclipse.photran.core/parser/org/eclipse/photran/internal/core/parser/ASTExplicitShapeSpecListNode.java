package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTExplicitShapeSpecListNode extends InteriorNode
{
    protected int count = -1;

    ASTExplicitShapeSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTExplicitShapeSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTExplicitShapeSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTExplicitShapeSpecListNode recurseToIndex(int listIndex)
    {
        ASTExplicitShapeSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTExplicitShapeSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTExplicitShapeSpecListNode(this);
    }

    public ASTExplicitShapeSpecNode getExplicitShapeSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTExplicitShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.EXPLICIT_SHAPE_SPEC_LIST_299)
            return (ASTExplicitShapeSpecNode)node.getChild(0);
        else if (node.getProduction() == Production.EXPLICIT_SHAPE_SPEC_LIST_300)
            return (ASTExplicitShapeSpecNode)node.getChild(2);
        else
            return null;
    }

    private ASTExplicitShapeSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXPLICIT_SHAPE_SPEC_LIST_300)
            return (ASTExplicitShapeSpecListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTExplicitShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.EXPLICIT_SHAPE_SPEC_LIST_300)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
