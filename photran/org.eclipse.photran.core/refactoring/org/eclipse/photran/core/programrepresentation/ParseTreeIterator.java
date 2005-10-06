package org.eclipse.photran.core.programrepresentation;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.photran.internal.core.f95parser.AbstractParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;

/**
 * An <code>Iterator</code> that performs a preorder traversal of a parse tree
 * 
 * @author joverbey
 */
public class ParseTreeIterator implements Iterator
{
    protected AbstractParseTreeNode root;
    
    protected Stack/*<ParseTreeNode>*/ parentNodes;
    protected Stack/*<Iterator>*/ parentNodeIterators;
    
    protected AbstractParseTreeNode nextNode;
    
    public ParseTreeIterator(ParseTreeNode parseTreeRoot)
    {
        this.root = parseTreeRoot;
        
        this.parentNodes = new Stack();
        this.parentNodeIterators = new Stack();
        
        this.nextNode = root; 
    }
    
    public void remove()
    {
        throw new Error("remove() is not a valid operation on a ParseTreeIterator");
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * @param currentNode
     * @return <code>null</code> if no children, or a non-empty list otherwise
     */
    protected List/*<AbstractParseTreeNode>*/ getChildren(AbstractParseTreeNode currentNode)
    {
        List/*<AbstractParseTreeNode>*/ children = null;
        
        if (currentNode != null && currentNode instanceof ParseTreeNode)
        {
            children = ((ParseTreeNode)currentNode).getChildren();
            if (children != null && children.size() == 0) children = null;
        }
        
        return children;
    }
    
    protected ParseTreeNode getCurrentParent()
    {
        return (ParseTreeNode)parentNodes.peek();
    }
    
    protected Iterator/*<AbstractParseTreeNode>*/ getCurrentParentIterator()
    {
        return (Iterator)parentNodeIterators.peek();
    }
    
    ///////////////////////////////////////////////////////////////////////////

    public boolean hasNext()
    {
        return this.nextNode != null;
    }

    public Object next()
    {
        AbstractParseTreeNode result = this.nextNode;
        determineNextNode(this.nextNode);
        return result;
    }

    protected void determineNextNode(AbstractParseTreeNode currentNode)
    {
        List/*<AbstractParseTreeNode>*/ children = getChildren(currentNode);
        if (children != null)
            startTraversingChildren(currentNode, children);
        else
            gotoSibling();
    }

    protected void gotoSibling()
    {
        Iterator/*<AbstractParseTreeNode>*/ it = getCurrentParentIterator();
        
        if (it.hasNext())
            this.nextNode = (AbstractParseTreeNode)it.next();
        else
            doneTraversingChildren();
    }

    protected void startTraversingChildren(AbstractParseTreeNode currentNode, List children)
    {
        this.parentNodes.push(currentNode);
        Iterator/*<AbstractParseTreeNode>*/ it = children.iterator();
        this.parentNodeIterators.push(it);
        this.nextNode = (AbstractParseTreeNode)it.next();
    }

    protected void doneTraversingChildren()
    {
        this.parentNodes.pop();
        this.parentNodeIterators.pop();
        
        if (this.parentNodes.isEmpty())
            this.nextNode = null;
        else
            gotoSibling();
    }
}
