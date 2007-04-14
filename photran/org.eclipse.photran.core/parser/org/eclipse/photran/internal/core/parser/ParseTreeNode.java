package org.eclipse.photran.internal.core.parser; import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.photran.internal.core.lexer.Token;

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

        public String printOn(PrintStream out, String currentPreprocessorDirective)
        {
            return currentPreprocessorDirective;
        }
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
    private AbstractParseTreeNode[] childArray;
    private int numChildren;
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////
    
    public ParseTreeNode(Nonterminal nonterminal, Production production)
    {
        this.nonterminal = nonterminal;
        this.production = production;
        this.childArray = null;
        this.numChildren = 0;
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
        ensureCapacity();
        childArray[numChildren++] = child;
        child.parent = this;
    }

    private void ensureCapacity()
    {
        if (childArray == null)
            childArray = new AbstractParseTreeNode[16]; // Heuristic
        else if (numChildren >= childArray.length)
            expandArray();
    }

    private void expandArray()
    {
        AbstractParseTreeNode[] newChildArray = new AbstractParseTreeNode[Math.min(childArray.length*2, 1024)];
        System.arraycopy(childArray, 0, newChildArray, 0, childArray.length);
        childArray = newChildArray;
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
            for (Iterator it = child.iterator(); it.hasNext(); )
            {
                Object o = it.next();
                if (o instanceof Token)
                    addChild((Token)o);
            }
        }
    }
    
    public void addChild(int index, AbstractParseTreeNode nodeToAdd)
    {
        if (index < 0 || index > numChildren) throw new IllegalArgumentException("Invalid index " + index);
        
        ensureCapacity();
        for (int i = numChildren; i >= index; i--)
            childArray[i+1] = childArray[i];
        childArray[index] = nodeToAdd;
        numChildren++;
    }
    
    public int findChild(AbstractParseTreeNode child)
    {
        if (childArray == null) return -1;
        
        for (int i = 0; i < numChildren; i++)
            if (childArray[i].equals(child))
                return i;
        return -1;
    }

    public boolean removeChild(AbstractParseTreeNode childToRemove)
    {
        int index = findChild(childToRemove);
        return index < 0 ? false : removeChild(index);
    }
    
    public boolean removeChild(int index)
    {
        if (index < 0 || index >= numChildren) throw new IllegalArgumentException("Invalid index " + index);
        if (childArray == null) return false;
        
        for (int i = index + 1; i < numChildren; i++)
            childArray[i-1] = childArray[i];
        numChildren--;
        return true;
    }
    
    private AbstractParseTreeNode privateGetChild(int index)
    {
        if (index < 0 || index >= numChildren) throw new IllegalArgumentException("Invalid index " + index);
        if (childArray == null) return null;
        
        AbstractParseTreeNode result = childArray[index];
        return result == EMPTY ? null : result;
    }
    
    public ParseTreeNode getChild(int index)
    {
        AbstractParseTreeNode result = privateGetChild(index);
        if (result == null || result instanceof ParseTreeNode)
            return (ParseTreeNode)result;
        else
            throw new IllegalArgumentException("The child at index " + index + " is a " + result.getClass().getName() + ", not a ParseTreeNode");
    }
    
    public ParseTreeNode getChild(String name)
    {
        int index = production.getNamedIndex(name);
        return index < 0 ? null : getChild(index);
    }
    
    public Token getChildToken(int index)
    {
        AbstractParseTreeNode result = privateGetChild(index);
        if (result == null || result instanceof Token)
            return (Token)result;
        else
            throw new IllegalArgumentException("The child at index " + index + " is a " + result.getClass().getName() + ", not a Token");
    }
    
    public Token getChildToken(String name)
    {
        int index = production.getNamedIndex(name);
        return index < 0 ? null : getChildToken(index);
    }
    
    public int getNumberOfChildren()
    {
        return numChildren;
    }
    
//    public List getChildren()
//    {
//        return Arrays.asList(childArray);
//    }
//
//    public Iterator iterator()
//    {
//        class NullIterator implements Iterator
//        {
//            public boolean hasNext()
//            {
//                return false;
//            }
//
//            public Object next()
//            {
//                return null;
//            }
//
//            public void remove()
//            {
//                throw new Error();
//            }
//        }
//        
//        class ArrayIterator implements Iterator
//        {
//            private int index = 0;
//            
//            public boolean hasNext()
//            {
//                return index < numChildren;
//            }
//
//            public Object next()
//            {
//                return childArray[index++];
//            }
//
//            public void remove()
//            {
//                throw new Error();
//            }
//            
//        }
//        
//        return childArray == null ? (Iterator)new NullIterator() : (Iterator)new ArrayIterator();
//    }

    ///////////////////////////////////////////////////////////////////////////
    // Visitor Support
    ///////////////////////////////////////////////////////////////////////////

    public void visitTopDownUsing(ASTVisitor visitor)
    {
        visitThisNodeUsing(visitor);

        if (childArray != null)
            for (int i = 0; i < numChildren; i++)
                childArray[i].visitTopDownUsing(visitor);
    }

    public void visitBottomUpUsing(ASTVisitor visitor)
    {
        if (childArray != null)
            for (int i = 0; i < numChildren; i++)
                childArray[i].visitBottomUpUsing(visitor);

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
        if (childArray != null)
            for (int i = 0; i < numChildren; i++)
                childArray[i].visitUsing(visitor);
        visitor.doneVisitingChildrenOf(this);
    }

    public void visitUsing(GenericParseTreeVisitor visitor)
    {
        visitor.visitParseTreeNode(this);

        visitor.preparingToVisitChildrenOf(this);
        if (childArray != null)
            for (int i = 0; i < numChildren; i++)
                childArray[i].visitUsing(visitor);
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

        if (childArray != null)
            for (int i = 0; i < numChildren; i++)
                sb.append(childArray[i].toString(numSpaces + INDENT_SIZE));
        
        return sb.toString();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Source Code Reproduction
    ///////////////////////////////////////////////////////////////////////////
    
    public String printOn(PrintStream out, String currentPreprocessorDirective)
    {
        if (childArray != null)
            for (int i = 0; i < numChildren; i++)
                currentPreprocessorDirective = childArray[i].printOn(out, currentPreprocessorDirective);
        return currentPreprocessorDirective;
    }
}
