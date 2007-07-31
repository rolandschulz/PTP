package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTCloseSpecListNode extends InteriorNode
{
    protected int count = -1;

    ASTCloseSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTCloseSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTCloseSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTCloseSpecListNode recurseToIndex(int listIndex)
    {
        ASTCloseSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTCloseSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTCloseSpecListNode(this);
    }

    public ASTUnitIdentifierNode getUnitIdentifier(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTCloseSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.CLOSE_SPEC_LIST_757)
            return (ASTUnitIdentifierNode)node.getChild(0);
        else
            return null;
    }

    public ASTCloseSpecNode getCloseSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTCloseSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.CLOSE_SPEC_LIST_758)
            return (ASTCloseSpecNode)node.getChild(0);
        else if (node.getProduction() == Production.CLOSE_SPEC_LIST_759)
            return (ASTCloseSpecNode)node.getChild(2);
        else
            return null;
    }

    private ASTCloseSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CLOSE_SPEC_LIST_759)
            return (ASTCloseSpecListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTCloseSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.CLOSE_SPEC_LIST_759)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
