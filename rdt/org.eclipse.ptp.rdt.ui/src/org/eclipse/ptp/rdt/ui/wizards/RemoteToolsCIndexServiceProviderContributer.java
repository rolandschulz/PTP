/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.rdt.ui.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.rdt.ui.serviceproviders.RemoteToolsCIndexServiceProvider;
import org.eclipse.ptp.rdt.ui.wizards.DStoreServerWidget.FieldModifier;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;


public class RemoteToolsCIndexServiceProviderContributer implements IServiceProviderContributor {

	private RemoteToolsCIndexServiceProvider fProvider;
	
	private Map<Integer, IRemoteServices> fComboIndexToRemoteServicesProviderMap = new HashMap<Integer, IRemoteServices>();
	private IRemoteServices fSelectedServices;
	private Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();
	private IRemoteConnection fSelectedConnection;
	private DStoreServerWidget fServerWidget;
	private String fDStorePath;
	private String fDStoreCommand;
	private String fDStoreEnv;
	private String fConfigPath;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.ui.IServiceProviderContributor#configureServiceProvider(org.eclipse.ptp.services.core.IServiceProvider, org.eclipse.swt.widgets.Composite)
	 */
	public void configureServiceProvider(IServiceProvider sp, final Composite container) {
		if(!(sp instanceof RemoteToolsCIndexServiceProvider))
			throw new IllegalArgumentException(); // should never happen
		
		fProvider = (RemoteToolsCIndexServiceProvider) sp;

		container.setLayout(new GridLayout(1, false));
		
		Group connectionGroup = new Group(container, SWT.NONE);
		connectionGroup.setText("Connection"); //$NON-NLS-1$
		connectionGroup.setLayout(new GridLayout(3, false));
		connectionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
        // Label for "Provider:"
        Label providerLabel = new Label(connectionGroup, SWT.LEFT);
        providerLabel.setText("Provider:"); //$NON-NLS-1$
        
        // combo for providers
        final Combo providerCombo = new Combo(connectionGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        // set layout to grab horizontal space
        GridData data = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
        data.horizontalSpan = 2;
        providerCombo.setLayoutData(data);
        
        populateProviderCombo(providerCombo);
            
        // connection combo
        // Label for "Connection:"
        Label connectionLabel = new Label(connectionGroup, SWT.LEFT);
        connectionLabel.setText("Connection:"); //$NON-NLS-1$
        
        // combo for providers
        final Combo connectionCombo = new Combo(connectionGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        // set layout to grab horizontal space
        connectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        // populate the combo with a list of providers
        populateConnectionCombo(connectionCombo);
           
        // new connection button
        final Button newConnectionButton = new Button(connectionGroup, SWT.PUSH);
        newConnectionButton.setText("New..."); //$NON-NLS-1$
        updateNewConnectionButtonEnabled(newConnectionButton);
        
        newConnectionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
				if(connectionManager != null) {
					connectionManager.newConnection(container.getShell());
				}
				// refresh list of connections
				populateConnectionCombo(connectionCombo);
			}
        });
        
        providerCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = providerCombo.getSelectionIndex();
				fSelectedServices = fComboIndexToRemoteServicesProviderMap.get(selectionIndex);
				populateConnectionCombo(connectionCombo);
				updateNewConnectionButtonEnabled(newConnectionButton);
				fServerWidget.setConnection(fSelectedServices, fSelectedConnection);
			}
        });
        
        connectionCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = connectionCombo.getSelectionIndex();
				fSelectedConnection = fComboIndexToRemoteConnectionMap.get(selectionIndex);
				updateNewConnectionButtonEnabled(newConnectionButton);
				fProvider.setConnection(fSelectedServices, fSelectedConnection);
				fServerWidget.setConnection(fSelectedServices, fSelectedConnection);
			}
        });
        
        fServerWidget = new DStoreServerWidget(container, SWT.NONE);
        data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.horizontalSpan = 3;
        fServerWidget.setLayoutData(data); // set layout to grab horizontal space
        
        /*
         * Set connection information before updating widget with
         * saved data.
         */
		fServerWidget.setConnection(fSelectedServices, fSelectedConnection);
		
        fServerWidget.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (e.data == FieldModifier.VALUE_DSTORE_LOCATION) {
					fDStorePath = fServerWidget.getDStoreLocation();
					fProvider.setDStoreLocation(fDStorePath);
				}
				if (e.data == FieldModifier.VALUE_DSTORE_COMMAND) {
			        fDStoreCommand = fServerWidget.getDStoreCommand();
					fProvider.setDStoreCommand(fDStoreCommand);
				}
				if (e.data == FieldModifier.VALUE_DSTORE_ENV) {
					fDStoreEnv = fServerWidget.getDStoreEnvironment();
					fProvider.setDStoreEnv(fDStoreEnv);
				}
				if (e.data == FieldModifier.VALUE_INDEX_LOCATION) {
					fConfigPath = fServerWidget.getIndexLocation();
					fProvider.setIndexLocation(fConfigPath);
				}
			}
		});
 
		/*
         * Initialize widget with default values. This will trigger
         * ModifyEvents on the widget to update the field variables.
         */
        fServerWidget.setDStoreCommand(fProvider.getDStoreCommand());
        fServerWidget.setDStoreEnvironment(fProvider.getDStoreEnv());
        fServerWidget.setDStoreLocation(fProvider.getDStoreLocation());
        fServerWidget.setIndexLocation(fProvider.getIndexLocation());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.ui.IServiceProviderContributor#getWizard(org.eclipse.ptp.services.core.IServiceProvider, org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizard getWizard(IServiceProvider provider, IWizardPage page) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.ptp.services.ui.IServiceProviderContributor#getWizardPages(org.eclipse.jface.wizard.IWizard, org.eclipse.ptp.services.core.IServiceProvider)
     */
    public WizardPage[] getWizardPages(IWizard wizard, IServiceProvider provider) {
		// TODO Auto-generated method stub
		return null;
	}
	
    private IRemoteUIConnectionManager getUIConnectionManager() {
		IRemoteUIConnectionManager connectionManager = 
			PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fSelectedServices).getUIConnectionManager();
		return connectionManager;
	}
    
	private void populateConnectionCombo(final Combo connectionCombo) {
		connectionCombo.removeAll();
		
		//attempt to restore settings from saved state
        IRemoteConnection connectionSelected = null;
	    if (fProvider.getServiceId() != null) {
		    IRemoteServices providerSelected = PTPRemoteCorePlugin.getDefault().getRemoteServices(fProvider.getServiceId()); 
	        if (providerSelected != null && fProvider.getConnectionName() != null) {
	        	connectionSelected = providerSelected.getConnectionManager().getConnection(fProvider.getConnectionName());
	        }
	    }
	    
		IRemoteConnection[] connections = fSelectedServices.getConnectionManager().getConnections();
		int toSelect = 0;
        
        for(int k = 0; k < connections.length; k++) {
        	connectionCombo.add(connections[k].getName(), k);
        	fComboIndexToRemoteConnectionMap.put(k, connections[k]);
        	if (connectionSelected != null && connectionSelected.getName().equals(connections[k].getName())) {
        		toSelect = k;
        	}
        }
        
        // set selected connection to be the first one if we're not restoring from settings
        connectionCombo.select(toSelect);
        fSelectedConnection = fComboIndexToRemoteConnectionMap.get(toSelect);
	}
	
	private void populateProviderCombo(final Combo providerCombo) {
        //attempt to restore settings from saved state
        IRemoteServices providerSelected = null;
        if (fProvider.getServiceId() != null) {
        	providerSelected = PTPRemoteCorePlugin.getDefault().getRemoteServices(fProvider.getServiceId()); 
        }

        // populate the combo with a list of providers
        IRemoteServices[] providers = PTPRemoteCorePlugin.getDefault().getAllRemoteServices();
        int toSelect = 0;
        
        for(int k = 0; k < providers.length; k++) {
        	providerCombo.add(providers[k].getName(), k);
        	fComboIndexToRemoteServicesProviderMap.put(k, providers[k]);
        	
        	if (providerSelected != null && providerSelected.getId().compareTo(providers[k].getId()) == 0) {
        		toSelect = k;
        	}
        }
        
        // set selected host to be the first one if we're not restoring from settings
        providerCombo.select(toSelect);
        fSelectedServices = fComboIndexToRemoteServicesProviderMap.get(toSelect);
	}
	
	private void updateNewConnectionButtonEnabled(Button button) {
    	IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
    	button.setEnabled(connectionManager != null);  	
    }
}
