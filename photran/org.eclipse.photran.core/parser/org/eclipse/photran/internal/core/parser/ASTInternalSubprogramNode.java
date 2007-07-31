package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTInternalSubprogramNode extends InteriorNode
{
    ASTInternalSubprogramNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTInternalSubprogramNode(this);
    }

    public ASTFunctionSubprogramNode getFunctionSubprogram()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERNAL_SUBPROGRAM_55)
            return (ASTFunctionSubprogramNode)getChild(0);
        else
            return null;
    }

    public ASTSubroutineSubprogramNode getSubroutineSubprogram()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERNAL_SUBPROGRAM_56)
            return (ASTSubroutineSubprogramNode)getChild(0);
        else
            return null;
    }
}
