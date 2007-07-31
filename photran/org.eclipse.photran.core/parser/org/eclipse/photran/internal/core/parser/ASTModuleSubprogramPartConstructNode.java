package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTModuleSubprogramPartConstructNode extends InteriorNode
{
    ASTModuleSubprogramPartConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTModuleSubprogramPartConstructNode(this);
    }

    public ASTContainsStmtNode getContainsStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MODULE_SUBPROGRAM_PART_CONSTRUCT_57)
            return (ASTContainsStmtNode)getChild(0);
        else
            return null;
    }

    public ASTModuleSubprogramNode getModuleSubprogram()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MODULE_SUBPROGRAM_PART_CONSTRUCT_58)
            return (ASTModuleSubprogramNode)getChild(0);
        else
            return null;
    }
}
