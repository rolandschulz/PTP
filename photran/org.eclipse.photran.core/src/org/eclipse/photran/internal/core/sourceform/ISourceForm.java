/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.sourceform;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.resources.IFile;

/**
 * Interface implemented by contributions to the {@value SourceForm#SOURCE_FORM_EXTENSION_POINT_ID}
 * extension point.
 * 
 * @author Jeff Overbey
 */
public interface ISourceForm
{
    /**
     * Creates a lexical analyzer for this source form.
     * <p>
     * <b>Important.</b> The parameters methods to this method are <i>not</i> redundant; they have a
     * somewhat confusing relationship that must be obeyed by the lexical analyzer that is created:
     * <ol>
     * <li><i>The lexer must ALWAYS read from the supplied {@link Reader} <code>in</code>.</i>
     * When a file is open in an editor but the editor has unsaved changes, the unsaved changes
     * should be parsed, not the file on disk, so that the Outline view appears correct. The current
     * editor contents are passed as the Reader. Still, the underlying {@link IFile} is made
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
    <T> T createLexer(Reader in, IFile file, String filename, boolean accumulateWhitetext) throws IOException;
    
    /**
     * Configures this source form.
     * <p>
     * This is used to configure Fortran's preprocessed source form with an
     * <code>IncludeLoaderCallback</code>, although other source forms may
     * use it however they wish.
     * <p>
     * Implementations should generally return <code>this</code>.
     * 
     * @param data an object
     * 
     * @return <code>this</code>
     */
    ISourceForm configuredWith(Object data);

    /**
     * @return true iff this source form represents (some variant of) fixed source form
     */
    boolean isFixedForm();

    /**
     * @return true iff this source form allows (and processes) C preprocessor directives
     */
    boolean isCPreprocessed();
}
