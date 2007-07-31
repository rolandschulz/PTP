package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSectionSubscriptListNode extends InteriorNode
{
    protected int count = -1;

    ASTSectionSubscriptListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTSectionSubscriptListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTSectionSubscriptListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTSectionSubscriptListNode recurseToIndex(int listIndex)
    {
        ASTSectionSubscriptListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTSectionSubscriptListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSectionSubscriptListNode(this);
    }

    public ASTSectionSubscriptNode getSectionSubscript(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSectionSubscriptListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SECTION_SUBSCRIPT_LIST_442)
            return (ASTSectionSubscriptNode)node.getChild(0);
        else if (node.getProduction() == Production.SECTION_SUBSCRIPT_LIST_443)
            return (ASTSectionSubscriptNode)node.getChild(2);
        else
            return null;
    }

    private ASTSectionSubscriptListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SECTION_SUBSCRIPT_LIST_443)
            return (ASTSectionSubscriptListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSectionSubscriptListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SECTION_SUBSCRIPT_LIST_443)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
