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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeArray;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeBoolean;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeCharacter;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeEnumeration;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeFloating;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeFunction;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeInteger;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypePointer;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeString;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeStruct;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeUnion;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeUnknown;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeVoid;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueArray;
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
		IAIFValue value = null;
		try {
			type = getAIFType(format);
			value = getAIFValue(type, data);
		} catch (AIFException e) {
			System.out.println("************************* AIF Error: " + e.getMessage() + " ************************");
			type = new AIFTypeUnknown(format);
			value = new AIFValueUnknown();
		}
		aif.setType(type);
		aif.setValue(value);
	}
	
	private static IAIFValue getAIFValue(IAIFType type, byte[] data) throws AIFException {
		if (type instanceof AIFTypeCharacter) {
			return new AIFValueCharacter((char)data[0]);
		} else if (type instanceof AIFTypeFloating) {
			AIFTypeFloating floatType = (AIFTypeFloating)type;
			ByteBuffer floatBuf = ByteBuffer.wrap(data);
			floatBuf.order(ByteOrder.nativeOrder()); // TODO: MUST BE FIXED AT SERVER END!!!!
			double floatVal;
			if (floatType.getLength() > 4) {
				floatVal = floatBuf.getDouble();
			} else {
				floatVal = (double)floatBuf.getFloat();
			}
			return new AIFValueFloating(floatVal);
		} else if (type instanceof AIFTypeInteger) {
			AIFTypeInteger intType = (AIFTypeInteger)type;
			ByteBuffer intBuf = ByteBuffer.wrap(data);
			intBuf.order(ByteOrder.nativeOrder()); // TODO: MUST BE FIXED AT SERVER END!!!!
			long val;
			if (intType.getLength() > 4) {
				val = intBuf.getLong();
			}
			else {
				val = (long)intBuf.getInt();
			}
			if (!intType.isSigned()) 
				val = Math.abs(val);

			//intVal = random_num(10, 100);
			return new AIFValueInteger(val);			
		} else if (type instanceof AIFTypeString) {
			int len = 0;
			len = data[0];
			len <<= 8;
			len += data[1];
			
			byte[] strBytes = new byte[len];
			for (int i = 0; i < len; i++) {
				strBytes[i] = data[i+2];
			}
			return new AIFValueString(new String(strBytes));
		} else if (type instanceof AIFTypeArray) {
			AIFTypeArray arrayType = (AIFTypeArray)type;
			IAIFType baseType = arrayType.getBaseType();
			//TODO hard code
			int len = 0;
			if (baseType instanceof AIFTypeInteger) {
				len = ((AIFTypeInteger)baseType).getLength();
			}
			else if (baseType instanceof AIFTypeFloating) {
				len = ((AIFTypeFloating)baseType).getLength();
			}
			else {
				len = 1;
			}
			return getAIFArrayValue(ByteBuffer.wrap(data), arrayType, 1, len, baseType);
		} else if (type instanceof AIFTypeBoolean) {
			return new AIFValueBoolean(new BigInteger(data).intValue()==0?false:true);
		} else if (type instanceof AIFTypeEnumeration) {
			return new AIFValueUnknown();			
		} else if (type instanceof AIFTypeFunction) {
			return new AIFValueUnknown();			
		} else if (type instanceof AIFTypeStruct) {
			return new AIFValueUnknown();			
		} else if (type instanceof AIFTypePointer) {
			return new AIFValueUnknown();			
		} else if (type instanceof AIFTypeUnion) {
			return new AIFValueUnknown();			
		} else if (type instanceof AIFTypeVoid) {
			return new AIFValueUnknown();			
		} else {//AIFTypeUnknown
			return new AIFValueUnknown();	
		}
	}
	private static AIFValueArray getAIFArrayValue(ByteBuffer dataBuf, AIFTypeArray arrayType, int dimension_pos, int baseLength, IAIFType baseType) throws AIFException {
		int lower = arrayType.getLowIndex(dimension_pos);
		int upper = arrayType.getHighIndex(dimension_pos);
		int inner_length = upper-lower+1;
		IAIFValue[] innerValues = new IAIFValue[inner_length];
		byte[] innerData = new byte[baseLength];
		
		for (int j=0; j<inner_length; j++) {
			if (dimension_pos < arrayType.getDimension()) {
				innerValues[j] = getAIFArrayValue(dataBuf, arrayType, dimension_pos+1, baseLength, baseType);
			}
			else {
				for (int h=0; h<baseLength; h++) {
					if (!dataBuf.hasRemaining()) {
						throw new AIFException("AIF Array out bound exception");
					}
					innerData[h] = dataBuf.get();
				}
				innerValues[j] = getAIFValue(baseType, innerData);
			}
		}
		return new AIFValueArray(innerValues);
	}
	private static IAIFType getAIFType(String format) throws AIFException {
		switch (format.charAt(0)) {
		case FDS_CHARACTER:
			System.out.println("        ======= character: " + format);
			return new AIFTypeCharacter();
		case FDS_FLOATING:
			System.out.println("        ======= floating: " + format);
			int floatLen = Character.digit(format.charAt(FDS_FLOATING_LEN_POS), 10);
			return new AIFTypeFloating(floatLen);
		case FDS_INTEGER:
			System.out.println("        ======= integer: " + format);
			int intLen = Character.digit(format.charAt(FDS_INTEGER_LEN_POS), 10);
			boolean signed = (format.charAt(FDS_INTEGER_SIGN_POS) == 's');
			return new AIFTypeInteger(signed, intLen);
		case FDS_STRING:
			System.out.println("        ======= string: " + format);
			return new AIFTypeString();
		case FDS_ARRAY:
			System.out.println("        ======= array: " + format);
			String[] dims = format.split("]");
			int dimension = dims.length - 1;
			IAIFType baseType = getAIFType(dims[dimension]);
			int[] range = new int[dimension*2];
			for (int i=0; i<dimension; i++) {
				//format example: [r0..9is4]
				int lower_start_pos = dims[i].indexOf(FDS_START_RANGE) + 2;
				int lower_end_pos = getDigitPos(dims[i], lower_start_pos);
				int lower = Integer.parseInt(dims[i].substring(lower_start_pos, lower_end_pos), 10);
				int upper_start_pos = lower_end_pos + FDS_RANGE_DOT_LEN;
				int upper_end_pos = getDigitPos(dims[i], upper_start_pos);
				int upper = Integer.parseInt(dims[i].substring(upper_start_pos, upper_end_pos), 10);
				range[i*2] = lower;
				range[i*2 + 1] = upper;
			}
			return new AIFTypeArray(baseType, range);
		case FDS_BOOLEAN:
			System.out.println("        ======= boolean: " + format);
			return new AIFTypeBoolean();
		case FDS_ENUMERATION:
			System.out.println("        ======= enum: " + format);
			return new AIFTypeEnumeration(null, null);//TODO String[], String[]
		case FDS_FUNCTION:
			System.out.println("        ======= function: " + format);
			return new AIFTypeFunction(null, null);//TODO IAIFType[], IAIFType
		case FDS_STRUCT:
			System.out.println("        ======= structure: " + format);
			return new AIFTypeStruct(null);//TODO AIFTypeField
		case FDS_POINTER:
			System.out.println("        ======= pointer: " + format);
			return new AIFTypePointer(null);//TODO IAIFType
		case FDS_UNION:
			System.out.println("        ======= union: " + format);
			return new AIFTypeUnion(null);//TODO AIFTypeField[]
		case FDS_VOID:
			System.out.println("        ======= void: " + format);
			return new AIFTypeVoid(0);//TODO int
		default:
			System.out.println("        ======= unknown: " + format);
			return new AIFTypeUnknown(format);//TODO String
		}
	}
	
	private static int random_num(int min, int max) {
	    Random generator = new Random();
	    long range = (long)max - (long)min + 1;
	    long fraction = (long)(range * generator.nextDouble());
	    return (int)(fraction + min);
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
		try {
			return "<\"" + aifType.toString() + "\", " + aifValue.toString() + ">";
		} catch (Exception e) {
			return "err: " + e.getMessage();
		}
	}
}
