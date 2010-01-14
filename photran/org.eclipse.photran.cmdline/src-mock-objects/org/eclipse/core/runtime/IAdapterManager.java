package org.eclipse.core.runtime;


public interface IAdapterManager
{
    Object getAdapter(Object node, Class<?> clazz);
}
