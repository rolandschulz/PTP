package org.eclipse.ptp.ui;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Same as WizardDialog except it is modeless.
 * 
 * @author arossi
 * @since 5.0
 * 
 */
public class ModelessWizardDialog extends WizardDialog {

	public ModelessWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		int newstyle = newShellStyle & ~SWT.APPLICATION_MODAL; /*
																 * turn off
																 * APPLICATION_MODAL
																 */
		super.setShellStyle(newstyle | SWT.MODELESS); /* turn on MODELESS */
	}
}
