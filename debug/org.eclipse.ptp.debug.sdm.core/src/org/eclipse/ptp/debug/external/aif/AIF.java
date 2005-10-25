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

package org.eclipse.ptp.debug.external.aif;

public class AIF {
	private static final char FDS_INTEGER = 'i';
	
	private static final int FDS_INTEGER_SIGN_POS = 1;
	private static final int FDS_INTEGER_LEN_POS = 2;
	
	public static IAIF toAIF(String format, String data) {
		IAIF res = null;
		
		switch (format.charAt(0)) {
		case FDS_INTEGER:
			int len = Character.digit(format.charAt(FDS_INTEGER_LEN_POS), 10);
			int intVal = 0;
			byte[] intBytes = data.getBytes();
			
			if ( len > 4) {
				// throw AIFLenException
			}
			
			for (int i = 0 ; i < len ; i++ )
				intVal = intVal * 0x100 + (intBytes[i] & 0xff);

			res = new AIFInt(intVal);
		}
		
		return res;
	}
}
