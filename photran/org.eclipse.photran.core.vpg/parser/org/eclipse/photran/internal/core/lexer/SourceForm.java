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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.PreprocessingFreeFormLexerPhase1;

/**
 * Contains constants enumerating the various Fortran source forms.
 * <p>
 * Internally, this class is used as a Strategy object in <code>LexerFactory</code>.
 * 
 * @author Jeff Overbey
 */
public abstract class SourceForm
{
    protected SourceForm() {;}
    
    public abstract IAccumulatingLexer createLexer(InputStream in, IFile file, String filename, boolean accumulateWhitetext) throws IOException;
    
    public static final SourceForm UNPREPROCESSED_FREE_FORM = new SourceForm()
    {
        @Override public IAccumulatingLexer createLexer(InputStream in, IFile file, String filename, boolean accumulateWhitetext) throws IOException
        {
            return new LexerPhase3(new FreeFormLexerPhase2(new FreeFormLexerPhase1(in, file, filename, ASTTokenFactory.getInstance(), accumulateWhitetext)));
        }
    };
    
    public static final SourceForm FIXED_FORM = new SourceForm()
    {
        @Override public IAccumulatingLexer createLexer(InputStream in, IFile file, String filename, boolean accumulateWhitetext) throws IOException
        {
            return new LexerPhase3(new FixedFormLexerPhase2(in, file, filename, ASTTokenFactory.getInstance()));
        }
    };
    
    // TODO: JEFF: Automatically detect lexer type from filename extension
    public static final SourceForm AUTO_DETECT_SOURCE_FORM = UNPREPROCESSED_FREE_FORM;

    public static final String SOURCE_FORM_EXTENSION_POINT_ID = "org.eclipse.photran.core.vpg.sourceform";
    public static SourceForm preprocessedFreeForm(final IncludeLoaderCallback callback)
    {
        IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(SOURCE_FORM_EXTENSION_POINT_ID);
        if (configs.length > 0)
        {
            int index = findPreferredSourceForm(configs);
            try { return (SourceForm)configs[index].createExecutableExtension("class"); }
            catch (CoreException e) { e.printStackTrace(); }
        }
            
        return new SourceForm()
        {
            @Override public IAccumulatingLexer createLexer(InputStream in, IFile file, String filename, boolean accumulateWhitetext) throws IOException
            {
                return new LexerPhase3(new FreeFormLexerPhase2(new PreprocessingFreeFormLexerPhase1(in, file, filename, callback, accumulateWhitetext)));
            }
        };
    }

    private static int findPreferredSourceForm(IConfigurationElement[] configs)
    {
//        for (int i = 0; i < configs.length; i++)
//            if (configs[i].getAttribute("id").equals(FortranPreferences.PREFERRED_MODEL_BUILDER.getValue()))
//                return i;
        return 0;
    }
}
