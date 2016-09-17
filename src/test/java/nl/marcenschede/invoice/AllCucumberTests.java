package nl.marcenschede.invoice;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        tags = "~@not_implemented",
        format = {"pretty", "html:target/cucumber"},
        glue = {"nl.marcenschede.invoice.glue"},
        features = "classpath:features/",
        strict = true
)
public class AllCucumberTests {
}
