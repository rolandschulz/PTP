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

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.internal.core.CoreMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class OMPIPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage, PreferenceConstants 
{
	public static final String EMPTY_STRING = "";

	protected Text orteServerText = null;

	protected Button browseButton = null;

	private String orteServerFile = EMPTY_STRING;

	private boolean loading = true;

	public OMPIPreferencesPage() {
		setPreferenceStore(PTPCorePlugin.getDefault().getPreferenceStore());
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener 
	{
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton)
				handlePathBrowseButtonSelected();
			else
				updatePreferencePage();
		}

		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(!loading && source == orteServerText)
				updatePreferencePage();
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePreferencePage();
		}
	}

	protected WidgetListener listener = new WidgetListener();

	protected Control createContents(Composite parent) 
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(createGridLayout(1, true, 0, 0));
		composite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));

		createORTEdContents(composite);

		loading = true;
		loadSaved();
		loading = false;
		
		defaultSetting();
		return composite;
	}

	private void createORTEdContents(Composite parent)
	{
		Group bGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		bGroup.setLayout(createGridLayout(1, true, 10, 10));
		bGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		bGroup.setText(CoreMessages.getResourceString("OMPIPreferencesPage.group_proxy"));
		
		new Label(bGroup, SWT.WRAP).setText("Enter the path to the PTP ORTE proxy server.");
		
		Composite orteserver = new Composite(bGroup, SWT.NONE);
		orteserver.setLayout(createGridLayout(3, false, 0, 0));
		orteserver.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		new Label(orteserver, SWT.NONE).setText(CoreMessages
				.getResourceString("OMPIPreferencesPage.orteServer_text"));
		orteServerText = new Text(orteserver, SWT.SINGLE | SWT.BORDER);
		orteServerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		orteServerText.addModifyListener(listener);
		browseButton = SWTUtil.createPushButton(orteserver, CoreMessages
				.getResourceString("PTPPreferencesPage.browseButton"), null);
		browseButton.addSelectionListener(listener);
	}

	protected void defaultSetting() 
	{
		orteServerText.setText(orteServerFile);
	}
	
	private void loadSaved()
	{
		Preferences preferences = PTPCorePlugin.getDefault().getPluginPreferences();
		
		orteServerFile = preferences.getString(PreferenceConstants.ORTE_SERVER_PATH);
		/* if they don't have the orte_server path set, let's try and give them a default that might help */
		if(orteServerFile.equals("")) {
			boolean found_orte_server = false;
			
			URL url = Platform.find(Platform.getBundle(PTPCorePlugin.PLUGIN_ID), new Path("/"));

			if (url != null) {
				try {
					File path = new File(Platform.asLocalURL(url).getPath());
					String ipath = path.getAbsolutePath();
					System.out.println("Plugin install dir = '"+ipath+"'");
					
					/* org.eclipse.ptp.orte.linux.x86_64_1.0.0
					   org.eclipse.ptp.orte.$(OS).$(ARCH)_$(VERSION) */
					String ptp_version = (String)PTPCorePlugin.getDefault().getBundle().getHeaders().get("Bundle-Version");
					System.out.println("PTP Version = "+ptp_version);
					Properties p = System.getProperties();
					String os = p.getProperty("osgi.os");
					String arch = p.getProperty("osgi.arch");
					System.out.println("osgi.os = "+os);
					System.out.println("osgi.arch = "+arch);
					if(os != null && arch != null && ptp_version != null) {
						String combo = "org.eclipse.ptp.core."+os+"."+arch+"_"+ptp_version;
						System.out.println("Searching for directory: "+combo);
						int idx = ipath.indexOf(combo);
						/* if we found it */
						if(idx > 0) {
							String ipath2 = ipath.substring(0, idx) + "org.eclipse.ptp.orte."+os+"."+arch+"_"+ptp_version+"/orte_server";
							File f = new File(ipath2);
							if(f.exists()) {
								orteServerFile = ipath2;
								found_orte_server = true;
							}
						}
					}
					
					if(!found_orte_server) {
						int idx = ipath.indexOf("org.eclipse.ptp.core");
						String ipath2 = ipath.substring(0, idx) + "org.eclipse.ptp.orte/orte_server";
						System.out.println("Searching for : "+ipath2);
						File f = new File(ipath2);
						if(f.exists()) {
							found_orte_server = true;
						}
						orteServerFile = ipath2;
					}
				} catch(Exception e) { 
				}
			}
        }
		
		orteServerText.setText(orteServerFile);
	}

	public void init(IWorkbench workbench) 
	{
	}

	public void dispose() 
	{
		super.dispose();
	}

	public void performDefaults() 
	{
		defaultSetting();
		updateApplyButton();
	}

	private void store() 
	{
		orteServerFile = orteServerText.getText();
	}

	public boolean performOk() 
	{
		store();
		Preferences preferences = PTPCorePlugin.getDefault()
				.getPluginPreferences();

		preferences.setValue(PreferenceConstants.ORTE_SERVER_PATH, orteServerFile);

		PTPCorePlugin.getDefault().savePluginPreferences();

		return true;
	}

	/**
	 * Show a dialog that lets the user select a file
	 */
	protected void handlePathBrowseButtonSelected() 
	{
		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(CoreMessages
				.getResourceString("OMPIPreferencesPage.Select_ORTE_PROXY_FILE"));
		String correctPath = getFieldContent(orteServerText.getText());
		if (correctPath != null) {
			File path = new File(correctPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile() ? correctPath : path
						.getParent());
		}

		String selectedPath = dialog.open();
		if (selectedPath != null)
			orteServerText.setText(selectedPath);
	}

	protected boolean isValidORTEdSetting() 
	{
		String name = getFieldContent(orteServerText.getText());
		if (name == null) {
			setErrorMessage(CoreMessages
					.getResourceString("OMPIPreferencesPage.Incorrect_server_file"));
			//setValid(false);
			//return false;
		}
		else {
			File path = new File(name);
			if (!path.exists() || !path.isFile()) {
				setErrorMessage(CoreMessages
					.getResourceString("OMPIPreferencesPage.Incorrect_server_file"));
				//setValid(false);
				//return false;
			}
		}

		return true;
	}
	
	protected void updatePreferencePage() 
	{
		setErrorMessage(null);
		setMessage(null);

		if (!isValidORTEdSetting())
			return;

		performOk();
		setValid(true);
	}

	protected String getFieldContent(String text) 
	{
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
			return null;

		return text;
	}

	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw)  {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

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
}
