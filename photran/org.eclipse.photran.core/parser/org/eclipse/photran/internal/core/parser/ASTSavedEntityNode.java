package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSavedEntityNode extends InteriorNode
{
    ASTSavedEntityNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSavedEntityNode(this);
    }

    public ASTVariableNameNode getVariableName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SAVED_ENTITY_336)
            return (ASTVariableNameNode)getChild(0);
        else
            return null;
    }

    public ASTSavedCommonBlockNode getSavedCommonBlock()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SAVED_ENTITY_337)
            return (ASTSavedCommonBlockNode)getChild(0);
        else
            return null;
    }
}
