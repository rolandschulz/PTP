package org.eclipse.ptp.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.pdi.model.IPDIMemory;

/**
 * Representation of data read from target memory
 * 
 */
public interface IPDIDataReadMemoryInfo {

	/**
	 * Get the address
	 * 
	 * @return
	 */
	public String getAddress();

	/**
	 * Get the number of bytes read
	 * 
	 * @return
	 */
	public long getNumberBytes();

	/**
	 * Get the total number of bytes
	 * 
	 * @return
	 */
	public long getTotalBytes();

	/**
	 * Get the next row
	 * 
	 * @return
	 */
	public long getNextRow();

	/**
	 * Get the previous row
	 * 
	 * @return
	 */
	public long getPreviousRow();

	/**
	 * Get the next page
	 * 
	 * @return
	 */
	public long getNextPage();

	/**
	 * Get the previous page
	 * 
	 * @return
	 */
	public long getPreviousPage();

	/**
	 * Get the memories
	 * 
	 * @return
	 */
	public IPDIMemory[] getMemories();

}