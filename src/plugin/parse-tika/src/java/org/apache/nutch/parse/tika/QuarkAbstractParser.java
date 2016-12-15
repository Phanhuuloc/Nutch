package org.apache.nutch.parse.tika;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.tika.parser.AbstractParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by htngan on 11/28/2016.
 */
public abstract class QuarkAbstractParser extends AbstractParser implements Configurable {
    public static final Logger LOG = LoggerFactory.getLogger(QuarkAbstractParser.class);
    @Override
    public void setConf(Configuration conf) {

    }

    @Override
    public Configuration getConf() {
        return NutchConfiguration.create();
    }

    protected long getCrawlDelay(){
//        return (long) (getConf().getFloat("fetcher.server.delay", 5.0f) * 1000);
        return 58000;//28s
    }


   /* protected void fetchPageBody(String url, BrowserParseAction action) throws FetcherTimeOutException {
        try {
           *//* BrowserVersionFeatures[] bvf = new BrowserVersionFeatures[1];
            bvf[0] = BrowserVersionFeatures.HTMLIFRAME_IGNORE_SELFCLOSING;
            BrowserVersion bv = new BrowserVersion(
                    "Netscape", "5.0 (Windows; en-US)",
                    "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8",
                    (float) 3.6, bvf);*//*

            *//*WebDriver client = new WebClient(BrowserVersion.FIREFOX_17);
//            webClient.setJavaScriptEnabled(true);
//            WebClient client = new WebClient(BrowserVersion.CHROME);
            client.getOptions().setJavaScriptEnabled(true);
            client.getOptions().setRedirectEnabled(true);
            client.getOptions().setThrowExceptionOnScriptError(true);
            client.getOptions().setCssEnabled(true);
            client.getOptions().setUseInsecureSSL(true);
            client.getOptions().setThrowExceptionOnFailingStatusCode(false);
            client.setAjaxController(new NicelyResynchronizingAjaxController());
            HtmlPage page = client.getPage(url);
            client.waitForBackgroundJavaScript(5 * 1000);
            return Jsoup.parse(page.getWebResponse().getContentAsStream(), "UTF-8", url);*//*
//            return Jsoup.connect(url).get();
           *//* ChromeDriverManager.getInstance().setup();
            ChromeOptions options = new ChromeOptions();
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            options.addArguments("--no-sandbox --user-data-dir");
            capabilities.setCapability("chromeOptions", options);
            ChromeDriverService service =
                    new ChromeDriverService.Builder().withWhitelistedIps("").withVerbose(true).build();*//*
//            System.setProperty("webdriver.gecko.driver","/usr/bin/firefox");
            String firefoxDriver = getConf().get("firefox.browser.driver");
            if(StringUtils.isBlank(firefoxDriver)){
                throw new IllegalStateException("You must define firefox driver");
            }
            System.setProperty("webdriver.gecko.driver",firefoxDriver);
            FirefoxDriverManager.getInstance().setup();
            WebDriver driver = new FirefoxDriver();
            WebDriverWait wait = new WebDriverWait(driver,getCrawlDelay());
            driver.get(url);
//            By searchInput = By.className("listing");
//            wait.until(
//                    ExpectedConditions.presenceOfElementLocated(searchInput));
            action.parse(driver,wait);
            driver.quit();
//            return null;
        } catch (Exception e){
            throw new FetcherTimeOutException(e);
        }
    }*/
}
