package io.jaegertracing.qe;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.0.0
 */

@Slf4j
public class MyListener implements ITestListener {
    @Override
    public void onFinish(ITestContext context) {
        //_logger.debug("Test: completed.. >> [{}]", context.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        //_logger.debug("Test: starting.. >> [{}]", context.getName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    @Override
    public void onTestFailure(ITestResult result) {
        _logger.debug("Test: failure >> [{}]", result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        _logger.debug("Test: skipping >> [{}]", result.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        _logger.debug("Test: starting >> [{}]", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        _logger.debug("Test: success >> [{}]", result.getName());
    }

}
