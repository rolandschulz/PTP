package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTPositionSpecListNode extends InteriorNode
{
    protected int count = -1;

    ASTPositionSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTPositionSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTPositionSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTPositionSpecListNode recurseToIndex(int listIndex)
    {
        ASTPositionSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTPositionSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTPositionSpecListNode(this);
    }

    public ASTUnitIdentifierNode getUnitIdentifier(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPositionSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POSITION_SPEC_LIST_829)
            return (ASTUnitIdentifierNode)node.getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPositionSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POSITION_SPEC_LIST_829)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.POSITION_SPEC_LIST_831)
            return (Token)node.getChild(1);
        else
            return null;
    }

    public ASTPositionSpecNode getPositionSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPositionSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POSITION_SPEC_LIST_829)
            return (ASTPositionSpecNode)node.getChild(2);
        else if (node.getProduction() == Production.POSITION_SPEC_LIST_830)
            return (ASTPositionSpecNode)node.getChild(0);
        else if (node.getProduction() == Production.POSITION_SPEC_LIST_831)
            return (ASTPositionSpecNode)node.getChild(2);
        else
            return null;
    }

    private ASTPositionSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POSITION_SPEC_LIST_831)
            return (ASTPositionSpecListNode)getChild(0);
        else
            return null;
    }
}
