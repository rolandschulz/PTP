package org.eclipse.photran.internal.ui.views;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.photran.internal.ui.editor.FortranKeywordRuleBasedScanner;
import org.eclipse.photran.internal.ui.editor_vpg.DefinitionMap;
import org.eclipse.photran.internal.ui.editor_vpg.FortranEditorTasks;
import org.eclipse.photran.internal.ui.editor_vpg.IFortranEditorASTTask;
import org.eclipse.photran.internal.ui.editor_vpg.IFortranEditorVPGTask;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
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
    private HashMap<String, ASTExecutableProgramNode> activeAST = new HashMap<String, ASTExecutableProgramNode>();
    private HashMap<String, TokenList> activeTokenList = new HashMap<String, TokenList>();
    private HashMap<String, DefinitionMap<String>> activeDefinitions = new HashMap<String, DefinitionMap<String>>();
    
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
        viewer.configure(new AbstractFortranEditor.FortranSourceViewerConfiguration()
        {
            @Override protected ITokenScanner getTokenScanner()
            {
                // Copied from FreeFormFortranEditor#getTokenScanner
                return new FortranKeywordRuleBasedScanner(false, viewer);
            }
        });
        viewer.setDocument(document);
        IDocumentPartitioner partitioner = new FastPartitioner(new RuleBasedPartitionScanner(), AbstractFortranEditor.PARTITION_TYPES);
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
                activeEditor = startObserving((AbstractFortranEditor)part);
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
    private AbstractFortranEditor startObserving(final AbstractFortranEditor editor)
    {
        if (editor != null)
        {
            String declViewEnabledProperty = SearchPathProperties.getProperty(
                editor.getIFile(),
                SearchPathProperties.ENABLE_DECL_VIEW_PROPERTY_NAME);
            if (declViewEnabledProperty != null && declViewEnabledProperty.equals("true"))
            {
                addCaretMovementListenerTo(editor);
                FortranEditorTasks tasks = FortranEditorTasks.instance(editor);
                tasks.addASTTask(this);
                tasks.addVPGTask(this);
                
                ((IPartService)getSite().getService(IPartService.class)).addPartListener(new IPartListener2()
                {
                    public void partActivated(IWorkbenchPartReference partRef)
                    {
                    }

                    public void partBroughtToTop(IWorkbenchPartReference partRef)
                    {
                    }

                    public void partClosed(IWorkbenchPartReference partRef)
                    {
                        if (partRef.getPart(false) == editor)
                        {
                            FortranEditorTasks tasks = FortranEditorTasks.instance(editor);
                            tasks.removeASTTask(DeclarationView.this);
                            tasks.removeVPGTask(DeclarationView.this);
                            
                            IFile ifile = editor.getIFile();
                            if (ifile != null)
                            {
                                String path = ifile.getFullPath().toPortableString();
                                activeAST.remove(path);
                                activeDefinitions.remove(path);
                                activeTokenList.remove(path);
                            }
                        }
                        ((IPartService)getSite().getService(IPartService.class)).removePartListener(this);
                    }

                    public void partDeactivated(IWorkbenchPartReference partRef)
                    {
                    }

                    public void partHidden(IWorkbenchPartReference partRef)
                    {
                    }

                    public void partInputChanged(IWorkbenchPartReference partRef)
                    {
                    }

                    public void partOpened(IWorkbenchPartReference partRef)
                    {
                    }

                    public void partVisible(IWorkbenchPartReference partRef)
                    {
                    }
                });
                
                tasks.getRunner().runTasks(true);
                return editor;
            }
        }
        
        return null;
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
        update("");
        if (editor != null)
            removeCaretMovementListenerFrom(editor);
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
    public synchronized void handle(IFile file, IFortranAST ast, DefinitionMap<Definition> defMap)
    {
        if (defMap == null) return;
        
        activeDefinitions.put(file.getFullPath().toPortableString(), new DefinitionMap<String>(defMap)
        {
            @Override protected String map(String qualifiedName, Definition def)
            {
                return def.describe();
            }
        });
    }

    /**
     * IFortranEditorASTTask - Callback run when a fresh AST for the file in the editor
     * is available.  May be newer than the information available in the VPG.
     * This method is run <i>outside</i> the UI thread.
     */
    public synchronized boolean handle(ASTExecutableProgramNode ast, TokenList tokenList, DefinitionMap<Definition> defMap)
    {
        if (activeEditor != null)
        {
            String path = activeEditor.getIFile().getFullPath().toPortableString();
            activeAST.put(path, ast);
            activeTokenList.put(path, tokenList);
        }
        return true;
    }

    /**
     * ISelectionChangedListener - Callback notifying the view that the editor's caret has moved
     */
    public synchronized void selectionChanged(SelectionChangedEvent event)
    {
        if (activeEditor == null) return;
        String path = activeEditor.getIFile().getFullPath().toPortableString();
        
        TokenList tokenList = activeTokenList.get(path);
        DefinitionMap<String> defMap = activeDefinitions.get(path);
        if (event.getSelection() instanceof TextSelection && tokenList != null && defMap != null)
        {
            String description = defMap.lookup((TextSelection)event.getSelection(), tokenList);
            update(description == null
                ? "" 
                : description);
        }
        else
        {
            update("");
        }
    }
}