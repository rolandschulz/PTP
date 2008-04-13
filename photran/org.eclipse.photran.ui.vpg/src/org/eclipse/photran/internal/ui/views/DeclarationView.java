
package org.eclipse.photran.internal.ui.views;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.photran.internal.ui.editor.FortranKeywordRuleBasedScanner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * Implements Photran's Declaration view
 * 
 * @author John Goode, Abe Hassan, Sean Kim
 *  Group: Fennel-Garlic
 *  University of Illinois at Urbana-Champaign 
 *  CS 427 Fall 2007
 * 
 * @author Jeff Overbey - modified to use MVC pattern
 */
public class DeclarationView extends ViewPart implements ISelectionListener, ISelectionChangedListener
{
    private AbstractFortranEditor activeEditor = null;
    
    private SourceViewer viewer = null;
    private Document document = new Document();

    private Color LIGHT_YELLOW = new Color(null, new RGB(255, 255, 191));

    /*
     * The content provider class is responsible for
     * providing objects to the view. It can wrap
     * existing objects in adapters or simply return
     * objects as-is. These objects may be sensitive
     * to the current input of the view, or ignore
     * it and always show the same content 
     * (like Task List, for example).
     */

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent)
    {
        this.viewer = createFortranSourceViewer(parent);

        // Add this view as a selection listener to the workbench page
        getSite().getPage().addSelectionListener(this);
        
        // Update the selection immediately
        try
        {
            IWorkbenchPage activePage = getSite().getWorkbenchWindow().getActivePage();
            selectionChanged(activePage.getActivePart(),
                             activePage.getSelection());
        }
        catch (Throwable e) // NullPointerException, etc.
        {
            ;
        }
    }

    private SourceViewer createFortranSourceViewer(Composite parent)
    {
        final SourceViewer viewer = new SourceViewer(parent, null, SWT.NONE); //TextViewer(parent, SWT.NONE);
        final String[] partitionTypes = new String[] { IDocument.DEFAULT_CONTENT_TYPE };
        viewer.configure(new SourceViewerConfiguration()
        {
            public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
            {
                return partitionTypes;
            }

            public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
            {
                PresentationReconciler reconciler = new PresentationReconciler();
        
                DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new FortranKeywordRuleBasedScanner(false, viewer));
                reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
                reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
       
                return reconciler;
            }
        });
        viewer.setDocument(document);
        IDocumentPartitioner partitioner = new FastPartitioner(new RuleBasedPartitionScanner(), partitionTypes);
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
        
        viewer.getControl().setBackground(LIGHT_YELLOW);
        viewer.setEditable(false);
        viewer.getTextWidget().setFont(JFaceResources.getTextFont());
        
        return viewer;
    }

    /**
     * This is how the view knows when some selected a new word in the editor.
     * Reads the file and displays the contents of the file.
     * 
     */
    public void selectionChanged(IWorkbenchPart part, ISelection selection)
    {
        if (part instanceof AbstractFortranEditor)
        {
            if (activeEditor != part)
            {
                // Observe new editor
                removeCaretListenerFrom(activeEditor);
                activeEditor = (AbstractFortranEditor)part;
                addCaretListenerTo(activeEditor);
            }
            else
            {
                // Leave everything as-is
            }
        }
        else
        {
            // Observe nothing
            removeCaretListenerFrom(activeEditor);
            activeEditor = null;
        }
    }

    /**
     * Registers this view to receive notifications of caret movement in <code>editor</code>
     * 
     * See http://dev.eclipse.org/mhonarc/newsLists/news.eclipse.platform/msg44602.html
     */
    private void addCaretListenerTo(AbstractFortranEditor editor)
    {
        if (editor != null)
        {
            TextViewer sourceViewer = (TextViewer)editor.getSourceViewerx();
            if (sourceViewer != null)
                sourceViewer.addPostSelectionChangedListener(this);
        }
    }

    /**
     * Unregisters this view to receive notifications of caret movement in <code>editor</code>
     */
    private void removeCaretListenerFrom(AbstractFortranEditor editor)
    {
        if (editor != null)
        {
            TextViewer sourceViewer = (TextViewer)editor.getSourceViewerx();
            if (sourceViewer != null)
                sourceViewer.removePostSelectionChangedListener(this);
        }
    }

    /**
     * Update document by displaying the new text
     */
    public void update(String str)
    {
        document.set(str);
        viewer.refresh();
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus()
    {
        viewer.getControl().setFocus();
    }

    /**
     * Callback used when the caret is moved in the editor
     */
    public void selectionChanged(SelectionChangedEvent event)
    {
        if (event.getSelection() instanceof TextSelection)
        {
            TextSelection selection = (TextSelection)event.getSelection();
            update("! This is some fortran at index " + selection.getOffset() + "\n" + selection.getText());
        }
        else
        {
            update("");
        }
    }
}