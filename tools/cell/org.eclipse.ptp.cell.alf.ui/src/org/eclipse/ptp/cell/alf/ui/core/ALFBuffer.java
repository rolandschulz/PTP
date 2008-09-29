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

/**
 * This ALFBuffer class represents a Buffer input that the user creates using the ALF API Wizard.
 * ALFBuffer holds (as instance variables) all of the necessary information - for each buffer input - 
 * that is needed to create the XML parameter description file.
 * 
 * @author Sean Curry
 * @since 3.0.0
 */
public class ALFBuffer {

	/**
	 * Represents the XML token "$VARIABLE_NAME", which is the name of this data array.
	 * Must be valid variable name in C language. It is case sensitive. Must have different name than any 
	 * other buffer.
	 */
	private String variable_name;
	
	/**
	 * Represents the XML token "$ELEMENT_TYPE", which is the type of element for the data array specified by $VARIABLE_NAME.
	 * Any C language built in type or user defined type is acceptable
	 */
	private String element_type;
	
	/**
	 * Represents the XML token "$ELEMENT_UNIT", which is the basic ALF type unit of the element. Acceptable values are:
	 * 1.) ALF_DATA_BYTE 2.) ALF_DATA_INT16 3.) ALF_DATA_INT32 4.) ALF_DATA_INT64 5.) ALF_DATA_FLOAT 6.) ALF_DATA_DOUBLE 7.) $ELEMENT_TYPE
	 */
	private int element_unit;
	
	/**
	 * Represents the XML token "$BUFFER_TYPE", which is the buffer type of the data array indicated by $VARIABLE_NAME. 
	 * Acceptable values are: 0 (ALF_BUFFER_INPUT) or 1 (ALF_BUFFER_OUTPUT).
	 */
	private int buffer_type;
	
	/**
	 * Represents the XML token "$NUM_DIMENSION", which is the dimension for the array specified by $VARIABLE_NAME. 
	 * Acceptable values are: 0 (1-D), 1 (2-D), or 2 (3-D). 
	 */
	private int num_dimensions;
	
	/**
	 * Represents the XML token "$DIMENSION_SIZE_X", which is the X dimension's size for the array indicated by $VARIABLE_NAME.
	 * Acceptable values are 1, 2, 3, ...
	 */
	private long dimension_size_x;
	
	/**
	 * Represents the XML token "$DIMENSION_SIZE_Y", which is the Y dimension's size for the array indicated by $VARIABLE_NAME.
	 * Acceptable values are 1, 2, 3, ... or 0 when the dimension of the array is less than 2.
	 */
	private long dimension_size_y;
	
	/**
	 * Represents the XML token "$DIMENSION_SIZE_Z", which is the Z dimension's size for the array indicated by $VARIABLE_NAME.
	 * Acceptable values are 1, 2, 3, ... or 0 when the dimension of the array is less than 3.
	 */
	private long dimension_size_z;
	
	/**
	 * Represents the XML token "$DISTRIBUTION_MODEL_X", which is the distribution model for the X dimension of the data array
	 * indicated by $VARIABLE_NAME. 
	 * Acceptable values are: 0 (*), 1 (BLOCK), or 2 (CYCLIC).
	 */
	private int distribution_model_x;
	
	/**
	 * Represents the XML token "$DISTRIBUTION_MODEL_Y", which is the distribution model for the Y dimension of the data array
	 * indicated by $VARIABLE_NAME. 
	 * Acceptable values are: 0 (*), 1 (BLOCK), or 2 (CYCLIC). This option will not be available if dimension of array is less than 2. 
	 */
	private int distribution_model_y;
	
	/**
	 * Represents the XML token "$DISTRIBUTION_MODEL_Z", which is the distribution model for the Z dimension of the data array
	 * indicated by $VARIABLE_NAME. 
	 * Acceptable values are: 0 (*) or 1 (BLOCK). This option will not be available if dimension of array is less than 3. 
	 */
	private int distribution_model_z;
	
	/**
	 * Represents the XML token "$DISTRIBUTION_SIZE_X", which is the distribution size for the X dimension of the data array 
	 * indicated by $VARIABLE_NAME. 
	 * Acceptrable values are: 
	 * 		CYCLIC - User input integer between 1 and $DIMENSION_SIZE_X
	 * 		BLOCK - ?? TODO
	 */
	private long distribution_size_x;
	
	/**
	 * Represents the XML token "$DISTRIBUTION_SIZE_Y", which is the distribution size for the Y dimension of the data array 
	 * indicated by $VARIABLE_NAME. 
	 * Acceptrable values are: 
	 * 		CYCLIC - User input integer between 1 and $DIMENSION_SIZE_Y
	 * 		BLOCK - ?? TODO
	 * 		0 if dimension of array is less than 2.
	 */
	private long distribution_size_y;
	
	/**
	 * Represents the XML token "$DISTRIBUTION_SIZE_Z", which is the distribution size for the Z dimension of the data array 
	 * indicated by $VARIABLE_NAME. 
	 * Acceptrable values are: 
	 * 		BLOCK - ?? TODO
	 * 		0 if dimension of array is less than 3.
	 */
	private long distribution_size_z;
	
	/**
	 * Holds the number of data transfer entries that this buffer will have. 
	 * The number of data transfer entries per buffer is given by the following equation : totalNumDT = numDT(x) * numDT(y) * numDT(z)
	 * If the array is 1-dimension, numDT(y) and numDT(z) would be excluded from the equation, and only numDT(z) would be excluded for a 2-dimensional array.
	 * each individual numDT(-) entry is calculated by: numDT(-) = dimension_size_- / distribution_size_-
	 */
	private long numDTEntries;
	
	/**
	 * Determines whether or not this alf buffer object is valid
	 */
	private boolean isValid;
	
	public ALFBuffer(){
		variable_name = null;
		element_type = null;
		element_unit = -1;
		buffer_type = -1;
		num_dimensions = -1;
		dimension_size_x = -1;
		dimension_size_y = -1;
		dimension_size_z = -1;
		distribution_model_x = -1;
		distribution_model_y = -1;
		distribution_model_z = -1;
		distribution_size_x = -1;
		distribution_size_y = -1;
		distribution_size_z = -1;
		numDTEntries = -1;
		isValid = false;
	}
	
	public ALFBuffer(String name, String type, int unit, int buffer, int num_dim, long dim_size_x, long dim_size_y, long dim_size_z,
			int dist_model_x, int dist_model_y, int dist_model_z, long dist_size_x, long dist_size_y, long dist_size_z, boolean isValid){
		variable_name = name;
		element_type = type;
		element_unit = unit;
		buffer_type = buffer;
		num_dimensions = num_dim;
		dimension_size_x = dim_size_x;
		dimension_size_y = dim_size_y;
		dimension_size_z = dim_size_z;
		distribution_model_x = dist_model_x;
		distribution_model_y = dist_model_y;
		distribution_model_z = dist_model_z;
		distribution_size_x = dist_size_x;
		distribution_size_y = dist_size_y;
		distribution_size_z = dist_size_z;
		this.isValid = isValid;
		switch (num_dimensions){
			case ALFConstants.ONE_DIMENSIONAL:
				numDTEntries = (dim_size_x / dist_size_x);
				break;
			
			case ALFConstants.TWO_DIMENSIONAL:
				numDTEntries = (dim_size_x / dist_size_x) * (dim_size_y / dist_size_y);
				break;
				
			case ALFConstants.THREE_DIMENSIONAL:
				numDTEntries = (dim_size_x / dist_size_x) * (dim_size_y / dist_size_y) * (dim_size_z / dist_size_z);
				break;
				
			default:
				throw new IllegalArgumentException("A runtime error has occured while trying to create the ALFBuffer object. The value of num_dimensions is invalid."); //$NON-NLS-1$
				
		}
	}
	
	public String getName(){ return this.variable_name; }
	
	public void setName(String newName){ this.variable_name = newName; }
	
	public String getElementType(){ return this.element_type; }
	
	public void setElementType(String newType){ this.element_type = newType; }
	
	public int getElementUnit(){ return this.element_unit; }
	
	public void setElementUnit(int newUnit){ this.element_unit = newUnit; }
	
	public int getBufferType(){ return this.buffer_type; }
	
	public void setBufferType(int newBufferType){ this.buffer_type = newBufferType; }
	
	public int getNumDimensions(){ return this.num_dimensions; }
	
	public void setNumDimensions(int newNumDimensions){ this.num_dimensions = newNumDimensions; }
	
	public long getDimensionSizeX(){ return this.dimension_size_x; }
	
	public void setDimensionSizeX(long newDimensionSizeX){ this.dimension_size_x = newDimensionSizeX; }
	
	public long getDimensionSizeY(){ return this.dimension_size_y; }
	
	public void setDimensionSizeY(long newDimensionSizeY){ this.dimension_size_y = newDimensionSizeY; }
	
	public long getDimensionSizeZ(){ return this.dimension_size_z; }
	
	public void setDimensionSizeZ(long newDimensionSizeZ){ this.dimension_size_z = newDimensionSizeZ; }
	
	public int getDistributionModelX(){ return this.distribution_model_x; }
	
	public void setDistributionModelX(int newDistributionModelX){ this.distribution_model_x = newDistributionModelX; }
	
	public int getDistributionModelY(){ return this.distribution_model_y; }
	
	public void setDistributionModelY(int newDistributionModelY){ this.distribution_model_y = newDistributionModelY; }
	
	public int getDistributionModelZ(){ return this.distribution_model_z; }
	
	public void setDistributionModelZ(int newDistributionModelZ){ this.distribution_model_z = newDistributionModelZ; }
	
	public long getDistributionSizeX(){ return this.distribution_size_x; }
	
	public void setDistributionSizeX(long newDistributionSizeX){ this.distribution_size_x = newDistributionSizeX; }
	
	public long getDistributionSizeY(){ return this.distribution_size_y; }
	
	public void setDistributionSizeY(long newDistributionSizeY){ this.distribution_size_y = newDistributionSizeY; }
	
	public long getDistributionSizeZ(){ return this.distribution_size_z; }
	
	public void setDistributionSizeZ(long newDistributionSizeZ){ this.distribution_size_z = newDistributionSizeZ; }
	
	public boolean isValid(){ return this.isValid; }
	
	public void setIsValid(boolean newIsValid){ this.isValid = newIsValid; }
	
	public long getNumberDTEntries(){ updateNumDTEntries(); return this.numDTEntries; }
	
	public void updateNumDTEntries(){
		switch (num_dimensions){
		case ALFConstants.ONE_DIMENSIONAL:
			numDTEntries = (dimension_size_x / distribution_size_x);
			break;
		
		case ALFConstants.TWO_DIMENSIONAL:
			numDTEntries = (dimension_size_x / distribution_size_x) * (dimension_size_y / distribution_size_y);
			break;
			
		case ALFConstants.THREE_DIMENSIONAL:
			numDTEntries = (dimension_size_x / distribution_size_x) * (dimension_size_y / distribution_size_y) * (dimension_size_z / distribution_size_z);
			break;
			
		default:
			throw new IllegalArgumentException("A runtime error has occured while trying to create the ALFBuffer object. The value of num_dimensions is invalid."); //$NON-NLS-1$
			
		}
	}
}
