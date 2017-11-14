package tk.wizardmerlin.operations;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CeCloud360 extends AvailabilityTestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CeCloud360.class);

    @Override
    public boolean test(String urlToTest) throws Exception {
        PhantomJSDriver phantom = getPhantomJSDriver();
        phantom.manage().window().setSize(new Dimension(1028, 1024));
        LOGGER.info("testing " + urlToTest);
        phantom.get(getTesterSiteUrl());
        phantom.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        LOGGER.info("title: " + phantom.getTitle());

        WebElement inputElement = phantom.findElement(By.id("domain"));
        inputElement.click();
        inputElement.sendKeys(urlToTest);
        inputElement.submit();
        LOGGER.info("waiting for speed test results");
//        Thread.sleep(30*1000L);
        Thread.sleep(5 * 1000L);
        List<WebElement> rows = phantom.findElements(By.tagName("tr"));
        StringBuilder sb = new StringBuilder();
        for (WebElement row : rows) {
            sb.append(row.getText()).append("\n");
        }
        String text = sb.toString();
        LOGGER.debug(text);
        Pattern p = Pattern.compile("(?<province>\\S+)\\s+(?<city>\\S+)\\s+(?<ISP>\\S+)\\s+(?<IP>\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s+(?<serverCountry>\\S+)\\s+(?<httpStatus>\\d{3})\\s+(?<totaltimeInMs>[\\d.]+)ms");
        Matcher m = p.matcher(text);

        List<AvailabilityTestResult> results = new ArrayList<>();
        while (m.find()) {
            LOGGER.debug(m.group("province"));
            LOGGER.debug(m.group("city"));
            LOGGER.debug(m.group("ISP"));
            LOGGER.debug(m.group("IP"));
            LOGGER.debug(m.group("serverCountry"));
            LOGGER.debug(m.group("httpStatus"));
            LOGGER.debug(m.group("totaltimeInMs"));
            LOGGER.debug("=========================");
            AvailabilityTestResult result = new AvailabilityTestResult()
                    .province(m.group("province"))
                    .city(m.group("city"))
                    .ISP(m.group("ISP"))
                    .ipAddr(m.group("IP"))
                    .httpStatus(Integer.valueOf(m.group("httpStatus")))
                    .totalTimeInMs(Double.valueOf(m.group("totaltimeInMs")));
            results.add(result);
        }
        for (AvailabilityTestResult result : results) {
            if (ISPsInChina.contains(result.getISP()) && result.getHttpStatus() == 200) {
                return true;
            }
        }
        return false;
    }
}
