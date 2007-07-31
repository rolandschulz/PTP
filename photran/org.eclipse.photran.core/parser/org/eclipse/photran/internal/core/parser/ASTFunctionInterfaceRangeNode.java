package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTFunctionInterfaceRangeNode extends InteriorNode
{
    ASTFunctionInterfaceRangeNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTFunctionInterfaceRangeNode(this);
    }

    public ASTSubprogramInterfaceBodyNode getSubprogramInterfaceBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_INTERFACE_RANGE_938)
            return (ASTSubprogramInterfaceBodyNode)getChild(0);
        else
            return null;
    }

    public ASTEndFunctionStmtNode getEndFunctionStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_INTERFACE_RANGE_938)
            return (ASTEndFunctionStmtNode)getChild(1);
        else if (getProduction() == Production.FUNCTION_INTERFACE_RANGE_939)
            return (ASTEndFunctionStmtNode)getChild(0);
        else
            return null;
    }
}
