/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.fdt.utils.xcoff.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.IBinaryParser;
import org.eclipse.fdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.fdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.fdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.fdt.utils.BinaryFile;
import org.eclipse.fdt.utils.xcoff.AR;

/**
 * XCOFF32 binary archive
 * 
 * @author vhirsl
 */
public class XCOFFBinaryArchive extends BinaryFile implements IBinaryArchive {
	private ArrayList children;

	/**
	 * @param parser
	 * @param path
	 * @throws IOException
	 */
	public XCOFFBinaryArchive(IBinaryParser parser, IPath path) throws IOException {
		super(parser, path, IBinaryFile.ARCHIVE);
		new AR(path.toOSString()).dispose(); // check file type
		children = new ArrayList(5);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.IBinaryParser.IBinaryArchive#getObjects()
	 */
	public IBinaryObject[] getObjects() {
		if (hasChanged()) {
			children.clear();
			AR ar = null;
			try {
				ar = new AR(getPath().toOSString());
				AR.MemberHeader[] headers = ar.getHeaders();
				for (int i = 0; i < headers.length; i++) {
					IBinaryObject bin = new XCOFFBinaryObject(getBinaryParser(), getPath(), headers[i]);
					children.add(bin);
				}
			} catch (IOException e) {
				//e.printStackTrace();
			}
			if (ar != null) {
				ar.dispose();
			}
			children.trimToSize();
		}
		return (IBinaryObject[]) children.toArray(new IBinaryObject[0]);
	}
}
