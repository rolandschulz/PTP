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

import java.math.BigDecimal;
import java.math.BigInteger;

public class AIF implements IAIF {
	private IAIFType		aifType;
	private IAIFValue	aifValue;

	private static final char FDS_ARRAY = '[';
	private static final char FDS_BOOLEAN = 'b';
	private static final char FDS_CHARACTER = 'c';
	private static final char FDS_ENUMERATION = '<';
	private static final char FDS_FLOATING = 'f';
	private static final char FDS_FUNCTION = '&';
	private static final char FDS_INTEGER = 'i';
	private static final char FDS_POINTER = '^';
	private static final char FDS_STRING = 's';
	private static final char FDS_STRUCT = '{';
	private static final char FDS_UNION = '(';
	private static final char FDS_VOID = 'v';

	private static final int FDS_FLOATING_LEN_POS = 1;
	private static final int FDS_INTEGER_SIGN_POS = 1;
	private static final int FDS_INTEGER_LEN_POS = 2;
	
	public AIF(String fds, String data) {
		convertToAIF(this, fds, data);
	}
	
	public static void convertToAIF(AIF aif, String format, String data) {
		switch (format.charAt(0)) {
		case FDS_FLOATING:
			int floatLen = Character.digit(format.charAt(FDS_FLOATING_LEN_POS), 10);
			BigDecimal floatVal = new BigDecimal(data);
			
			aif.setType(new AIFTypeFloating(floatLen));
			aif.setValue(new AIFValueFloating(floatVal));
			break;
			
		case FDS_INTEGER:
			int intLen = Character.digit(format.charAt(FDS_INTEGER_LEN_POS), 10);
			boolean signed;

			BigInteger intVal = new BigInteger(data);
			
			if (format.charAt(FDS_INTEGER_SIGN_POS) == 's')
				signed = true;
			else {
				signed = false;
				intVal = intVal.abs();
			}

			aif.setType(new AIFTypeInteger(signed, intLen));
			aif.setValue(new AIFValueInteger(intVal));
			break;
		}
	}

	public IAIFType getType() {
		return aifType;
	}

	public IAIFValue getValue() {
		return aifValue;
	}
	
	protected void setType(IAIFType t) {
		aifType = t;
	}

	protected void setValue(IAIFValue v) {
		aifValue = v;
	}
}
