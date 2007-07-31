package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTAttrSpecSeqNode extends InteriorNode
{
    protected int count = -1;

    ASTAttrSpecSeqNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTAttrSpecSeqNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTAttrSpecSeqNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTAttrSpecSeqNode recurseToIndex(int listIndex)
    {
        ASTAttrSpecSeqNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTAttrSpecSeqNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAttrSpecSeqNode(this);
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAttrSpecSeqNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ATTR_SPEC_SEQ_232)
            return (Token)node.getChild(0);
        else if (node.getProduction() == Production.ATTR_SPEC_SEQ_233)
            return (Token)node.getChild(1);
        else
            return null;
    }

    public ASTAttrSpecNode getAttrSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAttrSpecSeqNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ATTR_SPEC_SEQ_232)
            return (ASTAttrSpecNode)node.getChild(1);
        else if (node.getProduction() == Production.ATTR_SPEC_SEQ_233)
            return (ASTAttrSpecNode)node.getChild(2);
        else
            return null;
    }

    private ASTAttrSpecSeqNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ATTR_SPEC_SEQ_233)
            return (ASTAttrSpecSeqNode)getChild(0);
        else
            return null;
    }
}
