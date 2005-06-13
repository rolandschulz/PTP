package org.eclipse.ptp.tools.vprof.internal.ui.views;


import java.util.ArrayList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CElementContentProvider;

/**
 * CViewContentProvider
 */
public class VprofViewContentProvider extends CElementContentProvider {
	/**
	 * 
	 */
	public VprofViewContentProvider() {
		super();
	}

	/**
	 * @param provideMembers
	 * @param provideWorkingCopy
	 */
	public VprofViewContentProvider(boolean provideMembers, boolean provideWorkingCopy) {
		super(provideMembers, provideWorkingCopy);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		Object[] objs = super.getChildren(element);
		Object[] extras = null;
		try {
			if (element instanceof ICProject) {
				extras = getProjectChildren((ICProject)element);
			} else if (element instanceof IBinaryContainer) {
				extras = getExecutables((IBinaryContainer)element);
			}
			/*
			 * Do not to this for now, since ILibraryReference is an Archive.
			 else if (element instanceof ILibraryReference) {
				extras =  ((ILibraryReference)element).getChildren();
			}*/
		} catch (CModelException e) {
			extras = null;
		}
		if (extras != null && extras.length > 0) {
			return extras;
			//objs = concatenate(objs, extras);
		}
		return objs;
	}
	
	/**
	 * @return
	 */
	private Object[] getProjectChildren(ICProject cproject) throws CModelException {
		Object[] extras = null;
		IBinaryContainer bin = cproject.getBinaryContainer(); 
		if (getExecutables(bin).length > 0) {
			Object[] o = new Object[] {bin};
			if (extras != null && extras.length > 0) {
				extras = concatenate(extras, o);
			} else {
				extras = o;
			}
		}
		return extras;
	}

	protected IBinary[] getExecutables(IBinaryContainer container) throws CModelException {
		ICElement[] celements = container.getChildren();
		ArrayList list = new ArrayList(celements.length);
		for (int i = 0; i < celements.length; i++) {
			if (celements[i] instanceof IBinary) {
				IBinary bin = (IBinary)celements[i];
				if (bin.isExecutable()) {
					list.add(bin);
				}
			}
		}
		IBinary[] bins = new IBinary[list.size()];
		list.toArray(bins);
		return bins;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.BaseCElementContentProvider#internalGetParent(java.lang.Object)
	 */
	public Object internalGetParent(Object element) {
		// since we insert logical containers we have to fix
		// up the parent for {IInclude,ILibrary}Reference so that they refer
		// to the container and containers refere to the project
		Object parent = super.internalGetParent(element);
		return parent;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof IBinaryContainer) {
			try {
				IBinaryContainer cont = (IBinaryContainer)element;
				IBinary[] bins = getBinaries(cont);
				return (bins != null) && bins.length > 0;
			} catch (CModelException e) {
				return false;
			}
		}
		return super.hasChildren(element);
	}
}
