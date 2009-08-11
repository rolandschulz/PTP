package org.eclipse.ptp.services.internal.ui.adapters;

import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ui.model.WorkbenchAdapter;

public class ServiceModelWorkbenchAdapter extends WorkbenchAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		return ((IServiceModelManager)parentElement).getConfigurations().toArray();
	}
}
