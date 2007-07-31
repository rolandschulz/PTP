package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTTypeSpecNode extends InteriorNode
{
    ASTTypeSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTTypeSpecNode(this);
    }

    public Token getTInteger()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_234)
            return (Token)getChild(0);
        else if (getProduction() == Production.TYPE_SPEC_240)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTReal()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_235)
            return (Token)getChild(0);
        else if (getProduction() == Production.TYPE_SPEC_241)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTDoubleprecision()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_236)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTComplex()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_237)
            return (Token)getChild(0);
        else if (getProduction() == Production.TYPE_SPEC_243)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTLogical()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_238)
            return (Token)getChild(0);
        else if (getProduction() == Production.TYPE_SPEC_245)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTCharacter()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_239)
            return (Token)getChild(0);
        else if (getProduction() == Production.TYPE_SPEC_244)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTKindSelectorNode getKindSelector()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_240)
            return (ASTKindSelectorNode)getChild(1);
        else if (getProduction() == Production.TYPE_SPEC_241)
            return (ASTKindSelectorNode)getChild(1);
        else if (getProduction() == Production.TYPE_SPEC_243)
            return (ASTKindSelectorNode)getChild(1);
        else if (getProduction() == Production.TYPE_SPEC_245)
            return (ASTKindSelectorNode)getChild(1);
        else
            return null;
    }

    public Token getTDouble()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_242)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTPrecision()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_242)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTCharSelectorNode getCharSelector()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_244)
            return (ASTCharSelectorNode)getChild(1);
        else
            return null;
    }

    public Token getTType()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_246)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_246)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTTypeNameNode getTypeName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_246)
            return (ASTTypeNameNode)getChild(2);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.TYPE_SPEC_246)
            return (Token)getChild(3);
        else
            return null;
    }
}
