package tk.wizardmerlin.operations;

import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.util.HashSet;
import java.util.Set;

public abstract class AvailabilityTestService {
    static Set<String> ISPsInChina = new HashSet<>();

    static {
        ISPsInChina.add("电信");
        ISPsInChina.add("联通");
        ISPsInChina.add("移动");
    }

    private String testerSiteUrl;
    private PhantomJSDriver phantomJSDriver;

    public abstract boolean test(String urlToTest) throws Exception;

    public String getTesterSiteUrl() {
        return testerSiteUrl;
    }

    public void setTesterSiteUrl(String testerSiteUrl) {
        this.testerSiteUrl = testerSiteUrl;
    }

    public PhantomJSDriver getPhantomJSDriver() {
        return phantomJSDriver;
    }

    public void setPhantomJSDriver(PhantomJSDriver phantomJSDriver) {
        this.phantomJSDriver = phantomJSDriver;
    }
}
