package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTFunctionPrefixNode extends InteriorNode
{
    ASTFunctionPrefixNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTFunctionPrefixNode(this);
    }

    public Token getTFunction()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_PREFIX_982)
            return (Token)getChild(0);
        else if (getProduction() == Production.FUNCTION_PREFIX_983)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTPrefixSpecListNode getPrefixSpecList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_PREFIX_983)
            return (ASTPrefixSpecListNode)getChild(0);
        else
            return null;
    }
}
