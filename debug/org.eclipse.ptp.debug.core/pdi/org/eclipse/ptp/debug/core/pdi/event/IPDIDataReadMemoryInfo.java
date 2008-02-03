package org.eclipse.ptp.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.pdi.model.IPDIMemory;

public interface IPDIDataReadMemoryInfo {

	/**
	 * @return
	 */
	public String getAddress();

	/**
	 * @return
	 */
	public long getNumberBytes();

	/**
	 * @return
	 */
	public long getTotalBytes();

	/**
	 * @return
	 */
	public long getNextRow();

	/**
	 * @return
	 */
	public long getPreviousRow();

	/**
	 * @return
	 */
	public long getNextPage();

	/**
	 * @return
	 */
	public long getPreviousPage();

	/**
	 * @return
	 */
	public IPDIMemory[] getMemories();

}