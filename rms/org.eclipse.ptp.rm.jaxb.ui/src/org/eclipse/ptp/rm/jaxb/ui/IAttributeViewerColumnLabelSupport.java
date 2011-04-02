package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.swt.graphics.Image;

public interface IAttributeViewerColumnLabelSupport {

	Image getColumnImage(String columnName);

	String getDisplayValue(String columnName);
}
