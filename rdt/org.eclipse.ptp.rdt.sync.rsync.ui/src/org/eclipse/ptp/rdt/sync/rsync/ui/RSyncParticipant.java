/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.rsync.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.newui.PageLayout;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.cdt.ui.wizards.CNewWizard;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.rdt.sync.rsync.core.RSyncServiceProvider;
import org.eclipse.ptp.rdt.sync.rsync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Launches a dialog that configures a remote sync target with OK and Cancel
 * buttons. Also has a text field to allow the name of the configuration to be
 * changed.
 */
public class RSyncParticipant implements ISynchronizeParticipant {
	private static final String FILE_SCHEME = "file"; //$NON-NLS-1$

	// private IServiceConfiguration fConfig;
	private IRemoteConnection fSelectedConnection;
	private IRemoteServices fSelectedProvider;
	// private final IRunnableContext fContext;
	private String fProjectName = ""; //$NON-NLS-1$

	private final Map<Integer, IRemoteServices> fComboIndexToRemoteServicesProviderMap = new HashMap<Integer, IRemoteServices>();
	private final Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();

	private Control fDialogControl;
	private Point fDialogSize;
	private Text fNameText;
	private Button fBrowseButton;
	private Button fNewConnectionButton;
	private Combo fProviderCombo;
	private Combo fConnectionCombo;
	private Text fLocationText;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#createConfigurationArea
	 * (org.eclipse.swt.widgets.Composite,
	 * org.eclipse.jface.operation.IRunnableContext)
	 */
	public void createConfigurationArea(Composite parent, IRunnableContext context) {
		final Composite configArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		configArea.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		configArea.setLayoutData(gd);

		// Label for "Provider:"
		Label providerLabel = new Label(configArea, SWT.LEFT);
		providerLabel.setText(Messages.RSyncParticipant_remoteProvider);

		// combo for providers
		fProviderCombo = new Combo(configArea, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		gd.horizontalSpan = 2;
		fProviderCombo.setLayoutData(gd);
		fProviderCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleServicesSelected();
			}
		});

		// attempt to restore settings from saved state
		// IRemoteServices providerSelected = fProvider.getRemoteServices();

		// populate the combo with a list of providers
		IRemoteServices[] providers = PTPRemoteUIPlugin.getDefault().getRemoteServices(context);
		int toSelect = 0;

		for (int k = 0; k < providers.length; k++) {
			fProviderCombo.add(providers[k].getName(), k);
			fComboIndexToRemoteServicesProviderMap.put(k, providers[k]);
		}

		// set selected host to be the first one if we're not restoring from
		// settings
		fProviderCombo.select(toSelect);
		fSelectedProvider = fComboIndexToRemoteServicesProviderMap.get(toSelect);

		// connection combo
		// Label for "Connection:"
		Label connectionLabel = new Label(configArea, SWT.LEFT);
		connectionLabel.setText(Messages.RSyncParticipant_connection);

		// combo for providers
		fConnectionCombo = new Combo(configArea, SWT.DROP_DOWN | SWT.READ_ONLY);
		// set layout to grab horizontal space
		fConnectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fConnectionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleConnectionSelected();
			}
		});

		// populate the combo with a list of providers
		populateConnectionCombo(fConnectionCombo);

		// new connection button
		fNewConnectionButton = new Button(configArea, SWT.PUSH);
		fNewConnectionButton.setText(Messages.RSyncParticipant_new);
		updateNewConnectionButtonEnabled(fNewConnectionButton);
		fNewConnectionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
				if (connectionManager != null) {
					connectionManager.newConnection(fNewConnectionButton.getShell());
				}
				// refresh list of connections
				populateConnectionCombo(fConnectionCombo);
			}
		});

		Label locationLabel = new Label(configArea, SWT.LEFT);
		locationLabel.setText(Messages.RSyncParticipant_location);

		fLocationText = new Text(configArea, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 250;
		fLocationText.setLayoutData(gd);
		fLocationText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// MBSCustomPageManager.addPageProperty(REMOTE_SYNC_WIZARD_PAGE_ID,
				// PATH_PROPERTY, fLocationText.getText());
			}
		});

		// new connection button
		fBrowseButton = new Button(configArea, SWT.PUSH);
		fBrowseButton.setText(Messages.RSyncParticipant_browse);
		fBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fSelectedConnection != null) {
					checkConnection();
					if (fSelectedConnection.isOpen()) {
						IRemoteUIServices remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fSelectedProvider);
						if (remoteUIServices != null) {
							IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
							if (fileMgr != null) {
								fileMgr.setConnection(fSelectedConnection);
								String correctPath = fLocationText.getText();
								String selectedPath = fileMgr.browseDirectory(
										fLocationText.getShell(),
										"Project Location (" + fSelectedConnection.getName() + ")", correctPath, IRemoteUIConstants.NONE); //$NON-NLS-1$ //$NON-NLS-2$
								if (selectedPath != null) {
									fLocationText.setText(selectedPath);
								}
							}
						}
					}
				}
			}
		});

		createDynamicGroup(configArea);
		// switchTo(updateData(tree, right, show_sup, RemoteMainWizardPage.this,
		// getWizard()), getDescriptor(tree));
	}

	private Tree tree;
	private Composite right;
	private Button show_sup;
	private Label right_label;
	public CWizardHandler h_selected = null;
	private Label categorySelectedLabel;
	public static final String DESC = "EntryDescriptor"; //$NON-NLS-1$ 
	private static final Image IMG_CATEGORY = CPluginImages.get(CPluginImages.IMG_OBJS_SEARCHFOLDER);
	private static final Image IMG_ITEM = CPluginImages.get(CPluginImages.IMG_OBJS_VARIABLE);
	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.CDTWizard"; //$NON-NLS-1$
	private static final String ELEMENT_NAME = "wizard"; //$NON-NLS-1$
	private static final String CLASS_NAME = "class"; //$NON-NLS-1$

	private void createDynamicGroup(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		c.setLayout(new GridLayout(2, true));

		Label l1 = new Label(c, SWT.NONE);
		l1.setText("Project type:");
		l1.setFont(parent.getFont());
		l1.setLayoutData(new GridData(GridData.BEGINNING));

		right_label = new Label(c, SWT.NONE);
		right_label.setFont(parent.getFont());
		right_label.setLayoutData(new GridData(GridData.BEGINNING));

		tree = new Tree(c, SWT.SINGLE | SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] tis = tree.getSelection();
				if (tis == null || tis.length == 0) {
					return;
				}
				switchTo((CWizardHandler) tis[0].getData(), (EntryDescriptor) tis[0].getData(DESC));
				// setPageComplete(validatePage());
			}
		});
		tree.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				for (int i = 0; i < tree.getItemCount(); i++) {
					if (tree.getItem(i).getText().compareTo(e.result) == 0) {
						return;
					}
				}
				e.result = "Project type:";
			}
		});
		right = new Composite(c, SWT.NONE);
		right.setLayoutData(new GridData(GridData.FILL_BOTH));
		right.setLayout(new PageLayout());

		show_sup = new Button(c, SWT.CHECK);
		show_sup.setText(UIMessages.getString("CMainWizardPage.1")); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		show_sup.setLayoutData(gd);
		show_sup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (h_selected != null) {
					h_selected.setSupportedOnly(show_sup.getSelection());
				}
				// switchTo(updateData(tree, right, show_sup,
				// RemoteMainWizardPage.this, getWizard()),
				// getDescriptor(tree));
			}
		});

		// restore settings from preferences
		show_sup.setSelection(!CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOSUPP));
	}

	private void switchTo(CWizardHandler h, EntryDescriptor ed) {
		if (h == null) {
			h = ed.getHandler();
		}
		if (ed.isCategory()) {
			h = null;
		}
		try {
			if (h != null) {
				h.initialize(ed);
			}
		} catch (CoreException e) {
			h = null;
		}
		if (h_selected != null) {
			h_selected.handleUnSelection();
		}
		h_selected = h;
		if (h == null) {
			if (ed.isCategory()) {
				if (categorySelectedLabel == null) {
					categorySelectedLabel = new Label(right, SWT.WRAP);
					categorySelectedLabel
							.setText("Project category is selected. Expand the category and select a concrete project type.");
					right.layout();
				}
				categorySelectedLabel.setVisible(true);
			}
			return;
		}
		right_label.setText(h_selected.getHeader());
		if (categorySelectedLabel != null) {
			categorySelectedLabel.setVisible(false);
		}
		h_selected.handleSelection();
		h_selected.setSupportedOnly(show_sup.getSelection());
	}

	public static CWizardHandler updateData(Tree tree, Composite right, Button show_sup, IWizardItemsListListener ls, IWizard wizard) {
		// remember selected item
		TreeItem[] sel = tree.getSelection();
		String savedStr = (sel.length > 0) ? sel[0].getText() : null;

		tree.removeAll();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
		if (extensionPoint == null) {
			return null;
		}
		IExtension[] extensions = extensionPoint.getExtensions();
		if (extensions == null) {
			return null;
		}

		List<EntryDescriptor> items = new ArrayList<EntryDescriptor>();
		for (int i = 0; i < extensions.length; ++i) {
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getName().equals(ELEMENT_NAME)) {
					CNewWizard w = null;
					try {
						w = (CNewWizard) element.createExecutableExtension(CLASS_NAME);
					} catch (CoreException e) {
						System.out.println(UIMessages.getString("CMainWizardPage.5") + e.getLocalizedMessage()); //$NON-NLS-1$
						return null;
					}
					if (w == null) {
						return null;
					}
					w.setDependentControl(right, ls);
					for (EntryDescriptor ed : w.createItems(show_sup.getSelection(), wizard)) {
						items.add(ed);
					}
				}
			}
		}
		// If there is a EntryDescriptor which is default for category, make
		// sure it
		// is in the front of the list.
		for (int i = 0; i < items.size(); ++i) {
			EntryDescriptor ed = items.get(i);
			if (ed.isDefaultForCategory()) {
				items.remove(i);
				items.add(0, ed);
				break;
			}
		}

		// bug # 211935 : allow items filtering.
		if (ls != null) {
			items = ls.filterItems(items);
		}
		addItemsToTree(tree, items);

		if (tree.getItemCount() > 0) {
			TreeItem target = null;
			// try to search item which was selected before
			if (savedStr != null) {
				TreeItem[] all = tree.getItems();
				for (TreeItem element : all) {
					if (savedStr.equals(element.getText())) {
						target = element;
						break;
					}
				}
			}
			if (target == null) {
				target = tree.getItem(0);
				if (target.getItemCount() != 0) {
					target = target.getItem(0);
				}
			}
			tree.setSelection(target);
			return (CWizardHandler) target.getData();
		}
		return null;
	}

	private static void addItemsToTree(Tree tree, List<EntryDescriptor> items) {
		// Sorting is disabled because of users requests
		// Collections.sort(items, CDTListComparator.getInstance());

		ArrayList<TreeItem> placedTreeItemsList = new ArrayList<TreeItem>(items.size());
		ArrayList<EntryDescriptor> placedEntryDescriptorsList = new ArrayList<EntryDescriptor>(items.size());
		for (EntryDescriptor wd : items) {
			if (wd.getParentId() == null) {
				wd.setPath(wd.getId());
				TreeItem ti = new TreeItem(tree, SWT.NONE);
				ti.setText(TextProcessor.process(wd.getName()));
				ti.setData(wd.getHandler());
				ti.setData(DESC, wd);
				ti.setImage(calcImage(wd));
				placedTreeItemsList.add(ti);
				placedEntryDescriptorsList.add(wd);
			}
		}
		while (true) {
			boolean found = false;
			Iterator<EntryDescriptor> it2 = items.iterator();
			while (it2.hasNext()) {
				EntryDescriptor wd1 = it2.next();
				if (wd1.getParentId() == null) {
					continue;
				}
				for (int i = 0; i < placedEntryDescriptorsList.size(); i++) {
					EntryDescriptor wd2 = placedEntryDescriptorsList.get(i);
					if (wd2.getId().equals(wd1.getParentId())) {
						found = true;
						wd1.setParentId(null);
						CWizardHandler h = wd2.getHandler();
						/*
						 * If neither wd1 itself, nor its parent (wd2) have a
						 * handler associated with them, and the item is not a
						 * category, then skip it. If it's category, then it's
						 * possible that children will have a handler associated
						 * with them.
						 */
						if (h == null && wd1.getHandler() == null && !wd1.isCategory()) {
							break;
						}

						wd1.setPath(wd2.getPath() + "/" + wd1.getId()); //$NON-NLS-1$
						wd1.setParent(wd2);
						if (h != null) {
							if (wd1.getHandler() == null && !wd1.isCategory()) {
								wd1.setHandler((CWizardHandler) h.clone());
							}
							if (!h.isApplicable(wd1)) {
								break;
							}
						}

						TreeItem p = placedTreeItemsList.get(i);
						TreeItem ti = new TreeItem(p, SWT.NONE);
						ti.setText(wd1.getName());
						ti.setData(wd1.getHandler());
						ti.setData(DESC, wd1);
						ti.setImage(calcImage(wd1));
						placedTreeItemsList.add(ti);
						placedEntryDescriptorsList.add(wd1);
						break;
					}
				}
			}
			// repeat iterations until all items are placed.
			if (!found) {
				break;
			}
		}
		// orphan elements (with not-existing parentId) are ignored
	}

	private static Image calcImage(EntryDescriptor ed) {
		if (ed.getImage() != null) {
			return ed.getImage();
		}
		if (ed.isCategory()) {
			return IMG_CATEGORY;
		}
		return IMG_ITEM;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#getProvider(org.eclipse
	 * .core.resources.IProject)
	 */
	public ISyncServiceProvider getProvider(IProject project) {
		ServiceModelManager smm = ServiceModelManager.getInstance();
		IService syncService = smm.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
		RSyncServiceProvider provider = (RSyncServiceProvider) smm.getServiceProvider(syncService
				.getProviderDescriptor(RSyncServiceProvider.ID));
		provider.setLocation(fLocationText.getText());
		provider.setRemoteConnection(fSelectedConnection);
		provider.setRemoteServices(fSelectedProvider);
		provider.setProject(project);
		return provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#isConfigComplete()
	 */
	public boolean isConfigComplete() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Attempt to open a connection.
	 */
	private void checkConnection() {
		IRemoteUIConnectionManager mgr = getUIConnectionManager();
		if (mgr != null) {
			mgr.openConnectionWithProgress(fConnectionCombo.getShell(), null, fSelectedConnection);
		}
	}

	/**
	 * Return the path we are going to display. If it is a file URI then remove
	 * the file prefix.
	 * 
	 * Only do this if the connection is open. Otherwise we will attempt to
	 * connect to the first machine in the list, which is annoying.
	 * 
	 * @return String
	 */
	private String getDefaultPathDisplayString() {
		String projectName = ""; //$NON-NLS-1$
		// IWizardPage page = getWizard().getStartingPage();
		// if (page instanceof CDTMainWizardPage) {
		// projectName = ((CDTMainWizardPage) page).getProjectName();
		// }
		if (fSelectedConnection != null && fSelectedConnection.isOpen()) {
			IRemoteFileManager fileMgr = fSelectedProvider.getFileManager(fSelectedConnection);
			URI defaultURI = fileMgr.toURI(fSelectedConnection.getWorkingDirectory());

			// Handle files specially. Assume a file if there is no project to
			// query
			if (defaultURI != null && defaultURI.getScheme().equals(FILE_SCHEME)) {
				return Platform.getLocation().append(fProjectName).toOSString();
			}
			if (defaultURI == null) {
				return ""; //$NON-NLS-1$
			}
			return new Path(defaultURI.getPath()).append(fProjectName).toOSString();
		}
		return ""; //$NON-NLS-1$
	}
	
	/** 
	 * @see ISynchronizeParticipant#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}

	/**
	 * @return
	 */
	private IRemoteUIConnectionManager getUIConnectionManager() {
		IRemoteUIConnectionManager connectionManager = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fSelectedProvider)
				.getUIConnectionManager();
		return connectionManager;
	}

	/**
	 * Handle new connection selected
	 */
	private void handleConnectionSelected() {
		int selectionIndex = fConnectionCombo.getSelectionIndex();
		fSelectedConnection = fComboIndexToRemoteConnectionMap.get(selectionIndex);
		updateNewConnectionButtonEnabled(fNewConnectionButton);
		fLocationText.setText(getDefaultPathDisplayString());
	}

	/**
	 * Handle new remote services selected
	 */
	private void handleServicesSelected() {
		int selectionIndex = fProviderCombo.getSelectionIndex();
		fSelectedProvider = fComboIndexToRemoteServicesProviderMap.get(selectionIndex);
		populateConnectionCombo(fConnectionCombo);
		updateNewConnectionButtonEnabled(fNewConnectionButton);
		handleConnectionSelected();
	}

	/**
	 * @param connectionCombo
	 */
	private void populateConnectionCombo(final Combo connectionCombo) {
		connectionCombo.removeAll();

		IRemoteConnection[] connections = fSelectedProvider.getConnectionManager().getConnections();

		for (int k = 0; k < connections.length; k++) {
			connectionCombo.add(connections[k].getName(), k);
			fComboIndexToRemoteConnectionMap.put(k, connections[k]);
		}

		connectionCombo.select(0);
		fSelectedConnection = fComboIndexToRemoteConnectionMap.get(0);
	}

	/**
	 * @param button
	 */
	private void updateNewConnectionButtonEnabled(Button button) {
		IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
		button.setEnabled(connectionManager != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#setProjectName(String projectName)
	 */
	public void setProjectName(String projectName) {
		fProjectName = projectName;
		fLocationText.setText(getDefaultPathDisplayString());
	}
}
