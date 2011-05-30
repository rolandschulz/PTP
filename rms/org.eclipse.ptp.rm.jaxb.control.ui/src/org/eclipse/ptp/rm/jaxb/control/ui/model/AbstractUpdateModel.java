/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.model;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.control.ui.launch.JAXBControllerLaunchConfigurationTab;
import org.eclipse.ptp.rm.jaxb.control.ui.utils.WidgetActionUtils;
import org.eclipse.ptp.rm.jaxb.control.ui.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.ValidatorType;
import org.eclipse.ui.progress.UIJob;

/**
 * Base class for implementations of the IUpdateModel controlling the data
 * associated with a widget or cell editor.
 * 
 * @author arossi
 * 
 */
public abstract class AbstractUpdateModel implements IUpdateModel {

	/**
	 * Used with ModifyListeners so as to avoid a save on every keystroke.
	 * 
	 * @author arossi
	 */
	protected class ValidateJob extends UIJob {
		public ValidateJob() {
			super(JAXBControlUIConstants.VALIDATE);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			Object value = storeValue();
			handleUpdate(value);
			return Status.OK_STATUS;
		}
	}

	protected final Job validateJob;

	protected boolean canSave;
	protected String name;
	protected List<String> linkUpdateTo;
	protected LCVariableMap lcMap;
	protected ValueUpdateHandler handler;
	protected boolean refreshing;
	protected ValidatorType validator;
	protected JAXBControllerLaunchConfigurationTab tab;
	protected String defaultValue;
	protected Object mapValue;

	/**
	 * @param name
	 *            name of the model, which will correspond to the name of a
	 *            Property or Attribute if the widget value is to be saved
	 * @param linkUpdateTo
	 *            if a change in this property or attribute value overwrites
	 *            other property or attribute values
	 * @param handler
	 *            the handler for notifying other widgets to refresh their
	 *            values
	 */
	protected AbstractUpdateModel(String name, List<String> linkUpdateTo, ValueUpdateHandler handler) {
		this.name = name;
		this.linkUpdateTo = linkUpdateTo;
		canSave = (name != null && !JAXBControlUIConstants.ZEROSTR.equals(name));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel#isWritable()
	 */
	public boolean isWritable() {
		return canSave;
	}

	/**
	 * Sets the actual value in the current environment to the default value.
	 */
	public void restoreDefault() {
		lcMap.put(name, defaultValue);
	}

	/**
	 * @param validator
	 *            JAXB data element describing either regex or efs validation
	 *            for the widget value.
	 * @param tab
	 *            provided in case the validation is to be done on a file path;
	 *            the delegate must be retrieved lazily as the tab is
	 *            initialized after the widgets are constructed
	 */
	public void setValidator(ValidatorType validator, JAXBControllerLaunchConfigurationTab tab) {
		this.validator = validator;
		this.tab = tab;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel#validate()
	 */
	public String validate() {
		if (validator != null) {
			try {
				WidgetActionUtils.validate(String.valueOf(getValueFromControl()), validator, getRemoteFileManager());
			} catch (Exception t) {
				return validator.getErrorMessage();
			}
		}
		return null;
	}

	/**
	 * Delegates to the handler update method.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler#handleUpdate(Object,
	 *      Object)
	 * 
	 * @param value
	 *            updated value (currently unused)
	 */
	protected void handleUpdate(Object value) {
		handler.handleUpdate(getControl(), value);
	}

	/**
	 * Retrieves the value from the control, then writes to the current
	 * environment map and calls the update handler. <br>
	 * <br>
	 * If the value is linked to another value, that value is also overwritten.
	 */
	protected Object storeValue() {
		Object value = getValueFromControl();
		lcMap.put(name, value);
		if (linkUpdateTo != null) {
			for (String link : linkUpdateTo) {
				if (name.equals(link)) {
					continue;
				}
				if (value == null || JAXBControlUIConstants.ZEROSTR.equals(value)) {
					lcMap.put(link, lcMap.getDefault(link));
				} else {
					lcMap.put(link, value);
				}
			}
		}
		return value;
	}

	/**
	 * Retrieves manager lazily from tab.
	 * 
	 * @return remote file manager, or <code>null</code> if undefined
	 */
	private IRemoteFileManager getRemoteFileManager() {
		if (tab != null) {
			RemoteServicesDelegate d = tab.getDelegate();
			if (d != null) {
				return d.getRemoteFileManager();
			}
		}
		return null;
	}
}
