package org.eclipse.ptp.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Clement
 *
 */
public class ViewStatusAction extends ParallelAction {
	private IWorkbenchWindow window;
	private static ViewStatusAction instance = null;

	public ViewStatusAction(ViewPart viewPart) {
	    super(viewPart);
	}
	
	public ViewStatusAction(ViewPart viewPart, boolean isEnable) {
	    super(viewPart, isEnable);	    
	}

	public void init(boolean isEnable) {
	    /*
	    this.setText(UIMessage.getResourceString("ViewStatusAction.text"));
	    this.setToolTipText(UIMessage.getResourceString("ViewStatusAction.tooltip"));
	    this.setImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_VIEWSTATUS_ACTION_NORMAL));
	    this.setDisabledImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_VIEWSTATUS_ACTION_DISABLE));
	    this.setHoverImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_VIEWSTATUS_ACTION_HOVER));
	    this.setEnabled(getLaunchManager().getCurrentState() == LaunchManager.STATE_RUN);
	    */
	}
	
	public void run() {
	    System.out.println("view status action - run");
	    try {
	        getLaunchManager().mpistatus();
	    } catch (CoreException e) {
	        System.out.println("view status action error: " + e.getMessage());
	    }
	}
}