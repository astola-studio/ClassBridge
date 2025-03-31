package dd.astolastudio; 

import dd.astolastudio.annotation.ClassName; 
import dd.astolastudio.annotation.Static; 
import java.lang.reflect.Constructor; 
import java.lang.reflect.InvocationHandler; 
import java.lang.reflect.Method; 
import java.lang.reflect.Proxy; 
import java.lang.reflect.Field; 
import java.lang.annotation.Annotation; 
import java.util.Arrays; 
import java.util.Collection; 
import java.util.List; 
import java.util.ArrayList; 

public class ClassBridge
{
	ClassLoader classLoader; 

	ClassBridge(ClassLoader externalClassLoader)
	{
		this.classLoader = externalClassLoader; 
	}

	public static ClassBridge Get()
	{
		return Get(null); 
	}

	public static ClassBridge Get(ClassLoader cl)
	{
		return new ClassBridge(cl != null ? cl : ClassBridge.class.getClassLoader()); 
	}

	public <T> T Static(Class<T> cls) throws Exception
	{
		return createProxy(cls, null, null); 
	}

	public <T> T New(Class<T> cls, Object[] args) throws Exception
	{
		return createProxy(cls, null, args); 
	}

	public <T> T New(Object instance, Class<T> cls) throws Exception
	{
		return createProxy(cls, instance, null); 
	}

	<T> T createProxy(Class<T> cls, Object instance, Object[] args) throws Exception
	{
		Class<?> realClass = getRealClass(cls); 
		if (realClass == null)
			throw new IllegalStateException("@ClassName(\"...\") annotation missing."); 

		if (args != null)
		{
			args = fixArgs(args); 
			Constructor<?> constructor = findMatchingConstructor(realClass, args); 
			if (constructor == null)
				throw new NoSuchMethodException("No Such Constructor Found !"); 
			instance = constructor.newInstance(args); 
		}
		
		return (T) Proxy.newProxyInstance(
			cls.getClassLoader(),
			new Class[]{cls},
			new DynamicInvocationHandler(instance, realClass)
		); 
	}

	public static Object GetRealObject(Object inst)
	{
		if (inst != null && Proxy.isProxyClass(inst.getClass()))
		{
			InvocationHandler h = Proxy.getInvocationHandler(inst); 
			if (h instanceof DynamicInvocationHandler)
				return ((DynamicInvocationHandler)h).targetInstance; 
		}
		return inst; 
	}

	Class<?> getRealClass(Class<?> abstractClass, Annotation...annotations) throws Exception
	{
		ClassName annotation = null; 
		if (annotations != null && annotations.length > 0)
			for (Annotation a : annotations)
				if (a.annotationType().equals(ClassName.class))
				{
					if (annotation != null)
						throw new IllegalStateException("More than one @ClassName annotations found !"); 
					annotation = (ClassName) a; 
				}
		
		if (annotation == null)
			annotation = abstractClass.getAnnotation(ClassName.class); 

		if (annotation == null)
			return null;

		String cls = annotation.value();
		if (cls.endsWith("."))
			cls += abstractClass.getSimpleName();
		else if (cls.endsWith("*"))
			cls = cls.substring(0, cls.length() - 1) + abstractClass.getSimpleName();

		return classLoader.loadClass(cls);
	}

	Constructor<?> findMatchingConstructor(Class<?> realClass, Object[] args) throws Exception
	{
		Constructor<?> c = null; 
		for (Constructor<?> ctor : realClass.getDeclaredConstructors())
		{
			Class<?>[] paramTypes = ctor.getParameterTypes(); 
			if (paramTypes.length == args.length)
			{
				boolean found = true; 
				for (int i = 0; i < args.length; i++)
				{
					Class<?> a = findClass(args[i]); 
					Class<?> b = paramTypes[i]; 
					if (a != null && !a.equals(b))
					{
						found = false; 
						break; 
					}
				}

				if (found)
				{
					if (c != null)
						throw new IllegalStateException("Found multiple matching constructors."); 
					c = ctor; 
				}
			}
		}
		
		if (c != null)
			c.setAccessible(true);
		
		return c; 
	}

	Class<?> findClass(Object o) throws Exception
	{
		Class<?> c = o == null ? null : getRealClass(o.getClass()); 
		return c != null ? c : o == null ? null : o.getClass(); 
	}

	public class DynamicInvocationHandler implements InvocationHandler
	{
		final Object targetInstance; 
		final Class<?> realClass; 

		public DynamicInvocationHandler(Object targetInstance, Class<?> realClass)
		{
			this.targetInstance = targetInstance; 
			this.realClass = realClass; 
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
		{
			Object res; 

			if (method.isAnnotationPresent(dd.astolastudio.annotation.Field.class))
				res = handleFieldMethod(method, args); 
			else
				res = handleInstanceMethod(method, args); 

			if (getRealClass(method.getReturnType()) != null)
				return fixObject(method.getReturnType(), res); 

			return res; 
		}

		Object handleFieldMethod(Method method, Object[] args) throws Exception
		{
			String fieldName = method.getName().replaceFirst("^(get_|set_)", ""); 
			Field field = realClass.getDeclaredField(fieldName); 
			field.setAccessible(true);
			
			if (args == null || args.length == 0)
				return field.get(targetInstance); 
			else if (args.length > 1)
				throw new IllegalStateException(method + " has more than 1 parameters."); 
			else
			{
				field.set(targetInstance, fixArgs(args[0])[0]); 
				return null; 
			}
		}

		Object handleInstanceMethod(Method method, Object[] args) throws Exception
		{
			Object inst = method.isAnnotationPresent(Static.class) ? null : targetInstance; 

			Class<?>[] paramTypes2 = method.getParameterTypes(); 
			Annotation[][] annotations = method.getParameterAnnotations(); 

			if (paramTypes2 != null)
				for (int i = 0; i < paramTypes2.length; i++)
				{
					Class<?> c = getRealClass(paramTypes2[i], annotations[i]); 
					paramTypes2[i] = c != null ? c : paramTypes2[i]; 
				}

			Method realMethod = null; 
			for (Method m : findMethodsByName(realClass, method.getName()))
				if (Equals(m.getParameterTypes(), paramTypes2))
					if (realMethod != null &&
						! Equals(realMethod.getParameterTypes(), m.getParameterTypes())) // skip override
							throw new IllegalStateException("Found multiple matching methods."); 
					else
						realMethod = m; 
			
			if (realMethod == null)
				throw new NoSuchMethodError(method.getName()); 

			realMethod.setAccessible(true); 
			return realMethod.invoke(inst, fixArgs(args)); 
		}
		

		List<Method> findMethodsByName(Class<?> cls, String name)
		{
			ArrayList<Method> al = new ArrayList < >(); 
			if (cls != null)
			{
				for (Method m : cls.getDeclaredMethods())
					if (m.getName().equals(name))
						al.add(m); 

				if (cls.getInterfaces() != null)
					for (Class<?> i : cls.getInterfaces())
						al.addAll(findMethodsByName(i, name)); 

				if (!cls.equals(java.lang.Object.class))
					al.addAll(findMethodsByName(cls.getSuperclass(), name)); 
			}
			return al; 
		}

		boolean Equals(Class<?>[] paramTypes, Class<?>[] paramTypes2)
		{
			int len = paramTypes == null ? 0 : paramTypes.length; 
			int len2 = paramTypes2 == null ? 0 : paramTypes2.length; 

			if (len == len2)
			{
				if (len > 0)
					for (int i = 0; i < len; i++)
					{
						Class<?> a = paramTypes2[i]; 
						Class<?> b = paramTypes[i]; 

						if (!a.equals(b))
							return false;
					}

				return true; 
			}
			return false; 
		}
	}

	Object fixObject(Class<?> proxy, Object o) throws Exception
	{
		Class<?> real = getRealClass(proxy); 
		Class<?> cls = o == null ? null : o.getClass(); 
		if (cls != null && real.isAssignableFrom(cls))
			o = Proxy.newProxyInstance(
				proxy.getClassLoader(),
				new Class[]{proxy},
				new DynamicInvocationHandler(o, real)
			);
		
		return o; 
	}

	Object[] fixArgs(Object...args)
	{
		if (args != null)
			for (int i = 0; i < args.length; i++)
				args[i] = GetRealObject(args[i]);
		
		return args; 
	}

}
