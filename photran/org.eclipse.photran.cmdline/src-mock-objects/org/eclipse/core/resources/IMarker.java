package org.eclipse.core.resources;

public interface IMarker
{
    public static final String TEXT = null;
    public static final String CHAR_START = null;
    public static final String CHAR_END = null;
    void setAttribute(String charStart, int startOffset);
}
