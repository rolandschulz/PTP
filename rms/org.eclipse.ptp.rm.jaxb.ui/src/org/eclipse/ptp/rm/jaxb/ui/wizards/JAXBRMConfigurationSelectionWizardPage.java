package org.eclipse.ptp.rm.jaxb.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.rm.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.xml.JAXBUtils;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetUtils;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.osgi.framework.Bundle;

public class JAXBRMConfigurationSelectionWizardPage extends RMConfigurationWizardPage implements IJAXBNonNLSConstants {

	private class WidgetListener implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == preset) {
				handlePresetSelected();
			} else if (source == external) {
				handleExternalSelected();
			} else if (source == browseHomeButton) {
				handlePathBrowseButtonSelected(getUserHome());
			} else if (source == browseProjectButton) {
				try {
					handlePathBrowseButtonSelected(chooseProject());
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} else if (source == editButton) {
				try {
					WidgetUtils.openIDEEditor(selected);
				} catch (IOException t) {
					t.printStackTrace();
				}
			}
			setValid();
		}
	}

	private String[] types;
	private Properties rmXmlNames;
	private Properties rmXmlValues;
	private IJAXBResourceManagerConfiguration jaxbConfig;
	private final WidgetListener listener;

	private Text choice;
	private Combo preset;
	private Combo external;
	private Button browseHomeButton;
	private Button browseProjectButton;
	private Button editButton;
	private String selected;

	public JAXBRMConfigurationSelectionWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.JAXBRMConfigurationSelectionWizardPage_Title);
		setTitle(Messages.JAXBRMConfigurationSelectionWizardPage_Title);
		setDescription(Messages.JAXBConfigurationWizardPage_Description);
		types = new String[0];
		selected = ZEROSTR;
		setPageComplete(false);
		listener = new WidgetListener();
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		composite.setLayout(topLayout);
		createContents(composite);
		setControl(composite);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			initContents();
		}
		super.setVisible(visible);
	}

	/**
	 * Open a dialog that allows the user to choose a project.
	 * 
	 * @return selected project
	 */
	private File chooseProject() {
		IProject[] projects = getLocalProjects();
		WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(Messages.JAXBRMConfigurationSelectionWizardPage_Project_Selection_Title);
		dialog.setMessage(Messages.JAXBRMConfigurationSelectionWizardPage_Project_Selection_Message);
		dialog.setElements(projects);
		if (dialog.open() == Window.OK) {
			IProject project = (IProject) dialog.getFirstResult();
			return new File(project.getLocationURI());
		}
		return null;
	}

	private void createContents(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout layout = WidgetUtils.createGridLayout(3, true, 10, 0);
		GridData gd = WidgetUtils.spanGridData(GridData.FILL_HORIZONTAL, 3);
		group.setLayout(layout);
		group.setLayoutData(gd);

		Label label = new Label(group, SWT.NONE);
		label.setText(Messages.JAXBRMConfigurationSelectionWizardPage_4);

		choice = WidgetUtils.createText(group, selected, true, null, null);
		choice.setEditable(false);

		editButton = SWTUtil.createPushButton(group, Messages.JAXBRMConfigurationSelectionWizardPage_3, null);
		editButton.addSelectionListener(listener);

		group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		layout = WidgetUtils.createGridLayout(3, true, 10, 0);
		gd = WidgetUtils.spanGridData(GridData.FILL_HORIZONTAL, 3);
		group.setLayout(layout);
		group.setLayoutData(gd);

		preset = WidgetUtils.createItemCombo(group, Messages.JAXBRMConfigurationSelectionComboTitle_0, types, ZEROSTR, ZEROSTR,
				true, null, 2);
		preset.addSelectionListener(listener);

		group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		layout = WidgetUtils.createGridLayout(3, true, 10, 0);
		gd = WidgetUtils.spanGridData(GridData.FILL_HORIZONTAL, 3);
		group.setLayout(layout);
		group.setLayoutData(gd);

		external = WidgetUtils.createItemCombo(group, Messages.JAXBRMConfigurationSelectionComboTitle_1, types, ZEROSTR, ZEROSTR,
				true, null, 2);
		external.addSelectionListener(listener);

		browseHomeButton = SWTUtil.createPushButton(group, Messages.JAXBRMConfigurationSelectionWizardPage_1, null);
		browseHomeButton.addSelectionListener(listener);

		browseProjectButton = SWTUtil.createPushButton(group, Messages.JAXBRMConfigurationSelectionWizardPage_2, null);
		browseProjectButton.addSelectionListener(listener);
	}

	private IProject[] getLocalProjects() {
		IProject[] all = getWorkspaceRoot().getProjects();
		List<IProject> local = new ArrayList<IProject>();
		for (IProject p : all) {
			if (FILE_SCHEME.equals(p.getLocationURI().getScheme())) {
				local.add(p);
			}
		}
		return local.toArray(new IProject[0]);
	}

	private void getPluginResourceConfigurations() throws IOException {
		rmXmlNames = new Properties();
		rmXmlValues = new Properties();
		URL url = null;
		if (JAXBCorePlugin.getDefault() != null) {
			Bundle bundle = JAXBCorePlugin.getDefault().getBundle();
			url = FileLocator.find(bundle, new Path(DATA + RM_CONFIG_PROPS), null);
		} else {
			url = new File(RM_CONFIG_PROPS).toURL();
		}

		if (url == null) {
			return;
		}
		InputStream s = null;
		try {
			s = url.openStream();
			rmXmlNames.load(s);
		} finally {
			try {
				if (s != null) {
					s.close();
				}
			} catch (IOException e) {
			}
		}

		for (Object name : rmXmlNames.keySet()) {
			String value = (String) name;
			String key = rmXmlNames.getProperty(value);
			rmXmlValues.setProperty(key, value);
		}
	}

	private File getUserHome() {
		return new File(System.getProperty(JAVA_USER_HOME));
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	private void handleExternalSelected() {
		String text = external.getText();
		if (text != null) {
			selected = text;
			choice.setText(text);
		}
	}

	private void handlePathBrowseButtonSelected(File initPath) {
		if (initPath == null) {
			return;
		}
		IRemoteServices localServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		IRemoteUIServices localUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(localServices);
		if (localServices != null && localUIServices != null) {
			IRemoteConnectionManager lconnMgr = localServices.getConnectionManager();
			IRemoteConnection lconn = lconnMgr.getConnection(ZEROSTR);
			IRemoteUIFileManager localUIFileMgr = localUIServices.getUIFileManager();
			localUIFileMgr.setConnection(lconn);
			selected = localUIFileMgr.browseFile(getShell(), Messages.JAXBRMConfigurationSelectionWizardPage_0,
					initPath.getAbsolutePath(), 0);
			if (selected != null) {
				choice.setText(selected);
				updateExternal();
			}
		}
	}

	private void handlePresetSelected() {
		String text = preset.getText();
		if (text != null) {
			if (text.length() == 0) {
				selected = ZEROSTR;
			} else {
				selected = rmXmlNames.getProperty(text);
			}
			choice.setText(text);
		}
	}

	private void initContents() {
		jaxbConfig = (IJAXBResourceManagerConfiguration) getConfigurationWizard().getConfiguration();
		setAvailableConfigurations();
		setValid();
	}

	private boolean isValidSetting() {
		if (selected == null || selected.length() == 0) {
			return false;
		}
		try {
			JAXBUtils.validate(selected);
			jaxbConfig.setRMInstanceXMLLocation(selected);
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
		return true;
	}

	private void setAvailableConfigurations() {
		setPreset();
		setExternal();
		selected = jaxbConfig.getRMInstanceXMLLocation();
		if (rmXmlValues.containsKey(selected)) {
			choice.setText(rmXmlValues.getProperty(selected));
		} else {
			choice.setText(selected);
		}
	}

	private void setExternal() {
		String[] items = jaxbConfig.getExternalRMInstanceXMLLocations();
		if (external != null) {
			external.setItems(items);
		}
	}

	private void setPreset() {
		try {
			getPluginResourceConfigurations();
			types = rmXmlNames.keySet().toArray(new String[0]);
		} catch (IOException t) {
			t.printStackTrace();
			types = new String[0];
		}
		if (preset != null) {
			String[] items = new String[types.length + 1];
			items[0] = ZEROSTR;
			for (int i = 0; i < types.length; i++) {
				items[i + 1] = types[i];
			}
			preset.setItems(items);
		}
	}

	private void setValid() {
		setPageComplete(isValidSetting());
	}

	private void updateExternal() {
		int len = external.getItems().length;
		List<String> newItems = new ArrayList<String>();
		int i = 0;
		for (; i < len; i++) {
			if (selected.equals(external.getItem(i))) {
				external.select(i);
				break;
			}
			newItems.add(external.getItem(i));
		}
		if (i == len) {
			newItems.add(selected);
			jaxbConfig.addExternalRMInstanceXMLLocation(selected);
			external.setItems(newItems.toArray(new String[0]));
			external.select(i);
		}
	}
}
