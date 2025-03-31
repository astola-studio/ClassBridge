package test;

import dd.astolastudio.annotation.ClassName;
import dd.astolastudio.annotation.Static;
import dd.astolastudio.ClassBridge;
import test.Example1.AppGlobals;
import test.Example1.Application;

// In this example we use the library to print the current app's package name (Android)
public class Example1 {
    public static void main(String... args) throws Exception {
        // Obtain a ClassBridge instance
        ClassBridge bridge = ClassBridge.Get();

        // Get a static instance of AppGlobals
        AppGlobals appGlobals = bridge.Static(AppGlobals.class);

        // Retrieve the initial application instance
        Application application = appGlobals.getInitialApplication();

        // Print the application's package name
        System.out.println("Package Name: " + application.getPackageName());
    }

    @ClassName("android.app.")
    interface AppGlobals {
        Application getInitialApplication();
    }

    @ClassName("android.app.")
    interface Application {
        String getPackageName();
    }
}

