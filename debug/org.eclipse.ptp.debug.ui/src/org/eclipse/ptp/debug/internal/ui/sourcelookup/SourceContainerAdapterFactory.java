package org.eclipse.ptp.debug.internal.ui.sourcelookup;

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
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(IWorkbenchAdapter.class)) {
			return new SourceContainerWorkbenchAdapter();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[]{ IWorkbenchAdapter.class };
	}
}
