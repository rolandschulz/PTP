package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTInquireSpecNode extends InteriorNode
{
    ASTInquireSpecNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTInquireSpecNode(this);
    }

    public Token getTUniteq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_840)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTUnitIdentifierNode getUnitIdentifier()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_840)
            return (ASTUnitIdentifierNode)getChild(1);
        else
            return null;
    }

    public Token getTFileeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_841)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTCExprNode getCExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_841)
            return (ASTCExprNode)getChild(1);
        else
            return null;
    }

    public Token getTErreq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_842)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTLblRefNode getLblRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_842)
            return (ASTLblRefNode)getChild(1);
        else
            return null;
    }

    public Token getTIostateq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_843)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTScalarVariableNode getScalarVariable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_843)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_844)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_845)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_846)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_847)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_848)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_849)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_850)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_851)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_852)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_853)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_854)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_856)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_857)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_858)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_859)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_860)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_861)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_862)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_863)
            return (ASTScalarVariableNode)getChild(1);
        else if (getProduction() == Production.INQUIRE_SPEC_864)
            return (ASTScalarVariableNode)getChild(1);
        else
            return null;
    }

    public Token getTExisteq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_844)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTOpenedeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_845)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTNumbereq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_846)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTNamedeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_847)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTNameeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_848)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTAccesseq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_849)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTSequentialeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_850)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTDirecteq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_851)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTFormeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_852)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTFormattedeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_853)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTUnformattedeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_854)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTRecleq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_855)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_855)
            return (ASTExprNode)getChild(1);
        else
            return null;
    }

    public Token getTNextreceq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_856)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTBlankeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_857)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTPositioneq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_858)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTActioneq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_859)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTReadeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_860)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTWriteeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_861)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTReadwriteeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_862)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTDelimeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_863)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTPadeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INQUIRE_SPEC_864)
            return (Token)getChild(0);
        else
            return null;
    }
}
