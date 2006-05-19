/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
/**
 * 
 */
package org.eclipse.ptp.internal.rm.ui.views;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ptp.rm.core.IRMElement;
import org.eclipse.ptp.rm.ui.viewerfilters.StatusViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public final class FilterDialog extends TitleAreaDialog {

	private class StatusContentProvider implements IStructuredContentProvider {

		public void dispose() {
			// no-op
		}

		public Object[] getElements(Object inputElement) {
			final IStatusDisplayProvider[] allStatuses = elementsProvider.getAllStatuses();
			return allStatuses;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// no-op
		}

	}

	private class StatusLableProvider extends LabelProvider {

		public Image getImage(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getText(Object element) {
			final IStatusDisplayProvider sdp = (IStatusDisplayProvider) element;
			return sdp.getText();
		}
	}

	private IRMElement[] elements;

	private final IRMElementsProvider elementsProvider;

	private CheckboxTableViewer statusesCheckbox;

	private final StatusViewerFilter statusViewerFilter;

	public FilterDialog(Shell shell, IRMElementsProvider elementsProvider) {
		super(shell);
		this.elementsProvider = elementsProvider;
		statusViewerFilter = new StatusViewerFilter(elementsProvider);
	}

	public boolean close() {
		final IStatusDisplayProvider[] selectedStatuses = findSelectedFilteredStatuses();
		statusViewerFilter.setFilteredStatuses(selectedStatuses);
		return super.close();
	}

	public ViewerFilter[] getFilters() {
		final ArrayList filters = new ArrayList();
		if (statusViewerFilter.getFilteredStatuses().length > 0)
			filters.add(statusViewerFilter);
		return (ViewerFilter[]) filters.toArray(new ViewerFilter[0]);
	}

	public int open(IRMElement[] elements) {
		this.elements = elements;
		return open();
	}
	
	public void resetFilters() {
		statusViewerFilter.reset();
	}

	private void createStatusCheckbox(Composite composite) {
		statusesCheckbox = CheckboxTableViewer.newCheckList(composite,
				SWT.BORDER);
		statusesCheckbox.getTable().setLayoutData(
				new GridData(GridData.FILL_BOTH));
		statusesCheckbox.setContentProvider(new StatusContentProvider());
		statusesCheckbox.setLabelProvider(new StatusLableProvider());
		statusesCheckbox.setInput(elementsProvider);

		// check the currently selected statuses.
		statusesCheckbox.setCheckedElements(statusViewerFilter.getFilteredStatuses());
	}

	private IStatusDisplayProvider[] findSelectedFilteredStatuses() {
		final Object[] checkedObjects = statusesCheckbox.getCheckedElements();
		final int length = checkedObjects.length;
		final IStatusDisplayProvider[] checkedElements = new IStatusDisplayProvider[length];
		System.arraycopy(checkedObjects, 0, checkedElements, 0, length);
		return checkedElements;
	}

	protected Control createContents(Composite parent) {
		final Control contents = super.createContents(parent);
		setTitle(elementsProvider.getNameFieldName() + " Filters");
		setMessage("Add Filters for " + elementsProvider.getNameFieldName()
				+ "s.");
		return contents;
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		createStatusCheckbox(composite);
		return composite;
	}
}