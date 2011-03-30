package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.Arg;

public class ArgImpl implements IJAXBNonNLSConstants {

	private final String uuid;
	private final Arg arg;
	private final IVariableMap map;

	public ArgImpl(String uuid, Arg arg, IVariableMap map) {
		this.uuid = uuid;
		this.arg = arg;
		this.map = map;
	}

	public String getResolved() {
		return getResolved(uuid, arg, map);
	}

	public static String[] getArgs(String uuid, List<Arg> args, IVariableMap map) {
		List<String> resolved = new ArrayList<String>();
		for (Arg a : args) {
			resolved.add(getResolved(uuid, a, map));
		}
		return resolved.toArray(new String[0]);
	}

	public static void toString(String uuid, List<Arg> args, IVariableMap map, StringBuffer b) {
		if (args.isEmpty()) {
			return;
		}
		b.append(getResolved(uuid, args.get(0), map));
		for (int i = 1; i < args.size(); i++) {
			b.append(SP).append(getResolved(uuid, args.get(0), map));
		}
	}

	private static String getResolved(String uuid, Arg arg, IVariableMap map) {
		if (arg == null) {
			return ZEROSTR;
		}
		String dereferenced = map.getString(uuid, arg.getContent());
		if (dereferenced != null) {
			dereferenced = dereferenced.trim();
		}
		String undefined = arg.getIsUndefinedIfMatches();
		if (undefined != null) {
			undefined = undefined.trim();
			undefined = map.getString(uuid, undefined);
			if (dereferenced != null && dereferenced.matches(undefined)) {
				return ZEROSTR;
			}
		}
		return dereferenced;
	}
}
