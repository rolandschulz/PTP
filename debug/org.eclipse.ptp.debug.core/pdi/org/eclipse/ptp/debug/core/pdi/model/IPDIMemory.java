package org.eclipse.ptp.debug.core.pdi.model;

/**
 * Represents a block of memory on the target
 * 
 */
public interface IPDIMemory {

	/**
	 * Get the address
	 * 
	 * @return
	 */
	public String getAddress();

	/**
	 * Get the data
	 * 
	 * @return
	 */
	public long[] getData();

	/**
	 * Get the bad offsets
	 * 
	 * @return
	 */
	public int[] getBadOffsets();

	/**
	 * Get an ascii version of the data
	 * 
	 * @return
	 */
	public String getAscii();

	/**
	 * Get the data as a string
	 * 
	 * @return
	 */
	public String toString();

}