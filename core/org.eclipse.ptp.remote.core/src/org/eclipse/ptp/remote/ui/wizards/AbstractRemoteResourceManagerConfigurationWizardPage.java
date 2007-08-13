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
package org.eclipse.ptp.remote.ui.wizards;

import java.io.File;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.remote.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteProxyOptions;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.remote.ui.Messages;
import org.eclipse.ptp.remote.ui.preferences.PreferenceConstants;
import org.eclipse.ptp.ui.utils.SWTUtil;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractRemoteResourceManagerConfigurationWizardPage extends
		RMConfigurationWizardPage {
	
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener 
	{
		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(!loading && source == serverText)
				updatePage();
		}
	
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePage();
		}
	
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton) {
				handlePathBrowseButtonSelected();
			} else {
				stdioMux = stdioButton.getSelection();
				portForwardingMux = portForwardingButton.getSelection();
				
				if (stdioMux) {
					manualButton.setEnabled(false);
					manualButton.setSelection(false);
					manualLaunch = false;
				} else {
					manualButton.setEnabled(true);
					manualLaunch = manualButton.getSelection();
				}
				
				updatePage();
			}
		}
	}

	public static final String EMPTY_STRING = "";
	private AbstractRemoteResourceManagerConfiguration config;
	private String proxyFile = EMPTY_STRING;
	private String connectionName = EMPTY_STRING;
	private String remoteServicesId = EMPTY_STRING;
	private boolean stdioMux = false;
	private boolean portForwardingMux = true;
	private boolean manualLaunch = false;
	private boolean loading = true;
	private boolean isValid;

	protected Text serverText = null;
	protected Button browseButton = null;
	protected Button noneButton = null;
	protected Button stdioButton = null;
	protected Button portForwardingButton = null;
	protected Button manualButton = null;
	protected WidgetListener listener = new WidgetListener();
	protected Button newRemoteConnectionButton;
	protected Label  connectionLabel;
	protected Combo  remoteCombo;
	protected Combo  connectionCombo;

	public AbstractRemoteResourceManagerConfigurationWizardPage(RMConfigurationWizard wizard,
			String title) {
		super(wizard, title);
		
		final RMConfigurationWizard confWizard = getConfigurationWizard();
		config = (AbstractRemoteResourceManagerConfiguration) confWizard.getConfiguration();
		setPageComplete(false);
		isValid = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
	    composite.setLayout(topLayout);

		createContents(composite, 3);
		
		loading = true;
		loadSaved();
		loading = false;
		
		defaultSetting();

		setControl(composite);
	}

	/**
	 * Get the preferences for this RM
	 * 
	 * @return RM preferences
	 */
	public abstract Preferences getPreferences();

	/**
	 * @return
	 */
	public boolean performOk() 
	{
		store();
		int options = 0;
		if (stdioMux) {
			options |= IRemoteProxyOptions.STDIO;
		}
		if (portForwardingMux) {
			options |= IRemoteProxyOptions.PORT_FORWARDING;
		}
		if (manualLaunch) {
			options |= IRemoteProxyOptions.MANUAL_LAUNCH;
		}
		config.setRemoteServicesId(remoteServicesId);
		config.setConnectionName(connectionName);
		config.setProxyServerPath(proxyFile);
		config.setOptions(options);
		config.setDefaultNameAndDesc();
		return true;
	}
	
	/**
	 * @param parent
	 * @param label
	 * @param type
	 * @return
	 */
	protected Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}
	
	/**
	 * @param parent
	 * @param label
	 * @return
	 */
	protected Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}
	
	/**
	 * Creates an new radiobutton instance and sets the default
	 * layout data.
	 *
	 * @param group  the composite in which to create the radiobutton
	 * @param label  the string to set into the radiobutton
	 * @param value  the string to identify radiobutton
	 * @return the new checkbox
	 */ 
	protected Button createRadioButton(Composite parent, String label, String value, SelectionListener listener) {
		Button button = createButton(parent, label, SWT.RADIO | SWT.LEFT);
		button.setData((null == value) ? label : value);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		button.setLayoutData(data);
		if(null != listener)
			button.addSelectionListener(listener);
		return button;
	}
	
	/**
	 * @param columns
	 * @param isEqual
	 * @param mh
	 * @param mw
	 * @return
	 */
	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw)  {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	/**
	 * @param style
	 * @param space
	 * @return
	 */
	protected GridData spanGridData(int style, int space) 
	{
		GridData gd = null;
		if (style == -1)
			gd = new GridData();
		else
			gd = new GridData(style);
		gd.horizontalSpan = space;
		return gd;
	}
	
	/**
	 * @param parent
	 * @param colSpan
	 */
	private void createContents(Composite parent, int colSpan) {
		Composite projComp = new Composite(parent, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 3;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projComp.setLayoutData(gd);

		Label label = new Label(projComp, SWT.NONE);
		label.setText(Messages.getString("RemoteConfigurationWizard.provider"));
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
		
		remoteCombo = new Combo(projComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		remoteCombo.setLayoutData(gd);
		remoteCombo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateConnectionPulldown();
			}
		});
		
		// TODO work out how to skip a cell!!!
		label = new Label(projComp, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);

		connectionLabel = new Label(projComp, SWT.NONE);
		connectionLabel.setText(Messages.getString("RemoteConfigurationWizard.location"));
		gd = new GridData();
		gd.horizontalSpan = 1;
		connectionLabel.setLayoutData(gd);
		
		connectionCombo = new Combo(projComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		connectionCombo.setLayoutData(gd);
		connectionCombo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateConnection();
			}
		});

		newRemoteConnectionButton = SWTUtil.createPushButton(projComp, Messages.getString("RemoteConfigurationWizard.newButton"), null);
		newRemoteConnectionButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent evt) {
				handleNewRemoteConnectionSelected();
				updateConnectionPulldown();
				updatePage();
			}
		});	

		updateRemotePulldown();
		
		Composite proxyComp = new Composite(parent, SWT.NONE);
		GridLayout proxyLayout = new GridLayout();
		proxyLayout.numColumns = 2;
		proxyLayout.marginHeight = 0;
		proxyLayout.marginWidth = 0;
		proxyComp.setLayout(proxyLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		proxyComp.setLayoutData(gd);
		
		Label proxyLabel = new Label(proxyComp, SWT.NONE);
		proxyLabel.setText(Messages.getString("RemoteConfigurationWizard.path"));
		gd = new GridData();
		gd.horizontalSpan = 2;
		proxyLabel.setLayoutData(gd);

		serverText = new Text(proxyComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		serverText.setLayoutData(gd);
		serverText.addModifyListener(listener);
		browseButton = SWTUtil.createPushButton(proxyComp, Messages.getString("RemoteConfigurationWizard.browseButton"), null);
		browseButton.addSelectionListener(listener);
		
		Group mxGroup = new Group(proxyComp, SWT.SHADOW_ETCHED_IN);
		mxGroup.setLayout(createGridLayout(1, true, 10, 10));
		mxGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		mxGroup.setText(Messages.getString("RemoteConfigurationWizard.mxOptions"));
		
		noneButton = createRadioButton(mxGroup, Messages.getString("RemoteConfigurationWizard.noneButton"), "mxGroup", listener);
		portForwardingButton = createRadioButton(mxGroup, Messages.getString("RemoteConfigurationWizard.portForwardingButton"), "mxGroup", listener);
		stdioButton = createRadioButton(mxGroup, Messages.getString("RemoteConfigurationWizard.stdioButton"), "mxGroup", listener);

		Group otherGroup = new Group(proxyComp, SWT.SHADOW_ETCHED_IN);
		otherGroup.setLayout(createGridLayout(1, true, 10, 10));
		otherGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		otherGroup.setText(Messages.getString("RemoteConfigurationWizard.otherOptions"));

		manualButton = createCheckButton(otherGroup, Messages.getString("RemoteConfigurationWizard.manualButton"));
		manualButton.addSelectionListener(listener);
	}

	/**
	 * 
	 */
	private void loadSaved()
	{
		Preferences preferences = getPreferences();
		
		proxyFile = preferences.getString(PreferenceConstants.PROXY_PATH);
		serverText.setText(proxyFile);
		int options = preferences.getInt(PreferenceConstants.OPTIONS);
		
		stdioMux = (options & IRemoteProxyOptions.STDIO) == IRemoteProxyOptions.STDIO;
		portForwardingMux =	(options & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING;
		manualLaunch = (options & IRemoteProxyOptions.MANUAL_LAUNCH) == IRemoteProxyOptions.MANUAL_LAUNCH;

		if (stdioMux) {
			stdioButton.setSelection(stdioMux);
		} else if (true) {
			portForwardingButton.setSelection(true);
		} else {
			noneButton.setSelection(true);
		}
		
		if (stdioMux) {
			manualButton.setEnabled(false);
			manualButton.setSelection(false);
		} else {
			manualButton.setSelection(manualLaunch);
		}
	}
	
	/**
	 * @param b
	 */
	private void setValid(boolean b) {
		isValid = b;
		setPageComplete(isValid);
	}
	
	/**
	 * 
	 */
	private void store() 
	{
		if (serverText != null) {
			proxyFile = serverText.getText();
		}
	}

	/**
	 * 
	 */
	protected void defaultSetting() 
	{
		serverText.setText(proxyFile);
	}

	/**
	 * @param text
	 * @return
	 */
	protected String getFieldContent(String text) 
	{
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
			return null;
	
		return text;
	}

	/**
	 * 
	 */
	protected void handleNewRemoteConnectionSelected() 
	{
		IRemoteServices remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(remoteServicesId);
		IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
		connMgr.newConnection(getControl().getShell());
	}

	/**
	 * Show a dialog that lets the user select a file
	 */
	protected void handlePathBrowseButtonSelected() 
	{
		IRemoteServices remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(remoteServicesId);
		IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
		IRemoteConnection connection = connMgr.getConnection(connectionName);
		IRemoteFileManager fileMgr = remoteServices.getFileManager();
		String correctPath = getFieldContent(serverText.getText());
		String selectedPath = fileMgr.browseRemoteFile(getControl().getShell(), connection, Messages.getString("RemoteConfigurationWizard.select"), correctPath);
		if (selectedPath != null) {
			serverText.setText(selectedPath);
		}
	}

	/**
	 * @return
	 */
	protected boolean isValidSetting() 
	{
		if (serverText != null) {
			String name = getFieldContent(serverText.getText());
			if (name == null) {
				setErrorMessage(Messages.getString("RemoteConfigurationWizard.invalid"));
				//setValid(false);
				return false;
			}
			else {
				File path = new File(name);
				if (!path.exists() || !path.isFile()) {
					setErrorMessage(Messages.getString("RemoteConfigurationWizard.invalid"));
					//setValid(false);
					return false;
				}
			}
		}
	
		return true;
	}
	
	/**
	 * 
	 */
	protected void updateConnection() {
		int currentSelection = connectionCombo.getSelectionIndex();
		if (currentSelection >= 0) {
			connectionName = connectionCombo.getItem(currentSelection);
		}
	}
	
	/**
	 * 
	 */
	protected void updateConnectionPulldown() {
		IRemoteServices[] allRemoteServices = PTPRemotePlugin.getDefault().getAllRemoteServices();
		int selectionIndex = remoteCombo.getSelectionIndex();
		if (allRemoteServices != null && allRemoteServices.length > 0 && selectionIndex >=0) {
			IRemoteServices selectedServices = allRemoteServices[selectionIndex];
			remoteServicesId = selectedServices.getId();
			IRemoteConnectionManager connMgr = selectedServices.getConnectionManager();
			IRemoteConnection[] connections = connMgr.getConnections();
			connectionCombo.removeAll();
			for (int i = 0; i < connections.length; i++) {
				connectionCombo.add(connections[i].getName());
			}
			if (connections.length > 0) {
				connectionCombo.select(connections.length - 1);
			}
			newRemoteConnectionButton.setEnabled(connMgr.supportsNewConnections());
		}
	}

	/**
	 * 
	 */
	protected void updatePage() 
	{
		setValid(false);
		setErrorMessage(null);
		setMessage(null);
	
		if (!isValidSetting()) {
			return;
		}
	
		performOk();
		setValid(true);
	}

	/**
	 * 
	 */
	protected void updateRemotePulldown() {
		IRemoteServices[] remoteServices = PTPRemotePlugin.getDefault().getAllRemoteServices();
		IRemoteServices defServices = PTPRemotePlugin.getDefault().getDefaultServices();
		int defIndex = remoteServices.length - 1; 
		remoteCombo.removeAll();
		for (int i = 0; i < remoteServices.length; i++) {
			remoteCombo.add(remoteServices[i].getName());
			if (remoteServices[i].equals(defServices)) {
				defIndex = i;
			}
		}
		if (remoteServices.length > 0) {
			remoteCombo.select(defIndex);
		}
	}
}
