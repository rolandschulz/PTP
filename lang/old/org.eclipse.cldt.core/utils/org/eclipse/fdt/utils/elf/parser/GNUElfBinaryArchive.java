/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.fdt.utils.elf.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.IBinaryParser;
import org.eclipse.fdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.fdt.utils.AR.ARHeader;


public class GNUElfBinaryArchive extends ElfBinaryArchive {

	/**
	 * @param parser
	 * @param p
	 * @throws IOException
	 */
	public GNUElfBinaryArchive(IBinaryParser parser, IPath p) throws IOException {
		super(parser, p);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.utils.elf.parser.BinaryArchive#addArchiveMembers(org.eclipse.fdt.utils.elf.AR.ARHeader[], java.util.ArrayList)
	 */
	protected void addArchiveMembers(ARHeader[] headers, ArrayList children2) {
		for (int i = 0; i < headers.length; i++) {
			IBinaryObject bin = new GNUElfBinaryObject(getBinaryParser(), getPath(), headers[i]);
			children.add(bin);
		}
	}

}
