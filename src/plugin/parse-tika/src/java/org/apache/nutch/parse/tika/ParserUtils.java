package org.apache.nutch.parse.tika;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.nutch.parse.tika.model.LocationInfo;
import org.apache.nutch.parse.tika.model.Product;
import org.apache.nutch.parse.tika.model.countrycode.Address;
import org.apache.nutch.parse.tika.model.countrycode.AddressType;
import org.apache.nutch.parse.tika.model.countrycode.GeoLocation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by laphuoc on 9/23/2016.
 */

public class ParserUtils {
    public static final Logger LOG = LoggerFactory.getLogger(ParserUtils.class);

    /**
     * Get point of number
     *
     * @return
     */
    public static double getPoints(int number) {
        int total_case = (number - 1) / 10;
        double point = 0;

        switch (total_case) {
            case 0:
                point = number / 10;
                break;
            case 1:
                point = 1 + (((number - 10) / 10) * 0.9);
                break;
            case 2:
                point = 1.9 + (((number - 20) / 10) * 0.8);
                break;
            case 3:
                point = 2.7 + (((number - 30) / 10) * 0.7);
                break;
            case 4:
                point = 3.4 + (((number - 40) / 10) * 0.6);
                break;
            case 5:
                point = 4 + (((number - 50) / 10) * 0.5);
                break;
            case 6:
                point = 4.5 + (((number - 60) / 10) * 0.4);
                break;
            case 7:
                point = 4.9 + (((number - 70) / 10) * 0.3);
                break;
            case 8:
                point = 5.2 + (((number - 80) / 10) * 0.2);
                break;
            default:
                point = 5.4 + (((number - 90) / 10) * 0.1);
                break;
        }

        return point;
    }

    /**
     * Get Quark Prop Values
     *
     * @return
     * @throws IOException
     */
    public static Map<String, String> getQuarkPropValues() throws IOException {
        Map<String, String> resultMap = new HashMap<>();
        InputStream inputStream = null;
        try {
            Properties prop = new Properties();
            String propFileName = "conf/quark.properties";
            inputStream = ParserUtils.class.getClassLoader().getResourceAsStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
            // Solr info
            String solrUrl = prop.getProperty("solr.url");
            String solrUser = prop.getProperty("solr.user");
            String solrPassword = prop.getProperty("solr.password");
            String solrAuth = prop.getProperty("solr.auth");
            String solrQuarkCore = prop.getProperty("solr.quark_core");
            String solrValueBandCore = prop.getProperty("solr.value_band_core");

            // MySQL info
            String mysqlDbUrl = prop.getProperty("mysql.dbconnection");
            String mysqlUser = prop.getProperty("mysql.user");
            String mysqlPassword = prop.getProperty("mysql.password");

            resultMap.put("solr.url", solrUrl);
            resultMap.put("solr.user", solrUser);
            resultMap.put("solr.password", solrPassword);
            resultMap.put("solr.auth", solrAuth);
            resultMap.put("solr.quark_core", solrQuarkCore);
            resultMap.put("solr.value_band_core", solrValueBandCore);

            resultMap.put("mysql.dbconnection", mysqlDbUrl);
            resultMap.put("mysql.user", mysqlUser);
            resultMap.put("mysql.password", mysqlPassword);

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            inputStream.close();
        }
        return resultMap;
    }

    /**
     * Get GeoLocation from address
     * @param
     * @return
     * @throws IOException
     */
    public static GeoLocation getGeoLocation(String addressString) throws IOException {
        String baseUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=";
        String language = "&language=en";
        String url = baseUrl + addressString + language;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        response = client.newCall(request).execute();
        String res = response.body().string();
        Gson gson = new Gson();
        GeoLocation geoLocation = gson.fromJson(res, GeoLocation.class);

        return geoLocation;
    }


    public static String getCountryCode(String addressString) throws IOException {
        String baseUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=";
        String language = "&language=en";
        String url = baseUrl + addressString + language;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        response = client.newCall(request).execute();
        String res = response.body().string();
        Gson gson = new Gson();
        GeoLocation geoLocation = gson.fromJson(res, GeoLocation.class);

        try {
            List<Address> addressList = geoLocation.getResults().get(0).getAddress_components();
            for (Address address : addressList) {
                for (AddressType type : address.getTypes()) {
                    if (type == AddressType.country) {
                        return address.getShort_name();
                    }
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    /*public static Address getCountry(String addressString) throws IOException {
        String baseUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=";
        String language = "&language=en&key=AIzaSyDEWhJHwMOQh7PoONN6plrjdt-Fz5GwmKw";
        String url = baseUrl + addressString + language;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        response = client.newCall(request).execute();
        String res = response.body().string();
        Gson gson = new Gson();
        GeoLocation geoLocation = gson.fromJson(res, GeoLocation.class);

        try {
            List<Address> addressList = geoLocation.getResults().get(0).getAddress_components();
            for (Address address : addressList) {
                for (AddressType type : address.getTypes()) {
                    if (type == AddressType.country) {
                        return address;
                    }
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }*/

    // Optimize getCountry, created: dctan 2016/11/30

    public static Address getCountry(String addressString) throws IOException {
        LocationInfo locationInfo = null;
        try {
            locationInfo = DBUtils.getLocationInfoByAddress(addressString);
        } catch (Exception e){
            LOG.error("",e);
        }

        String res;
        if (locationInfo == null) {
            String baseUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=";
            String language = "&language=en&key=AIzaSyDEWhJHwMOQh7PoONN6plrjdt-Fz5GwmKw";
            String url = baseUrl + addressString + language;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = null;
            response = client.newCall(request).execute();
            res = response.body().string();
            res = res.replaceAll("^ +| +$| (?= ) | ","").replaceAll("\n","");
        } else {
            res = locationInfo.getJson();
        }

        Gson gson = new Gson();
        GeoLocation geoLocation = gson.fromJson(res, GeoLocation.class);

        try {
            List<Address> addressList = geoLocation.getResults().get(0).getAddress_components();
            for (Address address : addressList) {
                for (AddressType type : address.getTypes()) {
                    if (type == AddressType.country) {
                        if (locationInfo == null) {
                            // insert new quark address to db
                            try {
                                DBUtils.insertQuarkAddress(addressString, address.getShort_name(), address.getLong_name(),addressString.split(",")[0],  res);
                            } catch (Exception e) {
                                LOG.error("",e);
                            }
                        }
                        return address;
                    }
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }


    public static String getAddressOfCompany(String linkExternal){
        String addressString = "";
        try {
            Element doc = Jsoup
                    .connect(linkExternal)
                    .timeout(30000)
                    .get();
            Element addressEle = doc.select("div[itemprop='address']").first();
            Element streetEle = addressEle.select("span[itemprop='streetAddress']").first();
            Element postalCodeEle = addressEle.select("span[itemprop='postalCode']").first();
            Element addressCountryEle = addressEle.select("span[itemprop='addressCountry']").first();
            if (streetEle == null || postalCodeEle == null) {
                addressString = "";
            } else {
                addressString = streetEle.text() + ", " + postalCodeEle.text() + ", " + addressCountryEle.text();
            }
            return addressString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getHostName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String hostname = uri.getHost();
        // to provide fault proof result, check if not null then return only hostname, without www.
        if (hostname != null) {
            hostname =  hostname.startsWith("www.") ? hostname.substring(4) : hostname;
            hostname =uri.getScheme() + "://" + hostname;
            return hostname;
        }
        return hostname;
    }


    /**
     * Set ImageUrl Address Country
     *
     * @param product
     * @return
     */
    public static boolean setImageUrlAddressCountry(Product product) {
        try {
            String hostname = product.getLink_external();
            Element doc = Jsoup
                    .connect(hostname)
                    .timeout(30000)
                    .get();
            setImageUrl(product, doc);
            setAddressCountry(product, doc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            product.setAddress("");
            product.setCountry(Constants.COUNTRY);
            product.setCountry_code(Constants.COUNTRY_CODE);
        }

        return false;
    }

    public static void setImageUrl(Product product, Element doc) {
        Element productEle = doc.select("#content-wrapper").first();
        Element img = productEle.select("img[itemprop='image']").first();
        String imgUrl = "";
        if (img != null) {
            imgUrl = img.attr("abs:src");
        }
        if (imgUrl == null || imgUrl.equals("")) {
            return;
        }
        product.setImageUrl(imgUrl);
    }

    public static void setAddressCountry(Product product, Element doc) throws IOException {
        String addressString = "";
        String addressWithoutCountry = "";
        Element addressEle = doc.select("div[itemprop='address']").first();
        Element streetEle = addressEle.select("span[itemprop='streetAddress']").first();
        Element postalCodeEle = addressEle.select("span[itemprop='postalCode']").first();
        Element addressCountryEle = addressEle.select("span[itemprop='addressCountry']").first();

        if (addressCountryEle == null || StringUtils.isEmpty(addressCountryEle.text())) {
            product.setCountry(Constants.COUNTRY);
            product.setCountry_code(Constants.COUNTRY_CODE);
            product.setAddress("");
            return;
        }

        if (streetEle == null || postalCodeEle == null) {
            addressString = addressCountryEle.text();
            addressWithoutCountry = "";
        } else {
            addressString = streetEle.text() + ", " + postalCodeEle.text() + ", " + addressCountryEle.text();
            addressWithoutCountry = streetEle.text() + ", " + postalCodeEle.text();
        }

        GeoLocation geoLocation = getGeoLocation(addressString);
        // in case no result, set default value
        if (geoLocation == null || geoLocation.getStatus() == null
                || geoLocation.getStatus().equals("ZERO_RESULTS")) {
            // retry to get country info
            Address countryInfo = getCountry(addressCountryEle.text());
            if (countryInfo != null) {
                product.setCountry(countryInfo.getLong_name());
                product.setCountry_code(countryInfo.getShort_name());
            }
            product.setAddress(addressWithoutCountry);
        } else {
            try {
                List<Address> addressList = geoLocation.getResults().get(0).getAddress_components();
                for (Address address : addressList) {
                    for (AddressType type : address.getTypes()) {
                        if (type == AddressType.country) {
                            product.setCountry(address.getLong_name());
                            product.setCountry_code(address.getShort_name());
                        } else if (type == AddressType.administrative_area_level_1) {
                            product.setAddress(address.getLong_name());
                        }
                    }
                }
            } catch (Exception ex) {
//                ex.printStackTrace();
            }
        }

        // Set default value
        if (StringUtils.isEmpty(product.getCountry_code())) {
            product.setAddress("");
            product.setCountry(Constants.COUNTRY);
            product.setCountry_code(Constants.COUNTRY_CODE);
        }
    }

    
}
