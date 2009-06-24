package org.eclipse.core.resources;

import java.util.Map;

public interface IMarker
{
    public static final String TEXT = null;
    public static final String CHAR_START = null;
    public static final String CHAR_END = null;
	public static final String MESSAGE = null;
	public static final String USER_EDITABLE = null;
    void setAttribute(String charStart, int startOffset);
	@SuppressWarnings("unchecked") void setAttributes(Map attribs);
}
