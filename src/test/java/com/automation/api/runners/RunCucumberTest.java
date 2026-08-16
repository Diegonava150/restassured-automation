package com.automation.api.runners;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Entry point for the suite.
 *
 * <p>Replaces the old {@code @RunWith(Cucumber.class)} runner, which is JUnit 4 only. That
 * runner is why the project depended on JUnit 4 and JUnit 5 simultaneously while declaring
 * neither engine, and why {@code mvn test} was one surefire default away from silently
 * running nothing at all.
 *
 * <p>No tag filter. The previous runner was pinned to {@code "@active and @smoke"}, so any
 * scenario written without both tags would never run and nobody would be told. If a scenario
 * is in this repository it runs; if it should not run, it should not be here.
 */
@Suite
@IncludeEngines("cucumber")
// Package selector, not @SelectClasspathResource. The resource selector works but Cucumber
// warns against it for selecting a whole directory, because it resolves to a single resource
// rather than the features within it.
@SelectPackages("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.automation.api.steps")
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty,"
                + "html:target/cucumber/cucumber-report.html,"
                + "json:target/cucumber/cucumber.json,"
                + "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
public class RunCucumberTest {}
