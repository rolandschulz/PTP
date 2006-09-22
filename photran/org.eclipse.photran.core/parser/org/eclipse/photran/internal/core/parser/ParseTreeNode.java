package org.eclipse.photran.internal.core.parser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.photran.internal.core.lexer.Token;

public class ParseTreeNode extends AbstractParseTreeNode
{
    private Nonterminal root;
    private Production production;

    private LinkedList/*<AbstractParseTreeNode>*/ children;

    public Nonterminal getRootNonterminal()
    {
        return root;
    }
    
    public Production getProduction()
    {
        return production;
    }
    
    public ParseTreeNode(Nonterminal root, Production production)
    {
        this.root = root;
        this.production = production;
        this.children = null;
    }

    public void addChild(AbstractParseTreeNode child)
    {
        if (child == null)
            return;
        else
        {
            if (children == null) children = new LinkedList/*<AbstractParseTreeNode>*/();
            children.add(child);
            child.parent = this;
        }
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
                    t.parent = this;
                }
            }
        }
    }
    
    public void addChild(int index, ParseTreeNode nodeToAdd)
    {
        if (children == null) children = new LinkedList/*<AbstractParseTreeNode>*/();
        children.add(index, nodeToAdd);
    }

    public boolean removeChild(ParseTreeNode childToRemove)
    {
        return children == null ? false : children.remove(childToRemove);
    }
    
    public ParseTreeNode getFirstChild()
    {
        return (ParseTreeNode)(children == null || children.size() < 1 ? null : children.getFirst());
    }
    
    public ParseTreeNode getChild(int index)
    {
        return (ParseTreeNode)(children == null || children.size() <= index || index < 0 ? null : children.get(index));
    }
    
    public ParseTreeNode getChild(String name)
    {
        return getChild(production.getNamedIndex(name));
    }
    
    public Token getChildToken(int index)
    {
        return (Token)(children == null || children.size() <= index || index < 0 ? null : children.get(index));
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

    // -------------------------------------------------------------------------------------------

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
    
    protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        ;
    }
    
    public void visitUsing(ParseTreeVisitor visitor)
    {
        root.visitParseTreeNodeUsing(this, visitor);

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

    // -------------------------------------------------------------------------------------------
    
    private static final int INDENT_SIZE = 4;
    
    public String toString()
    {
        return toString(0);
    }
    
    public String toString(int numSpaces)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(indent(numSpaces));
        sb.append(root.getDescription());
        sb.append("\n");

        Iterator it = children.iterator();
        while (it.hasNext())
        {
            AbstractParseTreeNode n = (AbstractParseTreeNode)it.next(); 
            sb.append(n.toString(numSpaces + INDENT_SIZE));
        }
        
        return sb.toString();
    }
}
