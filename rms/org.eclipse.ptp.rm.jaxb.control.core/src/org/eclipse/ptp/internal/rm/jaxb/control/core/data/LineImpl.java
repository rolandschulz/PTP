package org.eclipse.ptp.internal.rm.jaxb.control.core.data;

import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.ArgType;
import org.eclipse.ptp.rm.jaxb.core.data.LineType;

/**
 * Wrapper implementation.
 * 
 * @author arossi
 * 
 */
public class LineImpl {

	private final String uuid;
	private final IVariableMap map;
	private final List<ArgType> args;

	/**
	 * @param uuid
	 *            unique id associated with this resource manager operation (can be <code>null</code>).
	 * @param line
	 *            JAXB data element
	 * @param map
	 *            environment in which to resolve content of the line
	 */
	public LineImpl(String uuid, LineType line, IVariableMap map) {
		this.uuid = uuid;
		this.map = map;
		args = line.getArg();
	}

	/**
	 * Will not return <code>null</code>.
	 * 
	 * @return argument resolved in the provided environment
	 */
	public String getResolved() {
		return ArgImpl.toString(uuid, args, map);
	}
}
