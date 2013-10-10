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
package org.eclipse.ptp.internal.debug.core.pdi.model.aif;

import java.util.Random;

import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFormatException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAddress;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeAggregate;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeArray;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeBool;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeChar;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeCharPointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeEnum;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeFloat;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeFunction;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeInt;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeNamed;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeReference;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeString;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeUnion;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeVoid;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue;
import org.eclipse.ptp.debug.core.pdi.model.aif.IValueParent;
import org.eclipse.ptp.internal.debug.core.PDebugOptions;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIF;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFType;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeAddress;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeAggregate;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeArray;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeBool;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeChar;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeCharPointer;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeEnum;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeFloat;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeFunction;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeIncomplete;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeInt;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeNamed;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypePointer;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeRange;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeReference;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeString;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeUnion;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFTypeVoid;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueAddress;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueAggregate;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueArray;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueBool;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueChar;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueCharPointer;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueEnum;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueFloat;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueFunction;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueInt;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueNamed;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValuePointer;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueReference;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueString;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueUnion;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueUnknown;
import org.eclipse.ptp.internal.debug.core.pdi.aif.AIFValueVoid;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

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
	/**
	 * @since 4.0
	 */
	public static final char FDS_AGGREGATE = '{';
	public static final char FDS_UNION = '(';
	public static final char FDS_VOID = 'v';
	public static final char FDS_REFERENCE = '>';
	public static final char FDS_NAMED = '%';
	public static final char FDS_ADDRESS = 'a';
	/**
	 * @since 4.0
	 */
	public static final char FDS_RANGE = 'r';

	public static final char FDS_CHAR_POINTER = 'p';
	public static final int FDS_FLOAT_SIZE_POS = 1;
	public static final int FDS_VOID_SIZE_POS = 1;
	public static final int FDS_INTEGER_SIGN_POS = 1;
	public static final int FDS_INTEGER_SIZE_POS = 2;

	/**
	 * @since 4.0
	 */
	public static final char FDS_ARRAY_END = ']';
	/**
	 * @since 4.0
	 */
	public static final char FDS_TYPENAME_END = '|';
	/**
	 * @since 4.0
	 */
	public static final char FDS_AGGREGATE_FIELD_NAME_END = '=';
	/**
	 * @since 4.0
	 */
	public static final char FDS_AGGREGATE_FIELD_SEP = ',';
	/**
	 * @since 4.0
	 */
	public static final char FDS_AGGREGATE_ACCESS_SEP = ';';
	/**
	 * @since 4.0
	 */
	public static final char FDS_AGGREGATE_END = '}';
	/**
	 * @since 4.0
	 */
	public static final char FDS_UNION_FIELD_NAME_END = '=';
	/**
	 * @since 4.0
	 */
	public static final char FDS_UNION_FIELD_SEP = ',';
	/**
	 * @since 4.0
	 */
	public static final char FDS_UNION_END = ')';
	/**
	 * @since 4.0
	 */
	public static final char FDS_ENUM_CONST_SEP = ',';
	/**
	 * @since 4.0
	 */
	public static final char FDS_ENUM_SEP = '=';
	public static final char FDS_ENUM_END = '>';
	public static final char FDS_FUNCTION_END = '/';
	/**
	 * @since 4.0
	 */
	public static final char FDS_FUNCTION_ARG_SEP = ',';
	/**
	 * @since 4.0
	 */
	public static final char FDS_INTEGER_SIGNED = 's';
	/**
	 * @since 4.0
	 */
	public static final char FDS_INTEGER_UNSIGNED = 'u';
	public static final char FDS_REFERENCE_END = '/';
	public static final char FDS_NAMED_END = '/';
	/**
	 * @since 4.0
	 */
	public static final char FDS_RANGE_SEP = ',';

	public static final int NO_SIZE = 0;
	public static final int SIZE_BOOL = 1;
	public static final int SIZE_CHAR = 1;
	public static final int SIZE_FLOAT = 4;
	public static final int SIZE_DOUBLE = 8;
	public static final int SIZE_INVALID = 0;

	public static final IAIFType UNKNOWNTYPE = new AIFTypeIncomplete();
	public static final IAIFValue UNKNOWNVALUE = new AIFValueUnknown(UNKNOWNTYPE);

	private static IAIFType fLastType;

	/**
	 * Construct an AIF type given a FDS.
	 * 
	 * @param fmt
	 *            format descriptor string
	 * @return IAIFType representing the FDS
	 * @throws AIFFormatException
	 *             if the string can't be parsed
	 */
	public static IAIFType getAIFType(String fmt) throws AIFFormatException {
		parseType(fmt);
		return fLastType;
	}

	/**
	 * Create a value given a type and an array containing the value data.
	 * 
	 * @param parent
	 *            parent value (if this value has a parent)
	 * @param type
	 *            type of the value
	 * @param data
	 *            raw data
	 * @return an IAIFValue representing the data
	 */
	public static IAIFValue getAIFValue(IValueParent parent, IAIFType type, byte[] data) {
		if (data == null || data.length < 0) {
			return new AIFValueUnknown(type);
		}
		return getAIFValue(parent, type, new SimpleByteBuffer(data));
	}

	/**
	 * Create a value given a type and a buffer containing the value data.
	 * 
	 * @param parent
	 *            parent value (if this value has a parent)
	 * @param type
	 *            type of the value
	 * @param buffer
	 *            buffer containing the data
	 * @return an IAIFValue representing the value
	 */
	public static IAIFValue getAIFValue(IValueParent parent, IAIFType type, SimpleByteBuffer buffer) {
		if (buffer.end()) {
			return new AIFValueUnknown(type);
		} else if (type instanceof IAIFTypeChar) {
			return new AIFValueChar((IAIFTypeChar) type, buffer);
		} else if (type instanceof IAIFTypeFloat) {
			return new AIFValueFloat((IAIFTypeFloat) type, buffer);
		} else if (type instanceof IAIFTypeInt) {
			return new AIFValueInt((IAIFTypeInt) type, buffer);
		} else if (type instanceof IAIFTypeCharPointer) {
			return new AIFValueCharPointer((IAIFTypeCharPointer) type, buffer);
		} else if (type instanceof IAIFTypeString) {
			return new AIFValueString((IAIFTypeString) type, buffer);
		} else if (type instanceof IAIFTypeBool) {
			return new AIFValueBool((IAIFTypeBool) type, buffer);
		} else if (type instanceof IAIFTypeArray) {
			return new AIFValueArray((IAIFTypeArray) type, buffer);
		} else if (type instanceof IAIFTypeEnum) {
			return new AIFValueEnum((IAIFTypeEnum) type, buffer);
		} else if (type instanceof IAIFTypeAddress) {
			return new AIFValueAddress((IAIFTypeAddress) type, buffer);
		} else if (type instanceof IAIFTypePointer) {
			return new AIFValuePointer(parent, (IAIFTypePointer) type, buffer);
		} else if (type instanceof IAIFTypeNamed) {
			return new AIFValueNamed(parent, (IAIFTypeNamed) type, buffer);
		} else if (type instanceof IAIFTypeReference) {
			return new AIFValueReference(parent, (IAIFTypeReference) type, buffer);
		} else if (type instanceof IAIFTypeUnion) {
			return new AIFValueUnion(parent, (IAIFTypeUnion) type, buffer);
		} else if (type instanceof IAIFTypeAggregate) {
			return new AIFValueAggregate(parent, (IAIFTypeAggregate) type, buffer);
		} else if (type instanceof IAIFTypeVoid) {
			return new AIFValueVoid(type, buffer);
		} else if (type instanceof IAIFTypeFunction) {
			return new AIFValueFunction((IAIFTypeFunction) type, buffer);
		}
		return new AIFValueUnknown(type);
	}

	/**
	 * Find the first character position immediately after an integer in the
	 * string starting at position 'pos'. If 'neg' is true then negative
	 * integers are allowed.
	 * 
	 * @param format
	 *            format string
	 * @param start
	 *            position in string to start
	 * @param neg
	 *            allow negative numbers
	 * @return first character after integer
	 * @since 4.0
	 */
	public static int getFirstNonDigitPos(String format, int start, boolean neg) {
		int len = format.length();
		if (format.charAt(start) == '-') {
			start++;
		}
		while (start < len) {
			if (!Character.isDigit(format.charAt(start))) {
				break;
			}
			start++;
		}
		return start;
	}

	/**
	 * Get the last type that was successfully parsed.
	 * 
	 * @return IAIFType
	 * @since 4.0
	 */
	public static IAIFType getType() {
		return fLastType;
	}

	/**
	 * Create an IAIF object from a type and a value
	 * 
	 * @param aifType
	 *            type of the object
	 * @param aifValue
	 *            value of the object
	 * @return new IAIF object
	 */
	public static IAIF newAIF(IAIFType aifType, IAIFValue aifValue) {
		return newAIF(aifType, aifValue, ""); //$NON-NLS-1$
	}

	/**
	 * Create an IAIF object from a type and a value
	 * 
	 * @param aifType
	 *            type of the object
	 * @param aifValue
	 *            value of the object
	 * @param desc
	 *            description
	 * @return new IAIF object
	 * @since 4.0
	 */
	public static IAIF newAIF(IAIFType aifType, IAIFValue aifValue, String desc) {
		return new AIF(aifType, aifValue, desc);
	}

	/**
	 * Create an AIF object given an FDS and value data
	 * 
	 * @param fds
	 *            format descriptor string
	 * @param data
	 *            value data
	 * @return new IAIF object
	 * @throws AIFFormatException
	 *             if the FDS can't be parsed
	 */
	public static IAIF newAIF(String fds, byte[] data) throws AIFFormatException {
		return newAIF(fds, data, ""); //$NON-NLS-1$
	}

	/**
	 * Create an AIF object given an FDS and value data
	 * 
	 * @param fds
	 *            format descriptor string
	 * @param data
	 *            value data
	 * @param description
	 * @return new IAIF object
	 * @throws AIFFormatException
	 *             if the FDS can't be parsed
	 */
	public static IAIF newAIF(String fds, byte[] data, String description) throws AIFFormatException {
		IAIFType type = AIFFactory.getAIFType(fds);
		IAIFValue val = AIFFactory.getAIFValue(null, type, data);
		return newAIF(type, val, description);
	}

	/**
	 * Parse a complete AIF type from the format descriptor. A newly constructed
	 * IAIFType objects will be available by calling {@link getType()}
	 * 
	 * @param fmt
	 *            format descriptor
	 * @return remainder of string after removing the complete FDS
	 * @throws AIFFormatException
	 *             if the string can't be parsed
	 * @since 4.0
	 */
	public static String parseType(String fmt) throws AIFFormatException {
		AIFType type;
		String debugStr = ""; //$NON-NLS-1$

		if (fmt == null || fmt.length() == 0) {
			PDebugOptions.trace(Messages.AIFFactory_0 + fmt);
			fLastType = UNKNOWNTYPE;
			return fmt;
		}

		char typeChar = fmt.charAt(0);
		fmt = fmt.substring(1);

		switch (typeChar) {
		case FDS_CHAR: // char is signed or unsigned ???
			debugStr = Messages.AIFFactory_1;
			type = new AIFTypeChar();
			break;
		case FDS_FLOAT:
			debugStr = Messages.AIFFactory_2;
			type = new AIFTypeFloat();
			break;
		case FDS_INT:
			debugStr = Messages.AIFFactory_4;
			type = new AIFTypeInt();
			break;
		case FDS_CHAR_POINTER:
			debugStr = Messages.AIFFactory_6;
			type = new AIFTypeCharPointer();
			break;
		case FDS_STRING:
			debugStr = Messages.AIFFactory_7;
			type = new AIFTypeString();
			break;
		case FDS_BOOL:
			debugStr = Messages.AIFFactory_8;
			type = new AIFTypeBool();
			break;
		case FDS_ENUM:
			debugStr = Messages.AIFFactory_9;
			type = new AIFTypeEnum();
			break;
		case FDS_FUNCTION:
			debugStr = Messages.AIFFactory_10;
			type = new AIFTypeFunction();
			break;
		case FDS_AGGREGATE: // struct or class
			debugStr = Messages.AIFFactory_12;
			type = new AIFTypeAggregate();
			break;
		case FDS_UNION:
			debugStr = Messages.AIFFactory_13;
			type = new AIFTypeUnion();
			break;
		case FDS_REFERENCE:
			debugStr = Messages.AIFFactory_14;
			type = new AIFTypeReference();
			break;
		case FDS_ADDRESS:
			debugStr = Messages.AIFFactory_15;
			type = new AIFTypeAddress();
			break;
		case FDS_POINTER:
			debugStr = Messages.AIFFactory_16;
			type = new AIFTypePointer();
			break;
		case FDS_VOID:
			debugStr = Messages.AIFFactory_17;
			type = new AIFTypeVoid();
			break;
		case FDS_ARRAY:
			debugStr = Messages.AIFFactory_18;
			type = new AIFTypeArray();
			break;
		case FDS_RANGE:
			debugStr = Messages.AIFFactory_21;
			type = new AIFTypeRange();
			break;
		case FDS_NAMED:
			debugStr = Messages.AIFFactory_19;
			type = new AIFTypeNamed();
			break;
		default:
			PDebugOptions.trace(Messages.AIFFactory_20 + fmt);
			fLastType = new AIFTypeIncomplete();
			return ""; //$NON-NLS-1$
		}

		fmt = type.parse(fmt);
		PDebugOptions.trace(debugStr + type.toString());
		fLastType = type;
		return fmt;

	}

	/**
	 * testing purpose
	 */
	public static int random_num(int min, int max) {
		Random generator = new Random();
		long range = (long) max - (long) min + 1;
		long fraction = (long) (range * generator.nextDouble());
		return (int) (fraction + min);
	}

	/**
	 * @return
	 */
	public static IAIF UNKNOWNAIF() {
		return newAIF(UNKNOWNTYPE, UNKNOWNVALUE);
	}
}
