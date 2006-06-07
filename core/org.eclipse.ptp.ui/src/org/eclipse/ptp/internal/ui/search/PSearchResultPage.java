/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.ui.search;

import java.util.Arrays;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.ui.old.PTPUIPlugin;
import org.eclipse.ptp.ui.old.ParallelElementLabelProvider;
import org.eclipse.ptp.ui.old.UIUtils;
import org.eclipse.ptp.ui.views.old.AbstractParallelView;
import org.eclipse.ptp.ui.views.old.ProcessEditorInput;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
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
                editor = PTPUIPlugin.getActivePage().openEditor(new ProcessEditorInput(process), UIUtils.ParallelProcessViewer_ID);
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