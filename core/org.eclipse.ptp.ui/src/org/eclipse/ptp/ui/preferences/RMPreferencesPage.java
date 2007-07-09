/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Clement chu
 * 
 */
public class RMPreferencesPage extends AbstractPerferencePage {
	protected Combo remoteServices;
		
	/** Constructor
	 * 
	 */
	public RMPreferencesPage() {
		super();
		setPreferenceStore(PTPUIPlugin.getDefault().getPreferenceStore());
		setDescription(PreferenceMessages.getString("RMPreferencePage.desc"));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		remoteServices = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		remoteServices.setLayoutData(data);
		remoteServices.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
			}
		});
		updateRemoteServices();
		setValues();
		return composite;
	}
	
	private void updateRemoteServices() {
		remoteServices.removeAll();
		String[] services = PTPRemotePlugin.getDefault().getRemoteServicesNames();
		for(String name : services) {
			remoteServices.add(name);
		}
		if(services.length > 0) {
			remoteServices.select(services.length - 1);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	public void performDefaults() { 
		IPreferenceStore store = getPreferenceStore();
		remoteServices.select(store.getDefaultInt(IPTPUIConstants.RM_REMOTE_SERVICES));
		super.performDefaults();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		storeValues();
		PTPUIPlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.preferences.AbstractPerferencePage#setValues()
	 */
	protected void setValues() {
		IPreferenceStore store = getPreferenceStore();
		remoteServices.select(store.getInt(IPTPUIConstants.RM_REMOTE_SERVICES));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.preferences.AbstractPerferencePage#storeValues()
	 */
	protected void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(IPTPUIConstants.RM_REMOTE_SERVICES, remoteServices.getSelectionIndex());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#isValid()
	 */
	public boolean isValid() {
		setErrorMessage(null);
		setMessage(null);
		return true;
	}	
}
