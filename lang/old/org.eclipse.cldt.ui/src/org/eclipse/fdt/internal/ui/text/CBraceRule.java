package org.eclipse.fdt.internal.ui.text;

import org.eclipse.jface.text.rules.IToken;

/**
 * Braces rule.
 *
 * @author P.Tomaszewski
 */
public class CBraceRule extends SingleCharRule
{

    /**
     * Creates new rule. 
     * @param token Style token.
     */
    public CBraceRule(IToken token)
    {
        super(token);
    }

    /**
     * @see org.eclipse.fdt.internal.ui.text.SingleCharRule#isRuleChar(int)
     */
    protected boolean isRuleChar(int ch)
    {
        return ch == '{' || ch == '}' || ch == '[' || ch == ']' || ch == '(' || ch == ')';
    }

    
}
