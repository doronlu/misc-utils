package net.katros.services.utils;

import java.lang.reflect.Method;

/**
 * Utilities related to reflection.
 * 
 * @author doron
 */
public abstract class ReflectionUtils
{
	/**
	 * Proper use of this class is
	 *     String name = (new ReflectionUtils.MethodNameHelper(){}).getFullMethodName();
	 * or
	 *     Method method = (new ReflectionUtils.MethodNameHelper(){}).getMethod();
	 * The anonymous class allows easy access to the method name of the enclosing scope.
	 * 
	 * Heavily based on: http://stackoverflow.com/questions/3142114/get-name-of-enclosing-method
	 */
	public static class MethodNameHelper
	{
		public String getFullMethodName()
		{
			final Method myMethod = this.getClass().getEnclosingMethod();
			if (myMethod == null)
			{
				// This happens when we are non-anonymously instantiated
				return this.getClass().getSimpleName() + ".unknown()"; // return a less useful string
			}
			final String className = myMethod.getDeclaringClass().getSimpleName();
			return className + "." + myMethod.getName() + "()";
		}

		public Method getMethod()
		{
			return this.getClass().getEnclosingMethod();
		}

		public String getMethodName()
		{
			return this.getClass().getEnclosingMethod().getName();
		}
	}
}