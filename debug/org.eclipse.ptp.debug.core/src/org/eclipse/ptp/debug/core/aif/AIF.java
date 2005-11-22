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

package org.eclipse.ptp.debug.core.aif;

import java.math.BigInteger;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeBoolean;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeCharacter;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeFloating;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeInteger;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeString;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeUnknown;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueBoolean;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueCharacter;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueFloating;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueInteger;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueString;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueUnknown;

public class AIF implements IAIF {
	private IAIFType	aifType;
	private IAIFValue	aifValue;
	private String 		typeDesc  = "";

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
	private static final String FDS_START_RANGE = "[";
	private static final String FDS_END_RANGE = "]";
	private static final int FDS_RANGE_DOT_LEN = 2;
	
	public AIF(String fds, byte[] data) {
		convertToAIF(this, fds, data);
	}
	
	public AIF(String fds, byte[] data, String desc) {
		this(fds, data);
		typeDesc = desc;
	}
	
	public static void convertToAIF(AIF aif, String format, byte[] data) {
		IAIFType type = null;
		IAIFValue val = null;
		
		switch (format.charAt(0)) {
		case FDS_CHARACTER:
			type = new AIFTypeCharacter();
			val = new AIFValueCharacter((char)data[0]);
			break;

		case FDS_FLOATING:
			int floatLen = Character.digit(format.charAt(FDS_FLOATING_LEN_POS), 10);
			double floatVal;
			if (floatLen > 4) {
				BigInteger longBits = new BigInteger(data);
				floatVal = Double.longBitsToDouble(longBits.longValue());
			} else {
				BigInteger intBits = new BigInteger(data);
				floatVal = (double)Float.intBitsToFloat(intBits.intValue());
			}
			type = new AIFTypeFloating(floatLen);
			val = new AIFValueFloating(floatVal);
			break;
			
		case FDS_INTEGER:
			int intLen = Character.digit(format.charAt(FDS_INTEGER_LEN_POS), 10);
			boolean signed = (format.charAt(FDS_INTEGER_SIGN_POS) == 's');
			BigInteger intVal = new BigInteger(data);
			if (!signed) 
				intVal = intVal.abs();
			type = new AIFTypeInteger(signed, intLen);
			val = new AIFValueInteger(intVal);
			break;
		
		case FDS_STRING:
			int len = 0;
			len = data[0];
			len <<= 8;
			len += data[1];
			
			byte[] strBytes = new byte[len];
			for (int i = 0; i < len; i++) {
				strBytes[i] = data[i+2];
			}
			type = new AIFTypeString();
			val = new AIFValueString(new String(strBytes));
			break;

		case FDS_ARRAY: //TODO check it pls
			System.out.println("        ======= array: " + format);
			getRange(format);
			type = new AIFTypeUnknown(format);
			val = new AIFValueUnknown();
			
			
			break;

		case FDS_BOOLEAN: //TODO check it pls
			System.out.println("        ======= boolean: " + format);
			BigInteger intBits = new BigInteger(data);
			type = new AIFTypeBoolean();
			val = new AIFValueBoolean(intBits.intValue()==0?false:true);
			break;
			
		case FDS_ENUMERATION:
			System.out.println("        ======= enum: " + format);

			break;
			
		case FDS_FUNCTION:
			break;

		case FDS_STRUCT:
			break;

		case FDS_POINTER:
			break;
			
		case FDS_UNION:
			break;
			
		case FDS_VOID:
			break;
		
		default:
			type = new AIFTypeUnknown(format);
			val = new AIFValueUnknown();
			break;
		}
		
		aif.setType(type);
		aif.setValue(val);
	}
	private static Range getRange(String format) {
		//format example: [r0..9is4]is4
		int lower_start_pos = format.indexOf(FDS_START_RANGE) + 2;
		int lower_end_pos = getDigitPos(format, lower_start_pos);
		int lower = Integer.parseInt(format.substring(lower_start_pos, lower_end_pos), 10);
		int upper_start_pos = lower_end_pos + FDS_RANGE_DOT_LEN;
		int upper_end_pos = getDigitPos(format, upper_start_pos);
		int upper = Integer.parseInt(format.substring(upper_start_pos, upper_end_pos), 10);
		
		Range range = new Range(lower, upper);
		
		int last_pos = format.indexOf(FDS_END_RANGE);
		
		return range;
	}
	private static int getDigitPos(String format, int pos) {
		int len = format.length();
		while (pos < len) {
			char aChar = format.charAt(pos);
			if (!Character.isDigit(aChar)) {
				break;
			}
			pos++;
		}
		return pos;
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
	
	public String getDescription() {
		return this.typeDesc;
	}
	
	public String toString() {
		return "<\"" + aifType.toString() + "\", " + aifValue.toString() + ">";
	}
	
	private static class Range {
		private int from = 0;
		private int to = 0;
		private String type = "";
		
		public Range(int from, int to) {
			this.from = from;
			this.to = to;
		}
		public void setType(String type) {
			this.type = type;
		}
		public int getFrom() {
			return from;
		}
		public int getTo() {
			return to;
		}
		public String getType() {
			return type;
		}
	}
}
