/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.etfw.preferences;

//import java.io.File;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.etfw.Activator;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.PreferenceConstants;
import org.eclipse.ptp.etfw.messages.Messages;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Provides a user-interface for and managed workspace-wide performance tool
 * settings. The location of the local tool xml definition file is the most
 * critical of these
 * 
 * @author wspear
 * @author "Chris Navarro" - ETFW JAXB Preferences
 */
public class ExternalToolPreferencePage extends PreferencePage implements IWorkbenchPreferencePage,
		IToolLaunchConfigurationConstants {
	protected List XMLLocs = null;
	protected Button browseXMLButton = null;
	protected Button removeItemButton = null;
	protected Combo parser = null;

	// protected Button checkAutoOpts=null;
	// protected Button checkAixOpts=null;

	public ExternalToolPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == browseXMLButton) {
				handleXMLBrowseButtonSelected();
			}
			if (source == removeItemButton) {
				handleRemoveItem();
			}
			updatePreferencePage();
		}

		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if (source == XMLLocs) {
			}

			updatePreferencePage();
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePreferencePage();
		}
	}

	protected WidgetListener listener = new WidgetListener();

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(createGridLayout(1, true, 0, 0));
		composite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));

		createParserSelection(composite);
		createTauConf(composite);
		loadSaved();
		defaultSetting();
		return composite;
	}

	private void createParserSelection(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setLayout(createGridLayout(1, true, 10, 10));
		group.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		group.setText(Messages.ExternalToolPreferencePage_ToolParser);

		Composite content = new Composite(group, SWT.NONE);
		content.setLayout(createGridLayout(2, false, 0, 0));
		content.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, SWT.WRAP));

		Label parserLbl = new Label(content, SWT.NONE);
		parserLbl.setText(Messages.ExternalToolPreferencePage_ETFW_PARSER);

		parser = new Combo(content, SWT.READ_ONLY);
		parser.add(USE_SAX_PARSER);
		parser.add(USE_JAXB_PARSER);
		// Text parser = new Text(content, SWT.BORDER);
		// parser.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 1));
	}

	/**
	 * Create the TAU options UI
	 * 
	 * @param parent
	 */
	private void createTauConf(Composite parent) {
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(1, true, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText(Messages.ExternalToolPreferencePage_ExToolConf);

		Composite xmlcom = new Composite(aGroup, SWT.NONE);
		xmlcom.setLayout(createGridLayout(2, false, 0, 0));
		xmlcom.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		Label tauarchComment = new Label(xmlcom, SWT.WRAP);
		tauarchComment.setText(Messages.ExternalToolPreferencePage_ToolDefFile);
		XMLLocs = new List(xmlcom, SWT.BORDER | SWT.V_SCROLL);
		XMLLocs.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// XMLLocs.add.addModifyListener(listener);

		browseXMLButton = new Button(xmlcom, SWT.PUSH);
		browseXMLButton.setText(Messages.ExternalToolPreferencePage_Add);
		browseXMLButton.addSelectionListener(listener);

		removeItemButton = new Button(xmlcom, SWT.PUSH);
		removeItemButton.setText(Messages.ExternalToolPreferencePage_Remove);
		removeItemButton.addSelectionListener(listener);
		// TODO: Implement tau-option checking
		// GridData gridData = new GridData(GridData.VERTICAL_ALIGN_END);
		// gridData.horizontalSpan = 3;
		// gridData.horizontalAlignment = GridData.FILL;
		//
		// if(org.eclipse.cdt.utils.Platform.getOS().toLowerCase().trim().indexOf("aix")>=0)
		// {
		// checkAixOpts=createCheckButton(tauarch,"Automatically use Eclipse internal builder (May be needed for AIX compatibility)");
		// checkAixOpts.setLayoutData(gridData);
		// checkAixOpts.addSelectionListener(listener);
		// }
		//
		// checkAutoOpts=createCheckButton(tauarch,
		// "Check for TAU System options");
		// checkAutoOpts.setLayoutData(gridData);
		// checkAutoOpts.addSelectionListener(listener);
	}

	protected void handleRemoveItem() {
		XMLLocs.remove(XMLLocs.getSelectionIndices());
	}

	/**
	 * Allow user to specify a TAU arch directory. The specified directory must
	 * contain at least one recognizable TAU makefile in its lib sub-directory
	 * to be accepted.
	 * 
	 */
	protected void handleXMLBrowseButtonSelected() {
		FileDialog dialog = new FileDialog(getShell());
		IFileStore path = null;
		String correctPath = null;
		int maxXDex = XMLLocs.getItemCount() - 1;
		if (maxXDex >= 0) {
			correctPath = getFieldContent(XMLLocs.getItem(maxXDex));
		}
		if (correctPath != null) {
			try {
				path = EFS.getStore(new URI(correctPath));
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block

				path = EFS.getLocalFileSystem().getStore(new Path(correctPath));
				// e.printStackTrace();
			}
			if (path != null && path.fetchInfo().exists())
				dialog.setFilterPath(!path.fetchInfo().isDirectory() ? correctPath : path.getParent().toURI().getPath()); // TODO:
																															// This
																															// may
																															// be
																															// bad
		}

		// String tlpath = correctPath+File.separator+"lib";
		//
		// class makefilter implements FilenameFilter{
		// public boolean accept(File dir, String name) {
		// if(name.indexOf("Makefile.tau")!=0 || name.indexOf("-pdt")<=0)
		// return false;
		// return true;
		// }
		// }
		// File[] mfiles=null;
		// makefilter mfilter = new makefilter();
		// File test = new File(tlpath);

		dialog.setText(Messages.ExternalToolPreferencePage_SelectToolDefXML);

		String out = getFieldContent(dialog.open());

		if (out != null) {
			IFileStore test = EFS.getLocalFileSystem().getStore(new Path(out));// new IFFile(out);
			if (test.fetchInfo().exists() && !test.fetchInfo().isDirectory()) {
				XMLLocs.add(out);
			} else {
				// TODO: print a warning?
			}
		}
		// dialog.setMessage("You must select a valid TAU architecture directory.  Such a directory should be created when you configure and install TAU.  It must contain least one valid stub makefile configured with the Program Database Toolkit (pdt)");

		// String selectedPath=null;
		// while(true)
		// {
		// selectedPath = dialog.open();
		// if(selectedPath==null)
		// break;
		//
		// tlpath=selectedPath+File.separator+"lib";
		// test = new File(tlpath);
		// if(test.exists()){
		// mfiles = test.listFiles(mfilter);
		// }
		// if (mfiles!=null&&mfiles.length>0)
		// {
		// if (selectedPath != null)
		// XMLLoc.setText(selectedPath);
		// break;
		// }
		// }

	}

	private void loadSaved() {

		// Preferences preferences = Activator.getDefault().getPluginPreferences();
		IPreferencesService service = Platform.getPreferencesService();
		String fiList = service.getString(Activator.PLUGIN_ID, XMLLOCID, EMPTY_STRING, null);// .getString(XMLLOCID);

		String[] files = fiList.split(",,,"); //$NON-NLS-1$
		for (String s : files) {
			XMLLocs.add(s);// setText(preferences.getString(XMLLOCID));
		}

		String etfwVersion = Preferences.getString(Activator.PLUGIN_ID, PreferenceConstants.ETFW_VERSION);
		for (int index = 0; index < parser.getItemCount(); index++) {
			if (parser.getItem(index).equals(etfwVersion)) {
				parser.select(index);
				break;
			}
		}
		// TODO: Add checks
		// checkAutoOpts.setSelection(preferences.getBoolean(ITAULaunchConfigurationConstants.TAU_CHECK_AUTO_OPT));
		// if(checkAixOpts!=null)
		// checkAixOpts.setSelection(preferences.getBoolean(ITAULaunchConfigurationConstants.TAU_CHECK_AIX_OPT));
	}

	@Override
	public boolean performOk() {
		// Activator.getDefault().getPluginPreferences();

		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);

		String fiList = ""; //$NON-NLS-1$

		for (int i = 0; i < XMLLocs.getItemCount(); i++) {
			fiList += XMLLocs.getItem(i);
			if (i < XMLLocs.getItemCount() - 1) {
				fiList += ",,,"; //$NON-NLS-1$
			}
		}
		preferences.put(XMLLOCID, fiList);// XMLLoc.getText());
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Activator.getDefault().refreshTools();

		Preferences.setString(Activator.PLUGIN_ID, PreferenceConstants.ETFW_VERSION, parser.getItem(parser.getSelectionIndex()));
		// TODO: Add checks
		// preferences.setValue(ITAULaunchConfigurationConstants.TAU_CHECK_AUTO_OPT,
		// checkAutoOpts.getSelection());
		// if(checkAixOpts!=null)
		// preferences.setValue(ITAULaunchConfigurationConstants.TAU_CHECK_AIX_OPT,
		// checkAixOpts.getSelection());
		//
		// Activator.getDefault().savePluginPreferences();
		return true;
	}

	protected Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}

	protected Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	public void init(IWorkbench workbench) {
	}

	protected void defaultSetting() {
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void performDefaults() {
		defaultSetting();
		updateApplyButton();
	}

	protected void updatePreferencePage() {
		setErrorMessage(null);
		setMessage(null);

		setValid(true);
	}

	protected String getFieldContent(String text) {
		if (text == null)
			return null;
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
			return null;

		return text;
	}

	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	protected GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1)
			gd = new GridData();
		else
			gd = new GridData(style);
		gd.horizontalSpan = space;
		return gd;
	}
}