package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTCOperandNode extends InteriorNode
{
    ASTCOperandNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTCOperandNode(this);
    }

    public Token getTScon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_498)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_499)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_500)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_501)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_502)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_503)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_504)
            return (ASTNameNode)getChild(0);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_500)
            return (Token)getChild(1);
        else if (getProduction() == Production.COPERAND_502)
            return (Token)getChild(3);
        else if (getProduction() == Production.COPERAND_503)
            return (Token)getChild(1);
        else if (getProduction() == Production.COPERAND_504)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_500)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.COPERAND_502)
            return (ASTSectionSubscriptListNode)getChild(4);
        else if (getProduction() == Production.COPERAND_503)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.COPERAND_504)
            return (ASTSectionSubscriptListNode)getChild(2);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_500)
            return (Token)getChild(3);
        else if (getProduction() == Production.COPERAND_502)
            return (Token)getChild(5);
        else if (getProduction() == Production.COPERAND_503)
            return (Token)getChild(3);
        else if (getProduction() == Production.COPERAND_504)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTPercent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_501)
            return (Token)getChild(1);
        else if (getProduction() == Production.COPERAND_502)
            return (Token)getChild(1);
        else if (getProduction() == Production.COPERAND_503)
            return (Token)getChild(4);
        else if (getProduction() == Production.COPERAND_504)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTDataRefNode getDataRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_501)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.COPERAND_502)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.COPERAND_503)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.COPERAND_504)
            return (ASTDataRefNode)getChild(5);
        else
            return null;
    }

    public Token getTLparen2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_504)
            return (Token)getChild(6);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_504)
            return (ASTSectionSubscriptListNode)getChild(7);
        else
            return null;
    }

    public Token getTRparen2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_504)
            return (Token)getChild(8);
        else
            return null;
    }

    public ASTFunctionReferenceNode getFunctionReference()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COPERAND_505)
            return (ASTFunctionReferenceNode)getChild(0);
        else
            return null;
    }
}
