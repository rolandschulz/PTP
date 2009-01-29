package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.cdt.internal.ui.text.CReconciler;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.photran.internal.ui.editor.IFortranSourceViewerConfigurationFactory;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor.FortranModelReconcilingSourceViewerConfiguration;
import org.eclipse.photran.internal.ui.editor_vpg.contentassist.FortranCompletionProcessor;
import org.eclipse.photran.internal.ui.editor_vpg.hover.FortranDeclarationHover;

@SuppressWarnings("restriction")
public class FortranVPGSourceViewerConfigurationFactory implements IFortranSourceViewerConfigurationFactory
{
    public SourceViewerConfiguration create(final AbstractFortranEditor editor)
    {
        return new FortranModelReconcilingSourceViewerConfiguration(editor)
        {
            private final FortranCompletionProcessor fortranCompletionProcessor =
                new FortranCompletionProcessor();
            
            @Override public IReconciler getReconciler(ISourceViewer sourceViewer)
            {
                MonoReconciler r = new CReconciler(editor,
                        new FortranVPGReconcilingStrategy(sourceViewer, editor, getConfiguredDocumentPartitioning(sourceViewer)));
                r.setIsIncrementalReconciler(false);
                r.setProgressMonitor(new NullProgressMonitor());
                r.setDelay(500);
                return r;
            }

            @Override public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
            {
                IContentAssistant result = fortranCompletionProcessor.setup(editor);
                return result == null ? super.getContentAssistant(sourceViewer) : result;
            }

            @Override public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
            {
                return new FortranDeclarationHover(sourceViewer, editor);
            }
        };
    }
}
