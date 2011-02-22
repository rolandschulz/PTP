package org.eclipse.ptp.rm.jaxb.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
			if (source == rmTypes) {
				handleComboSelection();
			} else if (source == browseHomeButton) {
				handlePathBrowseButtonSelected(getUserHome());
			} else if (source == browseProjectButton) {
				IProject project = chooseProject();
				handlePathBrowseButtonSelected(project.getFullPath().toFile());
			} else if (source == editButton) {
				try {
					WidgetUtils.openIDEEditor(choice);
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

	private Combo rmTypes;
	private Button browseHomeButton;
	private Button browseProjectButton;
	private Button editButton;
	private String choice;

	public JAXBRMConfigurationSelectionWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.JAXBRMConfigurationSelectionWizardPage_Title);
		setTitle(Messages.JAXBRMConfigurationSelectionWizardPage_Title);
		setDescription(Messages.JAXBConfigurationWizardPage_Description);
		types = new String[0];
		choice = ZEROSTR;
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
	 * Create a dialog that allows the user to choose a project.
	 * 
	 * @return selected project
	 */
	protected IProject chooseProject() {
		IProject[] projects = getWorkspaceRoot().getProjects();
		WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(Messages.JAXBRMConfigurationSelectionWizardPage_Project_Selection_Title);
		dialog.setMessage(Messages.JAXBRMConfigurationSelectionWizardPage_Project_Selection_Message);
		dialog.setElements(projects);
		if (dialog.open() == Window.OK) {
			return (IProject) dialog.getFirstResult();
		}
		return null;
	}

	private void createContents(Composite parent) {
		new Label(parent, SWT.NONE);
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setLayout(WidgetUtils.createGridLayout(3, true, 10, 10));
		group.setLayoutData(WidgetUtils.spanGridData(GridData.FILL_HORIZONTAL, 3));
		group.setText(Messages.JAXBRMSchemaComboGroupTitle);

		rmTypes = WidgetUtils.createItemCombo(group, Messages.JAXBRMSchemaComboTitle, types, choice, ZEROSTR, true, null, 2);
		rmTypes.addSelectionListener(listener);

		browseHomeButton = SWTUtil.createPushButton(group, Messages.JAXBRMConfigurationSelectionWizardPage_1, null);
		browseHomeButton.addSelectionListener(listener);

		browseProjectButton = SWTUtil.createPushButton(group, Messages.JAXBRMConfigurationSelectionWizardPage_2, null);
		browseProjectButton.addSelectionListener(listener);

		editButton = SWTUtil.createPushButton(group, Messages.JAXBRMConfigurationSelectionWizardPage_3, null);
		editButton.addSelectionListener(listener);
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

	private void handleComboSelection() {
		String text = rmTypes.getText();
		if (text != null && text.length() > 0) {
			choice = rmXmlNames.getProperty(text);
		}
	}

	private void handlePathBrowseButtonSelected(File initPath) {
		IRemoteServices localServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		IRemoteUIServices localUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(localServices);
		if (localServices != null && localUIServices != null) {
			IRemoteConnectionManager lconnMgr = localServices.getConnectionManager();
			IRemoteConnection lconn = lconnMgr.getConnection(ZEROSTR);
			IRemoteUIFileManager localUIFileMgr = localUIServices.getUIFileManager();
			localUIFileMgr.setConnection(lconn);
			choice = localUIFileMgr.browseFile(getShell(), Messages.JAXBRMConfigurationSelectionWizardPage_0,
					initPath.getAbsolutePath(), 0);
			if (choice != null) {
				rmTypes.setText(choice);
			}
		}
	}

	private void initContents() {
		jaxbConfig = (IJAXBResourceManagerConfiguration) getConfigurationWizard().getConfiguration();
		setAvailableConfigurations();
	}

	private boolean isValidSetting() {
		if (choice == null || choice.length() == 0) {
			return false;
		}
		try {
			JAXBUtils.validate(choice);
			jaxbConfig.setRMInstanceXMLLocation(choice);
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
		return true;
	}

	private void setAvailableConfigurations() {
		try {
			getPluginResourceConfigurations();
			types = rmXmlNames.keySet().toArray(new String[0]);
		} catch (IOException t) {
			t.printStackTrace();
			types = new String[0];
		}
		if (rmTypes != null) {
			rmTypes.setItems(types);
			choice = jaxbConfig.getRMInstanceXMLLocation();
			if (rmXmlValues.containsKey(choice)) {
				rmTypes.setText(rmXmlValues.getProperty(choice));
			} else {
				rmTypes.setText(choice);
			}
		}
	}

	private void setValid() {
		setPageComplete(isValidSetting());
	}
}
