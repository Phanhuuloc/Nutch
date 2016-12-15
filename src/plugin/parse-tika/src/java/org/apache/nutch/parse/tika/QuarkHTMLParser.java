package org.apache.nutch.parse.tika;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.nutch.parse.tika.model.Category;
import org.apache.nutch.parse.tika.model.CrawledBrand;
import org.apache.nutch.parse.tika.model.Product;
import org.apache.nutch.parse.tika.model.Quark;
import org.apache.nutch.parse.tika.model.QuarkCountry;
import org.apache.nutch.parse.tika.model.QuarkTranslation;
import org.apache.nutch.parse.tika.model.ValueBand;
import org.apache.nutch.parse.tika.model.countrycode.Address;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

/**
 * Created by laphuoc on 9/23/2016.
 */
public class QuarkHTMLParser extends QuarkProductListParser implements Constants {
    public static final Logger LOG = LoggerFactory.getLogger(QuarkHTMLParser.class);

    private static final Set<MediaType> SUPPORTED_TYPES = Collections.unmodifiableSet(new HashSet<MediaType>(Arrays.asList(MediaType.text("html"),
            MediaType.application("xhtml+xml"), MediaType.application("vnd.wap.xhtml+xml"), MediaType.application("x-asp"))));

    /**
     * Main for testing
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new QuarkHTMLParser().parseTradeFord("http://vietnam.tradeford.com/vn365844/cashew-peeling-machine_p431384.html");
//        new QuarkHTMLParser().readAndParseTobocSite("http://www.toboc.com/products/adhesives-sealers-629-1.aspx",null,null);
//        File file = new File("C:\\test1");
//        File file = new File("D:\\tmp\\webcrawler\\test.html");
//        DataInputStream in = new DataInputStream(new FileInputStream(file));
//        QuarkHTMLParser quarkHTMLParser = new QuarkHTMLParser();
//        quarkHTMLParser.readAndParseMadeInChinaSite("http://www.made-in-china.com/Agriculture-Food-Catalog/Bean-Preparation.html", in, new Metadata());
    }

    @Override
    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }

    @Override
    public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {

        parseIndiaMart("", stream, metadata);
    }

    /**
     * Parse IndiaMart.com site
     *
     * @param url
     * @param stream
     * @param metadata
     */
    public void parseIndiaMart(String url,InputStream stream,Metadata metadata){
        Document doc = null;
        try {
            doc = Jsoup.parse(stream,"UTF-8",url);
            List<Product> products = parseIndiaMartProducts(url,doc);
            if(products!=null&&!products.isEmpty()){
                saveProductsToDb(url,products,INDIAMART_ID);
            }
        } catch (Exception e) {
            LOG.error("",e);
        }

    }
    public List<Product> parseIndiaMartProducts(String url,Document document){
        List<Product> products = new ArrayList<>();
        Elements productElements = document.select("div[data-glid]");
        if(productElements!=null&&!productElements.isEmpty()){
            LOG.info("Number of products= " + productElements.size());
            for (Element p:productElements){
                Product product = new Product();
                product.setModel_name_en(Constants.INDIAMART_SITE);
                try{
                    // product name
                    String name = p.select(".ldf").first().text();
                    if (StringUtils.isBlank(name)) {
                        LOG.error("Product name empty, ignore");
                        continue;
                    }
                    product.setName(name);
                } catch (Exception e){
                    LOG.error("Product name empty, ignore");
                    continue;
                }
                // Set link_external
                try {
                    String linkExternal = p.select("a").first().attr("href");
                    if (StringUtils.isBlank(linkExternal)) {
                        LOG.error("Inner product, ignore");
                        continue;
                    }
                    if (!linkExternal.contains("http")) {
                        linkExternal = linkExternal.replace("//", "http://");
                    }
                    product.setLink_external(linkExternal);
                } catch (Exception e){
                    LOG.error("link external empty, ignore");
                    continue;
                }
                try {
                    // product image url
                    String imgUrl;
                    imgUrl = p.select("img").first().absUrl("src");
                    if(imgUrl.contains("z.gif")) {
                        imgUrl = p.select("img").first().absUrl("data-original");
                    }
                    if (imgUrl == null || imgUrl.isEmpty()) {
                        LOG.error("Doesn't have photo, ignore");
                        continue;
                    }
                    product.setImageUrl(imgUrl);
                } catch (Exception e){
                    LOG.error("Doesn't have photo, ignore");
                    continue;
                }
                try {
                    // product price
                    String price = p.select(".prc").first().ownText()+"/"+p.select(".prc").first().select("span").text();
                    product.setPrice(price);
                } catch (Exception e){
                    LOG.info("Empty price");
                }
                try {
                    // company name
                    String companyName = p.select(".lcname").first().text();
                    if (StringUtils.isBlank(companyName)) {
                        LOG.error("Doesn't have company, ignore");
                        continue;
                    }
                    product.setCompany(companyName);
                } catch (Exception e){
                    LOG.error("Doesn't have company, ignore");
                    continue;
                }
                /*try {
                    //temporay disable get log, becuase it blocks our IP
                        *//*Element companyLogoEle = Jsoup.connect(product.getLink_external()).get().select("img[alt=" + product.getCompany() + "]").first();
                        if (companyLogoEle != null) {
                            product.setCompanyLogo(companyLogoEle.attr("data-dataimg"));
                        }*//*
                } catch (Exception e) {
                    LOG.error("", e);
                }*/
                try {
                    // Location
                    Element locationEle = p.select("span[data-rlocation]").first();
                    if (locationEle != null) {
                        product.setAddress(locationEle.ownText());
                    }
                } catch (Exception e){

                }
                try {
                    // phone
                    String phone = p.select("span.ls_co").first().select("span").first().text();
                    if (phone != null) {
                        product.setPhone(phone);
                    }
                } catch (Exception e){
                    LOG.error("Doesn't have company phone");
                }

                // country code
                String defCode = "IN";
                String defName = "India";
                try {
                    Address country = ParserUtils.getCountry(product.getAddress());
                    if (country == null) {
                        product.setCountry_code(defCode);//India
                        product.setCountry(defName);
                    } else {
                        product.setCountry_code(country.getShort_name());
                        product.setCountry(country.getLong_name());
                    }
                } catch (Exception e) {
                    product.setCountry_code(defCode);//India
                    product.setCountry(defName);
                    LOG.error("", e);
                }

                products.add(product);

            }
        }
        return products;
    }
   /* public void parseIndiaMart(String url, InputStream stream, Metadata metadata) {
        LOG.info("parsing indiamart.com ...");
        try {
            // Using Jsoup to scrape the html content
            Document doc = Jsoup.parse(stream, "UTF-8", url);
            Elements productElements = doc.select("div[itemscope]");

            if (productElements != null && productElements.size() > 0) {
                LOG.info("Number of products= " + productElements.size());
                List<Product> products = new ArrayList<Product>();

                for (Element p : productElements) {
                    Product product = new Product();
                    product.setModel_name_en(Constants.INDIAMART_SITE);
                    try{
                        // product name
                        String name = p.select("a.product-name").first().text();
                        if (StringUtils.isBlank(name)) {
                        	LOG.info("Product name is empty");
                            continue;
                        }
                        product.setName(name);
                    } catch (Exception e){
                    	LOG.error("", e);
                        continue;
                    }
                   *//* // product name
                    Element name = p.select("a.product-name").first();
                    if (name == null || name.text() == null || name.text().isEmpty()) {
                        continue;
                    }
//                    LOG.info("Product name= " + name.text());
                    product.setName(name.text());*//*
                    // Set link_external
                    try {
                        String linkExternal = p.select("a.product-name").first().attr("href");
                        if (linkExternal == null || !linkExternal.contains("indiamart.com")) {
                        	LOG.info("linkExternal name is empty");
                            continue;
                        }
                        if (!linkExternal.contains("http")) {
                            linkExternal = linkExternal.replace("//", "http://");
                        }
                        product.setLink_external(linkExternal);
                    } catch (Exception e){
                    	LOG.error("", e);
                        continue;
                    }
                    try {
                        // product image url
                        String imgUrl = "";
                        // leader-image url
                        Element img = p.select("div.leader-image>img").first();
                        if (img != null) {
                            imgUrl = img.attr("abs:src");
                            if (imgUrl.equals("http://utils.imimg.com/imsrchui/imgs/z.gif")) {
                                imgUrl = img.attr("abs:data-original");
                            }
                        } else {
                            // normal-image
                            Element normalImg = p.select("div.normal-image>img").first();
                            if (normalImg != null) {
                                imgUrl = normalImg.attr("abs:src");
                                if (imgUrl.equals("http://utils.imimg.com/imsrchui/imgs/z.gif")) {
                                    imgUrl = normalImg.attr("abs:data-original");
                                }
                            } else {
                                // star-image
                                Element starImg = p.select("div.star-image>img").first();
                                if (starImg != null) {
                                    imgUrl = starImg.attr("abs:src");
                                    if (imgUrl.equals("http://utils.imimg.com/imsrchui/imgs/z.gif")) {
                                        imgUrl = starImg.attr("abs:data-original");
                                    }
                                }
                            }
                        }
                        if (imgUrl == null || imgUrl.isEmpty()) {
                        	LOG.info("imgUrl name is empty");
                            continue;
                        }
                        product.setImageUrl(imgUrl);
                    } catch (Exception e){
                    	LOG.error("", e);
                        continue;
                    }
                    try {
                        // product price
                        Element price = p.select("div.price-wdt").first();
                        if (price != null) {
                            Element unitEle = price.select("span").first();
                            if (unitEle != null) {
                                product.setPrice(price.ownText() + " / " + unitEle.text());
                            } else {
                                product.setPrice(price.ownText());
                            }
                        }
                    } catch (Exception e){
                    	LOG.error("", e);
                    }
                    try {
                        // company name
                        Element companyName = p.select("span.company-name>span>a").first();
                        if (companyName == null || companyName.ownText() == null || companyName.ownText().isEmpty()) {
                        	LOG.info("Company name is empty");
                            continue;
                        }
                        product.setCompany(companyName.ownText());
                    } catch (Exception e){
                    	LOG.error("", e);
                        continue;
                    }
                    try {
                    	//temporay disable get log, becuase it blocks our IP
                        *//*Element companyLogoEle = Jsoup.connect(product.getLink_external()).get().select("img[alt=" + product.getCompany() + "]").first();
                        if (companyLogoEle != null) {
                            product.setCompanyLogo(companyLogoEle.attr("data-dataimg"));
                        }*//*
                    } catch (Exception e) {
                        LOG.error("", e);
                    }
                    try {
                        // Location
                        Element locationEle = p.select("span.cityLocation-grid").first();
                        if (locationEle != null) {
                            product.setAddress(locationEle.ownText());
                        }
                    } catch (Exception e){

                    }
                    try {
                        // phone
                        Element phone = p.select("span[itemprop=\"telephone\"]").first();
                        if (phone != null) {
                            product.setPhone(phone.ownText());
                        }
                    } catch (Exception e){

                    }

                    // country code
                    String defCode = "IN";
                	String defName = "India";
                    try {
                        Address country = ParserUtils.getCountry(product.getAddress());
                        if (country == null) {
                            product.setCountry_code(defCode);//India
                            product.setCountry(defName);
                        } else {
                            product.setCountry_code(country.getShort_name());
                            product.setCountry(country.getLong_name());
                        }
                    } catch (Exception e) {
                    	product.setCountry_code(defCode);//India
                        product.setCountry(defName);
                        LOG.error("", e);
                    }

                    products.add(product);
                }
                saveProductsToDb(url, products, Constants.INDIAMART_ID);
            }

        } catch (Exception e) {
            LOG.error("Exception", e);
        }
    }*/

    /**
     * Parse ExportPages.com site
     *
     * @param url
     * @param stream
     * @param metadata
     */
    public void parseExportPages(String url, InputStream stream, Metadata metadata) throws IOException {
        LOG.info("parsing exportPage.com ...");
        Document doc = Jsoup.parse(stream, "UTF-8", url);
        Elements productEles = doc.select("div.media");
        if (productEles != null) {
            List<Product> products = new ArrayList<Product>();
            for (Element p : productEles) {
                Product product = new Product();
                product.setModel_name_en(Constants.EXPORTPAGES_SITE);
                try {
                    // product company logo url
                    Element companyLogoEle = p.select("div.media-left>p>img").first();
                    if (companyLogoEle != null) {
                        String logoUrl = companyLogoEle.attr("abs:src");
                        if (logoUrl == null || logoUrl.equals(Constants.EXPORTPAGES_FULL_URL)) {
                            logoUrl = "";
                        }
                        product.setCompanyLogo(logoUrl);
                    }
                } catch (Exception e){

                }
                Element body;
                try {
                    body = p.select("div.media-body").first();
                } catch (Exception e){
                    continue;
                }
                if (body != null) {
                    try {
                        // product name
                        Element titleEle = body.select("div.row").first().select("a").first();
                        if (titleEle == null || titleEle.text() == null || titleEle.text().equals("")) {
                            continue;
                        }
                        product.setName(titleEle.text());
                    } catch (Exception e){
                        continue;
                    }
                    try {
                        // product link_external
                        Element linkExternalEle = body.select("div.row>div>h4>a").first();
                        if (linkExternalEle == null || linkExternalEle.equals("")) {
                            continue;
                        }
                        product.setLink_external(linkExternalEle.attr("href"));
                    } catch (Exception e){
                        continue;
                    }
                    try {
                        // product company name
                        Element companyEle = body.select("div.row").first().select("h5").first();
                        if (companyEle == null || companyEle.text() == null || companyEle.text().equals("")) {
                            continue;
                        }
                        product.setCompany(companyEle.text());
                    } catch (Exception e){
                        continue;
                    }
                    try {
                        // product description
                        Elements descriptionEles = body.select("ul>li");
                        if (descriptionEles != null && descriptionEles.size() > 0) {
                            StringBuffer description = new StringBuffer();
                            for (Element descriptionEle : descriptionEles) {
                                description.append(descriptionEle.text()).append("\n");
                            }
                            product.setDescription(description.toString());
                        }
                    } catch (Exception e){

                    }

                    // product image-url, company address, country-code
                    ParserUtils.setImageUrlAddressCountry(product);
                    try {
                        if (StringUtils.isEmpty(product.getImageUrl())) {
                            // timeout
                            Element img = p.select("div.media-left>p>a>img").first();
                            String imgUrl = null;
                            if (img != null) {
                                imgUrl = img.attr("abs:src");
                            }
                            if (StringUtils.isNotBlank(imgUrl)) {
                                product.setImageUrl(imgUrl);
                            } else {
                                continue;
                            }
                        }
                    } catch (Exception e){
                        continue;
                    }
                }
                products.add(product);
            }
            LOG.info("Number of products= " + products.size() + "\n");
            try {
                saveProductsToDb(url, products, Constants.EXPORTPAGES_ID);
            } catch (SQLException e) {
                LOG.error("", e);
            } catch (SolrServerException e) {
                LOG.error("", e);
            }
        }
    }

    public void readAndParseMadeInChinaSite(String url, InputStream stream, Metadata metadata) throws IOException {
        LOG.info("reading made-in-china.com ...");
        Document doc = Jsoup.parse(stream, "UTF-8", url);
//        Document doc = Jsoup.connect(url).get();
        crawlProductList(doc, new ParseAction() {
            @Override
            public void parse(String productUrl) {
                try {
                    QuarkHTMLParser.this.parseMadeInChina(productUrl);
                } catch (IOException e) {
                    LOG.error("", e);
                }
            }
        },"h2.product-name","li.item.J-item");
    }
    public void readAndParseTradeFordSite(String url, InputStream stream, Metadata metadata) throws IOException {
        LOG.info("reading www.tradeford.com ...");
        Document doc = Jsoup.parse(stream, "UTF-8", url);
//        Document doc = Jsoup.connect(url).get();
        crawlProductList(doc, new ParseAction() {
            @Override
            public void parse(String productUrl) {
                try {
                    QuarkHTMLParser.this.parseTradeFord(productUrl);
                } catch (Exception e) {
                    LOG.error("", e);
                }
            }
        }, ".search_item.clearfix");
    }

    public void readAndParseTobocSite(String url, InputStream stream, Metadata metadata) throws IOException {
        LOG.info("reading www.toboc.com ....");
        Document doc = Jsoup.parse(stream, "UTF-8", url);
//        Document doc = null;
        /*try {
            fetchPageBody(url, new BrowserParseAction() {
                @Override
                public void parse(WebDriver driver, WebDriverWait wait) {
                    By searchInput = By.className("grid3");
                    wait.until(
                            ExpectedConditions.presenceOfElementLocated(searchInput));
                    List<WebElement> productElements = driver.findElements(searchInput);
                    if (productElements != null && !productElements.isEmpty()) {
                        for (WebElement productEle : productElements) {
                            try {
                                By by = By.tagName("a");
                                String product = productEle.findElement(by).getAttribute("href");
                                LOG.info("Crawling site: " + product);
                                QuarkHTMLParser.this.parseToboc(product);
                                Thread.sleep(QuarkHTMLParser.this.getCrawlDelay());
                            } catch (Exception e) {
                                LOG.error("", e);
                            }
                        }
                    }
                }
            });
        } catch (FetcherTimeOutException e) {
            LOG.error("",e);
        }*//* LOG.info(doc.html());*/
        crawlProductList(doc, new ParseAction() {
            @Override
            public void parse(String productUrl) {
                try {
                    QuarkHTMLParser.this.parseToboc(productUrl);
                } catch (IOException e) {
                    LOG.error("", e);
                }
            }
        },".grid3.listing");

    }

    /**
     * Parse made-in-china.com site
     *
     * @param url
//     * @param stream
//     * @param metadata
     * @throws IOException
     */
    public void parseMadeInChina(String url) throws IOException {
//    public void parseMadeInChina(String url, InputStream stream, Metadata metadata) throws IOException {
        LOG.info("parsing made-in-china.com ...");
//        Document doc = Jsoup.parse(stream, "UTF-8", url);
        Document doc = Jsoup.connect(url).get();
        Element productElement = doc.select(".page-product-details").first();
        Product product = new Product();
        product.setModel_name_en(Constants.MADE_IN_CHINA_SITE_NAME);
        product.setLink_external(url);
        //parse logo
        try {
            String companyLogo = doc.select("#logoAppHolder").first().select("img").first().absUrl("src");
            product.setCompanyLogo(StringUtils.defaultString(companyLogo));
            LOG.info("companyLogo: " + companyLogo);
        } catch (Exception e) {
            // ignore exception
        	LOG.error("", e);
        }
        //parse product name
        try {
            String productName = productElement.select(".pro-name").first().select("h1").first().text();
            if (StringUtils.isBlank(productName)) {
                return;
            }
            product.setName(productName);
            LOG.info("productName: " + productName);
        } catch (Exception e) {
        	LOG.error("", e);
            return;
        }
        //parse price
        try {
            Elements priceElements = productElement.select(".price-item");
            if (priceElements != null && priceElements.size() == 2) {
                String price = productElement.select(".price-item").first().text();
                product.setPrice(StringUtils.defaultString(price));
                LOG.info("price: " + price);
            }
        } catch (Exception e) {
            // ignore exception
        	LOG.error("", e);
        }

        //parse company name
        try {
            String companyName = productElement.select(".com-name").first().text();
            if (StringUtils.isBlank(companyName)) {
                return;
            }
            product.setCompany(StringUtils.defaultString(companyName));
            LOG.info("companyName: " + companyName);
        } catch (Exception e) {
        	LOG.error("", e);
            return;
        }
        //parse location and business type
        try {
            String[] locationAndBusiness = productElement.select(".local").first().html().split("<br>");
            String location = locationAndBusiness[0];
            String business = locationAndBusiness[1];
            // country code
            String defCode = "CN";
        	String defName = "China";
            try {
                Address country = ParserUtils.getCountry(location);
                if (country == null) {
                    product.setCountry_code(defCode);
                    product.setCountry(defName);
                } else {
                    product.setCountry_code(country.getShort_name());
                    product.setCountry(country.getLong_name());
                }
            } catch (Exception e) {
            	product.setCountry_code(defCode);
                product.setCountry(defName);
                LOG.error("", e);
            }

            if (location.toLowerCase().contains("china")) {
                product.setAddress(location.split(",")[0]);
            } else {
                product.setAddress(location);
            }

            LOG.info("location: " + location);
            //parse raw_name of valueband
            String valueband = doc.select(".main-block-minor").first().select("li").first().select("a").last().text();
            product.setValueBandname(valueband);
            LOG.info("valueband: " + valueband);
            // Category name of product
            if(valueband.toLowerCase().contains("services")) {
                product.setCategoryName(SERVICE_CATEGORY);
            } else if (StringUtils.defaultString(business).toLowerCase().contains("manufacturer")) {
                product.setCategoryName(Constants.MANUFACTURING_CATEGORY);
            } else if (StringUtils.defaultString(business).toLowerCase().contains("trading")) {
                product.setCategoryName(Constants.TRADING_CATEGORY);
            } else {
                product.setCategoryName(Constants.SALES_CATEGORY);
            }
        } catch (Exception e) {
            LOG.error("", e);
        }
        //parse image url
        try {
            /*List<String> photos = new ArrayList<>();
            for (Element photoElement : productElement.select(".enlargeHref")) {
                photos.add(photoElement.select("img").first().absUrl("src"));
            }
            if (photos.isEmpty()) {
                throw new NullPointerException();
            }
            product.setMultipleImageUrls(photos);*/
            String photo = productElement.select(".enlargeHref").first().select("img").first().absUrl("src");
            if (StringUtils.isBlank(photo)) {
                return;
            }
            product.setImageUrl(photo);
            LOG.info("photo: " + photo);
        } catch (Exception e) {
        	LOG.error("", e);
            return;
        }

        try {
        	LOG.info("product: " + product);
            saveProductToDb(url, product, Constants.MADE_IN_CHINA_ID);
        } catch (SolrServerException e) {
            LOG.error("", e);
        } catch (SQLException e) {
            LOG.error("", e);
        }
    }

    private void parseTradeFord(String url) throws IOException {
        LOG.info("parsing "+url);
        Document doc = Jsoup.connect(url).get();
       /* try {
            doc = fetchPageBody(url);
        } catch (FetcherTimeOutException e) {
            LOG.error("",e);
            return;
        }*/
        Element productElement = doc.select("#p_home").first();
        Product product = new Product();
        product.setModel_name_en(TRADEFORD_SITE_NAME);
        product.setLink_external(url);
        //parse photo
        try {
            String photo = productElement.select(".img-thumbnail").first().select("img").first().absUrl("src");
            product.setImageUrl(StringUtils.defaultString(photo));
        } catch (Exception e) {
            LOG.error("",e);
        }
        //parse product name
        try {
            String productName = doc.select(".heading_cp1").first().text();
            if (StringUtils.isBlank(productName)) {
                return;
            }
            product.setName(productName);
        } catch (Exception e) {
            LOG.error("",e);
            return;
        }
        //parse company name
        try {
            String companyName = productElement.select(".cgrid_sub2").first().select("a").first().text();
            if (StringUtils.isBlank(companyName)) {
                return;
            }
            product.setCompany(StringUtils.defaultString(companyName));
        } catch (Exception e) {
            LOG.error("",e);
            return;
        }
        //parse address
        try{
            String address = productElement.select(".icon-home").first().parent().text();
            if(StringUtils.isNotBlank(address)){
                product.setAddress(address);
            }
        } catch (Exception e){
            //LOG.error("",e);
        }
        try {
            String country = productElement.select(".cgrid_sub2").get(2).text();
            if (StringUtils.isNotBlank(country)) {
                product.setCountry(country);
                //get country code
                Address address = ParserUtils.getCountry(country);
                if (StringUtils.isNotBlank(address.getShort_name())) {
                    product.setCountry_code(address.getShort_name());
                }
            }
        } catch (Exception e){
            LOG.error("",e);
        }
        /*// country code
        try {
            String countryName = productElement.select(".icon-globe").last().parent().text();
            Address country = ParserUtils.getCountry(countryName);
            if (country == null) {
                product.setCountry_code(Constants.COUNTRY_CODE);
                product.setCountry(Constants.COUNTRY);
            } else {
                product.setCountry_code(country.getShort_name());
                product.setCountry(country.getLong_name());
            }
        } catch (Exception e) {
            product.setCountry_code(Constants.COUNTRY_CODE);
            product.setCountry(Constants.COUNTRY);
            e.printStackTrace();
        }*/

        //parse business type
        String businessType;
        try{
            String business = productElement.select(".cgrid_sub2").get(1).text();
            if(business.toLowerCase().contains("agent")
                    ||business.toLowerCase().contains("retailer")
                    ||business.toLowerCase().contains("wholesaler")
                    ||business.toLowerCase().contains("distributor")){
                businessType = "sales";
            } else if(business.toLowerCase().contains("buying house")){
                businessType = "buying";
            }  else if(business.toLowerCase().contains("exporter")
                    ||business.toLowerCase().contains("importer")
                    ||business.toLowerCase().contains("trader")){
                businessType = "trading";
            } else if(business.toLowerCase().contains("manufacturer")){
                businessType = "manufactururing";
            } else {
                businessType = "supply";
            }
        } catch (Exception e){
            LOG.error("",e);
            businessType = "supply";
        }
        product.setCategoryName(businessType);
        try {
            String category = productElement.select(".cgrid_sub2").get(3).select("a").text();
            if(StringUtils.isNotBlank(category)) {
                product.setValueBandname(category);
            }
        } catch (Exception e){
            LOG.error("",e);
        }
        try {
            saveProductToDb(url,product,TRADEFORD_ID);
        } catch (Exception e) {
            LOG.error("",e);
        }
    }

    private void parseToboc(String url) throws IOException {
        LOG.info("parsing "+url);
        Document doc = Jsoup.connect(url).get();
        /*try {
            doc = fetchPageBody(url);
        } catch (FetcherTimeOutException e) {
            LOG.error("",e);
            return;
        }*/
        Element productElement = doc;
        Product product = new Product();
        product.setModel_name_en(TOBOC_SITE_NAME);
        product.setLink_external(url);
        //parse product name
        try {
            String productName = productElement.select("span.font_inc").first().select("span").first().text();
            if (StringUtils.isBlank(productName)) {
                return;
            }
            product.setName(productName);
        } catch (Exception e){
            LOG.error("",e);
        }
        //parse product image
        try {
            String photo = productElement.select(".productimg_container").first().select("img").first().absUrl("src");
            product.setImageUrl(photo);
        } catch (Exception e){
            LOG.error("",e);
        }
        //parse company logo
        try {
            String companyLogo = productElement.select("#subContent").first().select("img").first().absUrl("src");
            product.setCompanyLogo(companyLogo);
        } catch (Exception e){
            LOG.error("",e);
        }
        //parse price
        try {
            String price = productElement.select(".prod_detail_box").first().select("tr").get(1).select("td").get(1).text();
            product.setPrice(price);
        } catch (Exception e){
            LOG.error("",e);
        }
        //parse company
        try {
            String companyName = productElement.select("#lnkCompanyBrocher").first().text();
            if (StringUtils.isBlank(companyName)) {
                return;
            }
            product.setCompany(companyName);
        } catch (Exception e){
            LOG.error("",e);
        }
        String companyAddressAndWebLink = productElement.select(".companyAddress").first().select("p").first().text();
        String webLink = productElement.select(".companyAddress").first().select("span").first().text();
        String companyAddress = companyAddressAndWebLink.substring(0,companyAddressAndWebLink.indexOf(webLink));
        if(StringUtils.isNotBlank(companyAddress)){
            product.setAddress(companyAddress);
            // country code
            try {
                Address country = ParserUtils.getCountry(companyAddress);
                if (country == null) {
                    product.setCountry_code(Constants.COUNTRY_CODE);
                    product.setCountry(Constants.COUNTRY);
                } else {
                    product.setCountry_code(country.getShort_name());
                    product.setCountry(country.getLong_name());
                }
            } catch (Exception e) {
                product.setCountry_code(Constants.COUNTRY_CODE);
                product.setCountry(Constants.COUNTRY);
                e.printStackTrace();
            }
        }
        //parse valueband
        try {
            String valueBand = productElement.select("#companyProductsLeadsWrapper").first().select("b").first().select("a").first().select("span").first().text();
            product.setCategoryName("supply");
            product.setValueBandname(valueBand);
        } catch (Exception e){
            LOG.error("",e);
        }
        LOG.info(new Gson().toJson(product).toString());
        try {
            saveProductToDb(url,product,TOBOC_ID);
        } catch (Exception e) {
            LOG.error("",e);
        }
    }




    public void saveProductToDb(String url, Product product, int siteId) throws SolrServerException, SQLException, IOException {
        List<Product> products = new ArrayList<>();
        products.add(product);
        saveProductsToDb(url, products, siteId);
    }

    /**
     * Save Products To MySQL, Solr
     *
     * @param url
     * @param products
     * @param siteId
     * @throws SQLException
     * @throws IOException
     * @throws SolrServerException
     */
    private void saveProductsToDb(String url, List<Product> products, int siteId) throws SQLException, IOException, SolrServerException {
        if (products != null && products.size() > 0) {
            List<Quark> quarks = buildQuarksFromProducts(products);

            Category category = null;
            ValueBand valueBand = null;
            if (siteId == Constants.INDIAMART_ID || siteId == Constants.EXPORTPAGES_ID) {
                category = getCategory(url, siteId);
                LOG.info("category:" + " id=" + category.getId() + " name=" + category.getCategory());
                valueBand = buildValueBand(url, category, siteId);
                LOG.info("valueBand:" + " name=" + valueBand.getName());
            } else if (siteId == Constants.MADE_IN_CHINA_ID||siteId==TRADEFORD_ID||siteId==TOBOC_ID) {
                category = getCategoryForMadeInChinaSite(products.get(0));
                if(category!=null && StringUtils.isNotBlank(products.get(0).getValueBandname())) {
                    LOG.info("category:" + " id=" + category.getId() + " name=" + category.getCategory());
                    valueBand = buildValueBandForMadeInChinaSite(products.get(0), category);
                    LOG.info("valueBand:" + " name=" + valueBand.getName());
                }
            }

            // Update MySQL
            // insert or update quarks
            List<Integer> quarkIds = DBUtils.insertUpdateQuarks(quarks);

            // Check if Value Band existed?
            if(valueBand!=null) {
                valueBand = DBUtils.getValueBandByName(valueBand.getName());
                if (valueBand == null) {// value band not existed, insert it
                    if (siteId == Constants.INDIAMART_ID || siteId == Constants.EXPORTPAGES_ID) {
                        valueBand = buildValueBand(url, category, siteId);
                    } else if (siteId == Constants.MADE_IN_CHINA_ID || siteId == TRADEFORD_ID || siteId == TOBOC_ID) {
                        valueBand = buildValueBandForMadeInChinaSite(products.get(0), category);
                    }

                    // insert value band
                    valueBand = DBUtils.insertValueBand(valueBand);
                } else {// existed, just set needed fields for indexing Solr
                    LOG.info("valueBand with name=" + valueBand.getName() + " is existed!");
                    valueBand.setExisted(true);
                    valueBand.setCategory(category.getCategory());
                    valueBand.setLanguage(Constants.LANGUAGE);
                    valueBand.setSearch_name(valueBand.getName());
                }
            }
            // get Quark By Ids
            quarks = DBUtils.getQuarksByIds(quarkIds, valueBand, category);

            // build QuarkTranslations from Quarks
            List<QuarkTranslation> quarkTranslations = buildQuarkTranslationsFromQuarks(quarks);
            // insert quarkTranslations to MySQL
            DBUtils.insertUpdateQuarkTranslations(quarkTranslations);

            // build quark countries
            Set<QuarkCountry> countries = buildQuarkCountriesFromQuarks(quarks);
            // insert or update quark countries
            DBUtils.insertUpdateQuarkCountries(countries);

            // build crawled brands
            Set<CrawledBrand> crawledBrands = buildCrawledBrandsFromQuarks(quarks);
            // insert or update crawled brands
            DBUtils.insertUpdateCrawledBrands(crawledBrands);

            // Index Quark to Solr
            SolrUtils.insertUpdateQuarks(quarks);
            try {
            	Thread.sleep(2000);//delay 2s to effect commit add quarks
			} catch (Exception e) {
				LOG.error("", e);
			}
            // update point, count, count_brand of the value band
            SolrUtils.setFieldsOfValueBand(valueBand);

            // Index Value Bands to Solr
            SolrUtils.insertUpdateValueBand(valueBand);
        }
    }

    /**
     * Get Category
     *
     * @param siteId
     * @return
     * @throws IOException
     */
    public Category getCategory(String url, int siteId) throws IOException {
        Category category = null;
        if (siteId == Constants.INDIAMART_ID) {
            category = DBUtils.getOrInsertCategory(Constants.SALES_CATEGORY, Constants.LANGUAGE);
        } else if (siteId == Constants.EXPORTPAGES_ID) {
            String valueBandRawName = buildValueBandRawName(url, siteId);
            if (Arrays.asList(Constants.SERVICE_CATEGORY_ARRAY).contains(valueBandRawName)) {
                category = DBUtils.getOrInsertCategory(SERVICE_CATEGORY, Constants.LANGUAGE);
            } else {
                category = DBUtils.getOrInsertCategory(Constants.SUPPLY_CATEGORY, Constants.LANGUAGE);
            }
        }

        return category;
    }

    /**
     * Build ValueBand
     *
     * @param url
     * @param category
     * @param siteId
     * @return
     */
    private ValueBand buildValueBand(String url, Category category, int siteId) {
        ValueBand valueBand = new ValueBand();

        // parse url to get raw_name
        String baseName = FilenameUtils.getBaseName(url);
        String rawName = "";
        if (siteId == Constants.INDIAMART_ID) {
            rawName = baseName.replace("-", " ");
        } else if (siteId == Constants.EXPORTPAGES_ID) {
            try {
                String[] valueBandArray = baseName.split("-");
                int lastIndex = valueBandArray.length - 1;
                for (int i = 0; i < lastIndex - 1; i++) {
                    if (i < lastIndex - 2) {
                        rawName += valueBandArray[i] + " ";
                    } else {
                        rawName += valueBandArray[i];
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (rawName.equals("")) {
            rawName = baseName;
        }
        if(rawName.toLowerCase().contains("services")){
            rawName = rawName.replace("Services","").replace("services","");
            category.setCategory("services");
        }
        valueBand.setRaw_name(rawName);
        valueBand.setType(Constants.VALUEBAND_TYPE);
        valueBand.setCategory(category.getCategory());
        valueBand.setCategory_id(category.getId());
        valueBand.setLanguage(Constants.LANGUAGE);
        valueBand.setName(valueBand.getRaw_name() + " " + category.getCategory());
        valueBand.setSearch_name(valueBand.getName());
        valueBand.setCreated_time(DBUtils.getCurrentTimeSeconds());
        return valueBand;
    }

    /**
     * Build ValueBand RawName
     *
     * @param url
     * @param siteId
     * @return
     */
    private String buildValueBandRawName(String url, int siteId) {
        // parse url to get raw_name
        String baseName = FilenameUtils.getBaseName(url);
        String rawName = "";
        if (siteId == Constants.INDIAMART_ID) {
            rawName = baseName.replace("-", " ");
        } else if (siteId == Constants.EXPORTPAGES_ID) {
            try {
                String[] valueBandArray = baseName.split("-");
                int lastIndex = valueBandArray.length - 1;
                for (int i = 0; i < lastIndex - 1; i++) {
                    if (i < lastIndex - 2) {
                        rawName += valueBandArray[i] + " ";
                    } else {
                        rawName += valueBandArray[i];
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (rawName.equals("")) {
            rawName = baseName;
        }

        return rawName;
    }

    public Category getCategoryForMadeInChinaSite(Product product) throws IOException {
        if (StringUtils.isNotBlank(product.getCategoryName())) {
            Category category = null;
            category = DBUtils.getOrInsertCategory(product.getCategoryName(), Constants.LANGUAGE);

            return category;
        } else {
            return null;
        }
    }

    public ValueBand buildValueBandForMadeInChinaSite(Product product, Category category) {
        ValueBand valueBand = new ValueBand();

        valueBand.setRaw_name(product.getValueBandname());
        valueBand.setType(Constants.VALUEBAND_TYPE);
        valueBand.setCategory(category.getCategory());
        valueBand.setCategory_id(category.getId());
        valueBand.setLanguage(Constants.LANGUAGE);
        valueBand.setName(valueBand.getRaw_name() + " " + category.getCategory());
        valueBand.setSearch_name(valueBand.getName());
        valueBand.setCreated_time(DBUtils.getCurrentTimeSeconds());

        return valueBand;
    }


    /**
     * Build Quarks From Products
     *
     * @param products
     * @return
     */
    private List<Quark> buildQuarksFromProducts(List<Product> products) {
        List<Quark> quarks = new ArrayList<>();
        if (products == null || products.size() <= 0)
            return quarks;
        long now = DBUtils.getCurrentTimeSeconds();
        for (Product product : products) {
            now++;
            Quark quark = new Quark();

            quark.setQuark_type(Constants.QUARK_TYPE);
            quark.setLink_external(product.getLink_external());
            quark.setCountry_code(product.getCountry_code());
            quark.setLanguage(Constants.LANGUAGE);
            quark.setStatus(true);
            quark.setCreated_time(now);
            quark.setUpdated_time(now);

            ObjectMapper mapper = new ObjectMapper();
            try {
                String productString = mapper.writeValueAsString(product);
                quark.setCrawl_data(productString);
                LOG.info("productString= " + productString);
            } catch (IOException e) {
                LOG.error("", e);
            }

            quarks.add(quark);
        }

        return quarks;
    }

    /**
     * Build QuarkTranslations From Quarks
     *
     * @param quarks
     * @return
     */
    private List<QuarkTranslation> buildQuarkTranslationsFromQuarks(List<Quark> quarks) {
        List<QuarkTranslation> quarkTranslations = new ArrayList<>();
        if (quarks == null || quarks.size() <= 0) return quarkTranslations;

        for (Quark quark : quarks) {
            QuarkTranslation quarkTranslation = new QuarkTranslation();
            quarkTranslation.setQuark_id(quark.getId());
            quarkTranslation.setLanguage_code(quark.getLanguage());
            quarkTranslation.setTitle(quark.getTitle_en());
            quarkTranslation.setDescription(quark.getDescription_en());
            if (StringUtils.isEmpty(quarkTranslation.getDescription())) {
                quarkTranslation.setDescription("");
            }
            quarkTranslation.setManufacturer(quark.getManufacturer_en());
            quarkTranslation.setCountry(quark.getCountry_en());
            quarkTranslation.setAddress_lv1(quark.getAddress_lv1_en());

            quarkTranslations.add(quarkTranslation);
        }

        return quarkTranslations;
    }

    /**
     * Build QuarkCountries From Quarks
     *
     * @param quarks
     * @return
     */
    private Set<QuarkCountry> buildQuarkCountriesFromQuarks(List<Quark> quarks) {
        Set<QuarkCountry> quarkCountries = new HashSet<>();
        if (quarks == null || quarks.size() <= 0) return quarkCountries;

        for (Quark quark : quarks) {
            if (StringUtils.isEmpty(quark.getCountry_code_en())) {
                continue;
            }
            QuarkCountry quarkCountry = new QuarkCountry();
            quarkCountry.setCode(quark.getCountry_code_en());

            quarkCountries.add(quarkCountry);
        }

        return quarkCountries;
    }

    /**
     * Build CrawledBrands From Quarks
     *
     * @param quarks
     * @return
     */
    private Set<CrawledBrand> buildCrawledBrandsFromQuarks(List<Quark> quarks) {
        Set<CrawledBrand> crawledBrands = new HashSet<>();
        if (quarks == null || quarks.size() <= 0) return crawledBrands;

        for (Quark quark : quarks) {
            if (StringUtils.isEmpty(quark.getManufacturer_en())) {
                continue;
            }
            CrawledBrand brand = new CrawledBrand();
            brand.setName(quark.getManufacturer_en());

            crawledBrands.add(brand);
        }

        return crawledBrands;
    }

}
