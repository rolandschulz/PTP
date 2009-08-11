package org.eclipse.ptp.services.internal.ui.adapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ui.model.WorkbenchAdapter;

public class ServiceProviderWorkbenchAdapter extends WorkbenchAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
//		IPMachine machine = (IPMachine) object;
//		return new ImageImageDescriptor(ParallelImages.machineImages[machine.getState().ordinal()]);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object object) {
		return ((IServiceProvider)object).getName();
	}
}
