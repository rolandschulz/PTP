package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public interface IJAXBUINonNLSConstants extends IJAXBNonNLSConstants {

	int DEFAULT = UNDEFINED;

	Color DKBL = Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE);
	Color DKMG = Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA);
	Color DKRD = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);

	String COURIER = "Courier";//$NON-NLS-1$

	String COLUMN_NAME = "Name";//$NON-NLS-1$
	String COLUMN_VALUE = "Value";//$NON-NLS-1$
	String COLUMN_DEFAULT = "Default";//$NON-NLS-1$
	String COLUMN_DESC = "Description";//$NON-NLS-1$
	String COLUMN_STATUS = "Status";//$NON-NLS-1$
	String COLUMN_TOOLTIP = "Tooltip";//$NON-NLS-1$
	String COLUMN_TYPE = "Type";//$NON-NLS-1$

}
