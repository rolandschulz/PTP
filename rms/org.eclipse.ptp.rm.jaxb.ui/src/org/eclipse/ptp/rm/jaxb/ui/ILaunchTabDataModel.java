package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public interface ILaunchTabDataModel {

	String getValueAsString();

	void getValueFromConfiguration(ILaunchConfiguration configuration);

	Object getValueFromTarget();

	boolean isDynamic();

	void setValue(Object value);

	void setValueOnConfiguration(ILaunchConfigurationWorkingCopy configuration);

	Object setValueOnTarget();
}
