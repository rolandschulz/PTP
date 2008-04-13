
package org.eclipse.photran.internal.ui.views;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.types.DerivedType;
import org.eclipse.photran.internal.core.analysis.types.FunctionType;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.analysis.types.TypeProcessor;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranRefactoring;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.photran.internal.ui.editor.FortranKeywordRuleBasedScanner;
import org.eclipse.photran.internal.ui.editor_vpg.DefinitionMap;
import org.eclipse.photran.internal.ui.editor_vpg.FortranVPGReconcilingStrategy;
import org.eclipse.photran.internal.ui.editor_vpg.IFortranEditorASTTask;
import org.eclipse.photran.internal.ui.editor_vpg.IFortranEditorVPGTask;
import org.eclipse.photran.internal.ui.editor_vpg.FortranEditorVPGTasks;
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
 * @author Jeff Overbey - modified to use MVC pattern, SourceViewer, caret listener, DefinitionMap; based on code by...
 * @author John Goode, Abe Hassan, Sean Kim
 *  Group: Fennel-Garlic
 *  University of Illinois at Urbana-Champaign 
 *  CS 427 Fall 2007
 */
public class DeclarationView extends ViewPart
    implements ISelectionListener,
               ISelectionChangedListener,
               IFortranEditorVPGTask,
               IFortranEditorASTTask
{
    private AbstractFortranEditor activeEditor = null;
    private IFortranAST activeAST = null;
    private DefinitionMap<String> activeDefMap = null;
    
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
            if (activePage != null)
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
        final SourceViewer viewer = new SourceViewer(parent, null, SWT.V_SCROLL); //TextViewer(parent, SWT.NONE);
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
     * ISelectionListener - Callback notifying the view that a new workbench part has been selected.
     */
    public synchronized void selectionChanged(IWorkbenchPart part, ISelection selection)
    {
        if (part instanceof AbstractFortranEditor)
        {
            if (activeEditor != part)
            {
                // Observe new editor
                stopObserving(activeEditor);
                activeEditor = (AbstractFortranEditor)part;
                startObserving(activeEditor);
            }
            else
            {
                // Leave everything as-is
            }
        }
        else
        {
            // Observe nothing
            stopObserving(activeEditor);
            activeEditor = null;
        }
    }

    /**
     * Registers this view to receive notifications of caret movement in <code>editor</code>
     * 
     * See http://dev.eclipse.org/mhonarc/newsLists/news.eclipse.platform/msg44602.html
     */
    private void startObserving(AbstractFortranEditor editor)
    {
        if (editor != null)
        {
            addCaretMovementListenerTo(editor);
            FortranEditorVPGTasks tasks = FortranEditorVPGTasks.instance(editor);
            tasks.astTasks.add(this);
            tasks.vpgTasks.add(this);
        }
    }

    private void addCaretMovementListenerTo(AbstractFortranEditor editor)
    {
        TextViewer sourceViewer = (TextViewer)editor.getSourceViewerx();
        if (sourceViewer != null)
            sourceViewer.addPostSelectionChangedListener(this);
    }

    /**
     * Unregisters this view to receive notifications of caret movement in <code>editor</code>
     */
    private void stopObserving(AbstractFortranEditor editor)
    {
        if (editor != null)
        {
            removeCaretMovementListenerFrom(editor);
            FortranEditorVPGTasks tasks = FortranEditorVPGTasks.instance(editor);
            tasks.astTasks.remove(this);
            tasks.vpgTasks.remove(this);
        }
    }

    private void removeCaretMovementListenerFrom(AbstractFortranEditor editor)
    {
        TextViewer sourceViewer = (TextViewer)editor.getSourceViewerx();
        if (sourceViewer != null)
            sourceViewer.removePostSelectionChangedListener(this);
    }

    /**
     * IFortranEditorVPGTask - Callback run when the VPG is more-or-less up-to-date.
     * This method is run <i>outside</i> the UI thread.
     */
    public synchronized void handle(IFile file, IFortranAST ast)
    {
        if (activeEditor == null || activeAST == null) return;
        
        activeDefMap = new DefinitionMap<String>(ast)
        {
            @Override protected String map(Definition def)
            {
                if (def != null)
                    return def.describe();
                else
                    return null;
            }
        };
    }

    /**
     * IFortranEditorASTTask - Callback run when a fresh AST for the file in the editor
     * is available.  May be newer than the information available in the VPG.
     * This method is run <i>outside</i> the UI thread.
     */
    public synchronized void handle(final IFortranAST ast)
    {
        activeAST = ast;
    }

    /**
     * ISelectionChangedListener - Callback notifying the view that the editor's caret has moved
     */
    public synchronized void selectionChanged(SelectionChangedEvent event)
    {
        if (event.getSelection() instanceof TextSelection && activeAST != null && activeDefMap != null)
        {
            String description = activeDefMap.lookup(findTokenEnclosing((TextSelection)event.getSelection()));
            update(description == null ? "" : description);
        }
        else
        {
            update("");
        }
    }
    
    private Token findTokenEnclosing(TextSelection sel)
    {
        for (Token t : activeAST)
            if (t.containsFileOffset(sel.getOffset()))
                return t;
        return null;
    }
}