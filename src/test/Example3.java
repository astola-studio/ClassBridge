package test;

import dd.astolastudio.ClassBridge;
import dd.astolastudio.annotation.ClassName;

// Getting value from a dynamic class name
public class Example3
{
	public static void main(String...args) throws Exception{
		// Prepare The Bridge
		ClassBridge bridge = ClassBridge.Get();
		
		// Get the instance
		AppGlobals proxy = bridge.Static(decideClassName(), AppGlobals.class);
		
		// Use it accordingly
		System.out.println(proxy.getInitialApplication().getPackageName());
	}

	private static String decideClassName()
	{
		// Here we decide the class name according to our need
		// In some cases, the classes are not the same when we load them
		// dynamically, so with this new approach, we dont just rely on
		// Our @ClassName method, because thats just for constant classes.
		return "android.app.AppGlobals";
	}
	
	interface AppGlobals{
		Application getInitialApplication();
	}
	
	@ClassName("android.app.")
	interface Application{
		String getPackageName();
	}
}
