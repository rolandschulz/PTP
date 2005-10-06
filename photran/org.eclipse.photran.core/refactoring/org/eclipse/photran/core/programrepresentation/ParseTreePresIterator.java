package org.eclipse.photran.core.programrepresentation;

import java.util.Iterator;

import org.eclipse.photran.internal.core.f95parser.AbstractParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.IPresentationBlock;
import org.eclipse.photran.internal.core.f95parser.NonTreeToken;
import org.eclipse.photran.internal.core.f95parser.Token;

/**
 * An iterator that simultaneously traverses the tokens of a parse tree
 * and the non-tree tokens in a <code>Presentation</code> object,
 * ordering the tokens and non-tree tokens according to their line/column
 * fields.  In other words, they are iterated through in the same order
 * they would appear when printed by the <code>SourcePrinter</code>.
 * 
 * @author joverbey
 */
public class ParseTreePresIterator implements Iterator/*<IPresentationBlock>*/
{
    protected Iterator/*<AbstractParseTreeNode>*/ parseTreeIterator;
    protected Iterator/*<NonTreeToken>*/ presIterator;
    
    protected Token nextToken;
    protected NonTreeToken nextNonTreeToken;
    
    public ParseTreePresIterator(ParseTreePres ptp)
    {
        this.parseTreeIterator = new ParseTreeIterator(ptp.getParseTree());
        this.presIterator = ptp.getPresentation().iterator();
        
        determineNextToken();
        determineNextNonTreeToken();
    }

    public void remove()
    {
        throw new Error("remove() is not a valid operation on a ParseTreePresIterator");
    }

    public boolean hasNext()
    {
        return nextToken != null || nextNonTreeToken != null;
    }

    public Object next()
    {
        if (nextToken == null)
            return determineNextNonTreeToken();
        else if (nextNonTreeToken == null)
            return determineNextToken();
        else // neither is null
        {
            if (isLessThan(this.nextToken, this.nextNonTreeToken))
                return determineNextToken();
            else
                return determineNextNonTreeToken();
        }
    }

    /**
     * @return the <i>current</i> value of <code>nextToken</code>, but progresses the
     * <code>parseTreeIterator</code> to the next <code>Token</code>, updating
     * the <code>nextToken</code> field to its next value
     */
    protected Token determineNextToken()
    {
        Token currentToken = this.nextToken;
        
        AbstractParseTreeNode nextNode = null;
        boolean foundToken = false;
        
        while (parseTreeIterator.hasNext() && foundToken == false)
        {
            nextNode = (AbstractParseTreeNode)parseTreeIterator.next();
            foundToken = (nextNode instanceof Token);
        }
        
        if (foundToken)
            this.nextToken = (Token)nextNode;
        else
            this.nextToken = null;
        
        return currentToken;
    }
    
    /**
     * @return the <i>current</i> value of <code>nextNonTreeToken</code>, but progresses the
     * <code>presIterator</code> to the next <code>NonTreeToken</code>, updating
     * the <code>nextNonTreeToken</code> field to its next value
     */
    protected NonTreeToken determineNextNonTreeToken()
    {
        NonTreeToken currentNonTreeToken = this.nextNonTreeToken;
        
        this.nextNonTreeToken = presIterator.hasNext() ? (NonTreeToken)presIterator.next() : null;
        
        return currentNonTreeToken;
    }
    
    protected boolean isLessThan(IPresentationBlock a, IPresentationBlock b)
    {
        if (a.getStartLine() < b.getStartLine())
            return true;
        else if (a.getStartLine() > b.getStartLine())
            return false;
        else if (a.getStartCol() < b.getStartCol())
            return true;
        else if (a.getStartCol() > b.getStartCol())
            return false;
        else
            return false; // equal
    }
}
