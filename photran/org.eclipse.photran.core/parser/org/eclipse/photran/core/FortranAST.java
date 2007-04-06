package org.eclipse.photran.core;

import java.util.Iterator;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.ParseTreeVisitor;

public class FortranAST implements IFortranAST
{
    private ASTExecutableProgramNode root;
    private TokenList tokenList;
    
    public FortranAST(ASTExecutableProgramNode root, TokenList tokenList)
    {
        this.root = root;
        this.tokenList = tokenList;
    }
    
    public void visitBottomUpUsing(ASTVisitor visitor)
    {
        root.visitBottomUpUsing(visitor);
    }
    
    public void visitOnlyThisNodeUsing(ASTVisitor visitor)
    {
        root.visitOnlyThisNodeUsing(visitor);
    }
    
    public void visitTopDownUsing(ASTVisitor visitor)
    {
        root.visitTopDownUsing(visitor);
    }
    
    public void visitUsing(ParseTreeVisitor visitor)
    {
        root.visitUsing(visitor);
    }
    
    public void visitUsing(GenericParseTreeVisitor visitor)
    {
        root.visitUsing(visitor);
    }
    
    public ASTExecutableProgramNode getRoot()
    {
        return root;
    }

    public Iterator/*<Token>*/ iterator()
    {
        return tokenList.iterator();
    }

    public Token findTokenByOffsetLength(final int offset, final int length)
    {
        // Binary Search
        return tokenList.findOffsetLength(offset, length);

// or Linear Search...
//        for (int i = 0; i < tokenList.size(); i++)
//        {
//            Token token = (Token)tokenList.get(i);
//            if (token.getOffset() == offset && token.getLength() == length)
//                return token;
//        }
//        return null;
        
// or Parse Tree Traversal...
//        try
//        {
//            root.visitUsing(new GenericParseTreeVisitor()
//            {
//                public void visitToken(Token token)
//                {
//                    if (token.getOffset() == offset && token.getLength() == length)
//                        throw new Notification(token);
//                }
//            });
//        }
//        catch (Notification n)
//        {
//            return (Token)n.getResult();
//        }
//        return null;
    }
    
    public Token findFirstTokenOnLine(int line)
    {
        // Binary Search
        return tokenList.findFirstTokenOnLine(line);
    }
}