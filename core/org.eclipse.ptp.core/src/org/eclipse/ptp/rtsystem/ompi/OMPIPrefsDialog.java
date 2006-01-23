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

package org.eclipse.ptp.rtsystem.ompi;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.internal.core.CoreMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class OMPIPrefsDialog extends Dialog 
{
	public static final String EMPTY_STRING = "";

	protected Text ortedPathText = null;
	protected Text orteServerText = null;
	protected Text errorText = null;

	protected Button ok = null;
	protected Button cancel = null;
	protected Button browseButton = null;
	protected Button browseButton2 = null;

	private String defaultOrtedArgs = "--scope public --seed --persistent";
	private String ortedFile = EMPTY_STRING;
	private String orteServerFile = EMPTY_STRING;
	
	private boolean loading = true;

	
	public OMPIPrefsDialog(Shell parent) {
		/* we DON'T want this to be modal */
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}
	
	public OMPIPrefsDialog(Shell parent, int style) {
		super(parent, style);
		setText("ORTE Preferences");
	}
	
	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener 
	{
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseButton)
				handlePathBrowseButtonSelected();
			else if (source == browseButton2)
				handlePathBrowseButtonSelected2();
			else
				updatePreferencePage();
		}

		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(!loading && (source == ortedPathText || source == orteServerText))
				updatePreferencePage();
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePreferencePage();
		}
	}

	protected WidgetListener listener = new WidgetListener();
	
	public String open() {
		Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
		shell.setText(getText());
		createContents(shell);
		shell.pack();
		Point p = shell.getSize();
		shell.setSize((p.x) * 6/5, p.y);
		shell.open();
		Display display = getParent().getDisplay();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return null;
	}
	
	private void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(1, true));
		
		shell.setLayout(createGridLayout(1, true, 10, 10));
		shell.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));

		createORTEdContents(shell);
		
		loading = true;
		loadSaved();
		loading = false;
		isValidORTEdSetting();
	}

	private void createORTEdContents(final Shell parent)
	{
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(1, true, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText(CoreMessages.getResourceString("OMPIPreferencesPage.group_orted"));
		
		Label ortedComment = new Label(aGroup, SWT.WRAP);
		ortedComment.setText("Enter the path to the Open Runtime Environment Daemon (ORTEd).");
		ortedComment = new Label(aGroup, SWT.WRAP);
		ortedComment.setText("PTP will take care of starting and stopping this as necessary to interface to the ORTE.");
		
		Composite ortedFilecomposite = new Composite(aGroup, SWT.NONE);
		ortedFilecomposite.setLayout(createGridLayout(3, false, 0, 0));
		ortedFilecomposite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		new Label(ortedFilecomposite, SWT.NONE).setText(CoreMessages
				.getResourceString("OMPIPreferencesPage.ortedFile_text"));
		ortedPathText = new Text(ortedFilecomposite, SWT.SINGLE | SWT.BORDER);
		ortedPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ortedPathText.addModifyListener(listener);
		browseButton = SWTUtil.createPushButton(ortedFilecomposite, CoreMessages
				.getResourceString("PTPPreferencesPage.browseButton"), null);
		browseButton.addSelectionListener(listener);
		
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
		browseButton2 = SWTUtil.createPushButton(orteserver, CoreMessages
				.getResourceString("PTPPreferencesPage.browseButton"), null);
		browseButton2.addSelectionListener(listener);
	

		errorText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		errorText.setForeground(new Color(null, 255, 0, 0));
		errorText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Group cGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		cGroup.setLayout(createGridLayout(2, true, 10, 10));
		cGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		
        cancel = new Button(cGroup, SWT.PUSH);
        cancel.setText("Cancel");
		cancel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
        cancel.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent event) {
        		parent.dispose();
        	}
        });
        
        ok = new Button(cGroup, SWT.PUSH);
        ok.setText("OK");
        ok.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        ok.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent event) {
        		ortedFile = ortedPathText.getText();
        		String ortedArgs = defaultOrtedArgs;
        		orteServerFile = orteServerText.getText();
        		Preferences preferences = PTPCorePlugin.getDefault()
        				.getPluginPreferences();

        		preferences.setValue(PreferenceConstants.ORTE_ORTED_PATH, ortedFile);
        		preferences.setValue(PreferenceConstants.ORTE_ORTED_ARGS, ortedArgs);
        		preferences.setValue(PreferenceConstants.ORTE_SERVER_PATH, orteServerFile);

        		PTPCorePlugin.getDefault().savePluginPreferences();

        		parent.dispose();
        	}
        });
                    
        //parent.setDefaultButton(ca);
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
								orteServerText.setText(ipath2);
								found_orte_server = true;
							}
						}
					}
					
					if(!found_orte_server) {
						int idx = ipath.indexOf("org.eclipse.ptp.core");
						String ipath2 = ipath.substring(0, idx) + "org.eclipse.ptp.orte/orte_server";
						File f = new File(ipath2);
						if(f.exists()) {
							orteServerText.setText(ipath2);
							found_orte_server = true;
						}
						else {
							orteServerText.setText(ipath2);
						}
					}
				} catch(Exception e) { 
					orteServerText.setText("");
				}
			}
			else {
				orteServerText.setText("");
			}
        }
		else {
			orteServerText.setText(orteServerFile);
		}

		ortedFile = preferences.getString(PreferenceConstants.ORTE_ORTED_PATH);
		
		if(ortedFile.equals("")) {
			ortedPathText.setText("");
			
			try {
				Properties p=new Properties();
				Process pro = Runtime.getRuntime().exec("env");
				InputStream in = pro.getInputStream();
				p.load(in);
				
				String path = p.getProperty("PATH");
		    
				//String path = System.getenv("PATH");
				System.out.println("PATH = '"+path+"'");
				if(path != null) {
					String[] splits = path.split(":");
					for(int i=0; i<splits.length; i++) {
						//System.out.println(i+": '"+splits[i]+"'");
						File f = new File(splits[i] + "/orted");
						if(f.exists()) {
							System.out.println("Found orted in path: '"+splits[i]+"'");
							ortedPathText.setText(splits[i] + "/orted");
							break;
						}
					}
				}			
			} catch(Exception e) { }

		}
		else {
			ortedPathText.setText(ortedFile);
		}
	}
	
	public Properties executenv(String command) throws Exception{
	    Properties p=new Properties();
	    Process pro = Runtime.getRuntime().exec("env");
	    InputStream in = pro.getInputStream();
	    p.load(in);
	    return p;
	} 
	
	protected void handlePathBrowseButtonSelected() 
	{
		FileDialog dialog = new FileDialog(getParent());
		dialog.setText(CoreMessages
				.getResourceString("OMPIPreferencesPage.Select_ORTEd_FILE"));
		String correctPath = getFieldContent(ortedPathText.getText());
		if (correctPath != null) {
			File path = new File(correctPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile() ? correctPath : path
						.getParent());
		}

		String selectedPath = dialog.open();
		if (selectedPath != null)
			ortedPathText.setText(selectedPath);
	}
	
	protected void handlePathBrowseButtonSelected2() 
	{
		FileDialog dialog = new FileDialog(getParent());
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
	
	
	protected void updatePreferencePage() 
	{
		setErrorMessage("");

		if (!isValidORTEdSetting())
			return;
	}

	protected boolean isValidORTEdSetting() 
	{
		String name = getFieldContent(ortedPathText.getText());
		if (name == null) {
			setErrorMessage(CoreMessages
					.getResourceString("OMPIPreferencesPage.Incorrect_ORTEd_file"));
			//setValid(false);
			//return false;
		}
		else {
			File path = new File(name);
			if (!path.exists() || !path.isFile()) {
				setErrorMessage(CoreMessages
					.getResourceString("OMPIPreferencesPage.Incorrect_ORTEd_file"));
				//setValid(false);
				//return false;
			}
		}
		
		name = getFieldContent(orteServerText.getText());
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

	protected void setErrorMessage(String str)
	{
		errorText.setText(str);
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
