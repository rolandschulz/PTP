/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.utils.macho.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.IBinaryParser;
import org.eclipse.fdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.fdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.fdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.fdt.utils.BinaryFile;
import org.eclipse.fdt.utils.macho.AR;

/**
 */
public class MachOBinaryArchive extends BinaryFile implements IBinaryArchive {

	ArrayList children;

	public MachOBinaryArchive(IBinaryParser parser, IPath p) throws IOException {
		super(parser, p, IBinaryFile.ARCHIVE);
		new AR(p.toOSString()).dispose(); // check file type
		children = new ArrayList(5);
	}

	/**
	 * @see org.eclipse.fdt.core.model.IBinaryParser.IBinaryArchive#getObjects()
	 */
	public IBinaryObject[] getObjects() {
		if (hasChanged()) {
			children.clear();
			AR ar = null;
			try {
				ar = new AR(getPath().toOSString());
				AR.ARHeader[] headers = ar.getHeaders();
				for (int i = 0; i < headers.length; i++) {
					IBinaryObject bin = new MachOBinaryObject(getBinaryParser(), getPath(), headers[i]);
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
