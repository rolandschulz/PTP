package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTForallHeaderNode extends InteriorNode
{
    ASTForallHeaderNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTForallHeaderNode(this);
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_HEADER_632)
            return (Token)getChild(0);
        else if (getProduction() == Production.FORALL_HEADER_633)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTForallTripletSpecListNode getForallTripletSpecList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_HEADER_632)
            return (ASTForallTripletSpecListNode)getChild(1);
        else if (getProduction() == Production.FORALL_HEADER_633)
            return (ASTForallTripletSpecListNode)getChild(1);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_HEADER_632)
            return (Token)getChild(2);
        else if (getProduction() == Production.FORALL_HEADER_633)
            return (Token)getChild(4);
        else
            return null;
    }

    public Token getTComma()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_HEADER_633)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTScalarMaskExprNode getScalarMaskExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_HEADER_633)
            return (ASTScalarMaskExprNode)getChild(3);
        else
            return null;
    }
}
