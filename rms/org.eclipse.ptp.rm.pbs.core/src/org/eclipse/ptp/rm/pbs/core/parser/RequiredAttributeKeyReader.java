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

/**
 * @since 5.0
 */
public class RequiredAttributeKeyReader {

	// this parser read a custom text file which 
	// maps PBS Proxy IDs to Resource Manager Model Definitions
	static public List<String> parse(InputStream in) 
		throws Exception, IOException {
		
		List<String> tmpList = new ArrayList<String>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		// each line is a sequence (:: separated) of entries which correspond to fields in the attribute definiton
		// ignore lines starting with #
		// skip lines which are empty
			
		String line;

		Integer linenumber =0;
		while ((line = reader.readLine()) != null) {
			linenumber+=1;
			String tline = line.trim();
			if (tline.startsWith("#")) continue;
			if (tline.equals("")) continue;
			String[] linesplit = tline.split("::");
			Integer linesplitlen= linesplit.length;

			if (linesplitlen>1) {
				System.err.println("Required Attribute Key Reader entry ill-defined,"+"linenumber="+linenumber.toString());
				System.err.println("read:"+tline);
				for ( String lss : linesplit ) {
					System.err.println(lss);					
				}
			}
			// format required:
			// Proxy Attribute Definition :: Resource Manager Model Attribute Definition

			tmpList.add(linesplit[0].trim());

		}
					
		return tmpList;
	}

}
