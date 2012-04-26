package org.eclipse.ptp.rm.lml.monitor.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.ptp.rm.jaxb.ui.util.JAXBExtensionUtils;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class AddMonitorDialog extends TitleAreaDialog {

	private Combo fSystemTypeCombo;
	private RemoteConnectionWidget fRemoteConnectionWidget;
	private String fSystemType;
	private IRemoteConnection fRemoteConnection;

	public AddMonitorDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Get the remote connection selected in the dialog, or null if no connection selected
	 * 
	 * @return remote connection, or null
	 */
	public IRemoteConnection getRemoteConnection() {
		return fRemoteConnection;
	}

	/**
	 * Get the system type selected in the dialog, or null if no system type selected
	 * 
	 * @return system type or null
	 */
	public String getSystemType() {
		return fSystemType;
	}

	public String getMonitorId() {
		String id = null;
		if (fRemoteConnection != null) {
			id = fRemoteConnection.getRemoteServices().getId() + "_" + fRemoteConnection.getName() + "_" + getSystemType(); //$NON-NLS-1$//$NON-NLS-2$
		}
		return id;
	}

	private void updateEnablement() {
		String id = getMonitorId();
		Boolean valid = (id != null && MonitorControlManager.getInstance().getMonitorControl(id) == null);
		fRemoteConnectionWidget.setEnabled(fSystemTypeCombo.getSelectionIndex() > 0);
		Button button = getButton(IDialogConstants.OK_ID);
		if (button != null) {
			button.setEnabled(valid && fSystemTypeCombo.getSelectionIndex() > 0 && fRemoteConnectionWidget.getConnection() != null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		updateEnablement();
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Add Monitor");
		setTitle("Select a new monitor type to add");
		setMessage("Choose a monitor type, then select the connection to use for the monitor.");

		Composite composite = new Composite(parent, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		composite.setLayout(topLayout);
		composite.setLayoutData(gd);

		new Label(composite, SWT.NONE).setText("Monitor Type:");

		fSystemTypeCombo = new Combo(composite, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fSystemTypeCombo.setLayoutData(gd);
		fSystemTypeCombo.setItems(JAXBExtensionUtils.getMonitorTypes());
		fSystemTypeCombo.add("Please select a monitor type", 0);
		fSystemTypeCombo.select(0);
		fSystemTypeCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (fSystemTypeCombo.getSelectionIndex() > 0) {
					fSystemType = fSystemTypeCombo.getText();
				}
				updateEnablement();
			}
		});

		fRemoteConnectionWidget = new RemoteConnectionWidget(composite, SWT.NONE, null, null);
		fRemoteConnectionWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		fRemoteConnectionWidget.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				fRemoteConnection = fRemoteConnectionWidget.getConnection();
				updateEnablement();
			}
		});
		fRemoteConnectionWidget.setEnabled(false);

		Dialog.applyDialogFont(composite);

		return composite;
	}
}
