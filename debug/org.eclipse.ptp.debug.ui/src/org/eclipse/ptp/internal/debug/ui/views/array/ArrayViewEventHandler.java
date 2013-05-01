package org.eclipse.ptp.internal.debug.ui.views.array;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.internal.debug.ui.views.AbstractPDebugViewEventHandler;

/**
 * @author clement
 * 
 */
public class ArrayViewEventHandler extends AbstractPDebugViewEventHandler {
	/**
	 * Constructs a new event handler on the given view
	 */
	public ArrayViewEventHandler(ArrayView view) {
		super(view);
	}

	public ArrayView getArrayView() {
		return (ArrayView) getView();
	}

	@Override
	public void refresh(boolean all) {
		getArrayView().repaint(all);
	}

	protected void doHandleDebugEvent(IPDebugEvent event, IProgressMonitor monitor) {
		switch (event.getKind()) {
		case IPDebugEvent.TERMINATE:
			switch (event.getDetail()) {
			case IPDebugEvent.DEBUGGER:
				getArrayView().closeAllTabs();
				break;
			case IPDebugEvent.REGISTER:
				refresh();
				break;
			}
			break;
		case IPDebugEvent.RESUME:
			break;
		case IPDebugEvent.SUSPEND:
			refresh();
			break;
		}
	}
}
