package org.apache.nutch.parse.tika;

import com.google.gson.Gson;
import org.apache.nutch.parse.tika.model.Product;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by htngan on 12/7/2016.
 */
public class QuarkParserTest {
    QuarkHTMLParser parser;
    @Before
    public void setup(){
        parser = new QuarkHTMLParser();
    }
    @Test
    public void testIndiaMart() throws IOException {
        String url = "http://dir.indiamart.com/impcat/paper-plate-making-machine.html";
        List<Product> products = parser.parseIndiaMartProducts(url, Jsoup.connect(url).get());
        assertTrue(products!=null&&!products.isEmpty());
        System.out.println("Test Indiamart success");
        System.out.println("Number product: "+products.size());
        for (Product product:products){
            System.out.println("------------------------------------------------");
            System.out.println(new Gson().toJson(product));
        }
    }
}
