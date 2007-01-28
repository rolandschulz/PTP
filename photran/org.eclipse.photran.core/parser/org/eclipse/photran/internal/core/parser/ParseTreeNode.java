package org.eclipse.photran.internal.core.parser; import org.eclipse.photran.internal.core.lexer.*;

import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ParseTreeNode extends AbstractParseTreeNode
{
    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////
    
    private static final class EmptyNode extends AbstractParseTreeNode
    {
        private EmptyNode() {}

        public void visitBottomUpUsing(ASTVisitor visitor) {}
        public void visitTopDownUsing(ASTVisitor visitor) {}
        public void visitUsing(ParseTreeVisitor visitor) {}
        public void visitUsing(GenericParseTreeVisitor visitor) {}
        
        public String toString(int numSpaces)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(indent(numSpaces));
            sb.append("(empty node)");
            sb.append("\n");
            return sb.toString();
        }

        public void printOn(PrintStream out) {}
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////
    
    public static final AbstractParseTreeNode EMPTY = new EmptyNode();

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////
    
    private Nonterminal nonterminal;
    private Production production;
    private LinkedList/*<AbstractParseTreeNode>*/ children;
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////
    
    public ParseTreeNode(Nonterminal nonterminal, Production production)
    {
        this.nonterminal = nonterminal;
        this.production = production;
        this.children = null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Accessor/Mutator Methods
    ///////////////////////////////////////////////////////////////////////////

    public Nonterminal getNonterminal()
    {
        return nonterminal;
    }
    
    public Production getProduction()
    {
        return production;
    }

    public void addChild(AbstractParseTreeNode child)
    {
        if (child == null) child = EMPTY;

        if (children == null) children = new LinkedList/*<AbstractParseTreeNode>*/();
        children.add(child);
        child.parentRef = new WeakReference/*<ParseTreeNode>*/(this);
    }

    /**
     * In error productions, the %error token is associated with a LinkedList
     * which contains whatever was popped off the stack (ParseTreeNodes and Tokens,
     * in our case) as well as forthcoming tokens which were discarded.  This method
     * extracts all of the Tokens from that list and inserts them into this node.
     * @param child
     */
    public void addErrorChild(LinkedList/*<Object>*/ child)
    {
        if (child == null)
            return;
        else
        {
            if (children == null) children = new LinkedList/*<AbstractParseTreeNode>*/();
            for (Iterator it = child.iterator(); it.hasNext(); )
            {
                Object o = it.next();
                if (o instanceof Token)
                {
                    Token t = (Token)o;
                    children.add(t);
                    t.parentRef = new WeakReference/*<ParseTreeNode>*/(this);
                }
            }
        }
    }
    
    public void addChild(int index, AbstractParseTreeNode nodeToAdd)
    {
        if (children == null) children = new LinkedList/*<AbstractParseTreeNode>*/();
        children.add(index, nodeToAdd);
    }

    public boolean removeChild(AbstractParseTreeNode childToRemove)
    {
        return children == null ? false : children.remove(childToRemove);
    }
    
    public boolean removeChild(int index)
    {
        if (children == null) return false;
        
        children.remove(index);
        return true;
    }
    
    public ParseTreeNode getChild(int index)
    {
        if (children == null || index < 0 || index >= children.size() || children.get(index) == EMPTY)
            return null;
        else
            return (ParseTreeNode)children.get(index);
    }
    
    public ParseTreeNode getChild(String name)
    {
        return getChild(production.getNamedIndex(name));
    }
    
    public Token getChildToken(int index)
    {
        if (children == null || index < 0 || index >= children.size() || children.get(index) == EMPTY)
            return null;
        else
            return (Token)children.get(index);
    }
    
    public Token getChildToken(String name)
    {
        return getChildToken(production.getNamedIndex(name));
    }
    
    public int getNumberOfChildren()
    {
        return children == null ? 0 : children.size();
    }
    
    public List getChildren()
    {
        return children;
    }

    public Iterator iterator()
    {
        class NullIterator implements Iterator
        {
            public boolean hasNext()
            {
                return false;
            }

            public Object next()
            {
                return null;
            }

            public void remove()
            {
                throw new Error();
            }
        }
        
        return children == null ? new NullIterator() : children.iterator();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Visitor Support
    ///////////////////////////////////////////////////////////////////////////

    public void visitTopDownUsing(ASTVisitor visitor)
    {
        visitThisNodeUsing(visitor);

        Iterator it = children.iterator();
        while (it.hasNext())
        {
            AbstractParseTreeNode n = (AbstractParseTreeNode)it.next(); 
            n.visitTopDownUsing(visitor);
        }
    }

    public void visitBottomUpUsing(ASTVisitor visitor)
    {
        Iterator it = children.iterator();
        while (it.hasNext())
        {
            AbstractParseTreeNode n = (AbstractParseTreeNode)it.next(); 
            n.visitBottomUpUsing(visitor);
        }

        visitThisNodeUsing(visitor);
    }
    
    public void visitOnlyThisNodeUsing(ASTVisitor visitor)
    {
        visitThisNodeUsing(visitor);
    }
    
    protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        ;
    }
    
    public void visitUsing(ParseTreeVisitor visitor)
    {
        nonterminal.visitParseTreeNodeUsing(this, visitor);

        visitor.preparingToVisitChildrenOf(this);
        Iterator it = children.iterator();
        while (it.hasNext())
        {
            AbstractParseTreeNode n = (AbstractParseTreeNode)it.next(); 
            n.visitUsing(visitor);
        }
        visitor.doneVisitingChildrenOf(this);
    }

    public void visitUsing(GenericParseTreeVisitor visitor)
    {
        visitor.visitParseTreeNode(this);

        visitor.preparingToVisitChildrenOf(this);
        Iterator it = children.iterator();
        while (it.hasNext())
        {
            AbstractParseTreeNode n = (AbstractParseTreeNode)it.next(); 
            n.visitUsing(visitor);
        }
        visitor.doneVisitingChildrenOf(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Debugging Output
    ///////////////////////////////////////////////////////////////////////////
    
    public String toString(int numSpaces)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(indent(numSpaces));
        sb.append(nonterminal.getDescription());
        sb.append("\n");

        Iterator it = children.iterator();
        while (it.hasNext())
        {
            AbstractParseTreeNode n = (AbstractParseTreeNode)it.next(); 
            sb.append(n.toString(numSpaces + INDENT_SIZE));
        }
        
        return sb.toString();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Source Code Reproduction
    ///////////////////////////////////////////////////////////////////////////
    
    public void printOn(PrintStream out)
    {
        Iterator it = children.iterator();
        while (it.hasNext())
        {
            AbstractParseTreeNode n = (AbstractParseTreeNode)it.next(); 
            n.printOn(out);
        }
    }
}
