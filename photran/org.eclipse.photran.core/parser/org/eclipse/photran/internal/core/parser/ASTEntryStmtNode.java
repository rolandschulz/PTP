package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTEntryStmtNode extends InteriorNode
{
    ASTEntryStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTEntryStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTRY_STMT_1009)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ENTRY_STMT_1010)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTEntry()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTRY_STMT_1009)
            return (Token)getChild(1);
        else if (getProduction() == Production.ENTRY_STMT_1010)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTEntryNameNode getEntryName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTRY_STMT_1009)
            return (ASTEntryNameNode)getChild(2);
        else if (getProduction() == Production.ENTRY_STMT_1010)
            return (ASTEntryNameNode)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTRY_STMT_1009)
            return (Token)getChild(3);
        else if (getProduction() == Production.ENTRY_STMT_1010)
            return (Token)getChild(6);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTRY_STMT_1010)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTSubroutineParsNode getSubroutinePars()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTRY_STMT_1010)
            return (ASTSubroutineParsNode)getChild(4);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTRY_STMT_1010)
            return (Token)getChild(5);
        else
            return null;
    }
}
