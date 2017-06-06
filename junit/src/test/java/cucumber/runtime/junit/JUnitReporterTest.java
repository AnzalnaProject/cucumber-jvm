package cucumber.runtime.junit;

import cucumber.api.PendingException;
import cucumber.api.Result;
import cucumber.runner.EventBus;
import cucumber.runtime.junit.JUnitReporter.EachTestNotifier;
import cucumber.runtime.junit.PickleRunners.PickleRunner;
import gherkin.pickles.PickleStep;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class JUnitReporterTest {

    private JUnitReporter jUnitReporter;
    private RunNotifier runNotifier;

    @Test
    public void test_case_started_fires_test_started_for_pickle() {
        createNonStrictReporter();
        PickleRunner pickleRunner = mockPickleRunner(Collections.<PickleStep>emptyList());
        runNotifier = mock(RunNotifier.class);
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        jUnitReporter.handleTestCaseStarted();

        verify(runNotifier).fireTestStarted(pickleRunner.getDescription());
    }

    @Test
    public void test_step_started_fires_test_started_for_step() {
        createNonStrictReporter();
        PickleStep runnerStep = mockStep();
        PickleRunner pickleRunner = mockPickleRunner(runnerSteps(runnerStep));
        runNotifier = mock(RunNotifier.class);
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);

        jUnitReporter.handleStepStarted(runnerStep);

        verify(runNotifier).fireTestStarted(pickleRunner.describeChild(runnerStep));
    }

    @Test
    public void test_step_finished_fires_only_test_finished_for_passed_step() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        jUnitReporter.stepNotifier = new EachTestNotifier(runNotifier, description);
        Result result = mockResult(Result.Type.PASSED);

        jUnitReporter.handleStepResult(result);

        verify(runNotifier).fireTestFinished(description);
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_skipped_step() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        jUnitReporter.stepNotifier = new EachTestNotifier(runNotifier, description);
        Result result = mockResult(Result.Type.SKIPPED);

        jUnitReporter.handleStepResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertTrue(failure.getException() instanceof SkippedThrowable);
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_pending_step_in_non_strict_mode() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        jUnitReporter.stepNotifier = new EachTestNotifier(runNotifier, description);
        Result result = mockResult(Result.Type.PENDING);
        Throwable exception = new PendingException();
        when(result.getError()).thenReturn(exception);

        jUnitReporter.handleStepResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_pending_step_in_strict_mode() {
        createStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        jUnitReporter.stepNotifier = new EachTestNotifier(runNotifier, description);
        Result result = mockResult(Result.Type.PENDING);
        Throwable exception = new PendingException();
        when(result.getError()).thenReturn(exception);

        jUnitReporter.handleStepResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void test_step_finished_fires_assumption_failed_and_test_finished_for_undefined_step_in_non_strict_mode() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        jUnitReporter.stepNotifier = new EachTestNotifier(runNotifier, description);
        Result result = mockResult(Result.Type.UNDEFINED);

        jUnitReporter.handleStepResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertTrue(failure.getException() instanceof UndefinedThrowable);
    }

    @Test
    public void test_step_finished_fires_failure_and_test_finished_for_undefined_step_in_strict_mode() {
        createStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        jUnitReporter.stepNotifier = new EachTestNotifier(runNotifier, description);
        Result result = mockResult(Result.Type.UNDEFINED);

        jUnitReporter.handleStepResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertTrue(failure.getException() instanceof UndefinedThrowable);
    }

    @Test
    public void test_step_finished_fires_failure_and_test_finished_for_failed_step() {
        createNonStrictReporter();
        createDefaultRunNotifier();
        Description description = mock(Description.class);
        jUnitReporter.stepNotifier = new EachTestNotifier(runNotifier, description);
        Result result = mockResult(Result.Type.FAILED);
        Throwable exception = mock(Throwable.class);
        when(result.getError()).thenReturn(exception);

        jUnitReporter.handleStepResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void test_case_finished_fires_only_test_finished_for_passed_step() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Result result = mockResult(Result.Type.PASSED);

        jUnitReporter.handleTestCaseResult(result);

        verify(runNotifier).fireTestFinished(description);
    }

    @Test
    public void test_case_finished_fires_assumption_failed_and_test_finished_for_skipped_step() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Result result = mockResult(Result.Type.SKIPPED);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertTrue(failure.getException() instanceof SkippedThrowable);
    }

    @Test
    public void test_case_finished_fires_assumption_failed_and_test_finished_for_pending_step_in_non_strict_mode() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Result result = mockResult(Result.Type.PENDING);
        Throwable exception = new PendingException();
        when(result.getError()).thenReturn(exception);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void test_case_finished_fires_assumption_failed_and_test_finished_for_pending_step_in_strict_mode() {
        createStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Result result = mockResult(Result.Type.PENDING);
        Throwable exception = new PendingException();
        when(result.getError()).thenReturn(exception);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void test_case_finished_fires_assumption_failed_and_test_finished_for_undefined_step_in_non_strict_mode() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Result result = mockResult(Result.Type.UNDEFINED);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestAssumptionFailed(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertTrue(failure.getException() instanceof UndefinedThrowable);
    }

    @Test
    public void test_case_finished_fires_failure_and_test_finished_for_undefined_step_in_strict_mode() {
        createStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Result result = mockResult(Result.Type.UNDEFINED);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertTrue(failure.getException() instanceof UndefinedThrowable);
    }

    @Test
    public void test_case_finished_fires_failure_and_test_finished_for_failed_step() {
        createNonStrictReporter();
        Description description = mock(Description.class);
        createRunNotifier(description);
        Result result = mockResult(Result.Type.FAILED);
        Throwable exception = mock(Throwable.class);
        when(result.getError()).thenReturn(exception);

        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());
        verify(runNotifier).fireTestFinished(description);

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void result_with_assumption_violated() {
        createStrictReporter();
        Result result = mockResult(Result.Type.PENDING);
        Throwable exception = new AssumptionViolatedException("Oops");
        when(result.getError()).thenReturn(exception);

        PickleStep runnerStep = mockStep();
        Description runnerStepDescription = stepDescription(runnerStep);
        PickleRunner pickleRunner = mockPickleRunner(runnerSteps(runnerStep));
        when(pickleRunner.describeChild(runnerStep)).thenReturn(runnerStepDescription);
        Description pickleRunnerDescription = mock(Description.class);
        when(pickleRunner.getDescription()).thenReturn(pickleRunnerDescription);


        Description description = mock(Description.class);
        createRunNotifier(description);

        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);
        jUnitReporter.handleStepStarted(runnerStep);
        jUnitReporter.handleStepResult(result);
        jUnitReporter.handleTestCaseResult(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier, times(2)).fireTestAssumptionFailed(failureArgumentCaptor.capture());

        List<Failure> failure = failureArgumentCaptor.getAllValues();
        assertEquals(runnerStepDescription, failure.get(0).getDescription());
        assertEquals(exception, failure.get(0).getException());

        assertEquals(pickleRunnerDescription, failure.get(1).getDescription());
        assertEquals(exception, failure.get(1).getException());
    }

    private Result mockResult(Result.Type status) {
        Result result = mock(Result.class);
        when(result.getStatus()).thenReturn(status);
        for (Result.Type type : Result.Type.values()) {
            when(result.is(type)).thenReturn(type == status);
        }
        return result;
    }

    private PickleRunner mockPickleRunner(List<PickleStep> runnerSteps) {
        PickleRunner pickleRunner = mock(PickleRunner.class);
        when(pickleRunner.getDescription()).thenReturn(mock(Description.class));
        for (PickleStep runnerStep : runnerSteps) {
            Description runnerStepDescription = stepDescription(runnerStep);
            when(pickleRunner.describeChild(runnerStep)).thenReturn(runnerStepDescription);
        }
        return pickleRunner;
    }

    private List<PickleStep> runnerSteps(PickleStep step) {
        List<PickleStep> runnerSteps = new ArrayList<PickleStep>();
        runnerSteps.add(step);
        return runnerSteps;
    }

    private Description stepDescription(PickleStep runnerStep) {
        return Description.createTestDescription("", runnerStep.getText());
    }

    private PickleStep mockStep() {
        String stepName = "step name";
        return mockStep(stepName);
    }

    private PickleStep mockStep(String stepName) {
        PickleStep step = mock(PickleStep.class);
        when(step.getText()).thenReturn(stepName);
        return step;
    }

    private void createDefaultRunNotifier() {
        createRunNotifier(mock(Description.class));
    }

    private void createRunNotifier(Description description) {
        runNotifier = mock(RunNotifier.class);
        PickleRunner pickleRunner = mock(PickleRunner.class);
        when(pickleRunner.getDescription()).thenReturn(description);
        jUnitReporter.startExecutionUnit(pickleRunner, runNotifier);
    }

    private void createStrictReporter() {
        createReporter(true);
    }

    private void createNonStrictReporter() {
        createReporter(false);
    }

    private void createReporter(boolean strict) {
        jUnitReporter = new JUnitReporter(mock(EventBus.class), strict, new JUnitOptions(Collections.<String>emptyList()));
    }

}
