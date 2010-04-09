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
package org.eclipse.ptp.debug.core.pdi.model.aif;

import java.util.Random;

import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.pdi.messages.Messages;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIF;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeAddress;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeArray;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeBool;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeChar;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeCharPointer;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeClass;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeEnum;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeFloat;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeFunction;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeIncomplete;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeInt;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeNamed;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypePointer;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeReference;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeString;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeStruct;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeUnion;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFTypeVoid;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueAddress;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueArray;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueBool;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueChar;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueCharPointer;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueClass;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueEnum;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueFloat;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueInt;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueNamed;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValuePointer;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueReference;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueString;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueStruct;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueUnion;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueUnknown;
import org.eclipse.ptp.debug.internal.core.pdi.aif.AIFValueVoid;

/**
 * @author Clement chu
 * 
 */
public class AIFFactory {
	public static class SimpleByteBuffer {
		byte[] bytes;
		int pos = 0;
		SimpleByteBuffer(byte[] bytes) {
			if (bytes == null) {
				bytes = new byte[0];
			}
			this.bytes = bytes;
		}
		public boolean end() {
			return (pos == bytes.length);
		}
		public byte get() {
			return bytes[pos++];
		}
		public byte get(int pos) {
			return bytes[pos];
		}
		public byte[] getByte() {
			return bytes;
		}
		public int getCapacity() {
			return bytes.length;
		}
		public int getPosition() {
			return pos;
		}
		public void setPos(int pos) {
			this.pos = pos;
		}
	}
	
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
	public static final char FDS_ADDRESS = 'a';

	public static final char FDS_CHAR_POINTER = 'p';
	public static final int FDS_FLOAT_SIZE_POS = 1;
	public static final int FDS_VOID_SIZE_POS = 1;
	public static final int FDS_INTEGER_SIGN_POS = 1;
	public static final int FDS_INTEGER_SIZE_POS = 2;
	
	public static final int FDS_RANGE_DOT_LEN = 2;
	public static final String SIGN_OPEN = "["; //$NON-NLS-1$
	public static final String SIGN_CLOSE = "]"; //$NON-NLS-1$
	public static final String SIGN_STROKE = "|"; //$NON-NLS-1$
	public static final String SIGN_COMMA = ","; //$NON-NLS-1$
	public static final String SIGN_EQUAL = "="; //$NON-NLS-1$
	public static final String SIGN_SEMI_COLON = ";"; //$NON-NLS-1$
	public static final String SIGN_COLON = ":"; //$NON-NLS-1$
	
	public static final String SIGN_DOT = "."; //$NON-NLS-1$
	public static final String FDS_STRUCT_END = ";;;}"; //$NON-NLS-1$
	public static final String FDS_CLASS_END = "}"; //$NON-NLS-1$
	public static final String FDS_UNION_END = ")"; //$NON-NLS-1$
	public static final String FDS_ENUM_END =  ">"; //$NON-NLS-1$
	public static final String FDS_FUNCTION_END =  "/"; //$NON-NLS-1$
	public static final String FDS_REFERENCE_END = "/"; //$NON-NLS-1$
	
	public static final String FDS_NAMED_END = "/"; //$NON-NLS-1$
	public static final int NO_SIZE = 0;
	public static final int SIZE_BOOL = 1;
	public static final int SIZE_CHAR = 1;
	public static final int SIZE_FLOAT = 4;
	public static final int SIZE_DOUBLE = 8;
	
	public static final int SIZE_INVALID = 0;
	public static final IAIFType UNKNOWNTYPE = new AIFTypeIncomplete();
	
	public static final IAIFValue UNKNOWNVALUE = new AIFValueUnknown(UNKNOWNTYPE);
	
	/**
	 * @param fmt
	 * @param start_pos
	 * @param end_pos
	 * @return
	 */
	public static String extractFormat(String fmt, int start_pos, int end_pos) {
		return fmt.substring(start_pos, end_pos);
	}
	
	/**
	 * Create an AIF object
	 * 
	 * @param fds
	 * @param data
	 * @param description
	 * @return
	 */
	public static IAIF newAIF(String fds, byte[] data, String description) {
		return new AIF(fds, data, description);
	}
	
	/**
	 * Create an AIF object
	 * 
	 * @param aifType
	 * @param aifValue
	 * @return
	 */
	public static IAIF newAIF(IAIFType aifType, IAIFValue aifValue) {
		return new AIF(aifType, aifValue);
	}
	
	/**
	 * Create an AIF object
	 * 
	 * @param fds
	 * @param data
	 * @return
	 */
	public static IAIF newAIF(String fds, byte[] data) {
		return new AIF(fds, data);
	}

	/**
	 * @param fmt
	 * @return
	 */
	public static IAIFType getAIFType(String fmt) {
		if (fmt == null || fmt.length() == 0) {
			PDebugUtils.println(Messages.AIFFactory_0 + fmt);
			return UNKNOWNTYPE;			
		}
		switch (fmt.charAt(0)) {
		case FDS_CHAR: //char is signed or unsigned ???
			PDebugUtils.println(Messages.AIFFactory_1 + fmt);
			return new AIFTypeChar();
		case FDS_FLOAT:
			int float_size = Character.digit(fmt.charAt(FDS_FLOAT_SIZE_POS), 10);
			PDebugUtils.println(Messages.AIFFactory_2 + fmt + Messages.AIFFactory_3 + float_size);
			return new AIFTypeFloat(float_size);
		case FDS_INT: //long and int is same???  long long type
			boolean signed = (fmt.charAt(FDS_INTEGER_SIGN_POS) == 's');
			int int_size = Character.digit(fmt.charAt(FDS_INTEGER_SIZE_POS), 10);
			PDebugUtils.println(Messages.AIFFactory_4 + fmt + Messages.AIFFactory_5 + int_size);
			return new AIFTypeInt(signed, int_size);
		case FDS_CHAR_POINTER:
			PDebugUtils.println(Messages.AIFFactory_6 + fmt);
			return new AIFTypeCharPointer(getAIFType(fmt.substring(1, 3)));
		case FDS_STRING:
			PDebugUtils.println(Messages.AIFFactory_7 + fmt);
			return new AIFTypeString();
		case FDS_BOOL:
			PDebugUtils.println(Messages.AIFFactory_8 + fmt);
			return new AIFTypeBool();
		case FDS_ENUM:
			PDebugUtils.println(Messages.AIFFactory_9 + fmt);
			int enum_end_pos = getEndPosFromLast(fmt, FDS_ENUM_END);
			String enum_type = fmt.substring(enum_end_pos+FDS_ENUM_END.length());
			return new AIFTypeEnum(extractFormat(fmt, 1, enum_end_pos), getAIFType(enum_type));
		case FDS_FUNCTION:
			PDebugUtils.println(Messages.AIFFactory_10 + fmt);
			int func_end_pos = getEndPosFromLast(fmt, FDS_FUNCTION_END);
			String func_type = fmt.substring(func_end_pos+FDS_FUNCTION_END.length());
			return new AIFTypeFunction(extractFormat(fmt, 1, func_end_pos), getAIFType(func_type));
		case FDS_STRUCT_CLASS: //struct or class
			int struct_end_pos = getEndPosFromLast(fmt, FDS_STRUCT_END);
			if (fmt.length() == struct_end_pos + FDS_STRUCT_END.length()) {
				PDebugUtils.println(Messages.AIFFactory_11 + fmt);
				return new AIFTypeStruct(extractFormat(fmt, 1, struct_end_pos));
			}
			else {
				struct_end_pos = getEndPosFromLast(fmt, FDS_CLASS_END);
				PDebugUtils.println(Messages.AIFFactory_12 + fmt);
				return new AIFTypeClass(extractFormat(fmt, 1, struct_end_pos));
			}
		case FDS_UNION:
			PDebugUtils.println(Messages.AIFFactory_13 + fmt);
			int union_end_pos = getEndPosFromLast(fmt, FDS_UNION_END);
			return new AIFTypeUnion(extractFormat(fmt, 1, union_end_pos));
		case FDS_REFERENCE:
			PDebugUtils.println(Messages.AIFFactory_14 + fmt);
			int ref_end_pos = getEndPosFromStart(fmt, FDS_REFERENCE_END);
			return new AIFTypeReference(extractFormat(fmt, 1, ref_end_pos));
		case FDS_ADDRESS:
			PDebugUtils.println(Messages.AIFFactory_15 + fmt);
			return new AIFTypeAddress(Character.digit(fmt.charAt(1), 10));
		case FDS_POINTER:
			PDebugUtils.println(Messages.AIFFactory_16 + fmt);
			return new AIFTypePointer(getAIFType(fmt.substring(1, 3)), getAIFType(fmt.substring(3)));
		case FDS_VOID:
			PDebugUtils.println(Messages.AIFFactory_17 + fmt);
			int void_size = Character.digit(fmt.charAt(FDS_VOID_SIZE_POS), 10);
			return new AIFTypeVoid(void_size);
		case FDS_ARRAY:
			PDebugUtils.println(Messages.AIFFactory_18 + fmt);
			int array_end_pos = getEndPosFromStart(fmt, SIGN_CLOSE);
			return new AIFTypeArray(extractFormat(fmt, 1, array_end_pos), getAIFType(fmt.substring(array_end_pos+1)));
		case FDS_NAMED:
			PDebugUtils.println(Messages.AIFFactory_19 + fmt);
			int named_end_pos = getEndPosFromStart(fmt, FDS_NAMED_END);
			return new AIFTypeNamed(extractFormat(fmt, 1, named_end_pos), getAIFType(fmt.substring(named_end_pos+1)));
		default:
			PDebugUtils.println(Messages.AIFFactory_20 + fmt);
			return new AIFTypeIncomplete();
		}
	}
	
	/**
	 * @param parent
	 * @param type
	 * @param data
	 * @return
	 */
	public static IAIFValue getAIFValue(IValueParent parent, IAIFType type, byte[] data) {
		if (data == null || data.length < 0) {
			return new AIFValueUnknown(type);
		}
		return getAIFValue(parent, type, new SimpleByteBuffer(data));
	}
	
	/**
	 * @param parent
	 * @param type
	 * @param buffer
	 * @return
	 */
	public static IAIFValue getAIFValue(IValueParent parent, IAIFType type, SimpleByteBuffer buffer) {
		if (buffer.end()) {
			return new AIFValueUnknown(type);
		} else if (type instanceof IAIFTypeChar) {
			 return new AIFValueChar((IAIFTypeChar)type, buffer);
		} else if (type instanceof IAIFTypeFloat) {
			return new AIFValueFloat((IAIFTypeFloat)type, buffer);
		} else if (type instanceof IAIFTypeInt) {
			return new AIFValueInt((IAIFTypeInt)type, buffer);
		} else if (type instanceof IAIFTypeCharPointer) {
			return new AIFValueCharPointer((IAIFTypeCharPointer)type, buffer);
		} else if (type instanceof IAIFTypeString) {
			return new AIFValueString((IAIFTypeString)type, buffer);
		} else if (type instanceof IAIFTypeBool) {
			return new AIFValueBool((IAIFTypeBool)type, buffer);
		} else if (type instanceof IAIFTypeArray) {
			return new AIFValueArray((IAIFTypeArray)type, buffer);
		} else if (type instanceof IAIFTypeEnum) {
			return new AIFValueEnum((IAIFTypeEnum)type, buffer);		
		} else if (type instanceof IAIFTypeAddress) {
			return new AIFValueAddress((IAIFTypeAddress)type, buffer);
		} else if (type instanceof IAIFTypePointer) {
			return new AIFValuePointer(parent, (IAIFTypePointer)type, buffer);
		} else if (type instanceof IAIFTypeNamed) {
			return new AIFValueNamed(parent, (IAIFTypeNamed)type, buffer);
		} else if (type instanceof IAIFTypeReference) {
			return new AIFValueReference(parent, (IAIFTypeReference)type, buffer);
		} else if (type instanceof IAIFTypeStruct) {
			return new AIFValueStruct(parent, (IAIFTypeStruct)type, buffer);			
		} else if (type instanceof IAIFTypeUnion) {
			return new AIFValueUnion(parent, (IAIFTypeUnion)type, buffer);
		} else if (type instanceof IAIFTypeClass) {
			return new AIFValueClass(parent, (IAIFTypeClass)type, buffer);
		} else if (type instanceof IAIFTypeVoid) {
			return new AIFValueVoid((IAIFTypeVoid)type, buffer);
		/*
		} else if (type instanceof IAIFTypeFunction) {
			return new AIFValueFunction((IAIFTypeFunction)type, data);			
		*/				
		}		
		return new AIFValueUnknown(type);
	}
	
	/**
	 * @param format
	 * @param pos
	 * @return
	 */
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
	
	/**
	 * @param fmt
	 * @param regex
	 * @return
	 */
	public static int getEndPosFromLast(String fmt, String regex) {
		return fmt.lastIndexOf(regex);
	}
	
	/**
	 * @param fmt
	 * @param regex
	 * @return
	 */
	public static int getEndPosFromStart(String fmt, String regex) {
		return fmt.indexOf(regex);
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

	/**
	 * @return
	 */
	public static IAIF UNKNOWNAIF() {
		return new AIF(UNKNOWNTYPE, UNKNOWNVALUE);
	}
}


