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
package org.eclipse.ptp.debug.external.simulator;

import java.io.IOException;
import java.io.InputStream;

public class DInputStream extends InputStream {

	boolean finished;
	DQueue queue;
	
	String str;
	int strLen;
	
	public DInputStream() {
		super();
		queue = new DQueue();
		finished = false;
		str = null;
		strLen = -2;
	}
	
	public void printString(String s) {
		queue.addItem(s);
	}
	
	public void destroy() {
		finished = true;
		queue.addItem("destroy");
	}
	
	public int read() throws IOException {
		if (strLen == 0) {
			strLen--;
			return '\n';
		}
		
		if (strLen == -1) {
			strLen--;
			return -1;
		}
			
		if (strLen == -2) {
			try {
				if (finished) {
					return -1;
				}
				str = (String) queue.removeItem();
				if (str.equals("destroy")) {
					return -1;
				}
				strLen = str.length();
			} catch (InterruptedException e) {
			}
		}
			
		int chr = str.charAt(str.length() - strLen);
		strLen--;
		return chr;
	}
}
