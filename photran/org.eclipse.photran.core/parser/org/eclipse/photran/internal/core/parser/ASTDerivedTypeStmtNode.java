package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTDerivedTypeStmtNode extends InteriorNode
{
    ASTDerivedTypeStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDerivedTypeStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_STMT_184)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.DERIVED_TYPE_STMT_185)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.DERIVED_TYPE_STMT_186)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTType()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_STMT_184)
            return (Token)getChild(1);
        else if (getProduction() == Production.DERIVED_TYPE_STMT_185)
            return (Token)getChild(1);
        else if (getProduction() == Production.DERIVED_TYPE_STMT_186)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTTypeNameNode getTypeName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_STMT_184)
            return (ASTTypeNameNode)getChild(2);
        else if (getProduction() == Production.DERIVED_TYPE_STMT_185)
            return (ASTTypeNameNode)getChild(4);
        else if (getProduction() == Production.DERIVED_TYPE_STMT_186)
            return (ASTTypeNameNode)getChild(6);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_STMT_184)
            return (Token)getChild(3);
        else if (getProduction() == Production.DERIVED_TYPE_STMT_185)
            return (Token)getChild(5);
        else if (getProduction() == Production.DERIVED_TYPE_STMT_186)
            return (Token)getChild(7);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_STMT_185)
            return (Token)getChild(2);
        else if (getProduction() == Production.DERIVED_TYPE_STMT_186)
            return (Token)getChild(4);
        else
            return null;
    }

    public Token getTColon2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_STMT_185)
            return (Token)getChild(3);
        else if (getProduction() == Production.DERIVED_TYPE_STMT_186)
            return (Token)getChild(5);
        else
            return null;
    }

    public Token getTComma()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_STMT_186)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTAccessSpecNode getAccessSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_STMT_186)
            return (ASTAccessSpecNode)getChild(3);
        else
            return null;
    }
}
