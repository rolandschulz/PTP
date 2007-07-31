package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTConnectSpecNode extends InteriorNode
{
    ASTConnectSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTConnectSpecNode(this);
    }

    public Token getTUniteq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_743)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_743)
            return (ASTUnitIdentifierNode)getChild(1);
        else
            return null;
    }

    public Token getTErreq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_744)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTLblRefNode getLblRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_744)
            return (ASTLblRefNode)getChild(1);
        else
            return null;
    }

    public Token getTFileeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_745)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTCExprNode getCExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_745)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_746)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_747)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_748)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_750)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_752)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_753)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_754)
            return (ASTCExprNode)getChild(1);
        else if (getProduction() == Production.CONNECT_SPEC_755)
            return (ASTCExprNode)getChild(1);
        else
            return null;
    }

    public Token getTStatuseq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_746)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTAccesseq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_747)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTFormeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_748)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTRecleq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_749)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_749)
            return (ASTExprNode)getChild(1);
        else
            return null;
    }

    public Token getTBlankeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_750)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTIostateq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_751)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTScalarVariableNode getScalarVariable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_751)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public Token getTPositioneq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_752)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTActioneq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_753)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTDelimeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_754)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTPadeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_755)
            return (Token)getChild(0);
        else
            return null;
    }
}
