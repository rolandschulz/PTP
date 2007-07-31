package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSubroutinePrefixNode extends InteriorNode
{
    ASTSubroutinePrefixNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSubroutinePrefixNode(this);
    }

    public Token getTSubroutine()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_PREFIX_998)
            return (Token)getChild(0);
        else if (getProduction() == Production.SUBROUTINE_PREFIX_999)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTPrefixSpecListNode getPrefixSpecList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_PREFIX_999)
            return (ASTPrefixSpecListNode)getChild(0);
        else
            return null;
    }
}
