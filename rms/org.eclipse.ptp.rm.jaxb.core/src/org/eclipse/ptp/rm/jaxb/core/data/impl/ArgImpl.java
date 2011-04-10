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

/**
 * Wrapper implementation.
 * 
 * @author arossi
 * 
 */
public class ArgImpl implements IJAXBNonNLSConstants {

	private final String uuid;
	private final Arg arg;
	private final IVariableMap map;

	/**
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param arg
	 *            JAXB data element.
	 * @param map
	 *            environment in which to resolve content of the arg
	 */
	public ArgImpl(String uuid, Arg arg, IVariableMap map) {
		this.uuid = uuid;
		this.arg = arg;
		this.map = map;
	}

	/**
	 * Will not return <code>null</code>.
	 * 
	 * @return argument resolved in the provided environment
	 */
	public String getResolved() {
		return getResolved(uuid, arg, map);
	}

	/**
	 * Auxiliary iterator.
	 * 
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param args
	 *            JAXB data elements.
	 * @param map
	 *            environment in which to resolve content of the arg
	 * @return array of resolved arguments
	 */
	public static String[] getArgs(String uuid, List<Arg> args, IVariableMap map) {
		List<String> resolved = new ArrayList<String>();
		for (Arg a : args) {
			resolved.add(getResolved(uuid, a, map));
		}
		return resolved.toArray(new String[0]);
	}

	/**
	 * Auxiliary iterator.
	 * 
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param args
	 *            JAXB data elements.
	 * @param map
	 *            environment in which to resolve content of the arg
	 * @return whitespace separated string of resolved arguments
	 */
	public static String toString(String uuid, List<Arg> args, IVariableMap map) {
		if (args.isEmpty()) {
			return ZEROSTR;
		}
		StringBuffer b = new StringBuffer();
		String resolved = getResolved(uuid, args.get(0), map);
		if (!ZEROSTR.equals(resolved)) {
			b.append(resolved);
		}
		for (int i = 1; i < args.size(); i++) {
			resolved = getResolved(uuid, args.get(i), map);
			if (!ZEROSTR.equals(resolved)) {
				b.append(SP).append(resolved);
			}
		}
		return b.toString();
	}

	/**
	 * Checks first to see if resolution is indicated for the argument. After
	 * calling the resolver, checks to see if the resulting argument should be
	 * considered equivalent to undefined.
	 * 
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param arg
	 *            JAXB data element
	 * @param map
	 *            environment in which to resolve content of the arg
	 * @return result of resolution
	 */
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
