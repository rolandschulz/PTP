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
package org.eclipse.ptp.internal.rm.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ptp.internal.rm.ui.ResourceManagerUILog;
import org.eclipse.ptp.internal.rm.ui.util.AutoResizeTableLayout;
import org.eclipse.ptp.rm.core.IRMElement;
import org.eclipse.ptp.rm.core.IRMResourceManager;
import org.eclipse.ptp.rm.core.ResourceManagerPlugin;
import org.eclipse.ptp.rm.core.attributes.IAttrDesc;
import org.eclipse.ptp.rm.core.events.IRMResourceManagerChangedListener;
import org.eclipse.ptp.rm.core.events.IRMResourceManagerListener;
import org.eclipse.ptp.rm.ui.RMUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

/**
 * Abstract class that factors out the interaction of the view with the
 * {@link {@link ResourceManagerPlugin}} and the current
 * {@link {@link IRMResourceManager}} to display one type of
 * {@link {@link IRMElement}} view. It hooks up listeners for the
 * {@link {@link ResourceManagerPlugin}} to handle when the current resource
 * manager is changed. It will also hook up listeners for the current resource
 * manager to allow the view to be updated when the underlying elements are
 * added, removed, or modified.
 * 
 * @author rsqrd
 * 
 */
public abstract class AbstractElementsView extends ViewPart {

	private final class FiltersAction extends Action {

		private FilterDialog dialog;

		public FiltersAction(Composite composite) {
			super("Filters...");
			setImageDescriptor(RMUiPlugin.getImageDescriptor("icons/filter_ps.gif"));
			final Shell shell = composite.getShell();
			dialog = new FilterDialog(shell, elementsProvider);
		}

		public void resetFilters() {
			dialog.resetFilters();
		}

		public void run() {
			final int returnCode = dialog.open(elementsProvider.getElements(manager));
			if (returnCode == FilterDialog.OK) {
				// remove old filters
				viewer.resetFilters();

				// add new filters
				final ViewerFilter[] newFilters = dialog.getFilters();
				for (int i = 0; i < newFilters.length; ++i) {
					viewer.addFilter(newFilters[i]);
				}
			}

		}

	}

	/**
	 * This class is responsible for updating the view when the
	 * {@link {@link ResourceManagerPlugin}} changes the current
	 * {@link {@link IRMResourceManager}}.
	 * 
	 * @author rsqrd
	 * 
	 */
	class ResourceManagerChangedListener implements
			IRMResourceManagerChangedListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.core.events.IRMResourceManagerChangedListener#resourceManagerChanged(org.eclipse.ptp.rm.core.IRMResourceManager,
		 *      org.eclipse.ptp.rm.core.IRMResourceManager)
		 */
		public synchronized void resourceManagerChanged(
				IRMResourceManager oldManager, IRMResourceManager newManager) {

			// update the entire table if the manager has changed.
			if (newManager != manager) {
				manager = newManager;
				updateViewer();
			}
		}

	}

	/**
	 * The class used by the {@link {@link TableViewer}} to obtain its data.
	 * 
	 * @author rsqrd
	 * 
	 */
	class ViewContentProvider implements IStructuredContentProvider {

		public ViewContentProvider() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			// no-op
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			// Get the list of elements from the elementsProvider.
			final IRMElement[] elements = elementsProvider.getElements(manager);
			return elements;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

			// This method is called when dispose() called on the table.
			// This gives us the opportunity to remove ourselves from the
			// listener
			// when the table is disposed.

			// If the input is changed that means that an old listener is no
			// longer interested in listening to the current manager, and a new
			// one is.

			if (oldInput != null) {
				final IRMResourceManagerListener oldListener = (IRMResourceManagerListener) oldInput;
				manager.removeResourceManagerListener(oldListener);
			}
			if (newInput != null) {
				final IRMResourceManagerListener newListener = (IRMResourceManagerListener) newInput;
				manager.addResourceManagerListener(newListener);
			}
		}

	}

	/**
	 * LabelProvider required by the {@link {@link TableViewer}}
	 * 
	 * @author rsqrd
	 * 
	 */
	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public ViewLabelProvider(boolean hasStatus) {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			if (elementsProvider.hasStatus()
					&& columnIndex == statusColumnIndex) {
				final IRMElement rmElement = (IRMElement) element;
				final IStatusDisplayProvider status = elementsProvider.getStatus(rmElement);
				return status.getImage();
			} else
				return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {

			IRMElement rmElement = (IRMElement) element;

			if (columnIndex == nameColumnIndex) {
				return getText(rmElement.getName());
			} else if (elementsProvider.hasStatus()
					&& columnIndex == statusColumnIndex) {
				return elementsProvider.getStatus(rmElement).getText();
			}

			// The rest of the attributes follow the name and, optionally,
			// status columns

			final int descIndex = columnIndex - attributeColumnOffset;
			return getText(rmElement.getAttribute(attrDescs[descIndex]));
		}

	}

	private static String[] toProperties(
			IAttrDesc[] modifiedAttributeDescriptions, boolean statusChanged) {
		int nProperties = modifiedAttributeDescriptions.length;
		if (statusChanged)
			++nProperties;
		final String[] properties = new String[nProperties];
		for (int i = 0; i < modifiedAttributeDescriptions.length; ++i) {
			properties[i] = modifiedAttributeDescriptions[i].getName();
		}
		if (statusChanged) {
			properties[nProperties - 1] = IStatusDisplayProvider.STATUS_CHANGED_PROPERTY;
		}
		return properties;
	}

	// The attribute descriptions for the table column headers
	private IAttrDesc[] attrDescs;

	// Where do the attribute columns start?
	private int attributeColumnOffset = 1;

	// The factored out provider of IRMElement's
	private IRMElementsProvider elementsProvider;

	// The current resource manager of the ResourceManagerPlugin
	private IRMResourceManager manager = null;

	// Where is the name column?
	private int nameColumnIndex = 0;

	// Let's hold on to the listener for current resource manager swapping
	private ResourceManagerChangedListener resourceManagerChangedListener = new ResourceManagerChangedListener();

	// If and where is the status column? -1 means there is none.
	private int statusColumnIndex = -1;

	// Keep hold of the ViewContentProvider so we can give it to new
	// TableViewers when
	// we have to destroy the old one.
	private final ViewContentProvider viewContentProvider = new ViewContentProvider();

	// The main GUI element
	private TableViewer viewer;

	// Keep hold of the ViewLableProvider so we can give it to new TableViewers
	// when
	// we have to destroy the old one.
	private final ViewLabelProvider viewLabelProvider;

	private FiltersAction filterAction;

	public AbstractElementsView() {
		super();
		// Get the IRMElementsProvider and see if we can get status
		// info from it.
		this.elementsProvider = createElementsProvider();
		if (elementsProvider.hasStatus()) {
			statusColumnIndex = 0;
			++nameColumnIndex;
			++attributeColumnOffset;
		}

		// Now that we know whether there will be status, we can create the
		// label provider
		viewLabelProvider = new ViewLabelProvider(elementsProvider.hasStatus());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {

		filterAction = new FiltersAction(parent);

		createViewer(parent);

		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager dropDownMenu = actionBars.getMenuManager();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		dropDownMenu.add(filterAction);
		toolBar.add(filterAction);
		toolBar.update(false);
		actionBars.updateActionBars();

		ResourceManagerPlugin.getDefault().addResourceManagerChangedListener(
				resourceManagerChangedListener);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		viewer = null;
		// We don't need to listen to anything anylonger
		ResourceManagerPlugin.getDefault().removeResourceManagerChangedListener(
				resourceManagerChangedListener);
	}

	public void resetFilters() {
		filterAction.resetFilters();
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.resetFilters();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * We need to run some things in SWT's GUI thread.
	 * 
	 * @param runnable
	 */
	private void asyncExec(final Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}

	/**
	 * We need to set up a table's column headers.
	 * 
	 * @throws CoreException
	 */
	private void createTableStructure() throws CoreException {
		manager = getManager();
		this.attrDescs = elementsProvider.getElementAttrDescs(manager);

		final int numColumns = attrDescs.length
				+ (elementsProvider.hasStatus() ? 2 : 1);
		final TableColumn[] tableColumns = new TableColumn[numColumns];

		final Table table = viewer.getTable();
		final AutoResizeTableLayout layout = new AutoResizeTableLayout(table);
		table.setLayout(layout);

		if (elementsProvider.hasStatus()) {
			tableColumns[statusColumnIndex] = new TableColumn(table, SWT.LEFT);
			tableColumns[statusColumnIndex].setText("Status");
			layout.addColumnData(new ColumnWeightData(50));
		}

		tableColumns[nameColumnIndex] = new TableColumn(table, SWT.LEFT);
		tableColumns[nameColumnIndex].setText(elementsProvider.getNameFieldName());
		// tableColumns[0].setWidth(50);
		layout.addColumnData(new ColumnWeightData(50));

		for (int i = attributeColumnOffset; i < numColumns; ++i) {
			tableColumns[i] = new TableColumn(table, SWT.LEFT);
			final IAttrDesc attrDesc = attrDescs[i - attributeColumnOffset];
			tableColumns[i].setText(attrDesc.getName());
			tableColumns[i].setToolTipText(attrDesc.getDescription());
			// tableColumns[i].setWidth(50);
			layout.addColumnData(new ColumnWeightData(50));
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	/**
	 * Create a new TableViewer and set up its column headers Tell it where it
	 * gets its input.
	 * 
	 * @param parent
	 */
	private void createViewer(Composite parent) {

		resetFilters();

		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		try {
			createTableStructure();
		} catch (CoreException e) {
			ErrorDialog.openError(parent.getShell(), "Error", e.getMessage(),
					e.getStatus());
			ResourceManagerUILog.log(e.getStatus());
			viewer.getTable().dispose();
			viewer = null;
			return;
		}

		viewer.setContentProvider(viewContentProvider);
		viewer.setLabelProvider(viewLabelProvider);

		// The call to setInput(...) will set up the listener to listen to the
		// manager through
		// the call to ViewContentProvider.inputChanged(...)

		viewer.setInput(getListener());
	}

	/**
	 * Get the current manager
	 * 
	 * @return
	 * @throws CoreException
	 */
	private IRMResourceManager getManager() throws CoreException {
		return ResourceManagerPlugin.getDefault().getCurrentManager();
	}

	/**
	 * When the current resource manager is changed we have to destroy the old
	 * table and create a new table.
	 */
	private void updateViewer() {
		final Composite parent = viewer.getTable().getParent();
		asyncExec(new Runnable() {
			public void run() {
				viewer.getTable().dispose();
				createViewer(parent);
				parent.layout(true);
				// viewer.refresh();
			}
		});
	}

	/**
	 * @return the factored out elements provider
	 */
	protected abstract IRMElementsProvider createElementsProvider();

	/**
	 * This is called by instantiations when elems have been added by the
	 * manager.
	 * 
	 * @param elems
	 */
	protected synchronized void elementsAdded(final IRMElement[] elems) {
		asyncExec(new Runnable() {
			public void run() {
				viewer.add(elems);
			}
		});
	}

	/**
	 * This should be called by instantiations when the status or attributes to
	 * elems have been modified.
	 * 
	 * @param elems
	 * @param modifiedAttributeDescriptions
	 *            TODO
	 */
	protected synchronized void elementsModified(final IRMElement[] elems,
			final IAttrDesc[] modifiedAttributeDescriptions,
			final boolean statusChanged) {
		asyncExec(new Runnable() {
			public void run() {
				final boolean bug_24521_workaround = true;
				if (bug_24521_workaround) {
					viewer.update(elems, toProperties(
							modifiedAttributeDescriptions, statusChanged));
					viewer.refresh();
				} else {
					viewer.update(elems, toProperties(
							modifiedAttributeDescriptions, statusChanged));
				}
				// viewer.refresh();
			}

		});
	}

	/**
	 * This is called by instantiations when elems have been removed by the
	 * manager.
	 * 
	 * @param elems
	 */
	protected synchronized void elementsRemoved(final IRMElement[] elems) {
		asyncExec(new Runnable() {
			public void run() {
				viewer.remove(elems);
			}
		});
	}

	/**
	 * @return the resource manager listener to add to the current resouce
	 *         manager.
	 */
	protected abstract IRMResourceManagerListener getListener();

}
