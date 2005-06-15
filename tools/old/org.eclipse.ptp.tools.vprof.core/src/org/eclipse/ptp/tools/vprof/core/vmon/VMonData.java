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
import java.math.BigInteger;

public class VMonData {
	public final static int VMON_OFF = 4;
	public final static int VMON_OFF64 = 8;
	
	private VMonInfo data[];
	private VMonEvents events;
	private VMonEvents.VMonEvent event;
	private int outofrange;
	private int overflow;
	
	public class VMonInfo {
		BigInteger address; // format needs to include size of address
		int count;
	}
	
	public VMonData(VMonEvents evts) {
		events = evts;
		event = null;
	}
	
	public void Read(ERandomAccessFile efile, int file_version) throws IOException {
		StringBuffer name = new StringBuffer();
		BigInteger offset;
		int oor = 0;
		int ovr = 0;
		
		while ( true ) {
			byte tmp = efile.readByte();
			if (tmp == 0)
				break;
			name.append((char)tmp);
		}
		
		if (file_version > 3) {
			oor = (int)efile.readIntE();
			ovr = (int)efile.readIntE();
		}
		
		int npc = (int)efile.readIntE();
		byte off[] = efile.readBytesE(VMON_OFF64);
		offset = new BigInteger(off);
		
		event = events.findEventByName(name.toString());
		
		data = new VMonInfo[npc];
		
		for (int i = 0; i < npc; i++) {
			data[i] = new VMonInfo();
			
			int count = efile.readShortE();
			data[i].count = count;
			data[i].address = offset.add(BigInteger.valueOf(count));
		}
	}
	
	public VMonEvents.VMonEvent getEvent() {
		return event;
	}
	
	public VMonInfo[] getData() {
		return data;
	}
}
