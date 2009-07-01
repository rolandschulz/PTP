/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.cdt.internal.ui.text.CCompositeReconcilingStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.photran.internal.ui.editor.IFortranSourceViewerConfigurationFactory;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor.FortranSourceViewerConfiguration;
import org.eclipse.photran.internal.ui.editor_vpg.contentassist.FortranCompletionProcessor;
import org.eclipse.photran.internal.ui.editor_vpg.folding.FortranFoldingProvider;
import org.eclipse.photran.internal.ui.editor_vpg.hover.FortranDeclarationHover;

/**
 * Factory providing a <code>SourceViewerConfiguration</code> for the Fortran editors which supports
 * content assist, hover tips, and folding.
 * 
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public class FortranVPGSourceViewerConfigurationFactory implements IFortranSourceViewerConfigurationFactory
{
    public SourceViewerConfiguration create(final AbstractFortranEditor editor)
    {
        new FortranFoldingProvider().setup(editor);
        
        return new FortranSourceViewerConfiguration(editor)
        {
            private final FortranCompletionProcessor fortranCompletionProcessor = new FortranCompletionProcessor();
            
            @Override protected CCompositeReconcilingStrategy createReconcilingStrategy(ISourceViewer sourceViewer)
            {
                return new FortranVPGReconcilingStrategy(sourceViewer, editor, getConfiguredDocumentPartitioning(sourceViewer));
            }

            @Override public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
            {
                IContentAssistant result = fortranCompletionProcessor.setup((AbstractFortranEditor)editor);
                return result == null ? super.getContentAssistant(sourceViewer) : result;
            }

            @Override public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
            {
                return new FortranDeclarationHover(sourceViewer, (AbstractFortranEditor)editor);
            }
        };
    }
}
