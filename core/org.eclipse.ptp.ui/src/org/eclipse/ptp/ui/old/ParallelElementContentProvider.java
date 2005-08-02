/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.ui.old;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.ParallelModeException;

/**
 *
 */
public class ParallelElementContentProvider implements ITreeContentProvider {
	protected static final Object[] NO_CHILDREN = new Object[0];

	protected boolean fProvideMembers = false;
	protected boolean fProvideWorkingCopy = false;

	public ParallelElementContentProvider(boolean provideMembers, boolean provideWorkingCopy) {
		fProvideMembers = provideMembers;
		fProvideWorkingCopy = provideWorkingCopy;
	}

	/**
	 * Returns whether the members are provided when asking
	 * for a TU's or ClassFile's children.
	 */
	public boolean getProvideMembers() {
		return fProvideMembers;
	}	

	/**
	 * Returns whether the members are provided when asking
	 * for a TU's or ClassFile's children.
	 */
	public void setProvideMembers(boolean b) {
		fProvideMembers = b;
	}
	
	/**
	 * Sets whether the members are provided from
	 * a working copy of a compilation unit
	 */
	public void setProvideWorkingCopy(boolean b) {
		fProvideWorkingCopy = b;
	}
	
	/**
	 * Returns whether the members are provided
	 * from a working copy a compilation unit.
	 */
	public boolean getProvideWorkingCopy() {
		return fProvideWorkingCopy;
	}
	
	/**
	 * Method declared on IStructuredContentProvider.
	 */
	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}	
	
	/**
	 * Method declared on IContentProvider.
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
	/**
	 * Method declared on IContentProvider.
	 */
	public void dispose() {
	}
	
	/**
	 * Method declared on ITreeContentProvider.
	 */
	public Object[] getChildren(Object element) {
		if (!exists(element))
			return NO_CHILDREN;

		try {
			if (element instanceof IPJob)
				return  getPNodes((IPJob)element);
			
			if  (element instanceof IPNode)
				return getPProcesses((IPNode)element);

		} catch (ParallelModeException e) {
			return NO_CHILDREN;
		}
		return NO_CHILDREN;
	}
	
	/**
	 *
	 * @see ITreeContentProvider
	 */
	public boolean hasChildren(Object element) {
		if (fProvideMembers) {
			if (element instanceof IPProcess)
				return true;
		}

		if (element instanceof IPNode) {
			if (((IPNode)element).hasChildren())
				return true;
		}

		Object[] children= getChildren(element);
		return (children != null) && children.length > 0;
	}

	/**
	 * Method declared on ITreeContentProvider.
	 */
	public Object getParent(Object element) {
		if (!exists(element)) {
			return null;
		}
		return internalGetParent(element);
	}
	
	/**
	 * Note: This method is for internal use only. 
	 */
	protected boolean exists(Object element) {
		if (element == null)
			return false;

		return true;
	}
	
	protected Object internalGetParent(Object element) {
		if (element instanceof IPProcess)
			return ((IPProcess)element).getParent();
		
		return null;
	}
	
	protected Object[] getPNodes(IPJob root) {
	    return root.getSortedChildren();
	}
	
	protected Object[] getPProcesses(IPNode node) throws ParallelModeException {
	    if (node.hasChildren())
	        return NO_CHILDREN;
	    
	    return node.getSortedChildren();
	    /*
	    IPProcess[] processes = node.getProcesses();
	    List list = new ArrayList(processes.length);
	    for (int i=0; i<processes.length; i++) {
	        IPProcess process = processes[i];
	        if (!process.isTerminated())
	            list.add(process);
	    }
	    return list.toArray();
	    */
	}
}
