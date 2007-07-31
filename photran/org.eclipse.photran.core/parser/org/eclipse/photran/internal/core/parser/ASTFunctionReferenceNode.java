package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTFunctionReferenceNode extends InteriorNode
{
    ASTFunctionReferenceNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTFunctionReferenceNode(this);
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_REFERENCE_958)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.FUNCTION_REFERENCE_959)
            return (ASTNameNode)getChild(0);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_REFERENCE_958)
            return (Token)getChild(1);
        else if (getProduction() == Production.FUNCTION_REFERENCE_959)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_REFERENCE_958)
            return (Token)getChild(2);
        else if (getProduction() == Production.FUNCTION_REFERENCE_959)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTFunctionArgListNode getFunctionArgList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_REFERENCE_959)
            return (ASTFunctionArgListNode)getChild(2);
        else
            return null;
    }
}
