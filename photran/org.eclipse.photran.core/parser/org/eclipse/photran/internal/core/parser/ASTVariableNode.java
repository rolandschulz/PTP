package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTVariableNode extends InteriorNode
{
    ASTVariableNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTVariableNode(this);
    }

    public ASTDataRefNode getDataRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_419)
            return (ASTDataRefNode)getChild(0);
        else if (getProduction() == Production.VARIABLE_420)
            return (ASTDataRefNode)getChild(0);
        else if (getProduction() == Production.VARIABLE_421)
            return (ASTDataRefNode)getChild(0);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_420)
            return (Token)getChild(1);
        else if (getProduction() == Production.VARIABLE_421)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_420)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.VARIABLE_421)
            return (ASTSectionSubscriptListNode)getChild(2);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_420)
            return (Token)getChild(3);
        else if (getProduction() == Production.VARIABLE_421)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTSubstringRangeNode getSubstringRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_421)
            return (ASTSubstringRangeNode)getChild(4);
        else
            return null;
    }

    public ASTSubstrConstNode getSubstrConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.VARIABLE_422)
            return (ASTSubstrConstNode)getChild(0);
        else
            return null;
    }
}
