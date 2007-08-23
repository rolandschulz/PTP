/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

import java.io.IOException;
import java.io.InputStream;

/**
 * Contains constants enumerating the various Fortran source forms.
 * <p>
 * Internally, this class is used as a Strategy object in <code>LexerFactory</code>.
 * 
 * @author Jeff Overbey
 */
public abstract class SourceForm
{
    private SourceForm() {;}
    
    abstract IAccumulatingLexer createLexer(InputStream in, String filename) throws IOException;
    
    public static final SourceForm UNPREPROCESSED_FREE_FORM = new SourceForm()
    {
        IAccumulatingLexer createLexer(InputStream in, String filename) throws IOException
        {
            return new LexerPhase3(new FreeFormLexerPhase2(new FreeFormLexerPhase1(in, filename, ASTTokenFactory.getInstance())));
        }
    };
    
    public static final SourceForm FIXED_FORM = new SourceForm()
    {
        IAccumulatingLexer createLexer(InputStream in, String filename) throws IOException
        {
            return new LexerPhase3(new FixedFormLexerPhase2(in, filename, ASTTokenFactory.getInstance()));
        }
    };
    
    // FIXME: JEFF: Automatically detect lexer type from filename extension
    public static final SourceForm AUTO_DETECT_SOURCE_FORM = UNPREPROCESSED_FREE_FORM;

    public static SourceForm preprocessedFreeForm(final IncludeLoaderCallback callback)
    {
        return new SourceForm()
        {
            IAccumulatingLexer createLexer(InputStream in, String filename) throws IOException
            {
                return new LexerPhase3(new FreeFormLexerPhase2(new PreprocessingFreeFormLexerPhase1(in, filename, callback)));
            }
        };
    }
}
