package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.Arg;
import org.eclipse.ptp.rm.jaxb.core.data.Line;

public class LineImpl implements IJAXBNonNLSConstants {

	private final String uuid;
	private final IVariableMap map;
	private final List<Arg> args;

	public LineImpl(String uuid, Line line, IVariableMap map) {
		this.uuid = uuid;
		this.map = map;
		args = line.getArg();
	}

	public String getResolved() {
		return ArgImpl.toString(uuid, args, map);
	}
}
