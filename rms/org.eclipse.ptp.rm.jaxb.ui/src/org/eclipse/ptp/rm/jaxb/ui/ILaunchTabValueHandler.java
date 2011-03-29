package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.core.variables.LTVariableMap;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public interface ILaunchTabValueHandler {

	void setValuesOnMap(LTVariableMap ltMap);

	void setDefaultValuesOnControl(RMVariableMap rmMap);

	void getValuesFromMap(LTVariableMap ltMap);

	void validateControlValues(RMVariableMap rmMap, IRemoteFileManager manager) throws Throwable;

}
