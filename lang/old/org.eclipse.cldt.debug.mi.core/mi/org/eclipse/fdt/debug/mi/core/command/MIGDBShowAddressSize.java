/*
 * Created on Jun 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.fdt.debug.mi.core.command;

import org.eclipse.fdt.debug.mi.core.MIException;
import org.eclipse.fdt.debug.mi.core.output.MIGDBShowAddressSizeInfo;
import org.eclipse.fdt.debug.mi.core.output.MIInfo;
import org.eclipse.fdt.debug.mi.core.output.MIOutput;

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
