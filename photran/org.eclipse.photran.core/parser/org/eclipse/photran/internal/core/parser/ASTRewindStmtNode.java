package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTRewindStmtNode extends InteriorNode
{
    ASTRewindStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTRewindStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REWIND_STMT_827)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.REWIND_STMT_828)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTRewind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REWIND_STMT_827)
            return (Token)getChild(1);
        else if (getProduction() == Production.REWIND_STMT_828)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REWIND_STMT_827)
            return (ASTUnitIdentifierNode)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REWIND_STMT_827)
            return (Token)getChild(3);
        else if (getProduction() == Production.REWIND_STMT_828)
            return (Token)getChild(5);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REWIND_STMT_828)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTPositionSpecListNode getPositionSpecList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REWIND_STMT_828)
            return (ASTPositionSpecListNode)getChild(3);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REWIND_STMT_828)
            return (Token)getChild(4);
        else
            return null;
    }
}
