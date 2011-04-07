package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.core.data.Validator;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;

public interface IUpdateModel extends IJAXBUINonNLSConstants {

	Object getControl();

	String getName();

	Object getValueFromControl();

	void initialize(LCVariableMap lcMap);

	void refreshValueFromMap();

	void restoreDefault();

	void setValidator(Validator validator, IRemoteFileManager remoteFileManager);
}
