/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

/**
 * {@link IPreprocessorReplacement} for tokens from fixed form source code that have spaces in them.
 * <p>
 * For example, <tt>in t E g eR</tt> is recognized as a token of type {@link Terminal#T_INTEGER}
 * with "intEgeR" as its token text ({@link IToken#getText()}) and "in t E g eR" as its preprocessor
 * directive ({@link IToken#getPreprocessorDirective()}).
 * <p>
 * Technically, this is an abuse of that field -- this isn't a situation resulting from a bona fide
 * preprocessor -- but it's analogous to, say, how trigraphs are handled in the C preprocessor.
 * 
 * @author Jeff Overbey
 * 
 * @see IToken#getPreprocessorDirective()
 */
public class FixedFormReplacement implements IPreprocessorReplacement
{
    protected String replacementText;
    
    public FixedFormReplacement(String replacementText)
    {
        this.replacementText = replacementText;
    }
    
    public String toString()
    {
        return replacementText;
    }
    
    public void setReplacementText(String newText)
    {
        replacementText = newText;
    }
}
