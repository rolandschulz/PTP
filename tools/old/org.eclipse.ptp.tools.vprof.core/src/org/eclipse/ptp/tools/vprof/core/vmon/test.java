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

import org.eclipse.cdt.utils.elf.parser.ElfParser;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.core.runtime.Path;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		VMonFile vf = new VMonFile();
		try {
			vf.Read("vmon.out");
		} catch (IOException e) {
			System.out.println("could not open file: " + e.getMessage());
			return;
		}
		
		for (int i = 0; i < vf.getData().length; i++) {
			VMonData vd = vf.getData()[i];
			
			System.out.println("event " + vd.getEvent().getEventName() + ": " + vd.getEvent().getEventDescription());

			VMonData.VMonInfo vi[] = vd.getData();
			
			for (int j = 0; j < vi.length; j++) {
				System.out.println(" addr = " + vi[j].address.toString());
				System.out.println(" count = " + vi[j].count);
			}
		}
		
		IBinaryFile bf;
		ElfParser parser = new ElfParser();
		Path p = new Path("test2");
	
		try {
			bf = parser.getBinary(p);
		} catch (IOException e) {
			System.out.println("could not open binary: " + e.getMessage());
			return;
		}
		
		if (bf.getType() == IBinaryFile.EXECUTABLE)
			System.out.println("is an executable");
		else
			System.out.println("isnt an executable");
		
	}

}
