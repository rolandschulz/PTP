package org.eclipse.ptp.internal.ui.search;

import java.util.Arrays;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.ParallelPlugin;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.ui.ParallelElementLabelProvider;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.views.AbstractParallelView;
import org.eclipse.ptp.ui.views.ProcessEditorInput;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * @author Clement
 *  
 */
public class PSearchResultPage extends AbstractTextSearchViewPage {
    private PSearchContentProvider contentProvider;
    
    public void createControl(Composite parent) {
        super.createControl(parent);
        getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
               ISelection selection = event.getSelection();
               if (selection instanceof IStructuredSelection) {
                   handleSelection(((IStructuredSelection) selection).getFirstElement());
               }
			}
		});
    }
    
	protected TreeViewer createTreeViewer(Composite parent) {
		return new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
	}
    
    public void handleSelection(Object element) {
        if (element == null || !(element instanceof IPElement))
            return;
        
        IWorkbenchPage activePage = getSite().getPage();
		AbstractParallelView nodeView = (AbstractParallelView)activePage.findView(UIUtils.ParallelNodeStatusView_ID);
		AbstractParallelView treeView = (AbstractParallelView)activePage.findView(UIUtils.ParallelProcessesView_ID);
		
	    if (nodeView != null)
	        nodeView.selectReveal((IPElement)element);
	    if (treeView != null)
	        treeView.selectReveal((IPElement)element);
    }

    protected void showMatch(Match match, int currentOffset, int currentLength, boolean activateEditor) throws PartInitException {
        IEditorPart editor = null;
        Object element = match.getElement();
        if (element instanceof IPProcess) {
            IPProcess process = (IPProcess) element;
            try {
                editor = ParallelPlugin.getActivePage().openEditor(new ProcessEditorInput(process), UIUtils.ParallelProcessViewer_ID);
            } catch (PartInitException e1) {
                return;
            }
        }
        /*
        if (editor instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) editor;
            textEditor.selectAndReveal(currentOffset, currentLength);
        }
        */
    }
    
    protected void elementsChanged(Object[] objects) {
        if (contentProvider != null) {
        	Arrays.sort(objects);
            contentProvider.elementsChanged(objects);
        }
    }
    
	protected void clear() {
		if (contentProvider!=null)
			contentProvider.clear();
	}
	
	protected void configureTreeViewer(TreeViewer viewer) {
		viewer.setLabelProvider(new ParallelElementLabelProvider());
		contentProvider = new PSearchTreeContentProvider(viewer);
		viewer.setContentProvider(contentProvider);
	}
	
	protected void configureTableViewer(TableViewer viewer) {
		viewer.setLabelProvider(new ParallelElementLabelProvider());
		contentProvider = new PSearchTableContentProvider(viewer);
		viewer.setContentProvider(contentProvider);
	}    
}