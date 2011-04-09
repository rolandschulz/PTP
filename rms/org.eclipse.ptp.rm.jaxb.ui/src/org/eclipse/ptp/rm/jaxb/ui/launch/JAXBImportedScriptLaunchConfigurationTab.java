package org.eclipse.ptp.rm.jaxb.ui.launch;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class JAXBImportedScriptLaunchConfigurationTab extends AbstractJAXBLaunchConfigurationTab implements SelectionListener {

	private Text choice;
	private Text editor;
	private Button browseWorkspace;
	private Button clear;

	private String selected;
	private final StringBuffer contents;

	public JAXBImportedScriptLaunchConfigurationTab(IJAXBResourceManager rm, ILaunchConfigurationDialog dialog, String title,
			JAXBControllerLaunchConfigurationTab parentTab, int tabIndex) {
		super(parentTab, dialog, tabIndex);
		if (title != null) {
			this.title = title;
		}
		contents = new StringBuffer();
	}

	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
	}

	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = WidgetBuilderUtils.createComposite(parent, 1);
		GridLayout layout = WidgetBuilderUtils.createGridLayout(6, true);
		GridData gd = WidgetBuilderUtils.createGridDataFillH(6);
		Composite comp = WidgetBuilderUtils.createComposite(control, SWT.NONE, layout, gd);
		WidgetBuilderUtils.createLabel(comp, Messages.BatchScriptPath, SWT.LEFT, 1);
		GridData gdsub = WidgetBuilderUtils.createGridDataFillH(3);
		String s = selected == null ? ZEROSTR : selected.toString();
		choice = WidgetBuilderUtils.createText(comp, SWT.BORDER, gdsub, true, s);
		browseWorkspace = WidgetBuilderUtils.createPushButton(comp, Messages.JAXBRMConfigurationSelectionWizardPage_1, this);
		clear = WidgetBuilderUtils.createPushButton(comp, Messages.ClearScript, this);

		layout = WidgetBuilderUtils.createGridLayout(1, true);
		gd = WidgetBuilderUtils.createGridData(GridData.FILL_BOTH, true, true, 130, 300, 1, DEFAULT);
		Group grp = WidgetBuilderUtils.createGroup(control, SWT.NONE, layout, gd);
		int style = SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL;
		gdsub = WidgetBuilderUtils.createGridDataFill(DEFAULT, DEFAULT, 1);
		editor = WidgetBuilderUtils.createText(grp, style, gdsub, true, ZEROSTR, null, null);
		WidgetBuilderUtils.applyMonospace(editor);
		editor.setToolTipText(Messages.ReadOnlyWarning);

		selected = null;
		parentTab.resize(control);
		updateControls();
	}

	@Override
	public void fireContentsChanged() {
		super.fireContentsChanged();
	}

	public Control getControl() {
		return control;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getText() {
		return title;
	}

	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		try {
			String uriStr = configuration.getAttribute(SCRIPT_PATH, ZEROSTR);
			if (!ZEROSTR.equals(uriStr)) {
				selected = uriStr;
			} else {
				selected = null;
			}
			uploadScript();
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(control.getShell(), t, Messages.ErrorOnLoadFromStore, Messages.ErrorOnLoadTitle, false);
		}
		return new RMLaunchValidation(true, null);
	}

	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
	}

	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		return new RMLaunchValidation(true, null);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e) {
		Object source = e.getSource();
		try {
			if (source == browseWorkspace) {
				selected = WidgetActionUtils.browseWorkspace(control.getShell());
				updateContents();
			} else if (source == clear) {
				selected = null;
				updateContents();
			}
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(control.getShell(), t, Messages.WidgetSelectedError, Messages.WidgetSelectedErrorTitle,
					false);
		}
	}

	@Override
	protected void doRefreshLocal() {
		if (selected != null) {
			localMap.put(SCRIPT_PATH, selected);
		}
	}

	private void updateContents() throws Throwable {
		uploadScript();
		fireContentsChanged();
	}

	private void updateControls() {
		if (selected != null) {
			choice.setText(selected);
		} else {
			choice.setText(ZEROSTR);
		}
		editor.setText(contents.toString());
		if (ZEROSTR.equals(contents)) {
			clear.setEnabled(false);
		} else {
			clear.setEnabled(true);
		}
	}

	private void uploadScript() throws Throwable {
		contents.setLength(0);
		if (null != selected) {
			BufferedReader br = new BufferedReader(new FileReader(new File(selected)));
			while (true) {
				try {
					String line = br.readLine();
					if (line == null) {
						break;
					}
					contents.append(line).append(LINE_SEP);
				} catch (EOFException eof) {
					break;
				}
			}
		}
		updateControls();
	}
}
