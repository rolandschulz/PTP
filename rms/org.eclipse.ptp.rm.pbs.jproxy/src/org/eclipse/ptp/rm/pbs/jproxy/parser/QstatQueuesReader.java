/*******************************************************************************
 * Copyright (c) 2010 Dieter Krachtus and The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dieter Krachtus (dieter.krachtus@gmail.com) and Roland Schulz - initial API and implementation
 *    Benjamin Lindner (ben@benlabs.net)

 *******************************************************************************/

package org.eclipse.ptp.rm.pbs.jproxy.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.rm.proxy.core.attributes.AttributeDefinition;
import org.eclipse.ptp.rm.proxy.core.element.IElement;
import org.eclipse.ptp.rm.proxy.core.parser.IParser;

// TODO: Auto-generated Javadoc
/**
 * Parser for non-xml PBS queue format (qstat -Q -f -1)
 */
public class QstatQueuesReader implements IParser {

	/** The queues. */
	private Set<IElement> queues;

	private void _parse(InputStream in, AttributeDefinition attrDef)
			throws Exception, IOException {
		queues = new HashSet<IElement>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		Pattern queuepattern = Pattern.compile("^[Qq]ueue:(.*)$"); //$NON-NLS-1$
		Pattern keyvalpattern = Pattern.compile("^([^=]+)=(.+)$"); //$NON-NLS-1$

		String line;
		ArrayList<HashMap<String, String>> qhashes = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> thisqueue = new HashMap<String, String>();
		boolean firstqueue = true;
		// parse the input file and store the individual queues as Hashmaps in
		// qhashes
		while ((line = reader.readLine()) != null) {
			Matcher mq = queuepattern.matcher(line);

			boolean newqueue = mq.find();
			if (newqueue) {
				if (!firstqueue) {
					qhashes.add(thisqueue);
				} else {
					firstqueue = false;
				}
				thisqueue = new HashMap<String, String>();
				thisqueue.put("name", mq.group(1).trim()); //$NON-NLS-1$
				continue;
			}

			Matcher mkv = keyvalpattern.matcher(line);
			if (!mkv.find()) {
				continue;
			}
			if (mkv.groupCount() != 2) {
				continue;
			}
			String skey = mkv.group(1).trim().toLowerCase();
			String svalue = mkv.group(2).trim();

			thisqueue.put(skey, svalue);
		}
		if (!firstqueue) {
			qhashes.add(thisqueue);
		}

		// now convert the hashmap representation into beans
		for (HashMap<String, String> q : qhashes) {
			IElement e = attrDef.createElement();
			for (String attr : attrDef.getRequiredAttributes()) {
				e.setAttribute(attr, q.get(attr));
			}
			// for (Entry<String, String> entry : q.entrySet()) {
			// e.setAttribute(entry.getKey(), entry.getValue());
			// }
			queues.add(e);
		}
	}

	private Set<IElement> getQueues() {
		return queues;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.proxy.core.parser.IParser#parse(org.eclipse.ptp.rm
	 * .proxy.core.attributes.AttributeDefinition, java.io.InputStream)
	 */
	public Set<IElement> parse(AttributeDefinition attrDef, InputStream in) {
		Set<IElement> queues = new HashSet<IElement>();
		try {
			// qstat -Q -f is not XML - specific Reader has to be used.
			_parse(in, attrDef);
			queues = getQueues();
		} catch (IOException e) {
			System.out.println(e);
		} catch (Exception e) {
			System.out.println(e);
		}

		// System.err.println("queues length -> " + queues.size());
		return queues;
	}

}