package org.eclipse.ptp.debug.core.pdi.model;

public interface IPDIMemory {

	/**
	 * @return
	 */
	public String getAddress();

	/**
	 * @return
	 */
	public long[] getData();

	/**
	 * @return
	 */
	public int[] getBadOffsets();

	/**
	 * @return
	 */
	public String getAscii();

	/**
	 * @return
	 */
	public String toString();

}