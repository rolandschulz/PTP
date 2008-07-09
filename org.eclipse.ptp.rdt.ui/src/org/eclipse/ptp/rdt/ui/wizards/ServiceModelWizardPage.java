/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
import org.eclipse.ptp.rdt.services.ui.IServiceProviderConfiguration;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author crecoskie
 *
 */
public class ServiceModelWizardPage extends MBSCustomPage {

	public static final String SERVICE_MODEL_WIZARD_PAGE_ID = "org.eclipse.ptp.rdt.ui.serviceModelWizardPage"; //$NON-NLS-1$
	public static final String SELECTED_PROVIDERS_MAP_PROPERTY = "org.eclipse.ptp.rdt.ui.ServiceModelWizardPage.selectedProviders"; //$NON-NLS-1$
	public static final String ID_TO_PROVIDERS_MAP_PROPERTY = "org.eclipse.ptp.rdt.ui.ServiceModelWizardPage.providersMap"; //$NON-NLS-1$
	
	private static final String PROVIDER_KEY = "provider-id"; //$NON-NLS-1$
	private static final String SERVICE_KEY = "service-id"; //$NON-NLS-1$
	
	boolean fbVisited;
	
	private Map<String, String> fServiceIDToSelectedProviderID;
	private Map<String, IServiceProvider> fProviderIDToProviderMap;

	private Table fTable;
	private Button fConfigureButton;
	
	private String fTitle;
	
	private String fDescription;
	
	private ImageDescriptor fImageDescriptor;
	
	private Image fImage;
	
	/**
	 * @param pageID
	 */
	public ServiceModelWizardPage(String pageID) {
		super(pageID);
		fServiceIDToSelectedProviderID = new HashMap<String, String>();
		MBSCustomPageManager.addPageProperty(pageID, SELECTED_PROVIDERS_MAP_PROPERTY, fServiceIDToSelectedProviderID);
		fProviderIDToProviderMap = new HashMap<String, IServiceProvider>();
		MBSCustomPageManager.addPageProperty(pageID, ID_TO_PROVIDERS_MAP_PROPERTY, fProviderIDToProviderMap);
	}

	/**
	 * 
	 */
	public ServiceModelWizardPage() {
		this(SERVICE_MODEL_WIZARD_PAGE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage#isCustomPageComplete()
	 */
	protected boolean isCustomPageComplete() {
		return fbVisited;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 */
	public String getName() {
		return Messages.getString("ServiceModelWizardPage_0"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite canvas = new Composite(parent, SWT.NONE);
		GridLayout canvasLayout = new GridLayout(2, false);
		canvas.setLayout(canvasLayout);
		
		Composite tableParent = new Composite(canvas, SWT.NONE);
		tableParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// FIXME: Layout problem in composite above; bottom/right edges being clipped
		
		fTable = new Table (tableParent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		fTable.setLinesVisible (true);
		fTable.setHeaderVisible (true);
		
		TableColumnLayout layout = new TableColumnLayout();
		// create the columns and headers... note third column holds "Configure..." buttons and hence has no title.
		String[] titles = {Messages.getString("ServiceModelWizardPage.0"), Messages.getString("ServiceModelWizardPage.1")}; //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0; i<titles.length; i++) {
			TableColumn column = new TableColumn (fTable, SWT.NONE);
			column.setText (titles [i]);
			layout.setColumnData(column, new ColumnWeightData(1, true));
		}
		tableParent.setLayout(layout);
		fTable.setLayout(new FillLayout());
		
		// get the contributed services... we need one row for each
		ServiceModelManager modelManager = ServiceModelManager.getInstance();
		
		Set<IService> cppServices = modelManager.getServices(CCProjectNature.CC_NATURE_ID);
		Set<IService> cServices = modelManager.getServices(CProjectNature.C_NATURE_ID);
		
		Set<IService> allApplicableServices = new LinkedHashSet<IService>(cppServices);
		allApplicableServices.addAll(cServices);
		
		Iterator<IService> iterator = allApplicableServices.iterator();

		while(iterator.hasNext()) {
			final IService service = iterator.next();			
			
			TableItem item = new TableItem (fTable, SWT.NONE);

			// column 0 lists the name of the service
			item.setText (0, service.getName());
			item.setData(SERVICE_KEY, service);
			
			// column 1 holds a dropdown with a list of providers
			IServiceProviderDescriptor descriptor = service.getProviders().iterator().next();
			item.setText(1, descriptor.getName());
			item.setData(PROVIDER_KEY, descriptor);
			
			fServiceIDToSelectedProviderID.put(service.getId(), descriptor.getId());
		}
		
		fTable.setVisible(true);
		
		final TableEditor editor = new TableEditor(fTable);
		editor.horizontalAlignment = SWT.BEGINNING;
		editor.grabHorizontal = true;
		fTable.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				int selectionIndex = fTable.getSelectionIndex();
				if (selectionIndex == -1) {
					fConfigureButton.setEnabled(false);
					return;
				}
				fConfigureButton.setEnabled(true);
				final TableItem item = fTable.getItem(selectionIndex);
				IService service = (IService) item.getData(SERVICE_KEY);
				IServiceProviderDescriptor provider = (IServiceProviderDescriptor) item.getData(PROVIDER_KEY);
				final CCombo combo = new CCombo(fTable, SWT.READ_ONLY);
				
				// populate with list of providers
				Set<IServiceProviderDescriptor> providers = service.getProviders();
				Iterator<IServiceProviderDescriptor> providerIterator = providers.iterator();
				
				int index = 0;
				final List<IServiceProviderDescriptor> providerIds = new LinkedList<IServiceProviderDescriptor>();
				while(providerIterator.hasNext()) {
					IServiceProviderDescriptor descriptor = providerIterator.next();
					combo.add(descriptor.getName(), index);
					providerIds.add(descriptor);
					if (descriptor.equals(provider)) {
						combo.select(index);
					}
					++index;
				}
				
				combo.setFocus();
				Listener listener = new Listener() {
					public void handleEvent(Event event) {
						switch (event.type) {
						case SWT.FocusOut:
							combo.dispose();
							break;
						case SWT.Selection:
							int selection = combo.getSelectionIndex();
							if (selection == -1) {
								return;
							}
							IServiceProviderDescriptor descriptor = providerIds.get(selection);
							item.setText(1, descriptor.getName());
							IService service = (IService) item.getData(SERVICE_KEY);
							item.setData(PROVIDER_KEY, descriptor);
							
							fServiceIDToSelectedProviderID.put(service.getId(), descriptor.getId());
							combo.dispose();
							break;
						}
					}
				};
				combo.addListener(SWT.FocusOut, listener);
				combo.addListener(SWT.Selection, listener);

				editor.setEditor(combo, item, 1);
			}
		});
		
		fConfigureButton = new Button(canvas, SWT.PUSH);
		fConfigureButton.setEnabled(false);
		fConfigureButton.setText(Messages.getString("ServiceModelWizardPage.2")); //$NON-NLS-1$
		fConfigureButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		Listener configureListener = new Listener() {
			public void handleEvent(Event event) {
				// launch the configuration UI for the service provider
				TableItem[] selection = fTable.getSelection();
				if (selection.length == 0) {
					return;
				}
				TableItem item = selection[0];
				ServiceModelManager manager = ServiceModelManager.getInstance();
				IServiceProviderDescriptor descriptor = (IServiceProviderDescriptor) item.getData(PROVIDER_KEY);
				IServiceProvider serviceProvider = manager.getServiceProvider(descriptor);
				fProviderIDToProviderMap.put(descriptor.getId(), serviceProvider);

				IServiceProviderConfiguration configUI = manager.getServiceProviderConfigurationUI(serviceProvider);
				configUI.configureServiceProvider(serviceProvider, fConfigureButton.getShell());
			}
		};
		fConfigureButton.addListener(SWT.Selection, configureListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getControl()
	 */
	public Control getControl() {
		return fTable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
	 */
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	public Image getImage() {
		if(fImage == null && fImageDescriptor != null)
			fImage = fImageDescriptor.createImage();
		
		return fImage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessage()
	 */
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
	 */
	public String getTitle() {
		return fTitle;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#performHelp()
	 */
	public void performHelp() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		fDescription = description;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void setImageDescriptor(ImageDescriptor image) {
		fImageDescriptor = image;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		fTitle = title;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if(visible) {
			fbVisited = true;
		}
		
		fTable.setVisible(visible);

	}

}
