package com.testing.framework.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.testing.framework.stepDefinitions",
        snippets = CucumberOptions.SnippetType.CAMELCASE,
        monochrome = true,
        tags = "@active and @smoke",
        plugin = {"pretty", "pretty:target/cucumber/cucumber.txt",
                "html:target/cucumber/cucumber-reports.html",
                "json:target/cucumber/cucumber.json"}
)
public class TestRunner {
}
