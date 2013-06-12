package org.eclipse.ptp.debug.core.pdi.model;

/**
 * Represents the data associated with a signal
 * 
 */
public interface IPDISignalDescriptor {
	/**
	 * Get signal description
	 * 
	 * @return
	 */
	public String getDescription();

	/**
	 * Get signal name
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Get pass flag
	 * 
	 * @return
	 */
	public boolean getPass();

	/**
	 * Get print flag
	 * 
	 * @return
	 */
	public boolean getPrint();

	/**
	 * Get stop flag
	 * 
	 * @return
	 */
	public boolean getStop();

	/**
	 * Set signal description
	 * 
	 * @param desc
	 */
	public void setDescription(String desc);

	/**
	 * Set signal name
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * Set pass flag
	 * 
	 * @param pass
	 */
	public void setPass(boolean pass);

	/**
	 * Set print flag
	 * 
	 * @param print
	 */
	public void setPrint(boolean print);

	/**
	 * Set stop flag
	 * 
	 * @param stop
	 */
	public void setStop(boolean stop);
}
