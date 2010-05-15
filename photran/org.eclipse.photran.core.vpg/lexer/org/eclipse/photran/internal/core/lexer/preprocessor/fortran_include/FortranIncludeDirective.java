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
package org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include;

import org.eclipse.photran.internal.core.lexer.IPreprocessorReplacement;
import org.eclipse.photran.internal.core.lexer.IToken;

/**
 * {@link IPreprocessorReplacement} for tokens resulting from expansion of a Fortran INCLUDE line.
 * 
 * @author Jeff Overbey
 * 
 * @see IToken#getPreprocessorDirective()
 */
public class FortranIncludeDirective implements IPreprocessorReplacement
{
    protected String replacementText;
    
    public FortranIncludeDirective(String replacementText)
    {
        this.replacementText = replacementText;
    }
    
    public String toString()
    {
        return replacementText;
    }
}
