package org.eclipse.ptp.debug.internal.ui.views.locations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.internal.ui.views.AbstractPDebugViewEventHandler;

public class PLocationViewEventHandler extends AbstractPDebugViewEventHandler {
	
	public PLocationViewEventHandler(PLocationView view) {
		super(view);
	}
	public void dispose() {
		super.dispose();
	}
	public PLocationView getPSetView() {
		return (PLocationView)getView();
	}
	public void refresh(boolean all) {
		if (getPSetView().isVisible()) {
			getPSetView().refresh();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.ui.views.AbstractPDebugViewEventHandler#doHandleDebugEvent(org.eclipse.ptp.debug.core.event.IPDebugEvent, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doHandleDebugEvent(IPDebugEvent event, IProgressMonitor monitor) {
		switch(event.getKind()) {
			case IPDebugEvent.CHANGE:
			case IPDebugEvent.TERMINATE:
				refresh(true);
				break;
		}
	}
}
