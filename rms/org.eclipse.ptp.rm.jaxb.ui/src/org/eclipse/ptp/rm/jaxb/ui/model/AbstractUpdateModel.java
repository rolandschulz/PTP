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
import org.eclipse.ptp.rm.jaxb.core.data.ValidatorType;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

/**
 * Base class for implementations of the IUpdateModel controlling the data
 * associated with a widget or cell editor.
 * 
 * @author arossi
 * 
 */
public abstract class AbstractUpdateModel implements IUpdateModel, IJAXBNonNLSConstants {

	/**
	 * Used with ModifyListeners so as to avoid a save on every keystroke.
	 * 
	 * @author arossi
	 */
	protected class ValidateJob extends UIJob {
		public ValidateJob() {
			super(VALIDATE);
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
	protected ValidatorType validator;
	protected IRemoteFileManager remoteFileManager;
	protected String defaultValue;
	protected Object mapValue;

	/**
	 * @param name
	 *            name of the model, which will correspond to the name of a
	 *            Property or Attribute if the widget value is to be saved.
	 * @param handler
	 *            the handler for notifying other widgets to refresh their
	 *            values
	 */
	protected AbstractUpdateModel(String name, ValueUpdateHandler handler) {
		this.name = name;
		canSave = (name != null && !ZEROSTR.equals(name));
		this.handler = handler;
		refreshing = false;
		validateJob = new ValidateJob();
	}

	/**
	 * @return The widget or cell editor.
	 */
	public abstract Object getControl();

	/**
	 * @return name of the model, which will correspond to the name of a
	 *         Property or Attribute if the widget value is to be saved.
	 */
	public String getName() {
		return name;
	}

	/**
	 * If this widget saves its value to a Property of Attribute, then the
	 * default value here is retrieved. The widget value is then refreshed from
	 * the map, and if the value is <code>null</code>, the default value is
	 * restored to the map and another refresh is called on the actual value.
	 */
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

	/**
	 * Sets the actual value in the current environment to the default value.
	 */
	public void restoreDefault() {
		lcMap.put(name, defaultValue);
	}

	/**
	 * @validator JAXB data element describing either regex or efs validation
	 *            for the widget value.
	 * @remoteFileManager provided in case the validation is to be done on a
	 *                    file path.
	 */
	public void setValidator(ValidatorType validator, IRemoteFileManager remoteFileManager) {
		this.validator = validator;
		this.remoteFileManager = remoteFileManager;
	}

	/**
	 * Delegates to the handler update method.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler#handleUpdate(Object,
	 *      Object)
	 * 
	 * @param value
	 *            updated value (currently unused)
	 */
	protected void handleUpdate(Object value) {
		handler.handleUpdate(getControl(), value);
	}

	/**
	 * Retrieves the value from the control, validates if there is a validator
	 * set, then writes to the current environment map and calls the update
	 * handler.
	 */
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
