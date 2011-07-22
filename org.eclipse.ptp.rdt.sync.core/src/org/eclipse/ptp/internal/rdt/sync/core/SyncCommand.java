package org.eclipse.ptp.internal.rdt.sync.core;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

public class SyncCommand implements IHandler {
	public static enum SYNC_MODE {
		ACTIVE, ALL, NONE
	};

	private static SYNC_MODE syncMode = SYNC_MODE.ACTIVE;
	private static final String SYNC_COMMAND_PARAMETER_ID = "org.eclipse.ptp.internal.rdt.sync.core.syncCommand.syncModeParameter"; //$NON-NLS-1$

	public static SYNC_MODE getSyncMode() {
		return syncMode;
	}

	public void addHandlerListener(IHandlerListener handlerListener) {
		// Listeners not yet supported
	}

	public void dispose() {
		// Nothing to do
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String command = event.getParameter(SYNC_COMMAND_PARAMETER_ID);
		if (command.equals("sync")) { //$NON-NLS-1$
			// TODO: What to do?
		} else if (command.equals("active")) { //$NON-NLS-1$
			syncMode = SYNC_MODE.ACTIVE;
		} else if (command.equals("all")) { //$NON-NLS-1$
			syncMode = SYNC_MODE.ALL;
		} else if (command.equals("none")) { //$NON-NLS-1$
			syncMode = SYNC_MODE.NONE;
		}
		return null;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isHandled() {
		return false;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		// Listeners not yet supported
	}

}
