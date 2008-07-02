package org.eclipse.ptp.rm.mpi.openmpi.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class EmptyPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public EmptyPage() {
		super();
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		return parent;
	}

	public void init(IWorkbench workbench) {
		// Empty
	}

}
