package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTFunctionParsNode extends InteriorNode
{
    protected int count = -1;

    ASTFunctionParsNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTFunctionParsNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTFunctionParsNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTFunctionParsNode recurseToIndex(int listIndex)
    {
        ASTFunctionParsNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTFunctionParsNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTFunctionParsNode(this);
    }

    public ASTFunctionParNode getFunctionPar(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTFunctionParsNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.FUNCTION_PARS_979)
            return (ASTFunctionParNode)node.getChild(0);
        else if (node.getProduction() == Production.FUNCTION_PARS_980)
            return (ASTFunctionParNode)node.getChild(2);
        else
            return null;
    }

    private ASTFunctionParsNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_PARS_980)
            return (ASTFunctionParsNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTFunctionParsNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.FUNCTION_PARS_980)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
