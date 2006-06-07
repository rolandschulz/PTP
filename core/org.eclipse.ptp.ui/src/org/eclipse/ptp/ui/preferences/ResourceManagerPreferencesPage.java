package org.eclipse.ptp.ui.preferences;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ptp.rmsystem.ResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.ResourceManagerStore;
import org.eclipse.ptp.ui.PTPUIPlugin;

/**
 * Resource Manager preference page allows configuration of resource managers.
 * It provides controls for adding, removing and changing resource managers.
 * 
 */
public class ResourceManagerPreferencesPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	class ResourceManagerContentProvider implements IStructuredContentProvider {

		private ResourceManagerStore store;

		/*
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object input) {
			return store.getResourceManagers();
		}

		/*
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			store = (ResourceManagerStore) newInput;
		}

		/*
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
			store = null;
		}

	}

	private class ResourceManagerLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		/*
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/*
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			IResourceManager rm = (IResourceManager) element;
			IResourceManagerConfiguration config = rm.getConfiguration();

			switch (columnIndex) {
			case 0:
				return config.getName();
			case 1:
				return config.getResourceManagerId();
			case 2:
				return config.getHost().trim().length() > 0 ? config.getHost()
						: ""; //$NON-NLS-1$
			case 3:
				return config.getPort() >= 0 ? new Integer(config.getPort())
						.toString() : ""; //$NON-NLS-1$
//			case 4:
//				return config.getDescription();
			default:
				return ""; //$NON-NLS-1$
			}
		}
	}

	private class ResourceManagerViewerSorter extends ViewerSorter {

		/*
		 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public int compare(Viewer viewer, Object object1, Object object2) {
			if ((object1 instanceof IResourceManager)
					&& (object2 instanceof IResourceManager)) {
				IResourceManager left = ((IResourceManager) object1);
				IResourceManager right = ((IResourceManager) object2);
				int result = left
						.getConfiguration()
						.getName()
						.compareToIgnoreCase(right.getConfiguration().getName());
				if (result != 0)
					return result;
				return left.getConfiguration().getDescription()
						.compareToIgnoreCase(
								right.getConfiguration().getDescription());
			}
			return super.compare(viewer, object1, object2);
		}

		/*
		 * @see org.eclipse.jface.viewers.ViewerComparator#isSorterProperty(java.lang.Object,
		 *      java.lang.String)
		 */
		public boolean isSorterProperty(Object element, String property) {
			return true;
		}
	}

	/**
	 * The store for our resource managers.
	 */
	private ResourceManagerStore fResourceManagerStore;

	/**
	 * The table presenting the resource managers.
	 */
	private TableViewer fTableViewer;

	/* buttons */
	private Button fAddButton;

	private Button fEditButton;

	private Button fRemoveButton;

	/**
	 * Creates a new resource manager preference page.
	 */
	public ResourceManagerPreferencesPage() {
		super();
		setPreferenceStore(PTPUIPlugin.getDefault().getPreferenceStore());
		setDescription("Create, edit or remote resource managers:");
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		
		// Table
		
		Table table = new Table(composite, SWT.BORDER | SWT.MULTI
				| SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		
		TableLayout layout2 = new TableLayout();
		table.setLayout(layout2);

		TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText("Name");
		column1.setWidth(100);
		TableColumn column2 = new TableColumn(table, SWT.NONE);
		column2.setText("Type");
		column2.setWidth(50);
		TableColumn column3 = new TableColumn(table, SWT.NONE);
		column3.setText("Host");
		column3.setWidth(150);
		TableColumn column4 = new TableColumn(table, SWT.NONE);
		column4.setText("Port");
		column4.setWidth(50);
//		TableColumn column5 = new TableColumn(table, SWT.NONE);
//		column5.setText("Description");
//		column4.setWidth(100);
		
		fTableViewer = new TableViewer(table);
		fTableViewer.setLabelProvider(new ResourceManagerLabelProvider());
		fTableViewer.setContentProvider(new ResourceManagerContentProvider());
		fTableViewer.setSorter(new ResourceManagerViewerSorter());

		fTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				edit();
			}
		});

		fTableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent e) {
						handleSelectionChange();
					}
				});

		// Buttons

		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(
				GridData.VERTICAL_ALIGN_BEGINNING));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonsComposite.setLayout(layout);

		fAddButton = new Button(buttonsComposite, SWT.PUSH);
		fAddButton.setText("New...");
		fAddButton.setLayoutData(getButtonGridData(fAddButton));
		fAddButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				add();
			}
		});

		fEditButton = new Button(buttonsComposite, SWT.PUSH);
		fEditButton.setText("Edit...");
		fEditButton.setLayoutData(getButtonGridData(fEditButton));
		fEditButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				edit();
			}
		});

		fRemoveButton = new Button(buttonsComposite, SWT.PUSH);
		fRemoveButton.setText("Remote");
		fRemoveButton.setLayoutData(getButtonGridData(fRemoveButton));
		fRemoveButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				remove();
			}
		});

		updateButtons();
		Dialog.applyDialogFont(composite);
		return composite;
	}

	/**
	 * Creates a separator between buttons
	 * 
	 * @param parent
	 * @return a separator
	 */
	private Label createSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.NONE);
		separator.setVisible(false);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.heightHint = 4;
		separator.setLayoutData(gd);
		return separator;
	}

	private static GridData getButtonGridData(Button button) {
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		// TODO replace SWTUtil
		// data.widthHint= SWTUtil.getButtonWidthHint(button);
		// data.heightHint= SWTUtil.getButtonHeightHint(button);

		return data;
	}

	private void handleSelectionChange() {
		updateViewerInput();
		updateButtons();
	}

	/**
	 * Updates the pattern viewer.
	 */
	protected void updateViewerInput() {
		IStructuredSelection selection = (IStructuredSelection) fTableViewer
				.getSelection();

	}

	/**
	 * Updates the buttons.
	 */
	protected void updateButtons() {
		int selectionCount = 0;
		IStructuredSelection selection = (IStructuredSelection) fTableViewer
				.getSelection();
		if (selection != null) selectionCount = selection.size();
		int itemCount = fTableViewer.getTable().getItemCount();
		fEditButton.setEnabled(selectionCount == 1);
		fRemoveButton.setEnabled(selectionCount > 0
				&& selectionCount <= itemCount);
	}

	private void add() {

		IResourceManagerFactory[] resourceManagers = PTPCorePlugin.getDefault()
				.getResourceManagerFactories();

		if (resourceManagers != null && resourceManagers.length > 0) {
			String id = resourceManagers[0].getId();

			IResourceManagerFactory factory = PTPCorePlugin.getDefault()
					.getResourceManagerFactory(id);
			IResourceManagerConfiguration config = new ResourceManagerConfiguration(
					"", "", id, "", -1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			IResourceManager rm = factory.create(config);

			IResourceManager newRm = editResourceManager(rm, false);
			if (newRm != null) {
				fResourceManagerStore.add(newRm);
			}
		} else {
			new MessageDialog(
					getShell(),
					"No Resource Manager Available",
					null,
					"We have not found a resource manager extension. Please verify your installation and make sure that resource manager plug-ins are correctly installed.",
					MessageDialog.ERROR, new String[] { "OK" }, 0).open();
		}
	}

	/**
	 * Creates the edit dialog. Subclasses may override this method to provide a
	 * custom dialog.
	 * 
	 * @param rm
	 *            the resource manager being edited
	 * @param edit
	 *            whether the dialog should be editable
	 * @return the created or modified resource manager , or <code>null</code>
	 *         if the edition failed
	 */
	protected IResourceManager editResourceManager(IResourceManager rm, boolean edit) {
		IResourceManagerFactory[] factories = PTPCorePlugin.getDefault()
				.getResourceManagerFactories();

		ResourceManagerEditDialog dialog = new ResourceManagerEditDialog(
				getShell(), rm, edit, factories);
		if (dialog.open() == Window.OK) {
			return dialog.getResourceManager();
		}
		return null;
	}

	private void edit() {
		IStructuredSelection selection = (IStructuredSelection) fTableViewer
				.getSelection();

		Object[] objects = selection.toArray();
		if ((objects == null) || (objects.length != 1))
			return;

		IResourceManager rm = (IResourceManager) selection
				.getFirstElement();
		edit(rm);
	}

	private void edit(IResourceManager rm) {
		IResourceManager oldRm = rm;
		IResourceManager newRm = editResourceManager(oldRm, true);
		if (newRm != null) {

			if (!newRm.getConfiguration().getName().equals(
					oldRm.getConfiguration().getName())
					&& MessageDialog.openQuestion(getShell(), "Create New",
							"Create new resource manager?'")) {
				rm = newRm;
				fResourceManagerStore.add(rm);
				fTableViewer.refresh();
			} else {
				oldRm = newRm;
				fTableViewer.refresh(rm);
			}
			handleSelectionChange();
			fTableViewer.setSelection(new StructuredSelection(rm));
		}
	}

	private void remove() {
		IStructuredSelection selection = (IStructuredSelection) fTableViewer
				.getSelection();

		Iterator elements = selection.iterator();
		while (elements.hasNext()) {
			IResourceManager rm = (IResourceManager) elements.next();
			fResourceManagerStore.delete(rm);
		}

		fTableViewer.refresh();
	}

	/*
	 * @see Control#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible)
			setTitle("Resource Managers");
	}

	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		try {
			fResourceManagerStore.save();
		} catch (IOException e) {
			openWriteErrorDialog();
		}

		return super.performOk();
	}

	/*
	 * @see PreferencePage#performCancel()
	 */
	public boolean performCancel() {
		try {
			fResourceManagerStore.load();
		} catch (IOException e) {
			openReadErrorDialog();
			return false;
		}
		return super.performCancel();
	}

	private void openReadErrorDialog() {
		String title = "Error";
		String message = "Error reading resource managers.";
		MessageDialog.openError(getShell(), title, message);
	}

	private void openWriteErrorDialog() {
		String title = "Error'";
		String message = "Error saving resource managers";
		MessageDialog.openError(getShell(), title, message);
	}

	public void setPreferenceStore(IPreferenceStore store) {
		if (fResourceManagerStore == null) {
			fResourceManagerStore = new ResourceManagerStore(store, RMS_KEY);
			try {
				fResourceManagerStore.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		super.setPreferenceStore(store);
	}

	private static final String RMS_KEY = "org.eclipse.ptp.resourcemanagers";
}
