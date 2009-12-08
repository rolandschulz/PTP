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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
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
        return new SourceForm()
        {
            @Override public IAccumulatingLexer createLexer(InputStream in, IFile file, String filename, boolean accumulateWhitetext) throws IOException
            {
                SourceForm sf = findPreferredSourceForm(filename);
                if (sf != null)
                    return sf.createLexer(in, file, filename, accumulateWhitetext);
                else
                    return new LexerPhase3(
                        new FreeFormLexerPhase2(
                            new PreprocessingFreeFormLexerPhase1(
                                in,
                                file,
                                filename,
                                callback,
                                accumulateWhitetext)));
            }
        };
    }

    private static SourceForm findPreferredSourceForm(String filename)
    {
        try
        {
            IContentType ct = findContentType(filename);
            if (ct == null) return null;
            
            IConfigurationElement[] configs =
                Platform.getExtensionRegistry().getConfigurationElementsFor(
                    SOURCE_FORM_EXTENSION_POINT_ID);

            for (int i = 0; i < configs.length; i++)
                if (configs[i].getAttribute("contentType").equals(ct.getId()))
                    return (SourceForm)configs[i].createExecutableExtension("class");
    
            return null;
        }
        catch (CoreException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static IContentType findContentType(String filename)
    {
        IContentType[] cts = Platform.getContentTypeManager().findContentTypesFor(filename);
        if (cts.length == 0)
            return null;
        else if (cts.length == 1)
            return cts[0];
        
        // Annoyingly, Eclipse does not do case-sensitive matching of filename
        // extensions (at least on case-insensitive filesystems), which is
        // important for Fortran filenames; we have to do that manually
        
        List<IContentType> possibilities = new ArrayList<IContentType>(cts.length);
        
        String ext = filename.substring(filename.lastIndexOf('.')+1);
        for (IContentType ct : cts)
            if (getFilenameExtensions(ct.getId()).contains(ext))
                possibilities.add(ct);

        if (possibilities.isEmpty()) return cts[0];

        // Now find the most specific of the possible content types
        
        IContentType result = null;
        for (IContentType ct : possibilities)
        {
            if (result == null)
                result = ct;
            else if (ct.isKindOf(result))
                result = ct;
        }
        return result;
    }
    
    private static Set<String> getFilenameExtensions(String contentType)
    {
        for (IConfigurationElement elt :
                 Platform.getExtensionRegistry().getConfigurationElementsFor(
                     "org.eclipse.core.contenttype.contentTypes"))
        {
            if (elt.getName().equals("file-association")
                && elt.getAttribute("content-type").equals(contentType))
            {
                Set<String> result = new HashSet<String>();
                String fileExts = elt.getAttribute("file-extensions");
                for (String ext : fileExts.split(","))
                    result.add(ext.trim());
                return result;
            }
        }
        return Collections.emptySet();
    }
}
