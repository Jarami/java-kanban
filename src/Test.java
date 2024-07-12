import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Test {

    static class FailError extends Error {
        public FailError(String s) {
            super(s);
        }
    }

    private int passed;
    private int failed;
    private int errors;
    private List<String> messages;

    public Test() {
        passed = 0;
        failed = 0;
        errors = 0;
        messages = new ArrayList<>();
    }

    public void run() {
        for (Method method : getTestMethods()) {
            invokeTestMethod(method);
        }

        printTestSummary();
    }

    protected List<Method> getTestMethods() {
        List<Method> testMethods = new ArrayList<>();
        for (Method method : getClass().getMethods()) {
            if (method.getName().startsWith("test")) {
                testMethods.add(method);
            }
        }
        return testMethods;
    }

    protected void invokeTestMethod(Method method) {
        try {
            method.invoke(this);
            passed++;

        } catch(Exception e) {

            if (e.getCause() != null && e.getCause().getClass() == FailError.class){
                handleFail(method, e);
                failed++;

            } else {
                handleException(method, e);
                errors++;

            }
        }
    }

    protected void handleFail(Method method, Throwable e) {
        String message = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
        messages.add("Провал в методе " + method.getName() + '\n' + "  " + message);
    }

    protected void handleException(Method method, Throwable e) {
        String message = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
        messages.add("Ошибка в методе " + method.getName() + '\n' + "  " + message);
    }

    protected void printTestSummary() {

        PrintStream console = failed + errors > 0 ? System.err : System.out;

        console.println("================================================================");
        for (String message : messages) {
            console.println(message + '\n');
        }
        console.println("Выполнено тестов: " + (passed + failed + errors) + ", провалов: " + failed + ", ошибок: " + errors);
        console.println("================================================================");
    }

    public void assertEquals(Object o1, Object o2, String message) {
        if (!o1.equals(o2)) {
            throw new FailError(message);
        }
    }

    public void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new FailError(message);
        }
    }

    public void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new FailError(message);
        }
    }

    public void assertNotEquals(Object o1, Object o2, String message) {
        if (o1.equals(o2)) {
            throw new FailError(message);
        }
    }

    public void assertNull(Object o, String message) {
        if (o != null) {
            throw new FailError(message);
        }
    }

    public void assertNotNull(Object o, String message) {
        if (o == null) {
            throw new FailError(message);
        }
    }

    public <T> void assertEmpty(Collection<T> collection, String message) {
        if (!collection.isEmpty()) {
            throw new FailError(message);
        }
    }
}
