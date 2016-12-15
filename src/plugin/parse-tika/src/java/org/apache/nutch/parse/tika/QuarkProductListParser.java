package org.apache.nutch.parse.tika;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by htngan on 11/28/2016.
 */
public abstract class QuarkProductListParser extends QuarkAbstractParser {
    public static final Logger LOG = LoggerFactory.getLogger(QuarkProductListParser.class);
    protected List<String> readProductList(Document body, String ... inspectors){
        List<String> products = new ArrayList<>();
        for(String inspector:inspectors) {
            Elements productEles = body.select(inspector);
            if (productEles != null && !productEles.isEmpty()) {
                for(Element productEle:productEles){
                    try {
                        products.add(productEle.select("a").first().absUrl("href"));
                    } catch (Exception e){
                        //ignore error
                    	LOG.error("", e);
                    }
                }
            }
        }
        return products;
    }

    protected void crawlProductList(Document body, ParseAction parseAction, String ... inspectors){
        List<String> products = readProductList(body, inspectors);
        if(products!=null&&!products.isEmpty()){
            LOG.info(String.format("Found %s products",products.size()));
            for(String product:products) {
                parseAction.parse(product);
                try {
                    Thread.sleep(getCrawlDelay());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
