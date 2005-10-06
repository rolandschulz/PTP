package org.eclipse.photran.core.programrepresentation;

import java.util.Iterator;

import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;

/**
 * Represents a parse tree together with a <code>Presentation</code>. Common super-interface of
 * <code>Program</code>s and <code>PartialProgram</code>s.
 * 
 * @author joverbey
 */
public abstract class ParseTreePres
{
    protected ParseTreeNode parseTree;

    protected Presentation presentation;

    public ParseTreePres(ParseTreeNode parseTree, Presentation presentation)
    {
        this.parseTree = parseTree;
        this.presentation = (presentation == null ? new Presentation() : presentation);
    }

    public ParseTreeNode getParseTree()
    {
        return parseTree;
    }

    public Presentation getPresentation()
    {
        return presentation;
    }
    
    public Iterator iterator()
    {
        return new ParseTreePresIterator(this);
    }
}