package test;

import dd.astolastudio.ClassBridge;
import dd.astolastudio.annotation.ClassName;
import dd.astolastudio.annotation.Field;
import dd.astolastudio.annotation.Static;

// In this example, we are trying to access some inaccessible methods and fields.
public class Example2 {
    String message;

    private Example2(String message) {
        this.message = message;
    }

    private void printMessage() {
        System.out.println(message);
    }

    private Main2 getRunnable() {
        return new Main2();
    }

    private void test(Main3 obj) {
        System.out.println(obj);
    }
	
	private class Main2 implements Runnable {
        @Override
        public void run() {
            System.out.println("Parameter: " + message);
        }
    }

    class Main3 {}
	
    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            System.out.println("init();");

            // Creating a static instance of MyMain
            ClassBridge.Get().Static(MyMain.class).main(new String[]{"OK"});

            // Creating a new instance dynamically
            MyMain dynamicInstance = ClassBridge.Get().New(MyMain.class, new Object[]{"inside printMessage()"});
            dynamicInstance.printMessage();

            // Modifying field value dynamically
            dynamicInstance.set_message("set_message() called");
            dynamicInstance.printMessage();

            // Calling a method that returns an instance of MyMain2
            dynamicInstance.getRunnable().run();
        } else {
            System.out.println("inside main()");
        }
    }

    @ClassName("test.Example2")
    public static interface MyMain {

        @Field String get_message();

        @Field void set_message(String s);

        void printMessage();

        MyMain2 getRunnable();

        void test(MyMain3 obj);

        @Static void main(String... args);

        @ClassName("test.Example2$Main2")
        public static interface MyMain2 extends Runnable {}

        @ClassName("test.Example2$Main3")
        interface MyMain3 {}
    }
}

