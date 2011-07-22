/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.core;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

public class SyncCommand extends AbstractHandler implements IElementUpdater {
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

		ICommandService service = (ICommandService) HandlerUtil.getActiveWorkbenchWindowChecked(event).getService(
				ICommandService.class);
		service.refreshElements(event.getCommand().getId(), null);

		return null;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isHandled() {
		return true;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		// Listeners not yet supported
	}

	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		String parm = (String) parameters.get(SYNC_COMMAND_PARAMETER_ID);
		if (parm != null) {
			if ((parm.equals("active") && syncMode == SYNC_MODE.ACTIVE) || //$NON-NLS-1$
					(parm.equals("all") && syncMode == SYNC_MODE.ALL) || //$NON-NLS-1$
					(parm.equals("none") && syncMode == SYNC_MODE.NONE)) { //$NON-NLS-1$
				element.setChecked(true);
			} else {
				element.setChecked(false);
			}
		}
	}
}
