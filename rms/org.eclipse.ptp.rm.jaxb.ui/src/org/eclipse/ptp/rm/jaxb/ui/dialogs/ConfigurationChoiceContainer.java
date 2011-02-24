package org.eclipse.ptp.rm.jaxb.ui.dialogs;

import java.io.File;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.AvailableJAXBRMConfigurations;
import org.eclipse.ptp.rm.jaxb.core.rm.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.ConfigUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetUtils;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public abstract class ConfigurationChoiceContainer implements IJAXBNonNLSConstants {

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
				handlePathBrowseButtonSelected(ConfigUtils.getUserHome());
			} else if (source == browseProjectButton) {
				try {
					handlePathBrowseButtonSelected(ConfigUtils.chooseLocalProject(shell));
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			onUpdate();
		}
	}

	private final Text choice;
	private final Combo preset;
	private final Combo external;
	private final Button browseHomeButton;
	private final Button browseProjectButton;
	private final WidgetListener listener;
	private final Shell shell;

	private String selected;
	private boolean isPreset;
	private IJAXBResourceManagerConfiguration config;
	private AvailableJAXBRMConfigurations available;

	public ConfigurationChoiceContainer(Composite parent) {
		shell = parent.getShell();
		listener = new WidgetListener();
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout layout = WidgetUtils.createGridLayout(3, false, 10, 5);
		GridData gd = WidgetUtils.spanGridData(GridData.FILL_HORIZONTAL, 3);
		group.setLayout(layout);
		group.setLayoutData(gd);

		Label label = new Label(group, SWT.NONE);
		label.setText(Messages.JAXBRMConfigurationSelectionWizardPage_4);

		choice = WidgetUtils.createText(group, selected, true, null, null);
		choice.setEditable(false);

		group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		layout = WidgetUtils.createGridLayout(3, true, 10, 0);
		gd = WidgetUtils.spanGridData(GridData.FILL_HORIZONTAL, 3);
		group.setLayout(layout);
		group.setLayoutData(gd);

		preset = WidgetUtils.createItemCombo(group, Messages.JAXBRMConfigurationSelectionComboTitle_0, new String[0], ZEROSTR,
				ZEROSTR, true, null, 2);
		preset.addSelectionListener(listener);

		group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		layout = WidgetUtils.createGridLayout(3, true, 10, 0);
		gd = WidgetUtils.spanGridData(GridData.FILL_HORIZONTAL, 3);
		group.setLayout(layout);
		group.setLayoutData(gd);

		external = WidgetUtils.createItemCombo(group, Messages.JAXBRMConfigurationSelectionComboTitle_1, new String[0], ZEROSTR,
				ZEROSTR, true, null, 2);
		external.addSelectionListener(listener);

		browseHomeButton = SWTUtil.createPushButton(group, Messages.JAXBRMConfigurationSelectionWizardPage_1, null);
		browseHomeButton.addSelectionListener(listener);

		browseProjectButton = SWTUtil.createPushButton(group, Messages.JAXBRMConfigurationSelectionWizardPage_2, null);
		browseProjectButton.addSelectionListener(listener);

		selected = ZEROSTR;
		isPreset = true;
	}

	public boolean choiceIsPreset() {
		return isPreset;
	}

	public String getSelected() {
		return selected;
	}

	public void setAvailableConfigurations() {
		available = AvailableJAXBRMConfigurations.getInstance();
		if (config != null) {
			available.addExternalPaths(config.getExternalRMInstanceXMLLocations());
		}
		if (preset != null) {
			preset.setItems(available.getTypes());
		}
		if (external != null) {
			external.setItems(available.getExternal());
		}
		if (config != null) {
			selected = config.getRMInstanceXMLLocation();
		}
		String type = available.getTypeForPath(selected);
		if (type != null) {
			choice.setText(type);
		} else {
			choice.setText(selected);
		}
	}

	public void setConfig(IJAXBResourceManagerConfiguration config) {
		this.config = config;
	}

	protected abstract void onUpdate();

	private void handleExternalSelected() {
		String text = external.getText();
		if (text != null) {
			selected = text;
			isPreset = false;
		} else {
			selected = ZEROSTR;
		}
		choice.setText(text);
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
			String result = localUIFileMgr.browseFile(shell, Messages.JAXBRMConfigurationSelectionWizardPage_0,
					initPath.getAbsolutePath(), 0);
			if (result != null) {
				selected = result;
				choice.setText(selected);
				isPreset = false;
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
				selected = available.getPathForType(text);
				isPreset = true;
			}
			choice.setText(text);
		}
	}

	private void updateExternal() {
		int len = external.getItems().length;
		int i = 0;
		for (; i < len; i++) {
			if (selected.equals(external.getItem(i))) {
				external.select(i);
				break;
			}
		}
		if (i == len) {
			available.addExternalPath(selected);
			if (config != null) {
				config.addExternalRMInstanceXMLLocation(selected);
			}
			external.setItems(available.getExternal());
			external.select(i);
		}
	}
}
