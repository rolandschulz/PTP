/*******************************************************************************
 * Copyright (c) 2011, 2012 University of Illinois.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 * 	Jeff Overbey - Environment Manager support
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.ArgType;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.utils.core.ArgumentParser;

/**
 * Wrapper implementation. An argument is a part of a string sequence which can be resolved in an environment, and then tested to
 * see if the result matches a pattern indicating that the argument is equivalent to being undefined (and thus should be
 * eliminated).
 * 
 * @author arossi
 * @author Jeff Overbey - Environment Manager support
 */
public class ArgImpl {

	/**
	 * Auxiliary iterator.
	 * 
	 * @param uuid
	 *            unique id associated with this resource manager operation (can be <code>null</code>).
	 * @param args
	 *            JAXB data elements.
	 * @param map
	 *            environment in which to resolve content of the arg
	 * @return array of resolved arguments
	 */
	public static String[] getArgs(String uuid, List<ArgType> args, IVariableMap map) {
		List<String> resolved = new ArrayList<String>();
		for (ArgType a : args) {
			ArgumentParser arg = new ArgumentParser(getResolved(uuid, a, map));
			resolved.addAll(arg.getTokenList());
		}
		return resolved.toArray(new String[0]);
	}

	/**
	 * Auxiliary iterator.
	 * 
	 * @param uuid
	 *            unique id associated with this resource manager operation (can be <code>null</code>).
	 * @param args
	 *            JAXB data elements.
	 * @param map
	 *            environment in which to resolve content of the arg
	 * @return whitespace separated string of resolved arguments
	 */
	public static String toString(String uuid, List<ArgType> args, IVariableMap map) {
		if (args.isEmpty()) {
			return JAXBCoreConstants.ZEROSTR;
		}
		StringBuffer b = new StringBuffer();
		String resolved = getResolved(uuid, args.get(0), map);
		if (!JAXBCoreConstants.ZEROSTR.equals(resolved)) {
			b.append(resolved);
		}
		for (int i = 1; i < args.size(); i++) {
			resolved = getResolved(uuid, args.get(i), map);
			if (!JAXBCoreConstants.ZEROSTR.equals(resolved)) {
				b.append(JAXBCoreConstants.SP).append(resolved);
			}
		}
		return b.toString();
	}

	/**
	 * Checks first to see if resolution is indicated for the argument. After calling the resolver, checks to see if the resulting
	 * argument should be considered equivalent to undefined.
	 * 
	 * @param uuid
	 *            unique id associated with this resource manager operation (can be <code>null</code>).
	 * @param arg
	 *            JAXB data element
	 * @param map
	 *            environment in which to resolve content of the arg
	 * @return result of resolution
	 */
	private static String getResolved(String uuid, ArgType arg, IVariableMap map) {
		if (arg == null) {
			return JAXBCoreConstants.ZEROSTR;
		}
		if (!arg.isResolve()) {
			return arg.getContent();
		}
		String dereferenced = map.getString(uuid, arg.getContent());
		// This should already be handled by the variable resolver
		// XXX: Need to check!
		//
		// if (dereferenced != null && !dereferenced.equals("") && EnvManagerConfigString.isEnvMgmtConfigString(dereferenced)) {
		// IEnvManager mgr = map.getEnvManager();
		// if (mgr != null) {
		//				dereferenced = mgr.getBashConcatenation("\n", false, new EnvManagerConfigString(dereferenced), //$NON-NLS-1$
		// null);
		// } else {
		//				dereferenced = ""; //$NON-NLS-1$
		// }
		// }
		String undefined = arg.getIsUndefinedIfMatches();
		String attribute = arg.getAttribute();
		if (attribute != null && undefined != null) {
			AttributeType attrVal = map.get(attribute);
			if (attrVal == null || attrVal.getValue() == null || attrVal.getValue().toString().matches(undefined.trim())) {
				return JAXBCoreConstants.ZEROSTR;
			}
		} else if (undefined != null && dereferenced != null) {
			String dtrim = dereferenced.trim();
			String utrim = undefined.trim();
			utrim = map.getString(uuid, utrim);
			if (dtrim.matches(utrim)) {
				return JAXBCoreConstants.ZEROSTR;
			}
		}
		return dereferenced;
	}

	private ArgImpl() {
	}

}
