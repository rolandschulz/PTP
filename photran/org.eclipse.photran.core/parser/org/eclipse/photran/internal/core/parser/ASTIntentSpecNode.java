package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTIntentSpecNode extends InteriorNode
{
    ASTIntentSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTIntentSpecNode(this);
    }

    public Token getTIn()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTENT_SPEC_288)
            return (Token)getChild(0);
        else if (getProduction() == Production.INTENT_SPEC_291)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTOut()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTENT_SPEC_289)
            return (Token)getChild(0);
        else if (getProduction() == Production.INTENT_SPEC_291)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTInout()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTENT_SPEC_290)
            return (Token)getChild(0);
        else
            return null;
    }
}
