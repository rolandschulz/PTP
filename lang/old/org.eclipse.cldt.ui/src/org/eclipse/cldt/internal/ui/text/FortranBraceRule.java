package org.eclipse.cldt.internal.ui.text;

import org.eclipse.jface.text.rules.IToken;

/**
 * Braces rule.
 *
 * @author P.Tomaszewski
 */
public class FortranBraceRule extends SingleCharRule
{

    /**
     * Creates new rule. 
     * @param token Style token.
     */
    public FortranBraceRule(IToken token)
    {
        super(token);
    }

    /**
     * @see org.eclipse.cldt.internal.ui.text.SingleCharRule#isRuleChar(int)
     */
    protected boolean isRuleChar(int ch)
    {
        return ch == '{' || ch == '}' || ch == '[' || ch == ']' || ch == '(' || ch == ')';
    }

    
}
