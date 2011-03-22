package org.eclipse.ptp.rm.jaxb.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rm.ui.dialogs.ConnectionChoiceContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ConnectionChoiceDialog extends Dialog implements IJAXBUINonNLSConstants {

	private ConnectionChoiceContainer container;

	private IRemoteResourceManagerConfiguration config;

	public ConnectionChoiceDialog(Shell parentShell) {
		super(parentShell);
	}

	public IRemoteResourceManagerConfiguration getConfig() {
		return config;
	}

	@Override
	public int open() {
		super.open();
		config = container.getDataSource().getConfiguration();
		return getReturnCode();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.JAXBRMConnectionChoiceTitle);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Composite contents = new Composite(composite, SWT.NONE);
		WidgetBuilderUtils.createGridData(SWT.HORIZONTAL, 1);
		contents.setLayoutData(WidgetBuilderUtils.createGridData(SWT.HORIZONTAL, 1));
		contents.setLayout(WidgetBuilderUtils.createGridLayout(1, false, 200, 500));
		container = new ConnectionChoiceContainer(null) {
			@Override
			protected void resetErrorMessages() {
				// not implemented
			}
		};
		container.createContents(composite);
		container.getDataSource().loadAndUpdate();
		applyDialogFont(composite);
		return composite;
	}
}
