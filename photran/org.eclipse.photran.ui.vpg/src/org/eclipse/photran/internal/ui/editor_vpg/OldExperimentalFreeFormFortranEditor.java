package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.cdt.internal.ui.text.CReconciler;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.photran.internal.ui.editor.FreeFormFortranEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class OldExperimentalFreeFormFortranEditor extends FreeFormFortranEditor
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Custom Reconciler Support
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override protected SourceViewerConfiguration createSourceViewerConfiguration()
    {
        return new VPGFortranSourceViewerConfiguration();
    }
    
    protected class VPGFortranSourceViewerConfiguration extends FortranSourceViewerConfiguration
    {
        /*
         * The CReconciler is used to ensure that an ElementChangedEvent is fired.
         * Without this, the Outline view says "Pending..." but never populates.
         * 
         * From Anton Leherbaurer (cdt-dev, 8/16/07):
         *     The outline view waits for the initial reconciler to run and it requires
         *     an ElementChangedEvent when it is done to populate the view.
         *     See CContentOutlinerProvider$ElementChangedListener#elementChanged().
         *     The event should usually be issued from the
         *     ReconcileWorkingCopyOperation.
         */
        public IReconciler getReconciler(ISourceViewer sourceViewer)
        {
            MonoReconciler reconciler = new CReconciler(OldExperimentalFreeFormFortranEditor.this,
                                                        new FortranVPGReconcilingStrategy(OldExperimentalFreeFormFortranEditor.this));
            reconciler.setIsIncrementalReconciler(false);
            reconciler.setProgressMonitor(new NullProgressMonitor());
            reconciler.setDelay(500);
            return reconciler;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // EXPERIMENTAL Watermark
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);

        ISourceViewer sourceViewer = getSourceViewer();
        if (sourceViewer instanceof ITextViewerExtension2)
        {
            ITextViewerExtension2 painter = (ITextViewerExtension2)sourceViewer;
            
            painter.addPainter(new IPainter()
            {
                private boolean active = false;
                private StyledText widget = null;
                private PaintListener listener = null;

                public void paint(int reason)
                {
                    if (!active)
                    {
                        active = true;
                        widget = OldExperimentalFreeFormFortranEditor.this.getSourceViewer().getTextWidget();
                        final Font font = new Font(null, new FontData("Arial", 36, SWT.NORMAL));
                        final Color lightGray = new Color(null, new RGB(192, 192, 192));
                        listener = new PaintListener()
                        {
                            public void paintControl(PaintEvent e)
                            {
                                if (widget == null) return;
                                
                                Rectangle area = widget.getClientArea();
                                e.gc.setFont(font);
                                e.gc.setForeground(lightGray);
                                e.gc.drawString("EXPERIMENTAL", area.x + area.width/2, area.y, true);
                            }
                        };
                        widget.addPaintListener(listener);
                    }
                }
                
                public void dispose()
                {
                    if (listener != null)
                    {
                        widget.removePaintListener(listener);
                        listener = null;
                    }
                    
                    widget = null;
                }

                public void deactivate(boolean redraw) {}
                public void setPositionManager(IPaintPositionManager manager) {}
            });
        }
    }
}
