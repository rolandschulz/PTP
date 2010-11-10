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

package org.eclipse.ptp.rm.pbs.jproxy.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.core.attributes.BooleanAttributeDefinition;
import org.eclipse.ptp.core.attributes.DateAttributeDefinition;
import org.eclipse.ptp.core.attributes.DoubleAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;

import com.ibm.icu.text.DateFormat;

/**
 * @since 5.0
 */
public class AttributeDefinitionReader {
	static public List<IAttributeDefinition<?, ?, ?>> parse(InputStream in) throws Exception, IOException {

		List<IAttributeDefinition<?, ?, ?>> tmpList = new ArrayList<IAttributeDefinition<?, ?, ?>>();

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

			if (linesplitlen < 5) {
				System.err.println("Attribute Definition ill-defined," + "linenumber=" + linenumber.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				System.err.println("read:" + tline); //$NON-NLS-1$
				for (String lss : linesplit) {
					System.err.println(lss);
				}
			} else if (linesplitlen < 6) {
				// split discarded a trailing default which was empty and thus
				// trimed away:
				List<String> nlinesplit = new ArrayList<String>();
				for (String lss : linesplit) {
					nlinesplit.add(lss);
				}
				nlinesplit.add(""); //$NON-NLS-1$
				linesplit = nlinesplit.toArray(new String[nlinesplit.size()]);
			}

			// format required:
			// ID :: TYPE :: NAME :: DESCRIPTION :: DISPLAY :: DEFAULT :: CUSTOM
			// FIELD 1 :: CUSTOM FIELD 2 :: etc

			// somebool :: BOOLEAN :: someboolname :: this is an ex. for a bool
			// :: true :: true
			// somedate :: DATE :: somedatename :: this is an ex. for a date ::
			// true :: 00:30:00 :: HH:MM:SS

			if ("boolean".compareToIgnoreCase(linesplit[1].trim()) == 0) { //$NON-NLS-1$
				tmpList.add(new BooleanAttributeDefinition(linesplit[0].trim(), // id
						linesplit[2].trim(), // name
						linesplit[3].trim(), // description
						Boolean.valueOf(linesplit[4].trim()).booleanValue(), // display
						Boolean.valueOf(linesplit[5].trim()) // default
				));
			}
			if ("string".compareToIgnoreCase(linesplit[1].trim()) == 0) { //$NON-NLS-1$
				tmpList.add(new StringAttributeDefinition(linesplit[0].trim(), // id
						linesplit[2].trim(), // name
						linesplit[3].trim(), // description
						Boolean.valueOf(linesplit[4].trim()).booleanValue(), // display
						linesplit[5].trim() // default
				));
			}
			if ("integer".compareToIgnoreCase(linesplit[1].trim()) == 0) { //$NON-NLS-1$
				boolean minmax = false;
				if (linesplitlen >= 8)
					minmax = true;

				if (minmax) {
					tmpList.add(new IntegerAttributeDefinition(linesplit[0].trim(), // id
							linesplit[2].trim(), // name
							linesplit[3].trim(), // description
							Boolean.valueOf(linesplit[4].trim()).booleanValue(), // display
							Integer.parseInt(linesplit[5].trim()), // default
							Integer.parseInt(linesplit[6].trim()), // min
							Integer.parseInt(linesplit[7].trim()) // max
					));
				} else {
					tmpList.add(new IntegerAttributeDefinition(linesplit[0].trim(), // id
							linesplit[2].trim(), // name
							linesplit[3].trim(), // description
							Boolean.valueOf(linesplit[4].trim()).booleanValue(), // display
							Integer.parseInt(linesplit[5].trim()) // default
					));
				}
			}
			if ("double".compareToIgnoreCase(linesplit[1].trim()) == 0) { //$NON-NLS-1$
				boolean minmax = false;
				if (linesplitlen >= 8)
					minmax = true;

				if (minmax) {
					tmpList.add(new DoubleAttributeDefinition(linesplit[0].trim(), // id
							linesplit[2].trim(), // name
							linesplit[3].trim(), // description
							Boolean.valueOf(linesplit[4].trim()).booleanValue(), // display
							Double.parseDouble(linesplit[5].trim()), // default
							Double.parseDouble(linesplit[6].trim()), // min
							Double.parseDouble(linesplit[7].trim()) // max
					));
				} else {
					tmpList.add(new DoubleAttributeDefinition(linesplit[0].trim(), // id
							linesplit[2].trim(), // name
							linesplit[3].trim(), // description
							Boolean.valueOf(linesplit[4].trim()).booleanValue(), // display
							Double.parseDouble(linesplit[5].trim()) // default
					));
				}
			}
			if ("date".compareToIgnoreCase(linesplit[1].trim()) == 0) { //$NON-NLS-1$
				boolean minmax = false;
				if (linesplitlen >= 9)
					minmax = true;

				DateFormat df = DateFormat.getPatternInstance(linesplit[6].trim());

				if (minmax) {
					tmpList.add(new DateAttributeDefinition(linesplit[0].trim(), // id
							linesplit[2].trim(), // name
							linesplit[3].trim(), // description
							Boolean.valueOf(linesplit[4].trim()).booleanValue(), // display
							df.parse(linesplit[5].trim()), // default
							df, df.parse(linesplit[7].trim()), // min
							df.parse(linesplit[8].trim()) // max
					));
				} else {
					tmpList.add(new DateAttributeDefinition(linesplit[0].trim(), // id
							linesplit[2].trim(), // name
							linesplit[3].trim(), // description
							Boolean.valueOf(linesplit[4].trim()).booleanValue(), // display
							df.parse(linesplit[5].trim()), // default
							df));
				}
			}
		}

		return tmpList;

	}
}
