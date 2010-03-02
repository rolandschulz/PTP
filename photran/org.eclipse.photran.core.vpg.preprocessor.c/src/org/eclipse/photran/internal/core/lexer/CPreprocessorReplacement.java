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
 * An {@link IPreprocessorReplacement} for a C preprocessor directive.
 * 
 * @author Jeff Overbey
 */
public class CPreprocessorReplacement implements IPreprocessorReplacement
{
    public static CPreprocessorReplacement createFor(String string)
    {
        if (string == null)
            return null;
        else
            return new CPreprocessorReplacement(string);
    }
    
    private String contents;
    
    private CPreprocessorReplacement(String contents)
    {
        this.contents = contents;
    }
    
    @Override public String toString()
    {
        return contents;
    }
}
