package org.eclipse.ptp.internal.rdt.core.includebrowser;

import org.eclipse.cdt.core.index.IIndexInclude;

public interface IIndexIncludeValue extends IIndexInclude
{
	public long getIncludedByTimestamp();
}
