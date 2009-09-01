/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
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
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.rdt.ui.serviceproviders.RemoteBuildServiceProvider;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Allows the user to select a provider of Remote Services for a RemoteBuildServiceProvider.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 * @see org.eclipse.ptp.rdt.ui.serviceproviders.RemoteBuildServiceProvider
 */
public class RemoteServicesProviderSelectionDialog extends Dialog {

	
	private RemoteBuildServiceProvider fProvider;
	
	private Map<Integer, IRemoteServices> fComboIndexToRemoteServicesProviderMap = new HashMap<Integer, IRemoteServices>();
	
	private IRemoteServices fSelectedProvider;

	private Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();

	private IRemoteConnection fSelectedConnection;

	public RemoteServicesProviderSelectionDialog(IServiceProvider provider, Shell parentShell) {
		super(parentShell);
		
		if(provider instanceof RemoteBuildServiceProvider)
			fProvider = (RemoteBuildServiceProvider) provider;
		else
			throw new IllegalArgumentException(); // should never happen
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        getShell().setText(Messages.getString("RemoteServicesProviderSelectionDialog_0")); //$NON-NLS-1$
		
        GridLayout layout = new GridLayout();
        layout.numColumns = 5;
        container.setLayout(layout);
        
        // Label for "Provider:"
        Label providerLabel = new Label(container, SWT.LEFT);
        providerLabel.setText(Messages.getString("RemoteServicesProviderSelectionDialog_1")); //$NON-NLS-1$
        
        // combo for providers
        final Combo providerCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        // set layout to grab horizontal space
        providerCombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        
        //attempt to restore settings from saved state
        IRemoteServices providerSelected = fProvider.getRemoteServices(); 
        
        // populate the combo with a list of providers
        IRemoteServices[] providers = PTPRemoteCorePlugin.getDefault().getAllRemoteServices();
        int toSelect = 0;
        
        for(int k = 0; k < providers.length; k++) {
        	providerCombo.add(providers[k].getName(), k);
        	fComboIndexToRemoteServicesProviderMap.put(k, providers[k]);
        	
        	if (providerSelected != null && providerSelected.getName().compareTo(providers[k].getName()) == 0) {
        		toSelect = k;
        	}
        }
        
        // set selected host to be the first one if we're not restoring from settings
        providerCombo.select(toSelect);
        fSelectedProvider = fComboIndexToRemoteServicesProviderMap.get(toSelect);
            
        // connection combo
        // Label for "Connection:"
        Label connectionLabel = new Label(container, SWT.LEFT);
        connectionLabel.setText(Messages.getString("RemoteServicesProviderSelectionDialog.0")); //$NON-NLS-1$
        
        // combo for providers
        final Combo connectionCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        // set layout to grab horizontal space
        connectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        // populate the combo with a list of providers
        populateConnectionCombo(connectionCombo);
           
        // new connection button
        final Button newConnectionButton = new Button(container, SWT.PUSH);
        newConnectionButton.setText(Messages.getString("RemoteServicesProviderSelectionDialog.1")); //$NON-NLS-1$
        updateNewConnectionButtonEnabled(newConnectionButton);
        
        
        newConnectionButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
				
			}

			public void widgetSelected(SelectionEvent e) {
				
				IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
				if(connectionManager != null) {
					connectionManager.newConnection(getShell());
				}
				
				// refresh list of connections
				populateConnectionCombo(connectionCombo);
				
			}
        	
        });
        
        providerCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
				
			}

			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = providerCombo.getSelectionIndex();
				fSelectedProvider = fComboIndexToRemoteServicesProviderMap.get(selectionIndex);
				
				populateConnectionCombo(connectionCombo);
				updateNewConnectionButtonEnabled(newConnectionButton);
				
			}
        	
        });
        
        connectionCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
				
			}

			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = connectionCombo.getSelectionIndex();
				fSelectedConnection = fComboIndexToRemoteConnectionMap.get(selectionIndex);
				updateNewConnectionButtonEnabled(newConnectionButton);
			}
        	
        });
        
        
        return container;
    }
    
    
    private void updateNewConnectionButtonEnabled(Button button) {
    	IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
    	button.setEnabled(connectionManager != null);  	
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
	 * @param connectionCombo
	 */
	private void populateConnectionCombo(final Combo connectionCombo) {
		connectionCombo.removeAll();
		
		//attempt to restore settings from saved state
        IRemoteConnection connectionSelected = fProvider.getConnection();
		
		IRemoteConnection[] connections = fSelectedProvider.getConnectionManager().getConnections();
		int toSelect = 0;
        
        for(int k = 0; k < connections.length; k++) {
        	connectionCombo.add(connections[k].getName(), k);
        	fComboIndexToRemoteConnectionMap .put(k, connections[k]);
        	
        	if (connectionSelected != null && connectionSelected.getName().compareTo(connections[k].getName()) == 0) {
        		toSelect = k;
        	}
        }
        
        // set selected connection to be the first one if we're not restoring from settings
        connectionCombo.select(toSelect);
        fSelectedConnection = fComboIndexToRemoteConnectionMap.get(toSelect);
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID,
            IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
            IDialogConstants.CANCEL_LABEL, false);
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
     */
    protected Point getInitialSize() {
        return new Point(750, 125);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		super.okPressed();
		
		// set the provider
		fProvider.setRemoteToolsProviderID(fSelectedProvider.getId());
		fProvider.setRemoteToolsConnection(fSelectedConnection);

	}

    
}
