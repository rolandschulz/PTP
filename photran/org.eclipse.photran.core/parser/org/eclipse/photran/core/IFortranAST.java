package org.eclipse.photran.core;

import java.util.Iterator;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.ParseTreeVisitor;

public interface IFortranAST /*extends Iterable<Token>*/
{
    ///////////////////////////////////////////////////////////////////////////
    // Visitor Support
    ///////////////////////////////////////////////////////////////////////////

    public void visitTopDownUsing(ASTVisitor visitor);
    public void visitBottomUpUsing(ASTVisitor visitor);
    public void visitOnlyThisNodeUsing(ASTVisitor visitor);
    public void visitUsing(ParseTreeVisitor visitor);
    public void visitUsing(GenericParseTreeVisitor visitor);
    
    ///////////////////////////////////////////////////////////////////////////
    // Other Methods
    ///////////////////////////////////////////////////////////////////////////

    public ASTExecutableProgramNode getRoot();
    public Iterator/*<Token*/ iterator();
    
    public Token findTokenByStreamOffsetLength(int offset, int length);
    public Token findFirstTokenOnLine(int line);
}
