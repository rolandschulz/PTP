package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTAssignStmtNode extends InteriorNode
{
    ASTAssignStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAssignStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGN_STMT_1049)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTAssign()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGN_STMT_1049)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTLblRefNode getLblRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGN_STMT_1049)
            return (ASTLblRefNode)getChild(2);
        else
            return null;
    }

    public Token getTTo()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGN_STMT_1049)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTVariableNameNode getVariableName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGN_STMT_1049)
            return (ASTVariableNameNode)getChild(4);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGN_STMT_1049)
            return (Token)getChild(5);
        else
            return null;
    }
}
