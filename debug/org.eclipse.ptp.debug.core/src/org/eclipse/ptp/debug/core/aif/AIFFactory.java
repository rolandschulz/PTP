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

import java.util.Random;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeArray;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeBool;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeChar;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeClass;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeEnum;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeFloat;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeFunction;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeIncomplete;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeInt;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeNamed;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypePointer;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeReference;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeString;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeStruct;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeUnion;
import org.eclipse.ptp.debug.internal.core.aif.AIFTypeVoid;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueArray;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueBool;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueChar;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueClass;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueEnum;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueFloat;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueInt;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueNamed;
import org.eclipse.ptp.debug.internal.core.aif.AIFValuePointer;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueReference;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueString;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueStruct;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueUnion;
import org.eclipse.ptp.debug.internal.core.aif.AIFValueUnknown;

/**
 * @author Clement chu
 * 
 */
public class AIFFactory {
	public static final char FDS_ARRAY = '[';
	public static final char FDS_BOOL = 'b';
	public static final char FDS_CHAR = 'c';
	public static final char FDS_ENUM = '<';
	public static final char FDS_FLOAT = 'f';
	public static final char FDS_FUNCTION = '&';
	public static final char FDS_INT = 'i';
	public static final char FDS_POINTER = '^';
	public static final char FDS_STRING = 's';
	public static final char FDS_STRUCT_CLASS = '{';
	public static final char FDS_UNION = '(';
	public static final char FDS_VOID = 'v';
	public static final char FDS_REFERENCE = '>';
	public static final char FDS_NAMED = '%';

	public static final int FDS_FLOAT_SIZE_POS = 1;
	public static final int FDS_VOID_SIZE_POS = 1;
	public static final int FDS_INTEGER_SIGN_POS = 1;
	public static final int FDS_INTEGER_SIZE_POS = 2;
	public static final int FDS_RANGE_DOT_LEN = 2;
	
	public static final String SIGN_OPEN = "[";
	public static final String SIGN_CLOSE = "]";
	public static final String SIGN_STROKE = "|";
	public static final String SIGN_COMMA = ",";
	public static final String SIGN_EQUAL = "=";
	public static final String SIGN_SEMI_COLON = ";";
	public static final String SIGN_COLON = ":";
	public static final String SIGN_DOT = ".";
	
	public static final String FDS_STRUCT_END = ";;;}";
	public static final String FDS_CLASS_END = "}";
	public static final String FDS_UNION_END = ")";
	public static final String FDS_ENUM_END =  ">";
	public static final String FDS_FUNCTION_END =  "/";
	public static final String FDS_REFERENCE_END = "/";
	public static final String FDS_NAMED_END = "/";
	
	public static final int NO_SIZE = 0;
	public static final int SIZE_BOOL = 1;
	public static final int SIZE_CHAR = 1;
	public static final int SIZE_FLOAT = 4;
	public static final int SIZE_DOUBLE = 8;
	public static final int SIZE_INVALID = 0;
	
	public static final IAIFType UNKNOWNTYPE = new AIFTypeIncomplete();
	public static final IAIFValue UNKNOWNVALUE = new AIFValueUnknown(UNKNOWNTYPE);
		
	public static IAIFValue getAIFValue(IValueParent parent, IAIFType type, byte[] data) {
		if (data == null || data.length < 0) {
			return new AIFValueUnknown(type);
		}
		if (type instanceof IAIFTypeChar) {
			 return new AIFValueChar((IAIFTypeChar)type, data);
		} else if (type instanceof IAIFTypeFloat) {
			return new AIFValueFloat((IAIFTypeFloat)type, data);
		} else if (type instanceof IAIFTypeInt) {
			return new AIFValueInt((IAIFTypeInt)type, data);
		} else if (type instanceof IAIFTypeString) {
			return new AIFValueString((IAIFTypeString)type, data);
		} else if (type instanceof IAIFTypeBool) {
			return new AIFValueBool((IAIFTypeBool)type, data);
		} else if (type instanceof IAIFTypeArray) {
			return new AIFValueArray((IAIFTypeArray)type, data);
		} else if (type instanceof IAIFTypeEnum) {
			return new AIFValueEnum((IAIFTypeEnum)type, data);		
		} else if (type instanceof IAIFTypePointer) {
			return new AIFValuePointer(parent, (IAIFTypePointer)type, data);
		} else if (type instanceof IAIFTypeNamed) {
			return new AIFValueNamed(parent, (IAIFTypeNamed)type, data);
		} else if (type instanceof IAIFTypeReference) {
			return new AIFValueReference(parent, (IAIFTypeReference)type, data);
		} else if (type instanceof IAIFTypeStruct) {
			return new AIFValueStruct(parent, (IAIFTypeStruct)type, data);			
		} else if (type instanceof IAIFTypeUnion) {
			return new AIFValueUnion(parent, (IAIFTypeUnion)type, data);
		} else if (type instanceof IAIFTypeClass) {
			return new AIFValueClass(parent, (IAIFTypeClass)type, data);
		/*
		} else if (type instanceof IAIFTypeFunction) {
			return new AIFValueFunction((IAIFTypeFunction)type, data);			
		} else if (type instanceof IAIFTypeVoid) {
			return new AIFValueVoid((IAIFTypeVoid)type, data);
		*/				
		} else {//AIFTypeUnknown
			return new AIFValueUnknown(type);	
		}
	}
	
	/**
	 * testing purpose 
	 */
	public static int random_num(int min, int max) {
	    Random generator = new Random();
	    long range = (long)max - (long)min + 1;
	    long fraction = (long)(range * generator.nextDouble());
	    return (int)(fraction + min);
	}
	public static int getDigitPos(String format, int pos) {
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
	
	public static String extractFormat(String fmt, int start_pos, int end_pos) {
		return fmt.substring(start_pos, end_pos);
	}
	public static int getEndPosFromLast(String fmt, String regex) {
		return fmt.lastIndexOf(regex);
	}
	public static int getEndPosFromStart(String fmt, String regex) {
		return fmt.indexOf(regex);
	}	
	
	public static IAIFType getAIFType(String fmt) {
		if (fmt == null || fmt.length() == 0) {
			System.out.println("        ======= null: " + fmt);
			return UNKNOWNTYPE;			
		}
		switch (fmt.charAt(0)) {
		case FDS_CHAR: //char is signed or unsigned ???
			System.out.println("        ======= character: " + fmt);
			return new AIFTypeChar();
		case FDS_FLOAT:
			int float_size = Character.digit(fmt.charAt(FDS_FLOAT_SIZE_POS), 10);
			System.out.println("        ======= floating: " + fmt + ", size: " + float_size);
			return new AIFTypeFloat(float_size);
		case FDS_INT: //long and int is same???  long long type
			boolean signed = (fmt.charAt(FDS_INTEGER_SIGN_POS) == 's');
			int int_size = Character.digit(fmt.charAt(FDS_INTEGER_SIZE_POS), 10);
			System.out.println("        ======= int: " + fmt + ", size: " + int_size);
			return new AIFTypeInt(signed, int_size);
		case FDS_STRING:
			System.out.println("        ======= string: " + fmt);
			return new AIFTypeString();
		case FDS_BOOL:
			System.out.println("        ======= boolean: " + fmt);
			return new AIFTypeBool();
		case FDS_ENUM:
			System.out.println("        ======= enum: " + fmt);
			int enum_end_pos = getEndPosFromLast(fmt, FDS_ENUM_END);
			String enum_type = fmt.substring(enum_end_pos+FDS_ENUM_END.length());
			return new AIFTypeEnum(extractFormat(fmt, 1, enum_end_pos), getAIFType(enum_type));
		case FDS_FUNCTION:
			System.out.println("        ======= function: " + fmt);
			int func_end_pos = getEndPosFromLast(fmt, FDS_FUNCTION_END);
			String func_type = fmt.substring(func_end_pos+FDS_FUNCTION_END.length());
			return new AIFTypeFunction(extractFormat(fmt, 1, func_end_pos), getAIFType(func_type));
		case FDS_STRUCT_CLASS: //struct or class
			int struct_end_pos = getEndPosFromLast(fmt, FDS_STRUCT_END);
			if (fmt.length() == struct_end_pos + FDS_STRUCT_END.length()) {
				System.out.println("        ======= struct " + fmt);
				return new AIFTypeStruct(extractFormat(fmt, 1, struct_end_pos));
			}
			else {
				struct_end_pos = getEndPosFromLast(fmt, FDS_CLASS_END);
				System.out.println("        ======= class " + fmt);
				return new AIFTypeClass(extractFormat(fmt, 1, struct_end_pos));
			}
		case FDS_UNION:
			System.out.println("        ======= union: " + fmt);
			int union_end_pos = getEndPosFromLast(fmt, FDS_UNION_END);
			return new AIFTypeUnion(extractFormat(fmt, 1, union_end_pos));
		case FDS_REFERENCE:
			System.out.println("        ======= reference: " + fmt);
			int ref_end_pos = getEndPosFromStart(fmt, FDS_REFERENCE_END);
			return new AIFTypeReference(extractFormat(fmt, 1, ref_end_pos));
		case FDS_POINTER:
			System.out.println("        ======= pointer: " + fmt);
			return new AIFTypePointer(getAIFType(fmt.substring(1)));
		case FDS_VOID:
			System.out.println("        ======= void: " + fmt);
			int void_size = Character.digit(fmt.charAt(FDS_VOID_SIZE_POS), 10);
			return new AIFTypeVoid(void_size);
		case FDS_ARRAY:
			System.out.println("        ======= array: " + fmt);
			int array_end_pos = getEndPosFromLast(fmt, SIGN_CLOSE)+1;
			return new AIFTypeArray(extractFormat(fmt, 0, array_end_pos), getAIFType(fmt.substring(array_end_pos)));
		case FDS_NAMED:
			System.out.println("        ======= named: " + fmt);
			int named_end_pos = getEndPosFromStart(fmt, FDS_NAMED_END);
			return new AIFTypeNamed(extractFormat(fmt, 1, named_end_pos), getAIFType(fmt.substring(named_end_pos+1)));
		default:
			System.out.println("        ======= unknown: " + fmt);
			return new AIFTypeIncomplete();
		}
	}
	
	public static IAIF createAIFIndexedArray(IAIFValueArray parentType, int pos) {
		IAIFValue value = new AIFValueArray(parentType, pos);
		return new AIF(parentType.getType(), value);
	}
}

/*
public static int getBaseTypeLength(IAIFType baseType) {
	if (baseType instanceof AIFTypeIntegral) {
		return ((AIFTypeIntegral)baseType).getLength();
	}
	return 1;
}
public static AIFValueArray getAIFArrayValue(ByteBuffer dataBuf, AIFTypeArray arrayType, int dimension_pos, int baseLength, IAIFType baseType) throws AIFException {
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
*/
