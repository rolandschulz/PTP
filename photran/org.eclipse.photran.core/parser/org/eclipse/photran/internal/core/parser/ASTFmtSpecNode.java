package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTFmtSpecNode extends InteriorNode
{
    protected int count = -1;

    ASTFmtSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTFmtSpecNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTFmtSpecNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTFmtSpecNode recurseToIndex(int listIndex)
    {
        ASTFmtSpecNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTFmtSpecNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTFmtSpecNode(this);
    }

    public ASTFormateditNode getFormatedit(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTFmtSpecNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.FMT_SPEC_867)
            return (ASTFormateditNode)node.getChild(0);
        else if (node.getProduction() == Production.FMT_SPEC_869)
            return (ASTFormateditNode)node.getChild(1);
        else if (node.getProduction() == Production.FMT_SPEC_871)
            return (ASTFormateditNode)node.getChild(2);
        else if (node.getProduction() == Production.FMT_SPEC_872)
            return (ASTFormateditNode)node.getChild(2);
        else if (node.getProduction() == Production.FMT_SPEC_874)
            return (ASTFormateditNode)node.getChild(3);
        else
            return null;
    }

    public ASTFormatsepNode getFormatsep(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTFmtSpecNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.FMT_SPEC_868)
            return (ASTFormatsepNode)node.getChild(0);
        else if (node.getProduction() == Production.FMT_SPEC_869)
            return (ASTFormatsepNode)node.getChild(0);
        else if (node.getProduction() == Production.FMT_SPEC_870)
            return (ASTFormatsepNode)node.getChild(1);
        else if (node.getProduction() == Production.FMT_SPEC_871)
            return (ASTFormatsepNode)node.getChild(1);
        else if (node.getProduction() == Production.FMT_SPEC_873)
            return (ASTFormatsepNode)node.getChild(2);
        else if (node.getProduction() == Production.FMT_SPEC_874)
            return (ASTFormatsepNode)node.getChild(2);
        else
            return null;
    }

    private ASTFmtSpecNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FMT_SPEC_870)
            return (ASTFmtSpecNode)getChild(0);
        else if (getProduction() == Production.FMT_SPEC_871)
            return (ASTFmtSpecNode)getChild(0);
        else if (getProduction() == Production.FMT_SPEC_872)
            return (ASTFmtSpecNode)getChild(0);
        else if (getProduction() == Production.FMT_SPEC_873)
            return (ASTFmtSpecNode)getChild(0);
        else if (getProduction() == Production.FMT_SPEC_874)
            return (ASTFmtSpecNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTFmtSpecNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.FMT_SPEC_872)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.FMT_SPEC_873)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.FMT_SPEC_874)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
