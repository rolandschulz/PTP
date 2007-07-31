package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTRelOpNode extends InteriorNode
{
    ASTRelOpNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTRelOpNode(this);
    }

    public Token getTEq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_132)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTNe()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_133)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTLt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_134)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTLe()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_135)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTGt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_136)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTGe()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_137)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTEqeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_138)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTSlasheq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_139)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTLessthan()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_140)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTLessthaneq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_141)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTGreaterthan()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_142)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTGreaterthaneq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_143)
            return (Token)getChild(0);
        else
            return null;
    }
}
