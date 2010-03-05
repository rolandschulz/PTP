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
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.PreprocessingFreeFormLexerPhase1;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * Contains constants enumerating the various Fortran source forms (fixed form, free form, free
 * form with C preprocessor directives, etc.).
 * <p>
 * Internally, this class is used as a Strategy object in {@link LexerFactory}.
 * 
 * @author Jeff Overbey
 */
public abstract class SourceForm
{
    protected SourceForm() {;}

    /**
     * Creates a lexical analyzer.
     * <p>
     * <b>Important.</b> The parameters methods to this method are <i>not</i> redundant; they have a
     * somewhat confusing relationship that must be obeyed by the lexical analyzer that is created:
     * <ol>
     * <li><i>The lexer must ALWAYS read from the supplied {@link InputStream} <code>in</code>.</i>
     * When a file is open in an editor but the editor has unsaved changes, the unsaved changes
     * should be parsed, not the file on disk, so that the Outline view appears correct. The current
     * editor contents are passed as the input stream. Still, the underlying {@link IFile} is made
     * available so that it can be used to determine the include paths used by the C preprocessor.
     * 
     * <li><i>If the {@link IFile} <code>file</code> is not null, it should be used to determine the
     * path to the file;</i> in this case the (String) <code>filename</code> is used only in error
     * messages and likely does NOT represent a valid filesystem path.
     * 
     * <li><i>If the {@link IFile} <code>file</code> is null, then we are parsing a file that is not
     * available in the workspace...</i>
     *     <ol>
     *     <li>If possible, the (String) filename should provide a full path to a file on the local
     *     filesystem; this will be used as the working directory when the C preprocessor processes
     *     #include directives.
     * 
     *     <li>If the filename does not point to a valid path on the local filesystem, #include
     *     directives will not be processed.
     *     </ol>
     * </ol>
     */
    public abstract IAccumulatingLexer createLexer(InputStream in, IFile file, String filename, boolean accumulateWhitetext) throws IOException;
    
    public abstract String getDescription(String filename);
    
    public static final SourceForm UNPREPROCESSED_FREE_FORM = new SourceForm()
    {
        @Override public IAccumulatingLexer createLexer(InputStream in, IFile file, String filename, boolean accumulateWhitetext) throws IOException
        {
            return new LexerPhase3(new FreeFormLexerPhase2(new FreeFormLexerPhase1(in, file, filename, ASTTokenFactory.getInstance(), accumulateWhitetext)));
        }
        
        @Override public String getDescription(String filename) { return "Free Form"; }
    };
    
    public static final SourceForm FIXED_FORM = new SourceForm()
    {
        @Override public IAccumulatingLexer createLexer(InputStream in, IFile file, String filename, boolean accumulateWhitetext) throws IOException
        {
            return new LexerPhase3(new FixedFormLexerPhase2(in, file, filename, ASTTokenFactory.getInstance()));
        }
        
        @Override public String getDescription(String filename) { return "Fixed Form"; }
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
            
            @Override public String getDescription(String filename)
            {
                SourceForm sf = findPreferredSourceForm(filename);
                if (sf != null)
                    return sf.getDescription(filename);
                else
                    return "Free Form";
            }
        };
    }

    private static SourceForm findPreferredSourceForm(String filename)
    {
        try
        {
            IContentType ct = FortranCorePlugin.findContentType(filename);
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

    
}
