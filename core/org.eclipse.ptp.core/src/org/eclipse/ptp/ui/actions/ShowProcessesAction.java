package org.eclipse.ptp.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ptp.ui.ParallelImages;
import org.eclipse.ptp.ui.UIMessage;
import org.eclipse.ptp.ui.views.AbstractParallelView;
import org.eclipse.ui.part.ViewPart;

/**
 * @author ndebard
 */
public class ShowProcessesAction extends ParallelAction {

	public ShowProcessesAction(ViewPart view) {
		super(view, Action.AS_RADIO_BUTTON);
	}

	protected void init(boolean isEnable) {
	    this.setText(UIMessage.getResourceString("ShowProcessesAction.text"));
	    this.setToolTipText(UIMessage.getResourceString("ShowProcessesAction.tooltip"));
	    this.setImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWPROCESSES_ACTION_NORMAL));
	    this.setDisabledImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWPROCESSES_ACTION_DISABLE));
	    this.setHoverImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWPROCESSES_ACTION_HOVER));
	    this.setEnabled(getLaunchManager().isMPIRuning());
	}

	public void run() {
		((AbstractParallelView)getViewPart()).showProcesses();
	}

}
