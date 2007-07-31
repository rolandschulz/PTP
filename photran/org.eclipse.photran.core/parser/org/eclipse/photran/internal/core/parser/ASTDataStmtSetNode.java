package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTDataStmtSetNode extends InteriorNode
{
    ASTDataStmtSetNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDataStmtSetNode(this);
    }

    public ASTDataStmtObjectListNode getDataStmtObjectList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_SET_372)
            return (ASTDataStmtObjectListNode)getChild(0);
        else
            return null;
    }

    public Token getTSlash()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_SET_372)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTDataStmtValueListNode getDataStmtValueList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_SET_372)
            return (ASTDataStmtValueListNode)getChild(2);
        else
            return null;
    }

    public Token getTSlash2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_STMT_SET_372)
            return (Token)getChild(3);
        else
            return null;
    }
}
