/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Roland Schulz, University of Tennessee
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor;
import org.eclipse.ptp.rdt.sync.ui.SyncFileFilterPage;
import org.eclipse.ptp.rdt.sync.ui.SynchronizeParticipantRegistry;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * 
 */
public class NewRemoteSyncProjectWizardPage extends MBSCustomPage {
	public static final String REMOTE_SYNC_WIZARD_PAGE_ID = "org.eclipse.ptp.rdt.sync.ui.remoteSyncWizardPage"; //$NON-NLS-1$
	public static final String SERVICE_PROVIDER_PROPERTY = "org.eclipse.ptp.rdt.sync.ui.remoteSyncWizardPage.serviceProvider"; //$NON-NLS-1$
	public static final String SYNC_FILE_FILTER_PROPERTY = "org.eclipse.ptp.rdt.sync.ui.remoteSyncWizardPage.syncFileFilter"; //$NON-NLS-1$

	private SyncFileFilter customFilter = null;
	private boolean fbVisited;
	private String fTitle;
	private String fDescription;
	private ImageDescriptor fImageDescriptor;
	private Image fImage;
	private ISynchronizeParticipantDescriptor fSelectedProvider;

	private Control pageControl;
	private Combo fProviderCombo;
	private Composite fProviderArea;
	private StackLayout fProviderStack;
	private final List<Composite> fProviderControls = new ArrayList<Composite>();
	private final Map<Integer, ISynchronizeParticipantDescriptor> fComboIndexToDescriptorMap = new HashMap<Integer, ISynchronizeParticipantDescriptor>();

	/**
	 * 
	 */
	public NewRemoteSyncProjectWizardPage() {
		this(REMOTE_SYNC_WIZARD_PAGE_ID);
	}

	public NewRemoteSyncProjectWizardPage(String pageID) {
		super(pageID);
	}

	private void addProviderControl(ISynchronizeParticipantDescriptor desc) {
		Composite comp = null;
		ISynchronizeParticipant part = desc.getParticipant();
		if (part != null) {
			comp = new Composite(fProviderArea, SWT.NONE);
			comp.setLayout(new GridLayout(1, false));
			comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			part.createConfigurationArea(comp, getWizard().getContainer());
		}
		fProviderControls.add(comp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(final Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		pageControl = comp;
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		comp.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		comp.setLayoutData(gd);

		// Label for "Provider:"
		Label providerLabel = new Label(comp, SWT.LEFT);
		providerLabel.setText(Messages.NewRemoteSyncProjectWizardPage_syncProvider);

		// combo for providers
		fProviderCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		// set layout to grab horizontal space
		fProviderCombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProviderCombo.setLayoutData(gd);
		fProviderCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleProviderSelected();
			}
		});

		fProviderArea = new Group(comp, SWT.SHADOW_ETCHED_IN);
		fProviderStack = new StackLayout();
		fProviderArea.setLayout(fProviderStack);
		GridData providerAreaData = new GridData(SWT.FILL, SWT.FILL, true, true);
		providerAreaData.horizontalSpan = 3;
		fProviderArea.setLayoutData(providerAreaData);

		// populate the combo with a list of providers
		ISynchronizeParticipantDescriptor[] providers = SynchronizeParticipantRegistry.getDescriptors();

		for (int k = 0; k < providers.length; k++) {
			fProviderCombo.add(providers[k].getName(), k);
			fComboIndexToDescriptorMap.put(k, providers[k]);
			addProviderControl(providers[k]);
		}

		fProviderCombo.select(0);
		handleProviderSelected();
		
		final Button filterButton = new Button(comp, SWT.PUSH);
		filterButton.setText(Messages.NewRemoteSyncProjectWizardPage_0);
		filterButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 3, 1));
		filterButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				SyncFileFilter tmpFilter;
				if (customFilter == null) {
					tmpFilter = SyncManager.getDefaultFileFilter();
				} else {
					tmpFilter = new SyncFileFilter(customFilter);
				}
				int filterReturnCode = SyncFileFilterPage.openBlocking(tmpFilter, filterButton.getShell());
				if (filterReturnCode == Window.OK) {
					customFilter = tmpFilter;
					MBSCustomPageManager.addPageProperty(REMOTE_SYNC_WIZARD_PAGE_ID, SYNC_FILE_FILTER_PROPERTY, customFilter);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub
	}

	/**
	 * Find available remote services and service providers for a given project
	 * 
	 * If project is null, the C and C++ natures are used to determine which
	 * services are available
	 */
	protected Set<IService> getContributedServices() {
		ServiceModelManager smm = ServiceModelManager.getInstance();
		Set<IService> cppServices = smm.getServices(CCProjectNature.CC_NATURE_ID);
		Set<IService> cServices = smm.getServices(CProjectNature.C_NATURE_ID);

		Set<IService> allApplicableServices = new LinkedHashSet<IService>();
		allApplicableServices.addAll(cppServices);
		allApplicableServices.addAll(cServices);

		return allApplicableServices;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getControl()
	 */
	public Control getControl() {
		return pageControl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
	 */
	public String getDescription() {
		if (fDescription == null) {
			fDescription = Messages.RemoteSyncWizardPage_description;
		}
		return fDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	public String getErrorMessage() {
		if (fSelectedProvider==null)
			return Messages.ConvertToSyncProjectWizardPage_0; 
		else 
			return fSelectedProvider.getParticipant().getErrorMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	public Image getImage() {
		if (fImage == null && fImageDescriptor != null) {
			fImage = fImageDescriptor.createImage();
		}

		if (fImage == null && wizard != null) {
			fImage = wizard.getDefaultPageImage();
		}

		return fImage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessage()
	 */
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 */
	public String getName() {
		return Messages.RemoteSyncWizardPage_0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
	 */
	public String getTitle() {
		if (fTitle == null) {
			fTitle = Messages.RemoteSyncWizardPage_0;
		}
		return fTitle;
	}

	/**
	 * Handle synchronize provider selected.
	 */
	private void handleProviderSelected() {
		int index = fProviderCombo.getSelectionIndex();
		fProviderStack.topControl = fProviderControls.get(index);
		fSelectedProvider = fComboIndexToDescriptorMap.get(index);
		fProviderArea.layout();
		update();
		if (fSelectedProvider != null) {
			MBSCustomPageManager.addPageProperty(REMOTE_SYNC_WIZARD_PAGE_ID, SERVICE_PROVIDER_PROPERTY,
					fSelectedProvider.getParticipant());
		} else {
			MBSCustomPageManager.addPageProperty(REMOTE_SYNC_WIZARD_PAGE_ID, SERVICE_PROVIDER_PROPERTY, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage#isCustomPageComplete
	 * ()
	 */
	@Override
	protected boolean isCustomPageComplete() {
		return fbVisited  && getErrorMessage()==null && fSelectedProvider.getParticipant().isConfigComplete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#performHelp()
	 */
	public void performHelp() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		fDescription = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#setImageDescriptor(org.eclipse.
	 * jface.resource.ImageDescriptor)
	 */
	public void setImageDescriptor(ImageDescriptor image) {
		fImageDescriptor = image;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		fTitle = title;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			fbVisited = true;
		}
	}

	private void update() {
		getWizard().getContainer().updateMessage();
		getWizard().getContainer().updateButtons();
	}
}
