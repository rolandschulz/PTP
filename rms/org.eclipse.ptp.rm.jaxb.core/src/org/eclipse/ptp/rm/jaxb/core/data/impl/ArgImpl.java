/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
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

	public static String toString(String uuid, List<Arg> args, IVariableMap map) {
		if (args.isEmpty()) {
			return ZEROSTR;
		}
		StringBuffer b = new StringBuffer();
		b.append(getResolved(uuid, args.get(0), map));
		for (int i = 1; i < args.size(); i++) {
			b.append(SP).append(getResolved(uuid, args.get(0), map));
		}
		return b.toString();
	}

	private static String getResolved(String uuid, Arg arg, IVariableMap map) {
		if (arg == null) {
			return ZEROSTR;
		}
		if (!arg.isResolve()) {
			return arg.getContent();
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
