package org.eclipse.ptp.ui.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.MultiEditorInput;

/**
 * @author Clement
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
	
	public boolean exists() {
		return process.exists();
	}
	
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
