package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTPrefixSpecNode extends InteriorNode
{
    ASTPrefixSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTPrefixSpecNode(this);
    }

    public ASTTypeSpecNode getTypeSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PREFIX_SPEC_986)
            return (ASTTypeSpecNode)getChild(0);
        else
            return null;
    }

    public Token getTRecursive()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PREFIX_SPEC_987)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTPure()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PREFIX_SPEC_988)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTElemental()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PREFIX_SPEC_989)
            return (Token)getChild(0);
        else
            return null;
    }
}
