package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTArrayElementNode extends InteriorNode
{
    ASTArrayElementNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTArrayElementNode(this);
    }

    public ASTVariableNameNode getVariableName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ARRAY_ELEMENT_439)
            return (ASTVariableNameNode)getChild(0);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ARRAY_ELEMENT_439)
            return (Token)getChild(1);
        else if (getProduction() == Production.ARRAY_ELEMENT_440)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ARRAY_ELEMENT_439)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.ARRAY_ELEMENT_440)
            return (ASTSectionSubscriptListNode)getChild(2);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ARRAY_ELEMENT_439)
            return (Token)getChild(3);
        else if (getProduction() == Production.ARRAY_ELEMENT_440)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTStructureComponentNode getStructureComponent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ARRAY_ELEMENT_440)
            return (ASTStructureComponentNode)getChild(0);
        else
            return null;
    }
}
