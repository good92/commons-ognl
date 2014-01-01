package org.apache.commons.ognl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.ognl.enhance.ExpressionCompiler;
import org.apache.commons.ognl.enhance.OgnlExpressionCompiler;
import org.apache.commons.ognl.internal.CacheException;
import org.apache.commons.ognl.internal.entry.DeclaredMethodCacheEntry;
import org.apache.commons.ognl.internal.entry.GenericMethodParameterTypeCacheEntry;
import org.apache.commons.ognl.internal.entry.MethodAccessEntryValue;
import org.apache.commons.ognl.internal.entry.PermissionCacheEntry;

import static org.apache.commons.ognl.OgnlRuntime.*;

public class OgnlRuntime2
{

    OgnlCache cache = new OgnlCache();

    private SecurityManager securityManager = System.getSecurityManager();

    /**
     * Expression compiler used by {@link Ognl#compileExpression(OgnlContext, Object, String)} calls.
     */
    private OgnlExpressionCompiler compiler;

    /**
     * Clears all of the cached reflection information normally used to improve the speed of expressions that operate on
     * the same classes or are executed multiple times.
     * <p>
     * <strong>Warning:</strong> Calling this too often can be a huge performance drain on your expressions - use with
     * care.
     * </p>
     */
    public void clearCache()
    {
        cache.clear();
    }

    public void setCompiler( OgnlExpressionCompiler compiler )
    {
        this.compiler = compiler;
    }

    public OgnlExpressionCompiler getCompiler( OgnlContext ognlContext )
    {
        if ( compiler == null )
        {
            try
            {
                OgnlRuntime.classForName( ognlContext, "javassist.ClassPool" );
                compiler = new ExpressionCompiler();
            }
            catch ( ClassNotFoundException e )
            {
                throw new IllegalArgumentException(
                    "Javassist library is missing in classpath! Please add missed dependency!", e );
            }
        }
        return compiler;
    }

    /**
     * Returns the parameter types of the given method.
     */
    public Class<?>[] getParameterTypes( Method method )
        throws CacheException
    {
        return cache.getMethodParameterTypes( method );
    }
    
    /**
     * Finds the appropriate parameter types for the given {@link Method} and {@link Class} instance of the type the
     * method is associated with. Correctly finds generic types if running in >= 1.5 jre as well.
     *
     * @param type The class type the method is being executed against.
     * @param method    The method to find types for.
     * @return Array of parameter types for the given method.
     * @throws org.apache.commons.ognl.internal.CacheException
     */
    public Class<?>[] findParameterTypes( Class<?> type, Method method )
        throws CacheException
    {
        if ( type == null || type.getGenericSuperclass() == null || !ParameterizedType.class.isInstance(
            type.getGenericSuperclass() ) || method.getDeclaringClass().getTypeParameters() == null )
        {
            return getParameterTypes( method );
        }

        GenericMethodParameterTypeCacheEntry key = new GenericMethodParameterTypeCacheEntry( method, type );
        return cache.getGenericMethodParameterTypes( key );
    }

    /**
     * Returns the parameter types of the given method.
     * @param constructor
     * @return
     * @throws org.apache.commons.ognl.internal.CacheException
     */
    public Class<?>[] getParameterTypes( Constructor<?> constructor )
        throws CacheException
    {
        return cache.getParameterTypes( constructor );
    }
    
    
    /**
     * Gets the SecurityManager that OGNL uses to determine permissions for invoking methods.
     *
     * @return SecurityManager for OGNL
     */
    public SecurityManager getSecurityManager()
    {
        return securityManager;
    }
    
    /**
     * Sets the SecurityManager that OGNL uses to determine permissions for invoking methods.
     *
     * @param securityManager SecurityManager to set
     */
    public void setSecurityManager( SecurityManager securityManager )
    {
        this.securityManager = securityManager;
        cache.setSecurityManager(securityManager);
    }

    /**
     * Permission will be named "invoke.<declaring-class>.<method-name>".
     * @param method
     * @return
     * @throws org.apache.commons.ognl.internal.CacheException
     */
    public Permission getPermission( Method method )
        throws CacheException
    {
        PermissionCacheEntry key = new PermissionCacheEntry( method );
        return cache.getInvokePermission( key );
    }
    
    public Object invokeMethod( Object target, Method method, Object[] argsArray )
            throws InvocationTargetException, IllegalAccessException, CacheException
        {
            Object result;

            if ( securityManager != null && !cache.getMethodPerm( method ) )
            {
                throw new IllegalAccessException( "Method [" + method + "] cannot be accessed." );
            }

            MethodAccessEntryValue entry = cache.getMethodAccess( method );
            if ( !entry.isAccessible() )
            {
                // only synchronize method invocation if it actually requires it
                synchronized ( method )
                {

                    if ( entry.isNotPublic() && !entry.isAccessible() )
                    {
                        method.setAccessible( true );
                    }

                    result = method.invoke( target, argsArray );

                    if ( !entry.isAccessible() )
                    {
                        method.setAccessible( false );
                    }
                }
            }
            else
            {
                result = method.invoke( target, argsArray );
            }

            return result;
        }

    public List<Constructor<?>> getConstructors( Class<?> targetClass )
    {
        return cache.getConstructor( targetClass );
    }
    
    /**
     * @param targetClass
     * @param staticMethods if true (false) returns only the (non-)static methods
     * @return Returns the map of methods for a given class
     */
    public Map<String, List<Method>> getMethods( Class<?> targetClass, boolean staticMethods )
    {
        DeclaredMethodCacheEntry.MethodType type = staticMethods ?
            DeclaredMethodCacheEntry.MethodType.STATIC :
            DeclaredMethodCacheEntry.MethodType.NON_STATIC;
        DeclaredMethodCacheEntry key = new DeclaredMethodCacheEntry( targetClass, type );
        return cache.getMethod( key );
    }
    

    public Map<String, Field> getFields( Class<?> targetClass )
    {
        return cache.getField( targetClass );
    }

    /**
     * @param targetClass
     * @param propertyName
     * @param findSets
     * @return Returns the list of (g)setter of a class for a given property name
     * @
     */
    public List<Method> getDeclaredMethods( Class<?> targetClass, String propertyName, boolean findSets )
    {
        String baseName = Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 );
        List<Method> methods = new ArrayList<Method>();
        List<String> methodNames = new ArrayList<String>( 2 );
        if ( findSets )
        {
            methodNames.add( SET_PREFIX + baseName );
        }
        else
        {
            methodNames.add( IS_PREFIX + baseName );
            methodNames.add( GET_PREFIX + baseName );
        }
        for ( String methodName : methodNames )
        {
            DeclaredMethodCacheEntry key = new DeclaredMethodCacheEntry( targetClass );
            List<Method> methodList = cache.getMethod( key ).get( methodName );
            if ( methodList != null )
            {
                methods.addAll( methodList );
            }
        }

        return methods;
    }

    /**
     * This method returns the property descriptors for the given class as a Map.
     *
     * @param targetClass The class to get the descriptors for.
     * @return Map map of property descriptors for class.
     * @throws IntrospectionException on errors using {@link Introspector}.
     * @throws OgnlException          On general errors.
     */
    public Map<String, PropertyDescriptor> getPropertyDescriptors( Class<?> targetClass )
        throws IntrospectionException, OgnlException
    {
        return cache.getPropertyDescriptor( targetClass );
    }

    public void setMethodAccessor( Class<?> clazz, MethodAccessor accessor )
    {
        cache.setMethodAccessor( clazz, accessor );
    }

    public MethodAccessor getMethodAccessor( Class<?> clazz ) throws OgnlException
    {
        return cache.getMethodAccessor( clazz );
    }

    public void setPropertyAccessor( Class<?> clazz, PropertyAccessor accessor )
    {
        cache.setPropertyAccessor( clazz, accessor );
    }

    public PropertyAccessor getPropertyAccessor( Class<?> clazz ) throws OgnlException
    {
        return cache.getPropertyAccessor( clazz );
    }

    public ElementsAccessor getElementsAccessor( Class<?> clazz ) throws OgnlException
    {
        return cache.getElementsAccessor( clazz );
    }

    public void setElementsAccessor( Class<?> clazz, ElementsAccessor accessor )
    {
        cache.setElementsAccessor( clazz, accessor );
    }

    public NullHandler getNullHandler( Class<?> clazz ) throws OgnlException
    {
        return cache.getNullHandler( clazz );
    }
    
    public void setNullHandler( Class<?> clazz, NullHandler handler )
    {
        cache.setNullHandler( clazz, handler );
    }
    
    /**
     * Registers the specified {@link ClassCacheInspector} with all class reflection based internal caches. This may
     * have a significant performance impact so be careful using this in production scenarios.
     *
     * @param inspector The inspector instance that will be registered with all internal cache instances.
     */
    public void setClassCacheInspector( ClassCacheInspector inspector )
    {
        cache.setClassCacheInspector( inspector );
    }
    



}
