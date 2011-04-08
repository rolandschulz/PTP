package org.eclipse.ptp.rm.jaxb.ui.model;

import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Validator;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;

public abstract class AbstractUpdateModel implements IUpdateModel, IJAXBNonNLSConstants {

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
				JAXBUIPlugin.log(t);
				refreshValueFromMap();
				return;
			}
		}
		lcMap.put(name, value);
		handleUpdate(value);
	}
}
