package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTCaseBodyConstructNode extends InteriorNode
{
    ASTCaseBodyConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTCaseBodyConstructNode(this);
    }

    public ASTCaseStmtNode getCaseStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CASE_BODY_CONSTRUCT_680)
            return (ASTCaseStmtNode)getChild(0);
        else
            return null;
    }

    public ASTExecutionPartConstructNode getExecutionPartConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CASE_BODY_CONSTRUCT_681)
            return (ASTExecutionPartConstructNode)getChild(0);
        else
            return null;
    }
}
