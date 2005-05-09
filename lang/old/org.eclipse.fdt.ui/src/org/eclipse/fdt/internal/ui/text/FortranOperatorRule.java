package org.eclipse.fdt.internal.ui.text;

import org.eclipse.jface.text.rules.IToken;

import org.eclipse.cdt.internal.ui.text.SingleCharRule;

/**
 * Rule to recognize operators.
 *
 * @author P.Tomaszewski
 */
public class FortranOperatorRule extends SingleCharRule
{
    /**
     * Creates new rule.
     * @param token Style token.
     */
    public FortranOperatorRule(IToken token)
    {
        super(token);
    }

    /**
     * @see org.eclipse.cdt.internal.ui.text.SingleCharRule#isRuleChar(int)
     */
    public boolean isRuleChar(int ch)
    {
        return (ch == '=' || ch == '-'
            || ch == '+' || ch == '/' 
            || ch == '>' || ch == '<'
            || ch == '*');
    }
}
