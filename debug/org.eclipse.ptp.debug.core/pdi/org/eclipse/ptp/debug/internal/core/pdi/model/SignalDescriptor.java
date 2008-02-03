package org.eclipse.ptp.debug.internal.core.pdi.model;

import org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor;

public class SignalDescriptor implements IPDISignalDescriptor {
	private String name;
	private boolean stop;
	private boolean print;
	private boolean pass;
	private String desc;

	public SignalDescriptor(String name, boolean stop, boolean print, boolean pass, String desc) {
		this.name = name;
		this.stop = stop;
		this.print = print;
		this.pass = pass;
		this.desc = desc;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor#getDescription()
	 */
	public String getDescription() {
		return desc;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor#getPass()
	 */
	public boolean getPass() {
		return pass;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor#getPrint()
	 */
	public boolean getPrint() {
		return print;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor#getStop()
	 */
	public boolean getStop() {
		return stop;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor#setDescription(java.lang.String)
	 */
	public void setDescription(String desc) {
		this.desc = desc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor#setPass(boolean)
	 */
	public void setPass(boolean pass) {
		this.pass = pass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor#setPrint(boolean)
	 */
	public void setPrint(boolean print) {
		this.print = print;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor#setStop(boolean)
	 */
	public void setStop(boolean stop) {
		this.stop = stop;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Name: " + name + ", stop: " + stop + ", print: " + print + ", pass: " + pass + ", desc: " + desc; 
	}
}
