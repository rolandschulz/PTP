package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.swt.graphics.Image;

public interface IColumnViewerLabelSupport extends ITableColorProvider, ITableFontProvider {

	Image getColumnImage(String columnName);

	String getDescription();

	String getDisplayValue(String columnName);

	String getTooltip();
}
