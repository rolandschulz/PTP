package org.eclipse.ptp.rm.jaxb.core;

import java.util.Map;

public interface IVariableMap {

	Map<String, ?> getDiscovered();

	String getString(String value);

	String getString(String jobId, String value);

	Map<String, ?> getVariables();
}
