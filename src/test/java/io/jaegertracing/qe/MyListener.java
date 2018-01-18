package io.jaegertracing.qe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.0.0
 */
public class MyListener implements ITestListener {
    private static final Logger logger = LoggerFactory.getLogger(MyListener.class);

    @Override
    public void onFinish(ITestContext context) {
        logger.debug("Test: completed.. >> [{}]", context.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        logger.debug("Test: starting.. >> [{}]", context.getName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.warn("Test: failure: [{}]", result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.info("Test: skipping: [{}]", result.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("Test: starting: [{}]", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.debug("Test: success: [{}]", result.getName());
    }

}
