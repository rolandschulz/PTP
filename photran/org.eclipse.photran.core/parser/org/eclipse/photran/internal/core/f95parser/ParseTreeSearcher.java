package org.eclipse.photran.internal.core.f95parser;

import java.util.Iterator;

/**
 * Methods for finding nodes in a (Fortran) parse tree
 * 
 * @author joverbey
 */
public final class ParseTreeSearcher
{
    private static class VisitorTokenNotification extends Error
    {
        private static final long serialVersionUID = 1L;

        private Token notificationToken;

        public VisitorTokenNotification(Token notificationToken)
        {
            this.notificationToken = notificationToken;
        }

        public Token getNotificationToken()
        {
            return notificationToken;
        }
    }

    private static class VisitorNodeNotification extends Error
    {
        private static final long serialVersionUID = 1L;

        private ParseTreeNode notificationNode;

        public VisitorNodeNotification(ParseTreeNode notificationNode)
        {
            this.notificationNode = notificationNode;
        }

        public ParseTreeNode getNotificationNode()
        {
            return notificationNode;
        }
    }

    /**
     * Returns the first T_IDENT node in the given tree, or <code>null</code> if none could be
     * found.
     * 
     * @param tree
     * @return <code>Token</code> (possibly <code>null</code>)
     */
    public static Token findFirstIdentifierIn(AbstractParseTreeNode tree)
    {
        return findFirstTokenIn(tree, Terminal.T_IDENT);
    }

    /**
     * Returns the last T_IDENT node in the given tree, or <code>null</code> if none could be
     * found.
     * 
     * @param tree
     * @return <code>Token</code> (possibly <code>null</code>)
     */
    public static Token findLastIdentifierIn(AbstractParseTreeNode tree)
    {
        return findLastTokenIn(tree, Terminal.T_IDENT);
    }

    /**
     * Returns the first node in the given tree corresponding to the given nonterminal, or
     * <code>null</code> if none could be found.
     * @param tree
     * 
     * @return <code>Token</code> (possibly <code>null</code>)
     */
    public static ParseTreeNode findFirstNodeIn(AbstractParseTreeNode tree,
        final Nonterminal nodeType)
    {
        if (tree == null) return null;

        try
        {
            tree.visitUsing(new GenericParseTreeVisitor()
            {
                public void visitParseTreeNode(ParseTreeNode node)
                {
                    if (node.getRootNonterminal() == nodeType)
                        throw new VisitorNodeNotification(node);
                }
            });
        }
        catch (VisitorNodeNotification nodeFound)
        {
            return nodeFound.getNotificationNode();
        }

        return null;
    }

    /**
     * Returns the first token in the given tree, or <code>null</code> if none could be found.
     * 
     * @param tree
     * @return <code>Token</code> (possibly <code>null</code>)
     */
    public static Token findFirstTokenIn(AbstractParseTreeNode tree)
    {
        if (tree == null) return null;

        try
        {
            tree.visitUsing(new GenericParseTreeVisitor()
            {
                public void visitToken(Token token)
                {
                    throw new VisitorTokenNotification(token);
                }
            });
        }
        catch (VisitorTokenNotification tokenFound)
        {
            return tokenFound.getNotificationToken();
        }

        return null;
    }

    /**
     * Returns the last token in the given tree, or <code>null</code> if none could be found.
     * 
     * @param tree
     * @return <code>Token</code> (possibly <code>null</code>)
     */
    public static Token findLastTokenIn(AbstractParseTreeNode tree)
    {
        if (tree == null) return null;

        class TokenMemorizer extends GenericParseTreeVisitor
        {
            private Token lastToken = null;

            public Token getLastToken()
            {
                return lastToken;
            }

            public void visitToken(Token token)
            {
                lastToken = token;
            }
        }

        TokenMemorizer mem = new TokenMemorizer();
        tree.visitUsing(mem);
        return mem.getLastToken();
    }

    /**
     * Returns the first token of the given type in the given tree, or <code>null</code> if none
     * could be found.
     * @param tree
     * 
     * @return <code>Token</code> (possibly <code>null</code>)
     */
    public static Token findFirstTokenIn(AbstractParseTreeNode tree, final Terminal tokenType)
    {
        if (tree == null) return null;

        try
        {
            tree.visitUsing(new GenericParseTreeVisitor()
            {
                public void visitToken(Token token)
                {
                    if (token.getTerminal() == tokenType)
                        throw new VisitorTokenNotification(token);
                }
            });
        }
        catch (VisitorTokenNotification tokenFound)
        {
            return tokenFound.getNotificationToken();
        }

        return null;
    }

    /**
     * Returns the last token of the given type in the given tree, or <code>null</code> if none
     * could be found.
     * @param tree
     * 
     * @return <code>Token</code> (possibly <code>null</code>)
     */
    public static Token findLastTokenIn(AbstractParseTreeNode tree, final Terminal tokenType)
    {
        if (tree == null) return null;

        class TokenMemorizer extends GenericParseTreeVisitor
        {
            private Token lastToken = null;

            public Token getLastToken()
            {
                return lastToken;
            }

            public void visitToken(Token token)
            {
                if (token.getTerminal() == tokenType) lastToken = token;
            }
        }

        TokenMemorizer mem = new TokenMemorizer();
        tree.visitUsing(mem);
        return mem.getLastToken();
    }

    /**
     * Returns the last node in the given tree corresponding to the given nonterminal, or
     * <code>null</code> if none could be found.
     * @param tree
     * 
     * @return <code>ParseTreeNode</code> (possibly <code>null</code>)
     */
    public static ParseTreeNode findLastNodeIn(AbstractParseTreeNode tree, Nonterminal nodeType)
    {
        if (tree == null) return null;

        class NodeMemorizer extends GenericParseTreeVisitor
        {
            private ParseTreeNode lastNode = null;

            public ParseTreeNode getLastNode()
            {
                return lastNode;
            }

            public void visitParseTreeNode(ParseTreeNode node)
            {
                lastNode = node;
            }
        }

        NodeMemorizer mem = new NodeMemorizer();
        tree.visitUsing(mem);
        return mem.getLastNode();
    }

    /**
     * Returns true iff the given parse tree node has an <i>immediate</i> child of the given type.
     * 
     * @param node
     * @param targetTokenType
     * @return <code>boolean</code>
     */
    public static boolean containsImmediateChild(ParseTreeNode node, Terminal targetTokenType)
    {
        if (node == null) return false;

        for (Iterator it = node.getChildren().iterator(); it.hasNext();)
        {
            AbstractParseTreeNode child = (AbstractParseTreeNode)it.next();
            if (child instanceof Token)
            {
                Token token = (Token)child;
                if (token.getTerminal() == targetTokenType) return true;
            }
        }

        return false;
    }

    /**
     * Returns the identifier in the given tree occurring around the the given line and offset, or
     * <code>null</code> if none could be found.
     * @param tree
     * @param line
     * @param col
     * 
     * @return <code>ParseTreeNode</code> (possibly <code>null</code>)
     */
    public static Token findIdentifierAt(AbstractParseTreeNode tree, final int line, final int col)
    {
        if (tree == null) return null;

        try
        {
            tree.visitUsing(new GenericParseTreeVisitor()
            {
                public void visitToken(Token token)
                {
                    if (token.getTerminal() == Terminal.T_IDENT && tokenCovers(token, line, col))
                        throw new VisitorTokenNotification(token); // found
                    else if (tokenIsAfter(token, line, col))
                        throw new VisitorTokenNotification(null); // already passed that position;
                    // won't find it
                }

                private boolean tokenCovers(Token token, int line, int col)
                {
                    return tokenCoversColumnOnLine(token, col, line);
                }

                private boolean tokenCoversColumnOnLine(Token token, int col, int line)
                {
                    if (!tokenCoversLine(token, line)) return false;

                    int startColOnLine = token.getStartLine() == line ? token.getStartCol() : 1;
                    int endColOnLine = token.getEndLine() == line ? token.getEndCol()
                                                                 : Integer.MAX_VALUE;

                    return col >= startColOnLine && col <= endColOnLine;
                }

                private boolean tokenCoversLine(Token token, int line)
                {
                    return token.getStartLine() <= line && token.getEndLine() >= line;
                }

                private boolean tokenIsAfter(Token token, int line, int col)
                {
                    return token.getStartLine() > line || token.getStartLine() == line
                        && token.getStartCol() > col;
                }
            });
        }
        catch (VisitorTokenNotification n)
        {
            return n.getNotificationToken();
        }

        return null;
    }

    /**
     * Returns the parent of the given <code>childNode</code> in the given <code>tree</code>,
     * or <code>null</code> if one of the following holds:
     * <ol>
     * <li> the node is not in the tree,
     * <li> it is the root, which has no parent, or
     * <li> either parameter is <code>null</code>.
     * </ol>
     * @param tree
     * @param childNode
     * 
     * @return <code>Token</code> (possibly <code>null</code>)
     */
    public static ParseTreeNode findParent(AbstractParseTreeNode tree,
        final AbstractParseTreeNode childNode)
    {
        if (tree == null || childNode == null) return null;

        try
        {
            tree.visitUsing(new GenericParseTreeVisitor()
            {
                private ParseTreeNode currentParent = null;

                public void preparingToVisitChildrenOf(ParseTreeNode node)
                {
                    currentParent = node;
                }

                public void doneVisitingChildrenOf(ParseTreeNode node)
                {
                    currentParent = null;
                }

                public void visitParseTreeNode(ParseTreeNode node)
                {
                    if (node == childNode) throw new VisitorNodeNotification(currentParent);
                }
            });
        }
        catch (VisitorNodeNotification nodeFound)
        {
            return nodeFound.getNotificationNode();
        }

        return null;
    }

    /**
     * Returns the first token in the given tree with the given (starting) line number, or
     * <code>null</code> if none could be found.
     * 
     * @param targetLine
     * @param tree
     * @return <code>Token</code> (possibly <code>null</code>)
     */
    public static Token findFirstTokenOnLineIn(final int targetLine, AbstractParseTreeNode tree)
    {
        if (tree == null || targetLine < 1) return null;

        try
        {
            tree.visitUsing(new GenericParseTreeVisitor()
            {
                public void visitToken(Token token)
                {
                    int thisLine = token.getStartLine();

                    if (thisLine == targetLine)
                        throw new VisitorTokenNotification(token);
                    else if (thisLine > targetLine) throw new VisitorTokenNotification(null);
                }
            });
        }
        catch (VisitorTokenNotification tokenFound)
        {
            return tokenFound.getNotificationToken();
        }

        return null;
    }

    /**
     * Returns the first token in the given tree whose line number is
     * greater than the given value, or
     * <code>null</code> if none could be found.
     * 
     * @param targetLine
     * @param tree
     * @return <code>Token</code> (possibly <code>null</code>)
     */
    public static Token findFirstTokenAfterLineIn(final int targetLine, AbstractParseTreeNode tree)
    {
        if (tree == null) return null;

        try
        {
            tree.visitUsing(new GenericParseTreeVisitor()
            {
                public void visitToken(Token token)
                {
                    int thisLine = token.getStartLine();

                    if (thisLine > targetLine)
                        throw new VisitorTokenNotification(token);
                }
            });
        }
        catch (VisitorTokenNotification tokenFound)
        {
            return tokenFound.getNotificationToken();
        }

        return null;
    }

    /**
     * Returns true iff the given tree contains the given target (<code>Token</code> or
     * <code>ParseTreeNode</code>), according to an identity comparison.
     * 
     * @return <code>boolean</code>
     */
    public static boolean contains(AbstractParseTreeNode treeToSearch, final AbstractParseTreeNode target)
    {
        if (treeToSearch == null || target == null) return false;

        try
        {
            treeToSearch.visitUsing(new GenericParseTreeVisitor()
            {
                public void visitParseTreeNode(ParseTreeNode node)
                {
                    if (node == target)
                        throw new VisitorNodeNotification(node);
                }

                public void visitToken(Token token)
                {
                    if (token == target)
                        throw new VisitorTokenNotification(token);
                }
            });
        }
        catch (VisitorNodeNotification nodeFound)
        {
            return true;
        }
        catch (VisitorTokenNotification tokenFound)
        {
            return true;
        }

        return false;
    }

    /**
     * @param treeToSearch
     * @param target
     * @return
     */
    public static Token findLastTokenPreceding(AbstractParseTreeNode treeToSearch, final ParseTreeNode target)
    {
        if (treeToSearch == null || target == null) return null;
        
        if (!ParseTreeSearcher.contains(treeToSearch, target))
            throw new Error("findLastTokenPreceding: Target node not found in tree");

        class TokenMemorizer extends GenericParseTreeVisitor
        {
            private Token lastToken = null;

            public Token getLastToken()
            {
                return lastToken;
            }

            public void visitParseTreeNode(ParseTreeNode node)
            {
                if (node == target)
                    throw new VisitorNodeNotification(node);
            }

            public void visitToken(Token token)
            {
                lastToken = token;
            }
        }

        TokenMemorizer mem = new TokenMemorizer();
        try
        {
            treeToSearch.visitUsing(mem);
        }
        catch (VisitorNodeNotification e)
        {
            return mem.getLastToken();
        }
        
        return null;
    }

    public static Token findFirstTokenFollowing(ParseTreeNode treeToSearch, final ParseTreeNode target)
    {
        if (treeToSearch == null || target == null) return null;
        
        if (!ParseTreeSearcher.contains(treeToSearch, target))
            throw new Error("findFirstTokenFollowing: Target node not found in tree");

        class TokenMemorizer extends GenericParseTreeVisitor
        {
            private boolean shouldSave = false;

            public void doneVisitingChildrenOf(ParseTreeNode node)
            {
                if (node == target)
                    shouldSave = true;
            }

            public void visitToken(Token thisToken)
            {
                if (shouldSave)
                    throw new VisitorTokenNotification(thisToken);
            }
        }

        TokenMemorizer mem = new TokenMemorizer();
        try
        {
            treeToSearch.visitUsing(mem);
        }
        catch (VisitorTokenNotification e)
        {
            return e.getNotificationToken();
        }
        
        return null;
    }
}
