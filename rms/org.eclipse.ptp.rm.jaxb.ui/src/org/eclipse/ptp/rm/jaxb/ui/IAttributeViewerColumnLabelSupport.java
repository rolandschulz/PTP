package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.graphics.Image;

public interface IAttributeViewerColumnLabelSupport extends ITableColorProvider {

	Image getColumnImage(String columnName);

	String getDisplayValue(String columnName);
}
