/*******************************************************************************
 * Copyright (c) 2010 The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Benjamin Lindner (ben@benlabs.net) - initial implementation (bug 316671)
 *
 *******************************************************************************/

package org.eclipse.ptp.rm.pbs.core.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @since 5.0
 */
public class AttributeValueMapReader {

	static public List<List<Object>> parse(InputStream in) throws Exception, IOException {

		List<List<Object>> tmpMapMap = new ArrayList<List<Object>>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		// each line is a sequence (:: separated) of entries which correspond to
		// fields in the attribute definiton
		// ignore lines starting with #
		// skip lines which are empty

		String line;

		Integer linenumber = 0;
		while ((line = reader.readLine()) != null) {
			linenumber += 1;
			String tline = line.trim();
			if (tline.startsWith("#"))continue; //$NON-NLS-1$
			if (tline.equals(""))continue; //$NON-NLS-1$
			String[] linesplit = tline.split("::"); //$NON-NLS-1$
			Integer linesplitlen = linesplit.length;

			if (linesplitlen < 3) {
				System.err.println("Attribute Value Map ill-defined," + "linenumber=" + linenumber.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				System.err.println("read:" + tline); //$NON-NLS-1$
				for (String lss : linesplit) {
					System.err.println(lss);
				}
			}
			// format required:
			// Parsed Key Name :: Parsed Value :: PBS Proxy Value

			Pattern keyname = Pattern.compile(linesplit[0].trim(), Pattern.CASE_INSENSITIVE);
			Pattern oldvalue = Pattern.compile(linesplit[1].trim(), Pattern.CASE_INSENSITIVE);
			String newvalue = linesplit[2].trim();

			List<Object> newentry = new ArrayList<Object>();
			newentry.add(keyname);
			newentry.add(oldvalue);
			newentry.add(newvalue);

			tmpMapMap.add(newentry);
		}

		return tmpMapMap;
	}

}
