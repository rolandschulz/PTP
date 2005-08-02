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
package org.eclipse.ptp.ui.views.old;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.MultiEditorInput;

/**
 *
 */

public class ProcessEditorInput implements IEditorInput {
    private IPProcess process = null;
    
    public ProcessEditorInput(IPProcess process) {
		if (process == null) {
			throw new IllegalArgumentException();
		}
        this.process = process;
    }
    
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ProcessEditorInput))
			return false;
		ProcessEditorInput other = (ProcessEditorInput) obj;
		return process.equals(other.process);
	}
	
	/**
	 * From the implementing class IEditorInput.  This method only has to do
	 * with whether "an editor input should appear in the "File Most Recently 
	 * Used" menu.  In our case, just return true.
	 * 
	 * @see	IEditorInput
	 */
	public boolean exists() {
		/* return process.exists(); */
		return true;
	}
	/*
	public boolean exists() {
		return process.exists();
	}
	*/
	
	public IPProcess getProcess() {
	    MultiEditorInput l;
		return process;
	}
	
	public ImageDescriptor getImageDescriptor() {
		return null;
	}
	
	public String getName() {
		return process.getElementName();
	}
	
	public IPersistableElement getPersistable() {
		return null;
	}
	
	public String getToolTipText() {
		return getName();
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter == IPProcess.class)
			return process;
		return null;
	}	
}
