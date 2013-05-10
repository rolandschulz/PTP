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
package org.eclipse.ptp.internal.etfw.preferences;

//import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

//import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.etfw.ETFWUtils;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.toolopts.ExternalToolProcess;
import org.eclipse.ptp.internal.etfw.Activator;
import org.eclipse.ptp.internal.etfw.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Provides a user-interface for and managed workspace-wide TAU settings. The
 * location of the local TAU installation is the most critical of these
 * 
 * @author wspear
 * 
 */
public class ToolLocationPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private class BinDirPanel {
		protected class BinListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener {
			public void modifyText(ModifyEvent evt) {
				final Object source = evt.getSource();
				if (source == binDir) {
				}

				updatePreferencePage();
			}

			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) {
					updatePreferencePage();
				}
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				final Object source = e.getSource();
				if (source == browseBinButton) {
					handleBinBrowseButtonSelected(binDir, group);
				}
				updatePreferencePage();
			}
		}

		String group = ""; //$NON-NLS-1$
		Button browseBinButton = null;
		Text binDir = null;

		BinListener binLis = new BinListener();

		public BinDirPanel(String group) {
			this.group = group;
		}

		private void makeToolBinPane(Composite parent) {
			final Composite tauarch = new Composite(parent, SWT.NONE);
			tauarch.setLayout(createGridLayout(3, false, 0, 0));
			tauarch.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

			final Label taubinComment = new Label(tauarch, SWT.WRAP);
			taubinComment.setText(group + Messages.ToolLocationPreferencePage_BinDir);
			binDir = new Text(tauarch, SWT.BORDER | SWT.SINGLE);
			binDir.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			binDir.addModifyListener(binLis);

			browseBinButton = new Button(tauarch, SWT.PUSH);
			browseBinButton.setText(Messages.ToolLocationPreferencePage_Browse);
			browseBinButton.addSelectionListener(binLis);
		}
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener {
		public void modifyText(ModifyEvent evt) {
			// Object source = evt.getSource();
			// if(source==tauBin){
			// }

			updatePreferencePage();
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				updatePreferencePage();
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			// Object source = e.getSource();
			// if(source == browseBinButton) {
			// handleBinBrowseButtonSelected();
			// }
			updatePreferencePage();
		}
	}

	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	// protected Text tauBin = null;
	// protected Button browseBinButton = null;

	BinDirPanel[] toolGroups = null;

	protected WidgetListener listener = new WidgetListener();

	public ToolLocationPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		Iterator<Map.Entry<String, String>> eIt = null;
		String me = null;
		final ExternalToolProcess[] tools = ETFWUtils.getTools();
		final Set<String> groups = new LinkedHashSet<String>();
		for (final ExternalToolProcess tool : tools) {
			eIt = tool.groupApp.entrySet().iterator();
			while (eIt.hasNext()) {
				me = (eIt.next()).getKey().toString();
				if (!me.equals("internal")) {
					groups.add(me);
				}
			}
		}

		toolGroups = new BinDirPanel[groups.size()];
		final Iterator<String> gIt = groups.iterator();
		int i = 0;
		while (gIt.hasNext()) {
			toolGroups[i] = new BinDirPanel(gIt.next());
			i++;
		}
	}

	protected Button createButton(Composite parent, String label, int type) {
		final Button button = new Button(parent, type);
		button.setText(label);
		final GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	protected Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(createGridLayout(1, true, 0, 0));
		composite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));

		createTauConf(composite);
		loadSaved();
		defaultSetting();
		return composite;
	}

	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	/**
	 * Create the TAU options UI
	 * 
	 * @param parent
	 */
	private void createTauConf(Composite parent) {
		final Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(1, true, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText(Messages.ToolLocationPreferencePage_ToolLocationConf);

		if (toolGroups != null) {
			for (final BinDirPanel toolGroup : toolGroups) {
				toolGroup.makeToolBinPane(aGroup);
			}
		}

	}

	protected void defaultSetting() {
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	protected String getFieldContent(String text) {
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING)) {
			return null;
		}

		return text;
	}

	/**
	 * Allow user to specify a TAU bin directory.
	 * 
	 */
	protected void handleBinBrowseButtonSelected(Text field, String group) {
		final DirectoryDialog dialog = new DirectoryDialog(getShell());
		IFileStore path = null;
		final String correctPath = getFieldContent(field.getText());
		if (correctPath != null) {
			path = EFS.getLocalFileSystem().getStore(new Path(correctPath));// new File(correctPath);
			if (path.fetchInfo().exists()) {
				dialog.setFilterPath(!path.fetchInfo().isDirectory() ? correctPath : path.getParent().toURI().getPath());
			}
		}
		// The specified directory previously had to contain at least one
		// recognizable TAU makefile in its lib sub-directory to be accepted.
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

		dialog.setText(Messages.ToolLocationPreferencePage_Select + group + Messages.ToolLocationPreferencePage_BinDirectory);
		// dialog.setMessage("You must select a valid TAU bin directory.  Such a directory should be created when you configure and install TAU.  It should contain least one valid stub makefile configured with the Program Database Toolkit (pdt)");

		final String selectedPath = dialog.open();// null;
		if (selectedPath != null) {
			field.setText(selectedPath);
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
			// tauBin.setText(selectedPath);
			// break;
			// }
			// }
		}

	}

	public void init(IWorkbench workbench) {
	}

	private void loadSaved() {
		// Preferences preferences = Activator.getDefault().getPluginPreferences();
		final IPreferencesService service = Platform.getPreferencesService();

		if (toolGroups != null) {
			for (final BinDirPanel toolGroup : toolGroups) {
				toolGroup.binDir.setText(service.getString(Activator.PLUGIN_ID, IToolLaunchConfigurationConstants.TOOL_BIN_ID
						+ "." + toolGroup.group, "", null));// ITAULaunchConfigurationConstants.TAU_BIN_PATH)); //$NON-NLS-1$
			}
		}

	}

	@Override
	public void performDefaults() {
		defaultSetting();
		updateApplyButton();
	}

	@Override
	public boolean performOk() {
		// Preferences preferences = Activator.getDefault().getPluginPreferences();

		// InstanceScope is = new InstanceScope();

		final IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);

		if (toolGroups != null) {
			for (final BinDirPanel toolGroup : toolGroups) {
				preferences.put(IToolLaunchConfigurationConstants.TOOL_BIN_ID + "." + toolGroup.group, toolGroup.binDir.getText()); //$NON-NLS-1$
			}
		}

		try {
			preferences.flush();
		} catch (final BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Activator.getDefault().savePluginPreferences();
		return true;
	}

	protected GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1) {
			gd = new GridData();
		} else {
			gd = new GridData(style);
		}
		gd.horizontalSpan = space;
		return gd;
	}

	protected void updatePreferencePage() {
		setErrorMessage(null);
		setMessage(null);

		setValid(true);
	}
}