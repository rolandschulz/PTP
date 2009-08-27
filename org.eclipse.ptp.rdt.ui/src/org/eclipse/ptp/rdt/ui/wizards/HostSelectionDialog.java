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
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.rdt.ui.serviceproviders.RemoteCIndexServiceProvider;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.ui.wizards.newconnection.RSEDefaultNewConnectionWizard;
import org.eclipse.rse.ui.wizards.newconnection.RSEMainNewConnectionWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
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
 * Provides a dialog which allows you to select an RSE host for the RemoteCIndexServiceProvider.
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 *
 */
public class HostSelectionDialog extends Dialog {
	
	private RemoteCIndexServiceProvider fProvider;
	
	private Map<Integer, IHost> fHostComboIndexToHostMap = new HashMap<Integer, IHost>();
	
	private IHost fSelectedHost;
	private String configPath;

	public HostSelectionDialog(IServiceProvider provider, Shell parentShell) {
		super(parentShell);
		
		if(provider instanceof RemoteCIndexServiceProvider)
			fProvider = (RemoteCIndexServiceProvider) provider;
		else
			throw new IllegalArgumentException(); // should never happen
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        getShell().setText(Messages.getString("HostSelectionDialog.1")); //$NON-NLS-1$
		
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        container.setLayout(layout);
        
        // Label for "Host:"
        Label hostLabel = new Label(container, SWT.LEFT);
        hostLabel.setText(Messages.getString("HostSelectionDialog_0")); //$NON-NLS-1$
        
        // combo for hosts
        final Combo hostCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        hostCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false)); // set layout to grab horizontal space
        
        //attempt to restore settings from saved state
        IHost hostSelected = fProvider.getHost();
        
        // populate the combo with a list of hosts
        IHost[] hosts = SystemStartHere.getConnections();
        int toSelect = 0;
        
        for(int k = 0; k < hosts.length; k++) {
        	hostCombo.add(hosts[k].getAliasName(), k);
        	fHostComboIndexToHostMap.put(k, hosts[k]);
        	
        	if (hostSelected != null && hostSelected.getAliasName().compareTo(hosts[k].getAliasName()) == 0) {
        		toSelect = k;
        	}
        }
        
        // set selected host to be the first one if we're not restoring from settings
        hostCombo.select(toSelect);
        fSelectedHost = fHostComboIndexToHostMap.get(toSelect);
        
        
        
        // button for creating new connections
        Button newConnectionButton = new Button(container, SWT.PUSH);
        newConnectionButton.setText(Messages.getString("HostSelectionDialog.0")); //$NON-NLS-1$
        newConnectionButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				// launch the RSE New Connection Wizard
				RSEMainNewConnectionWizard wizard = new RSEMainNewConnectionWizard();
				WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
				wizardDialog.open();
				
				IWizard actualWizard = wizard.getSelectedWizard();
				if(actualWizard instanceof RSEDefaultNewConnectionWizard) {
					// get the new host, if any
					IHost host = ((RSEDefaultNewConnectionWizard)actualWizard).getCreatedHost();
					
					// add the host
					int index = hostCombo.getItemCount() - 1;
					hostCombo.add(host.getAliasName(), index);
		        	fHostComboIndexToHostMap.put(index, host);
		        	
		        	// select the new host
		        	hostCombo.select(index);
		            fSelectedHost = host;
				}
			}
        	
        });
        
        configPath = fProvider.getIndexLocation();
        if(fProvider.isConfigured() && configPath == null) // happens if the project was created before the index location feature was added
        	configPath = ""; //$NON-NLS-1$
      
        final IndexFileLocationWidget scopeWidget = new IndexFileLocationWidget(container, SWT.NONE, fSelectedHost, configPath);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.horizontalSpan = 3;
        scopeWidget.setLayoutData(data); // set layout to grab horizontal space
        scopeWidget.addPathListener(new IIndexFilePathChangeListener() {
			public void pathChanged(String newPath) {
				configPath = newPath;
			}
		});
        
        
        hostCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = hostCombo.getSelectionIndex();
				fSelectedHost = fHostComboIndexToHostMap.get(selectionIndex);
				scopeWidget.setHost(fSelectedHost);
			}
        });
        
        return container;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
     */
    protected Point getInitialSize() {
        return new Point(500,300);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		super.okPressed();
		
		// set the host for the service provider
		fProvider.setConnection(fSelectedHost, getDStoreConnectorService(fSelectedHost));
		fProvider.setIndexLocation(configPath);
		fProvider.setConfigured(true);
	}

	private IConnectorService getDStoreConnectorService(IHost host) {
		IConnectorService[] connectorServices = host.getConnectorServices();
		
		for(int k = 0; k < connectorServices.length; k++) {
			if(connectorServices[k] instanceof DStoreConnectorService)
				return connectorServices[k];
		}
		
		return null;
	}

    
    
}
