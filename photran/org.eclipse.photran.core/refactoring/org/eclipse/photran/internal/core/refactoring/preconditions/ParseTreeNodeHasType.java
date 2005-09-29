package org.eclipse.photran.internal.core.refactoring.preconditions;

import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;

/**
 * Precondition specifying that a <code>ParseTreeNode</code> corresponds
 * to a particular nonterminal in the grammar.
 * 
 * @author spiros
 * @author joverbey
 */
public class ParseTreeNodeHasType extends AbstractPrecondition
{
    ParseTreeNode node;
    Nonterminal nonterminal;
    
    public ParseTreeNodeHasType(ParseTreeNode node, Nonterminal nonterminal)
    {
        this.node = node;
        this.nonterminal = nonterminal;
    }

    protected boolean checkThisPrecondition()
    {
        if (node.getRootNonterminal() != nonterminal)
        {
            error = "Node is not a "
                + node.getRootNonterminal().getDescription()
                + ", not a " + nonterminal.getDescription();
            return false;
        }

        return true;
    }
}
