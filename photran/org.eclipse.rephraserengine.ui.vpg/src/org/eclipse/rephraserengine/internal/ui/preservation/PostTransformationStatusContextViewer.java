/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.ui.preservation;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.internal.ui.refactoring.FileStatusContextViewer;
import org.eclipse.ltk.ui.refactoring.TextStatusContextViewer;
import org.eclipse.rephraserengine.core.preservation.PostTransformationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Based on {@link FileStatusContextViewer}
 *
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public class PostTransformationStatusContextViewer extends TextStatusContextViewer
{

    public void createControl(Composite parent)
    {
        super.createControl(parent);
        getSourceViewer().configure(new SourceViewerConfiguration());
    }

    public void setInput(RefactoringStatusContext context)
    {
        PostTransformationContext fc = (PostTransformationContext)context;
        updateTitle(fc.getFile());
        IDocument document = new Document(fc.getFileContents());
        IRegion region = fc.getTextRegion();
        if (region != null && document.getLength() >= region.getOffset() + region.getLength())
            setInput(document, region);
        else
            setInput(document, new Region(0, 0));
    }

    protected SourceViewer createSourceViewer(Composite parent)
    {
        return new SourceViewer(parent, null,
            SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
    }
}
