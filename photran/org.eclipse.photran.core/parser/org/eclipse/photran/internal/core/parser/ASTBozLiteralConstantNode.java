package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTBozLiteralConstantNode extends InteriorNode
{
    ASTBozLiteralConstantNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTBozLiteralConstantNode(this);
    }

    public Token getTBcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BOZ_LITERAL_CONSTANT_171)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTOcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BOZ_LITERAL_CONSTANT_172)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTZcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BOZ_LITERAL_CONSTANT_173)
            return (Token)getChild(0);
        else
            return null;
    }
}
