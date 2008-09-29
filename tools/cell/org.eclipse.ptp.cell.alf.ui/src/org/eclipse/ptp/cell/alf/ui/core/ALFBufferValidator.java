/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.alf.ui.core;

import org.eclipse.ptp.cell.alf.ui.Messages;
import org.eclipse.ptp.cell.alf.ui.wizard.ALFWizardPageA;
import org.eclipse.ptp.cell.alf.ui.wizard.ALFWizardPageB;

/**
 * ALFBufferValidator provides a method to facilitate validation of ALFBuffer objects.
 * 
 * @author Sean Curry
 * @since 3.0.0
 */
public class ALFBufferValidator {
	
	private static Type[] TYPES = {		
		new Type("char", new int[]{ALFConstants.ALF_DATA_BYTE, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfChar)}), //$NON-NLS-1$
		new Type("signed char", new int[]{ALFConstants.ALF_DATA_BYTE, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfChar)}), //$NON-NLS-1$
		new Type("unsigned char", new int[]{ALFConstants.ALF_DATA_BYTE, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfChar)}), //$NON-NLS-1$
		new Type("wchar_t", new int[]{ALFConstants.ALF_DATA_INT16, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfShort)}), //$NON-NLS-1$
		
		new Type("short", new int[]{ALFConstants.ALF_DATA_INT16, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfShort)}), //$NON-NLS-1$
		new Type("short int", new int[]{ALFConstants.ALF_DATA_INT16, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfShort)}), //$NON-NLS-1$
		new Type("signed short", new int[]{ALFConstants.ALF_DATA_INT16, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfShort)}), //$NON-NLS-1$
		new Type("signed short int", new int[]{ALFConstants.ALF_DATA_INT16, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfShort)}), //$NON-NLS-1$
		new Type("unsigned short", new int[]{ALFConstants.ALF_DATA_INT16, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfShort)}), //$NON-NLS-1$
		new Type("unsigned short int", new int[]{ALFConstants.ALF_DATA_INT16, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfShort)}), //$NON-NLS-1$
				
		new Type("int", new int[]{ALFConstants.ALF_DATA_INT16, ALFConstants.ALF_DATA_INT32, ALFConstants.ALF_DATA_ADDR32, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfInt)}), //$NON-NLS-1$
		new Type("signed", new int[]{ALFConstants.ALF_DATA_INT16, ALFConstants.ALF_DATA_INT32, ALFConstants.ALF_DATA_ADDR32, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfInt)}), //$NON-NLS-1$
		new Type("signed int", new int[]{ALFConstants.ALF_DATA_INT16, ALFConstants.ALF_DATA_INT32, ALFConstants.ALF_DATA_ADDR32, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfInt)}), //$NON-NLS-1$
		new Type("unsigned", new int[]{ALFConstants.ALF_DATA_INT16, ALFConstants.ALF_DATA_INT32, ALFConstants.ALF_DATA_ADDR32, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfInt)}), //$NON-NLS-1$
		new Type("unsigned int", new int[]{ALFConstants.ALF_DATA_INT16, ALFConstants.ALF_DATA_INT32, ALFConstants.ALF_DATA_ADDR32, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfInt)}), //$NON-NLS-1$
		
		new Type("long", new int[]{ALFConstants.ALF_DATA_INT32, ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ADDR32, ALFConstants.ALF_DATA_ADDR64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLong32bit), Integer.parseInt(Messages.ALFBufferValidator_sizeOfLong64bit)}), //$NON-NLS-1$
		new Type("long int", new int[]{ALFConstants.ALF_DATA_INT32, ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ADDR32, ALFConstants.ALF_DATA_ADDR64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLong32bit), Integer.parseInt(Messages.ALFBufferValidator_sizeOfLong64bit)}), //$NON-NLS-1$
		new Type("signed long", new int[]{ALFConstants.ALF_DATA_INT32, ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ADDR32, ALFConstants.ALF_DATA_ADDR64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLong32bit), Integer.parseInt(Messages.ALFBufferValidator_sizeOfLong64bit)}), //$NON-NLS-1$
		new Type("signed long int", new int[]{ALFConstants.ALF_DATA_INT32, ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ADDR32, ALFConstants.ALF_DATA_ADDR64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLong32bit), Integer.parseInt(Messages.ALFBufferValidator_sizeOfLong64bit)}), //$NON-NLS-1$
		new Type("unsigned long", new int[]{ALFConstants.ALF_DATA_INT32, ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ADDR32, ALFConstants.ALF_DATA_ADDR64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLong32bit), Integer.parseInt(Messages.ALFBufferValidator_sizeOfLong64bit)}), //$NON-NLS-1$
		new Type("unsigned long int", new int[]{ALFConstants.ALF_DATA_INT32, ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ADDR32, ALFConstants.ALF_DATA_ADDR64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLong32bit), Integer.parseInt(Messages.ALFBufferValidator_sizeOfLong64bit)}), //$NON-NLS-1$
				
		new Type("long long", new int[]{ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ADDR64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLongLong)}), //$NON-NLS-1$
		new Type("long long int", new int[]{ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ADDR64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLongLong)}), //$NON-NLS-1$		
		new Type("signed long long", new int[]{ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ADDR64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLongLong)}), //$NON-NLS-1$
		new Type("signed long long int", new int[]{ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ADDR64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLongLong)}), //$NON-NLS-1$		
		new Type("unsigned long long", new int[]{ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ADDR64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLongLong)}), //$NON-NLS-1$
		new Type("unsigned long long int", new int[]{ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ADDR64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLongLong)}), //$NON-NLS-1$
		
		new Type("float", new int[]{ALFConstants.ALF_DATA_FLOAT, ALFConstants.ALF_DATA_INT32, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfFloat)}), //$NON-NLS-1$
		
		new Type("double", new int[]{ALFConstants.ALF_DATA_DOUBLE, ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfDouble)}), //$NON-NLS-1$
		new Type("long double", new int[]{ALFConstants.ALF_DATA_DOUBLE, ALFConstants.ALF_DATA_INT64, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfLongDouble32bit), Integer.parseInt(Messages.ALFBufferValidator_sizeOfLongDouble64bit)}), //$NON-NLS-1$

		new Type("_Bool", new int[]{ALFConstants.ALF_DATA_BYTE, ALFConstants.ALF_DATA_ELEMENT_TYPE}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfBool)}), //$NON-NLS-1$
		
		new Type("void*", new int[]{}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfVoid32bit), Integer.parseInt(Messages.ALFBufferValidator_sizeOfVoid64bit)}), //$NON-NLS-1$
		new Type("void *", new int[]{}, new int[]{Integer.parseInt(Messages.ALFBufferValidator_sizeOfVoid32bit), Integer.parseInt(Messages.ALFBufferValidator_sizeOfVoid64bit)}) //$NON-NLS-1$
	};
	
	/* definition of static string array which contains the list of spu-gcc keywords */
	private static String[] KEYWORDS = {
		"asm", //$NON-NLS-1$
		"auto", //$NON-NLS-1$
		"break", //$NON-NLS-1$
		"case", //$NON-NLS-1$
		"char", //$NON-NLS-1$
		"const", //$NON-NLS-1$
		"continue", //$NON-NLS-1$
		"default", //$NON-NLS-1$
		"do", //$NON-NLS-1$
		"double", //$NON-NLS-1$
		"else", //$NON-NLS-1$
		"enum", //$NON-NLS-1$
		"extern", //$NON-NLS-1$
		"float", //$NON-NLS-1$
		"for", //$NON-NLS-1$
		"goto", //$NON-NLS-1$
		"if", //$NON-NLS-1$
		"int",  //$NON-NLS-1$
		"inline", //$NON-NLS-1$
		"long", //$NON-NLS-1$
		"register", //$NON-NLS-1$
		"restrict", //$NON-NLS-1$
		"return", //$NON-NLS-1$
		"short",  //$NON-NLS-1$
		"signed", //$NON-NLS-1$
		"sizeof", //$NON-NLS-1$
		"static", //$NON-NLS-1$
		"struct", //$NON-NLS-1$
		"switch", //$NON-NLS-1$
		"typedef", //$NON-NLS-1$
		"typeof", //$NON-NLS-1$
		"union", //$NON-NLS-1$
		"unsigned", //$NON-NLS-1$
		"void", //$NON-NLS-1$
		"volatile", //$NON-NLS-1$
		"while", //$NON-NLS-1$
		"_Bool", //$NON-NLS-1$
		"_Complex", //$NON-NLS-1$
		"_Imaginary" //$NON-NLS-1$
	};
	
	/**
	 * This static member class is used to represent the relationship between a built-in element type, and 
	 * an element unit. This relationship will be used to give warnings to the user when they have entered
	 * a built-in element type and have selected an element unit that is not recommended to be used with the 
	 * specified built-in type. 
	 * For example, if a user enters "char" as the element type and selects ALF_DATA_DOUBLE as the element unit; 
	 * This selection is not encouraged (indicated by the LACK OF the element unit ALF_DATA_DOUBLE in 'int[] units'), 
	 * and a warning message will be given. 
	 * 
	 * @author spcurry
	 *
	 */
	static class Type {
		String value;
		int[] units; //note, if units == null or contains no elements, that indicates that this Type object's element type value is compatible with all element units
		int[] sizeof;
		
		Type(String value, int[] unit, int[] sizeof){
			this.value = value;
			this.units = unit;
			this.sizeof = sizeof;
		}
		
		String getValue(){ return this.value; }
		
		boolean containsUnit(int unit){ 
			if(units == null || units.length == 0) // from note above, if units[] == empty list, this element type is compatible with all element units
				return true;
			
			for(int i = 0; i < units.length; i++){
				if(units[i] == unit)
					return true;
			}
			
			return false;
		}
		
		int getSizeOf(boolean is64Bit){
			if(is64Bit && sizeof.length > 1)
				return sizeof[1];
			else
				return sizeof[0];
		}
	}
	
	/* link to pageB of ALF wizard (the page that contains list of ALFBuffers).
	 * used to find out the number of selected accelerator nodes, and check if a buffer 
	 * with the same name already exists. 
	 */
	private ALFWizardPageB pageB;
	
	public ALFBufferValidator(ALFWizardPageB pageB){
		this.pageB = pageB;
	}
	
	/**
	 * Computes the data_transfer_size of the block using the given parameters.
	 * 
	 * @param elementType the element type; either built-in type or user-defined type
	 * @param numDim the number of dimensions
	 * @param distSizeX the distribution size of the X dimension
	 * @param distSizeY the distribution size of the Y dimension
	 * @param distSizeZ the distribution size of the Z dimension
	 * @param is64bit true if the wizard is currently set to use the 64 bit PPU compiler
	 * @return the data_transfer_size if elementType is a built-in type, else return -1 if elementType is a user-defined type
	 */
	public static long computeDataTransferSize(String elementType, int numDim, long distSizeX, long distSizeY, long distSizeZ, boolean is64bit){
		// find out if the user has entered a built-in type for the element type
		boolean isBuiltInType = false;
		int i;
		for(i = 0; i < TYPES.length; i++){
			if(elementType.equals(TYPES[i].getValue())){
				isBuiltInType = true;
				break;
			}
		}
		
		// If the elementType is not a built-in type, then we do not know the sizeof that element type -> skip 16Byte alignment check
		if(!isBuiltInType)
			return -1;
		
		int sizeof = TYPES[i].getSizeOf(is64bit);
		long dataTransferSize = 0;
		
		switch(numDim){
			case ALFConstants.ONE_DIMENSIONAL:
				dataTransferSize = sizeof * distSizeX;
				break;
				
			case ALFConstants.TWO_DIMENSIONAL:
				dataTransferSize = sizeof * distSizeX * distSizeY;
				break;
				
			case ALFConstants.THREE_DIMENSIONAL:
				dataTransferSize = sizeof * distSizeX * distSizeY * distSizeZ;
				break;
		}
		
		return dataTransferSize;		
	}

	/**
	 * Function that returns the expected number of accelerator nodes; 
	 * as set by the user in the second page of the wizard. 
	 * 
	 * @return the expected number of accelerator nodes
	 */
	public int getExpectedAccelNum(){
		ALFWizardPageA pageA = (ALFWizardPageA) pageB.getPreviousPage();
		
		if(pageA != null)
			return pageA.getExpAccelNum();
		else
			return -1;
	}

	/**
	 * Checks whether or not the string argument is a valid $VARIABLE_NAME; valid variable names consist of any combination of
	 * alphabetic characters ('a'-'z', 'A'-'Z'), numeric characters ('0'-'9'), or the underscore '_', with the additional limitation
	 * that the first character must be an alphabetic character or the underscore (not a digit).
	 * 
	 * @param bufferName the buffer name to be validated
	 * @return null if the buffer name argument is valid, else return an error message to be displayed, which describes the problem
	 */
	public String isNameValid(String bufferName){
		// if the name is null or empty, then the user has not began filling in this field yet. so report it as valid
		if(bufferName == null || bufferName.length() == 0)
			return null;

		// if the name for this ALFBuffer object already exists, then an error message must be displayed
		if(pageB.doesSameBufferNameExist(bufferName))
			return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgSameNameExists;

		// make sure the first character of the buffer name is an alphabetic character
		char c = bufferName.charAt(0);
		if((c < 65 || c > 90) && (c < 97 || c > 122) && (c != 95))
			return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgInvalidName1;
		
		// check to make sure the name is not the same as any of the C language keywords
		for(int i = 0; i < KEYWORDS.length; i++){
			if(bufferName.equals(KEYWORDS[i]))
				return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgInvalidName2;
		}
		
		// make sure all characters in the string are valid
		// characters must be one of the 52 alphabetic characters, one of the 10 digits, or the underscore '_'
		for(int i = 0; i < bufferName.length(); i++){
			c = bufferName.charAt(i);
			if((c < 48 || c > 57) && (c < 65 || c > 90) && (c < 97 || c > 122) && (c != 95))
				return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgInvalidName3;
		}

		return null;
	}

	/**
	 * Determines if the given element type and unit are valid; returning an error message, a warning message, or null to indicate
	 * the element type and unit are valid.
	 * 
	 * @param elementType the element type ($ELEMENT_TYPE) that is to be validated
	 * @param elementUnit the element unit ($ELEMENT_UNIT) that is to be validated
	 * @return null if there is no warning message, "Error: <error message>" if there is an error that will prevent the element type and unit
	 * 	       from being used, or "Warning: <warning message>" if there is a warning message that needs to be displayed. A warning message
	 *         indicates that the given element type and unit can be used, but it is not recommended.
	 */
	public String isTypeAndUnitValid(String elementType, int elementUnit){
		// if the element type is null or empty, then the user has not began filling in this field yet. so report it as valid
		if(elementType == null || elementType.length() == 0)
			return null;

		// find out if the user has entered a built-in type for the element type
		boolean isBuiltInType = false;
		int i;
		for(i = 0; i < TYPES.length; i++){
			if(elementType.equals(TYPES[i].getValue())){
				isBuiltInType = true;
				break;
			}
		}
		
		// if element type is a built-in type, then return warning message if the selected element unit is not recommended for the built-in type
		// if a warning message is returned, the OK button will be enabled. 
		if(isBuiltInType){
			if(TYPES[i].getValue().equals("void*") || TYPES[i].getValue().equals("void *"))  //$NON-NLS-1$ //$NON-NLS-2$
				return Messages.ALFBufferValidator_warningMsg + Messages.ALFBufferValidator_warningMsgVoidStar;
			if(!TYPES[i].containsUnit(elementUnit))
				return Messages.ALFBufferValidator_warningMsg + Messages.ALFBufferValidator_warningMsgIncompatibleUnit;
		}
		else {
			// make sure the first character is an alphabetic character
			int j = 0;
			char c = elementType.charAt(0);
			if((c < 65 || c > 90) && (c < 97 || c > 122) && (c != 95))
				return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgInvalidType1;
			
			
			// make sure all characters in the user-defined element type are valid
			// characters must be one of the 52 alphabetic characters, one of the 10 digits, or the underscore '_'
			for(j = 1; j < elementType.length(); j++){
				c = elementType.charAt(j);
				if((c < 48 || c > 57) && (c < 65 || c > 90) && (c < 97 || c > 122) && (c != 95))
					return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgInvalidType2;
			}
		}

		return null;
	}
	
	/**
	 * Verifies the number of data transfer entries (that is computed using the arguments) matches the number of DT entries in the existing alf buffer objects
	 * 
	 * @param numDim the alf buffer's number of dimensions
	 * @param dimSizeX the size of the X-dimension
	 * @param dimSizeY the size of the Y-dimension
	 * @param dimSizeZ the size of the Z-dimension
	 * @param distSizeX the distribution size of the X-dimension
	 * @param distSizeY the distribution size of the Y-dimension
	 * @param distSizeZ the distribution size of the Z-dimension
	 * @return an error message if the number of DT entries (computed using the given arguments) does not match the number of 
	 * 		   DT entries of the existing alf buffer objects, else return null if the number of DT matches
	 */
	public String isNumDTEntriesValid(int numDim, long dimSizeX, long dimSizeY, long dimSizeZ, long distSizeX, long distSizeY, long distSizeZ){
		long numDT;
		
		switch(numDim){
			case ALFConstants.ONE_DIMENSIONAL:
				if(distSizeX == 0)
					return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgNumDTEntries;
				numDT = (dimSizeX / distSizeX);
				break;
				
			case ALFConstants.TWO_DIMENSIONAL:
				if(distSizeX == 0 || distSizeY == 0)
					return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgNumDTEntries;
				numDT = (dimSizeX / distSizeX) * (dimSizeY / distSizeY);
				break;
				
			case ALFConstants.THREE_DIMENSIONAL:
				if(distSizeX == 0 || distSizeY == 0 || distSizeZ == 0)
					return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgNumDTEntries;
				numDT = (dimSizeX / distSizeX) * (dimSizeY / distSizeY) * (dimSizeZ / distSizeZ);
				break;
				
			default: 
				throw new IllegalArgumentException("An illegal argument has been passed while trying to verify the number of data transfer entries."); //$NON-NLS-1$
		}
		
		return pageB.isNumDTEntriesValid(numDT) ? null : (Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgNumDTEntries);
	}
	
	public static String is16ByteAligned(String elementType, int numDim, long distSizeX, long distSizeY, long distSizeZ, boolean is64bit){
		if(elementType == null || elementType.length() == 0)
			return null;
		
		long dataTransferSize = computeDataTransferSize(elementType, numDim, distSizeX, distSizeY, distSizeZ, is64bit);
		
		if((dataTransferSize == -1) || (dataTransferSize == 1) || (dataTransferSize == 2) || (dataTransferSize == 4) || (dataTransferSize == 8) 
				|| (dataTransferSize == 16) || (dataTransferSize % 16 == 0))		
			return null;
		else
			return Messages.ALFBufferValidator_warningMsgNot16ByteAligned;
	}
	
	/**
     * Validates the given ALFBuffer parameters, depending on how many expected 
     * accelerator nodes there are.  Returns an error message to display
     * if the ALFBuffer parameters are invalid.  Returns <code>null</code> if there
     * is no error.  Note that the empty string is not treated the same
     * as <code>null</code>; it indicates an error state but with no message
     * to display.
     * 
     * @param bufferName the name of the buffer that needs to be validated
     * @param elementType the buffer's element type (C built-in type or user-defined type)
     * @param elementUnit the buffer's element unit
     * @param bufferType the buffer type (input or output)
     * @param numDimensions the buffer's number of dimensions
     * @param dimSizeX string representation of the X dimension's size
     * @param dimSizeY string representation of the Y dimension's size
     * @param dimSizeZ string representation of the Z dimension's size
     * @param distributionModelX the X dimension's distribution model (star, cyclic, or block)
     * @param distributionModelY the Y dimension's distribution model (star, cyclic, or block)
     * @param distributionModelZ the Z dimension's distribution model (star or block)
     * @param distSizeX string representation of the X dimension's distribution model size
     * @param distSizeY string representation of the Y dimension's distribution model size
     * @param distSizeZ string representation of the Z dimension's distribution model size
     * 
     * @return an error message or <code>null</code> if no error
     */
	public String isValid(String bufferName, String elementType, int elementUnit, int bufferType, int numDim, 
			String dimSizeX, String dimSizeY, String dimSizeZ,
			int distModelX, int distModelY, int distModelZ,
			String distSizeX, String distSizeY, String distSizeZ){
		
		long dimensionSizeX, dimensionSizeY, dimensionSizeZ, distributionSizeX, distributionSizeY, distributionSizeZ;
		
		// first, convert the String dimension and distribution sizes to type long, and return error if the String contains any text or is larger than Long.MAX_VALUE
		try{ dimensionSizeX = Long.parseLong(dimSizeX); }
		catch(NumberFormatException e){	return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDimensionSizeX; }
		
		try{ dimensionSizeY = Long.parseLong(dimSizeY); } 
		catch(NumberFormatException e){ return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDimensionSizeY; }
		
		try{ dimensionSizeZ = Long.parseLong(dimSizeZ); }
		catch(NumberFormatException e){ return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDimensionSizeZ; }
		
		try{ distributionSizeX = Long.parseLong(distSizeX); }
		catch(NumberFormatException e){ return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDistributionSizeX1; }
		
		try{ distributionSizeY = Long.parseLong(distSizeY); }
		catch(NumberFormatException e){ return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDistributionSizeY1; }

		try{ distributionSizeZ = Long.parseLong(distSizeZ); }
		catch(NumberFormatException e){ return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDistributionSizeZ1; }
		
		
		int expAccelNum = getExpectedAccelNum();
        
        // validate the z dimension's fields
        if(numDim == ALFConstants.THREE_DIMENSIONAL){
        	
        	// ensure that the z dimension's size is greater than 0
        	if(dimensionSizeZ < 1 )
        		return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDimensionSizeZ;
        	
        	// if expected number of accelerator nodes has been set to "all available", then the BLOCK distribution model cannot be used
        	if(expAccelNum == 0)
        		if(distModelZ == ALFConstants.DIST_MODEL_BLOCK)
        			return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgBlock;
        	
        	// ensure that the x dimension's distribution size is greater than 0
        	if(distModelZ == ALFConstants.DIST_MODEL_BLOCK)
        		if(distributionSizeZ < 1)
        			return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDistributionSizeZ2;
        	
        	// if the array is 3 dimensional, then the CYCLIC distribution model cannot be used
        	if(distModelX == ALFConstants.DIST_MODEL_CYCLIC || distModelY == ALFConstants.DIST_MODEL_CYCLIC)
        		return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgCyclic;
        }
        
        // validate the y dimension's fields
        if((numDim == ALFConstants.TWO_DIMENSIONAL) || (numDim == ALFConstants.THREE_DIMENSIONAL)){
        	
        	// ensure that the y dimension's size is greater than 0
        	if(dimensionSizeY < 1)
        		return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDimensionSizeY;
        	
        	// if expected number of accelerator nodes has been set to "all available", then the BLOCK distribution model cannot be used
        	if(expAccelNum == 0)
        		if(distModelY == ALFConstants.DIST_MODEL_BLOCK)
        			return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgBlock;
        	
        	// ensure that the y dimension's distribution size is greater than 0
        	if(distModelY == ALFConstants.DIST_MODEL_BLOCK)
        		if(distributionSizeY < 1)
        			return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDistributionSizeY3;
        	
        	// if the Y distribution model is set to CYCLIC, then ensure that the value for the distribution size is valid
        	if(distModelY == ALFConstants.DIST_MODEL_CYCLIC){
        		if((distributionSizeY < 1) || (distributionSizeY > dimensionSizeY)) // the value must be a whole number between 1 and the dimension's size
        			return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDistributionSizeY1;
        		// the distribution size must be: 1, 2, 4, 8, or a multiple of 16
        		//if((distributionSizeY != 1) && (distributionSizeY != 2) && (distributionSizeY != 4) && (distributionSizeY != 8) && (distributionSizeY % 16 != 0))
        			//return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDistributionSizeY2;
        	}

        }	

        // validate the x dimension's fields
        if((numDim == ALFConstants.ONE_DIMENSIONAL) || (numDim == ALFConstants.TWO_DIMENSIONAL) || (numDim == ALFConstants.THREE_DIMENSIONAL)){
        	
        	// ensure that the x dimension's size is greater than 0
        	if(dimensionSizeX < 1) // must be a positive (non-zero) value with no non-numeric character 
        		return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDimensionSizeX;
        	
        	// if expected number of accelerator nodes has been set to "all available", then the BLOCK distribution model cannot be used
        	if(expAccelNum == 0)
        		if(distModelX == ALFConstants.DIST_MODEL_BLOCK)
            		return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgBlock;
        	
        	// ensure that the x dimension's distribution size is greater than 0
        	if(distModelX == ALFConstants.DIST_MODEL_BLOCK)
        		if(distributionSizeX < 1)
        			return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDistributionSizeX3;
       	
        	// if the X distribution model is set to CYCLIC, then ensure that the value for the distribution size is valid
        	if(distModelX == ALFConstants.DIST_MODEL_CYCLIC){
        		if((distributionSizeX < 1) || (distributionSizeX > dimensionSizeX)) // the value must be a whole number between 1 and the dimension's size
        			return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDistributionSizeX1;
        		// the distribution size must be: 1, 2, 4, 8, or a multiple of 16
        		//if((distributionSizeX != 1) && (distributionSizeX != 2) && (distributionSizeX != 4) && (distributionSizeX != 8) && (distributionSizeX % 16 != 0))
        			//return Messages.ALFBufferValidator_errorMsg + Messages.ALFBufferValidator_errorMsgDistributionSizeX2;
        	}
        }
        
        String errMsg = isNameValid(bufferName);
        if(errMsg != null)
        	return errMsg;

        errMsg = isTypeAndUnitValid(elementType, elementUnit);

		return null;
	}
}
