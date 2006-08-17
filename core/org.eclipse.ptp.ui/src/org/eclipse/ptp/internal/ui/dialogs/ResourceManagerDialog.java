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
package org.eclipse.ptp.internal.ui.dialogs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ResourceManagerDialog extends TitleAreaDialog {
	public class ContentProvider implements IStructuredContentProvider {

		public void dispose() {
			// TODO Auto-generated method stub

		}

		public Object[] getElements(Object inputElement) {
			Set elements = getResultingSet();
			return elements.toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub

		}

	}
	public class LableProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element == NO_MANAGER) {
				return getText("none");
			}
			IResourceManager manager = (IResourceManager) element;
			IResourceManagerConfiguration configuration = manager.getConfiguration();
			IResourceManagerFactory factory = getFactory(configuration.getResourceManagerId());

			switch (columnIndex) {
			case COLUMN_NAME:
				return getText(configuration.getName());
			case COLUMN_TYPE:
				return getText(factory.getName());
			case COLUMN_DESC:
				return getText(configuration.getDescription());
			default:
				return getText("");	
			}
		}

	}

	private static final int COLUMN_DESC = 2;
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_TYPE = 1;
	private static final String NO_MANAGER = "No Manager";

	private final Set addedManagers = new HashSet();
	private final Map factories = new HashMap();
	private final Set orignalResourceManagers = new HashSet();
	private final Set removedManagers = new HashSet();
	private TableViewer tableViewer;
	private final String[] factIds;
	private Combo combo;

	public ResourceManagerDialog(Shell parentShell, IResourceManagerFactory[] factories,
			IResourceManager[] originalResourceManagers) {
		super(parentShell);
		for (int i=0; i<factories.length; ++i) {
			this.factories.put(factories[i].getId(), factories[i]);
		}
		this.orignalResourceManagers.addAll(Arrays.asList(originalResourceManagers));
		this.factIds = (String[]) this.factories.keySet().toArray(new String[0]);
	}

	public IResourceManager[] getAddedManagers() {
		return (IResourceManager[]) addedManagers.toArray(
				new IResourceManager[addedManagers.size()]);
	}

	public IResourceManager[] getRemovedManagers() {
		return (IResourceManager[]) removedManagers.toArray(
				new IResourceManager[removedManagers.size()]);
	}

	private void addResourceManager() {
		final int selectionIndex = combo.getSelectionIndex();
		IResourceManager addedManager = createResourceManager(factIds[selectionIndex]);
		if (addedManager != null) {
			addedManagers.add(addedManager);
		}
	}

	private IResourceManager createResourceManager(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	private IResourceManagerFactory getFactory(String resourceManagerId) {
		return (IResourceManagerFactory) factories.get(resourceManagerId);
	}

	private Set getResultingSet() {
		Set resulting = new HashSet(orignalResourceManagers);
		resulting.addAll(addedManagers);
		resulting.removeAll(removedManagers);
		if (resulting.size() == 0) {
			resulting.add(NO_MANAGER);
		}
		return resulting;
	}

	private void removeResourceManagers() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		addedManagers.removeAll(selection.toList());
		removedManagers.addAll(selection.toList());
		// We only want removedManagers to contain
		// removed "original" managers.
		removedManagers.retainAll(orignalResourceManagers);
		tableViewer.refresh();
	}

	protected void cancelPressed() {
		addedManagers.clear();
		removedManagers.clear();
		super.cancelPressed();
	}

	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		setMessage("Add and Remove Resource Managers");
		setTitle("Resource Managers");
		return control;
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Composite contents = new Composite(composite, SWT.NONE);
		contents.setLayout(new GridLayout(3, false));

		Composite leftSide = new Composite(contents, SWT.NONE);
		leftSide.setLayout(new GridLayout(1, false));
		leftSide.setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

		// Build the separator line
        Label titleBarSeparator = new Label(contents, SWT.VERTICAL
                | SWT.SEPARATOR);
        titleBarSeparator.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        
		Composite rightSide = new Composite(contents, SWT.NONE);
		rightSide.setLayout(new GridLayout(1, false));
		rightSide.setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		
		tableViewer = new TableViewer(leftSide, SWT.MULTI | SWT.H_SCROLL
						| SWT.V_SCROLL | SWT.FULL_SELECTION);
		Button removeButton = new Button(leftSide, SWT.PUSH);
		removeButton.setText("Remove");
		
		Label addLabel = new Label(rightSide, SWT.NONE);
		addLabel.setText("Add Resource Manager of Selected Type");
		combo = new Combo(rightSide, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(300, SWT.DEFAULT));
		final String[] factNames = new String[factIds.length];
		for (int i=0; i<factNames.length; ++i) {
			factNames[i] = getFactory(factIds[i]).getName();
		}
		combo.setItems(factNames);
		combo.setText(factNames[0]);
		Button addButton = new Button(rightSide, SWT.PUSH);
		addButton.setText("Add");
		
		tableViewer.setContentProvider(new ContentProvider());
		tableViewer.setLabelProvider(new LableProvider());
		
		Table table = tableViewer.getTable();
		//table.setLayoutData(new GridData(GridData.FILL_BOTH));
		new TableColumn(table, SWT.LEFT).setText("Name");
		new TableColumn(table, SWT.LEFT).setText("Type");
		new TableColumn(table, SWT.LEFT).setText("Description");
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		// name
		table.getColumn(0).setWidth(100);
		// type
		table.getColumn(1).setWidth(200);
		// description
		table.getColumn(2).setWidth(300);
		
		addButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
				// no-op
			}
			public void widgetSelected(SelectionEvent e) {
				addResourceManager();
			}});
		
		removeButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
				// no-op
			}
			public void widgetSelected(SelectionEvent e) {
				removeResourceManagers();
			}});
		
		tableViewer.setInput(this);
		tableViewer.getControl().setFocus();
		return composite;
	}

	protected void okPressed() {
		// TODO Auto-generated method stub
		super.okPressed();
	}

}
