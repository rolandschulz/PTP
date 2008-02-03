package org.eclipse.ptp.debug.core.pdi.model;


/**
 * Represents the data associated with a signal
 *
 */
public interface IPDISignalDescriptor {
	/**
	 * @return
	 */
	public String getDescription();
	
	/**
	 * @return
	 */
	public String getName();
	
	/**
	 * @return
	 */
	public boolean getPass();
	
	/**
	 * @return
	 */
	public boolean getPrint();
	
	/**
	 * @return
	 */
	public boolean getStop();
	
	/**
	 * @param desc
	 */
	public void setDescription(String desc);

	/**
	 * @param name
	 */
	public void setName(String name);

	/**
	 * @param pass
	 */
	public void setPass(boolean pass);

	/**
	 * @param print
	 */
	public void setPrint(boolean print);

	/**
	 * @param stop
	 */
	public void setStop(boolean stop);
}
