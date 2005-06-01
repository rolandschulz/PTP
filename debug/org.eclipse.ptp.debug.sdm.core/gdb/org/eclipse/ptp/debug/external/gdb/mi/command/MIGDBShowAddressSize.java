/*
 * Created on Jun 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.ptp.debug.external.gdb.mi.command;




import org.eclipse.ptp.debug.external.gdb.mi.MIException;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIGDBShowAddressSizeInfo;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIInfo;
import org.eclipse.ptp.debug.external.gdb.mi.output.MIOutput;




/**
 * @author root
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MIGDBShowAddressSize extends MIGDBShow {
	
	public MIGDBShowAddressSize () {
		super(new String[] { "remoteaddresssize" }); //$NON-NLS-1$
	}
	
	public MIInfo getMIInfo() throws MIException {
		MIGDBShowAddressSizeInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIGDBShowAddressSizeInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

}
