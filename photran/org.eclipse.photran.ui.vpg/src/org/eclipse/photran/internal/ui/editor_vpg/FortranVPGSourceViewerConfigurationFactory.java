package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.cdt.internal.ui.text.CReconciler;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.photran.internal.ui.editor.IFortranSourceViewerConfigurationFactory;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor.FortranModelReconcilingSourceViewerConfiguration;
import org.eclipse.photran.internal.ui.editor_vpg.contentassist.FortranCompletionProcessor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class FortranVPGSourceViewerConfigurationFactory implements IFortranSourceViewerConfigurationFactory
{
    public SourceViewerConfiguration create(final AbstractFortranEditor editor)
    {
        return new FortranModelReconcilingSourceViewerConfiguration(editor)
        {
            private final FortranCompletionProcessor fortranCompletionProcessor =
                new FortranCompletionProcessor();
            
            @Override protected IReconciler loadReconciler()
            {
                return new CReconciler(editor, new FortranVPGReconcilingStrategy(editor));
            }

            @Override public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
            {
                return fortranCompletionProcessor.setup(editor);
            }
        };
    }
}
