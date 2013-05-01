package org.eclipse.ptp.internal.debug.ui.sourcelookup;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author clement
 *
 */
public class SourceContainerAdapterFactory implements IAdapterFactory {
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(IWorkbenchAdapter.class)) {
			return new SourceContainerWorkbenchAdapter();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[]{ IWorkbenchAdapter.class };
	}
}
