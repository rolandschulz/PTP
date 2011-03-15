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
import java.util.Iterator;
import java.util.List;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.Arg;
import org.eclipse.ptp.rm.jaxb.core.data.Arglist;

public class ArglistImpl implements IJAXBNonNLSConstants {

	private final String uuid;
	private final Arglist args;
	private final IVariableMap map;

	public ArglistImpl(String uuid, Arglist args, IVariableMap map) {
		this.uuid = uuid;
		this.args = args;
		this.map = map;
		assert (null != this.args);
		assert (null != this.map);
	}

	public String[] toArray() {
		List<String> list = new ArrayList<String>();
		if (args.isDynamicAppend()) {
			composeDynamicArgs(list);
		} else {
			composeStandardArgs(list);
		}
		return list.toArray(new String[0]);
	}

	public void toString(StringBuffer buffer) {
		if (args.isDynamicAppend()) {
			composeDynamicArgs(buffer);
		} else {
			composeStandardArgs(buffer);
		}
	}

	private String addDynamicArg(String name, Arg next) {
		String content = next.getContent();
		content = content.replaceAll(AMP, name + PD).trim();
		String undefined = next.getIsUndefinedIfEquals();
		undefined = undefined.replaceAll(AMP, name + PD);
		content = map.getString(uuid, content);
		if (undefined != null) {
			undefined = undefined.trim();
			undefined = map.getString(uuid, undefined);
			if (undefined.equals(content)) {
				return ZEROSTR;
			}
		}
		return content;
	}

	private String addStandardArg(Arg next) {
		String dereferenced = map.getString(uuid, next.getContent());
		if (dereferenced != null) {
			dereferenced = dereferenced.trim();
		}
		String undefined = next.getIsUndefinedIfEquals();
		if (undefined != null) {
			undefined = undefined.trim();
			undefined = map.getString(uuid, undefined);
			if (undefined.equals(dereferenced)) {
				return ZEROSTR;
			}
		}
		return dereferenced;
	}

	/*
	 * Iterate over all dynamic attributes, appending the sequence of args for
	 * each attribute. By convention, '${@name}' and '${@value}' will refer to a
	 * given dynamic attribute name and value (of undefined position i in the
	 * list).
	 */
	private void composeDynamicArgs(List<String> list) {
		for (String name : map.getDiscovered().keySet()) {
			Iterator<Arg> i = args.getArg().iterator();
			while (i.hasNext()) {
				String arg = addDynamicArg(name, i.next());
				if (!ZEROSTR.equals(arg)) {
					list.add(arg);
				}
			}
		}
	}

	/*
	 * Iterate over all dynamic attributes, appending the sequence of args for
	 * each attribute. By convention, '${@name}' and '${@value}' will refer to a
	 * given dynamic attribute name and value (of undefined position i in the
	 * list).
	 */
	private void composeDynamicArgs(StringBuffer buffer) {
		for (String name : map.getDiscovered().keySet()) {
			Iterator<Arg> i = args.getArg().iterator();
			if (i.hasNext()) {
				buffer.append(addDynamicArg(name, i.next()));
			}
			while (i.hasNext()) {
				String arg = addDynamicArg(name, i.next());
				if (!ZEROSTR.equals(arg)) {
					buffer.append(SP).append(arg);
				}
			}
		}
	}

	private void composeStandardArgs(List<String> list) {
		Iterator<Arg> i = args.getArg().iterator();
		while (i.hasNext()) {
			String arg = addStandardArg(i.next());
			if (!ZEROSTR.equals(arg)) {
				list.add(arg);
			}
		}
	}

	private void composeStandardArgs(StringBuffer buffer) {
		Iterator<Arg> i = args.getArg().iterator();
		if (i.hasNext()) {
			buffer.append(addStandardArg(i.next()));
		}
		while (i.hasNext()) {
			String arg = addStandardArg(i.next());
			if (!ZEROSTR.equals(arg)) {
				buffer.append(SP).append(arg);
			}
		}
	}
}
