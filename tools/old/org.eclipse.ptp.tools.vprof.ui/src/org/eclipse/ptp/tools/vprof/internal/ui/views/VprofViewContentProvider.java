package org.eclipse.ptp.tools.vprof.internal.ui.views;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;

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

	/*
	 * Look for vmon.out
	 */
	protected boolean findVprofProject(Object object) throws CModelException {
		if (object instanceof ICProject) {
			Object[] nonC = ((ICProject)object).getNonCResources();
			for (int i = 0; i < nonC.length; i++) {
				if (findVprofProject(nonC[i]))
					return true;
			}
		} else if (object instanceof IFolder) {
			IFolder folder = (IFolder)object;
			try {
				IResource res[] = folder.members();
				for (int i = 0; i < res.length; i++) {
					if (findVprofProject(res[i]))
						return true;
				}
			} catch (CoreException e) {
				//
			}
		} else if (object instanceof File) {
			IFile file = (IFile)object;
			if (file.getName().compareTo("vmon.out") == 0)
					return true;
		} else {
			System.out.println("unknown");
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getCElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		List list = new ArrayList();
		System.out.println("getElements()");
		if (parent instanceof ICModel) {
			ICModel cModel = (ICModel)parent;
			try {
				ICProject[] cproj = cModel.getCProjects();
				for (int i = 0; i < cproj.length; i++) {
					if (findVprofProject(cproj[i]))
							list.add(cproj[i]);
				}
			} catch (CModelException e) {
				return NO_CHILDREN;
			}
		}
		
		Object[] objects = list.toArray();
		
		return objects;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		//Object[] objs = super.getChildren(element);
		System.out.println("getChildren ()");
		Object[] extras = null;
		try {
			if (element instanceof ICProject) {
				extras = getProjectChildren((ICProject)element);
			} else if (element instanceof IBinaryContainer) {
				extras = getExecutables((IBinaryContainer)element);
			}
		} catch (CModelException e) {
			extras = null;
		}
		if (extras != null && extras.length > 0) {
			return extras;
			//objs = concatenate(objs, extras);
		}
		return NO_CHILDREN;
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
		System.out.println("hasChildren()");
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
	
	public Object getParent(Object element) {
		System.out.println("getParent()");
		return super.getParent(element);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		System.out.println("inputChanged()");
		super.inputChanged(viewer, oldInput, newInput);
	}
	
	
}
