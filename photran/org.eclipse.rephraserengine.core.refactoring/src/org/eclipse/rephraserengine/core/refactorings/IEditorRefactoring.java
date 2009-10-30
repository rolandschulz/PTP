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
package org.eclipse.rephraserengine.core.refactorings;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;

/**
 * A refactoring that can be applied when the user has a file open in a text/code editor.
 * <p>
 * These refactorings will be available only when the user has a file open in a text/code editor.
 * They are provided with the current selection in the editor, the assumption being that what the
 * refactoring does will depend on what the user has selected.
 * <p>
 * Contrast this with an {@link IResourceRefactoring}, which is available from the editor but is
 * also available when the user has selected one or more resources in the Navigator view; these
 * refactorings may be applied to several resources at once, and an editor selection may not be
 * available; thus, an {@link IResourceRefactoring} is expected to operate on the entire file,
 * oblivious to the current selection.
 *
 * @author Jeff Overbey
 *
 * @see IResourceRefactoring
 * @see IRefactoring
 */
public interface IEditorRefactoring extends IRefactoring
{
    /**
     * This method should be invoked immediately after the constructor is called.
     * @param file the file currently open in the editor (non-<code>null</code>)
     * @param selection the current selection in the editor (non-<code>null</code>)
     */
    void initialize(IFile file, ITextSelection selection);
}
