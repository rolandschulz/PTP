package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTGenericSpecNode extends InteriorNode
{
    ASTGenericSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTGenericSpecNode(this);
    }

    public Token getTOperator()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.GENERIC_SPEC_948)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.GENERIC_SPEC_948)
            return (Token)getChild(1);
        else if (getProduction() == Production.GENERIC_SPEC_949)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTDefinedOperatorNode getDefinedOperator()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.GENERIC_SPEC_948)
            return (ASTDefinedOperatorNode)getChild(2);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.GENERIC_SPEC_948)
            return (Token)getChild(3);
        else if (getProduction() == Production.GENERIC_SPEC_949)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTAssignment()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.GENERIC_SPEC_949)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTEquals()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.GENERIC_SPEC_949)
            return (Token)getChild(2);
        else
            return null;
    }
}
