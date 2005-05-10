package org.eclipse.fdt.internal.ui.text;

import org.eclipse.jface.text.rules.IToken;

import org.eclipse.cdt.internal.ui.text.SingleCharRule;

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
     * @see org.eclipse.cdt.internal.ui.text.SingleCharRule#isRuleChar(int)
     */
    protected boolean isRuleChar(int ch)
    {
        return ch == '{' || ch == '}' || ch == '[' || ch == ']' || ch == '(' || ch == ')';
    }

    
}
