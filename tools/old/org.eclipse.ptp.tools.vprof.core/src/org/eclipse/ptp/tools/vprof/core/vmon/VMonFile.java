/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/

package org.eclipse.ptp.tools.vprof.core.vmon;

import java.io.IOException;

public class VMonFile {
	public final static int VMON_MAGIC = 0x766d6f6e;
	public final static int VMON_MAGIC_BE = 0x6e6f6d76;
	
	private VMonData data[];
	private VMonEvents events;
	
	public VMonFile() {
		data = null;
		events = new VMonEvents();
	}
	
	public void Read(String filename) throws IOException {
		ERandomAccessFile efile = new ERandomAccessFile(filename, "r");
		
		efile.setEndian(false);
		
		int magic = efile.readIntE();
		
		if (magic == VMON_MAGIC_BE)
			efile.setEndian(true);
		else if (magic != VMON_MAGIC )
			throw new IOException("bad magic number");
		
		int version = efile.readByte();
		
		if (version < 3)
			return;
		
		int count = efile.readIntE();
		data = new VMonData[count];
		
		for (int i = 0; i < count; i++) {
			data[i] = new VMonData(events);
			data[i].Read(efile, version);
		}
	}
	
	public VMonData[] getData() {
		return data;
	}
}
