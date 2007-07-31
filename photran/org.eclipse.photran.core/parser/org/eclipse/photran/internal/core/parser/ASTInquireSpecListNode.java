package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTInquireSpecListNode extends InteriorNode
{
    protected int count = -1;

    ASTInquireSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTInquireSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTInquireSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTInquireSpecListNode recurseToIndex(int listIndex)
    {
        ASTInquireSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTInquireSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTInquireSpecListNode(this);
    }

    public ASTUnitIdentifierNode getUnitIdentifier(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTInquireSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.INQUIRE_SPEC_LIST_837)
            return (ASTUnitIdentifierNode)node.getChild(0);
        else
            return null;
    }

    public ASTInquireSpecNode getInquireSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTInquireSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.INQUIRE_SPEC_LIST_838)
            return (ASTInquireSpecNode)node.getChild(0);
        else if (node.getProduction() == Production.INQUIRE_SPEC_LIST_839)
            return (ASTInquireSpecNode)node.getChild(2);
        else
            return null;
    }

    private ASTInquireSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_LIST_839)
            return (ASTInquireSpecListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTInquireSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.INQUIRE_SPEC_LIST_839)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
