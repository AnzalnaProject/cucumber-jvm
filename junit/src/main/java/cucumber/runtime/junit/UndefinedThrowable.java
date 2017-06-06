package cucumber.runtime.junit;


public class UndefinedThrowable extends Throwable {
    private static final long serialVersionUID = 1L;

    public UndefinedThrowable() {
        this(NotificationLevel.STEP);
    }

    public UndefinedThrowable(NotificationLevel scenarioOrStep) {
        super(createMessage(scenarioOrStep), null, false, false);
    }

    private static String createMessage(NotificationLevel scenarioOrStep) {
        switch (scenarioOrStep) {
        case SCENARIO:
            return "This scenario has undefined steps";
        case STEP:
        default:
            return "This step is undefined";
        }
    }
}
