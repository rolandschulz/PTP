package org.eclipse.ptp.services.internal.ui.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

public class ServiceModelWorkbenchAdapterFactory implements IAdapterFactory {

	private static final ServiceModelWorkbenchAdapter SERVICE_MODEL_WORKBENCH_ADAPTER =
		new ServiceModelWorkbenchAdapter();
	private static final ServiceConfigurationWorkbenchAdapter SERVICE_CONFIGURATION_WORKBENCH_ADAPTER =
		new ServiceConfigurationWorkbenchAdapter();
	private static final ServiceProviderWorkbenchAdapter SERVICE_PROVIDER_WORKBENCH_ADAPTER =
		new ServiceProviderWorkbenchAdapter();

	public Object getAdapter(Object adaptableObject, 
			@SuppressWarnings("unchecked") Class adapterType) {
		if (adapterType == IWorkbenchAdapter.class || adapterType == IWorkbenchAdapter2.class) {
			if (adaptableObject instanceof IServiceModelManager) {
				return SERVICE_MODEL_WORKBENCH_ADAPTER;
			}
			if (adaptableObject instanceof IServiceConfiguration) {
				return SERVICE_CONFIGURATION_WORKBENCH_ADAPTER;
			}
			if (adaptableObject instanceof IServiceProvider) {
				return SERVICE_PROVIDER_WORKBENCH_ADAPTER;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
		return new Class[] {IWorkbenchAdapter.class, IWorkbenchAdapter2.class};
	}

}
