/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Validator;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

public abstract class AbstractUpdateModel implements IUpdateModel, IJAXBNonNLSConstants {

	/*
	 * Used with ModifyListeners so as to avoid a save on every keystroke.
	 * 
	 * @author arossi
	 */
	protected class ValidateJob extends UIJob {
		public ValidateJob() {
			super("ValidateJob"); //$NON-NLS-1$
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			storeValue();
			return Status.OK_STATUS;
		}
	}

	protected final Job validateJob;

	protected boolean canSave;
	protected String name;
	protected LCVariableMap lcMap;
	protected ValueUpdateHandler handler;
	protected boolean refreshing;
	protected Validator validator;
	protected IRemoteFileManager remoteFileManager;
	protected String defaultValue;
	protected Object mapValue;

	protected AbstractUpdateModel(String name, ValueUpdateHandler handler) {
		this.name = name;
		canSave = (name != null && !ZEROSTR.equals(name));
		this.handler = handler;
		refreshing = false;
		validateJob = new ValidateJob();
	}

	public abstract Object getControl();

	public String getName() {
		return name;
	}

	public void initialize(LCVariableMap lcMap) {
		this.lcMap = lcMap;
		if (name != null) {
			defaultValue = lcMap.getDefault(name);
		}
		refreshValueFromMap();
		if (mapValue == null) {
			restoreDefault();
			refreshValueFromMap();
		}
	}

	public void restoreDefault() {
		lcMap.put(name, defaultValue);
	}

	public void setValidator(Validator validator, IRemoteFileManager remoteFileManager) {
		this.validator = validator;
		this.remoteFileManager = remoteFileManager;
	}

	protected void handleUpdate(Object value) {
		handler.handleUpdate(getControl(), value);
	}

	protected void storeValue() {
		Object value = getValueFromControl();
		if (validator != null) {
			try {
				WidgetActionUtils.validate(String.valueOf(value), validator, remoteFileManager);
			} catch (Exception t) {
				WidgetActionUtils.errorMessage(Display.getCurrent().getActiveShell(), t, Messages.ValidationError,
						Messages.ValidationError_title, false);
				refreshValueFromMap();
				return;
			}
		}
		lcMap.put(name, value);
		handleUpdate(value);
	}
}
