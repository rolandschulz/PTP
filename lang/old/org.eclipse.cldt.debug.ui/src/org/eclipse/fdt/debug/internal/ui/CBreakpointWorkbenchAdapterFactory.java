/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.fdt.debug.internal.ui; 

import org.eclipse.fdt.debug.core.model.ICBreakpoint;
import org.eclipse.fdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.fdt.debug.core.model.ICWatchpoint;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Adapter factory for C/C++ breakpoints.
 */
public class CBreakpointWorkbenchAdapterFactory implements IAdapterFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter( Object adaptableObject, Class adapterType ) {
		if ( adapterType != IWorkbenchAdapter.class || !(adaptableObject instanceof ICBreakpoint) ) {
			return null;
		}
		return new IWorkbenchAdapter() {

			public Object[] getChildren( Object o ) {
				return null;
			}

			public ImageDescriptor getImageDescriptor( Object object ) {
				return null;
			}

			public String getLabel( Object o ) {
				// for now
				if ( (o instanceof ICLineBreakpoint) ) {
					return CDebugUIMessages.getString( "CBreakpointWorkbenchAdapterFactory.0" ); //$NON-NLS-1$
				}
				if ( (o instanceof ICWatchpoint) ) {
					return CDebugUIMessages.getString( "CBreakpointWorkbenchAdapterFactory.1" ); //$NON-NLS-1$
				}
				return null; 
			}

			public Object getParent( Object o ) {
				return null;
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class };
	}
}
