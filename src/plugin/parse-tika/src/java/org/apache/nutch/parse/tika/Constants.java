package org.apache.nutch.parse.tika;

public interface Constants {

	String SALES_CATEGORY = "sales";
	String SERVICE_CATEGORY = "services";
	String SUPPLY_CATEGORY = "supply";
	String MANUFACTURING_CATEGORY = "manufacturing";
	String TRADING_CATEGORY = "trading";
	String QUARK_TYPE = "crawler";
	String VALUEBAND_TYPE = "I";
	String INDIAMART_SITE = "indiamartcom";
	String INDIAMART = "indiamart.com";
	String EXPORTPAGES_SITE = "exportpagescom";
	String EXPORTPAGES = "exportpages.com";
	String EXPORTPAGES_FULL_URL = "https://exportpages.com";
    String MADE_IN_CHINA = "made-in-china.com";
    String MADE_IN_CHINA_SITE_NAME = "made-in-chinacom";
    String MADE_IN_CHINA_DOMAIN = "http://www.made-in-china.com";
    String TOBOC_SITE_NAME = "toboccom";
    String TOBOC = "http://www.toboc.com";
    String TRADEFORD_SITE_NAME = "tradefordcom";
    String TRADEFORD = "http://www.tradeford.com";
	int INDIAMART_ID = 0;
	int EXPORTPAGES_ID = 1;
	int MADE_IN_CHINA_ID = 2;
	int TOBOC_ID = 3;
	int TRADEFORD_ID = 3;

	/**
	 * Constants for crawling website indiamart.com
	 */
	String COUNTRY_CODE = "US";
	String COUNTRY = "United States";
	String LANGUAGE = "en";

    /**
     *  Constants for crawling website exportpages.com
     */
    String[] SERVICE_CATEGORY_ARRAY = new String[]{"Business Services", "Advertising services",
            "Cleaning Services", "Disposal Recycling", "Filling Packaging", "Repair Maintenance",
            "Sonstiges", "Trade fairs Exhibitions"};


	/**
	 * Constants for made-in-china.com
	 */


	/**
	 * Solr constants
	 */
	// Solr Server local test
//	String QUARK_SOLR_URL = "http://192.168.1.212:8080/quark/quark";
//	String VALUEBAND_SOLR_URL = "http://192.168.1.212:8080/quark/value_band";
//	String SOLR_USERNAME = "";
//	String SOLR_PASSWORD = "";
//	boolean IS_AUTH_SOLR = false;

	// Solr Server for Deploy
//	String QUARK_SOLR_URL = "http://172.31.3.59:8080/solr/quark";
//	String VALUEBAND_SOLR_URL = "http://172.31.3.59:8080/solr/value_band";

	String QUARK_SOLR_URL = "http://52.87.173.107:8080/solr/quark";
	String VALUEBAND_SOLR_URL = "http://52.87.173.107:8080/solr/value_band";
	String SOLR_USERNAME = "solr";
	String SOLR_PASSWORD = "solr!@#";
	boolean IS_AUTH_SOLR = true;

	// MySQL for local test
	String DB_CONNECTION = "jdbc:mysql://192.168.1.200:3306/quark";
	String DB_USER = "root";
	String DB_PASSWORD = "123";

	// MySQL for deploy
//	String DB_CONNECTION = "jdbc:mysql://quark01.cluster-crbtw9cawoua.us-east-1.rds.amazonaws.com:3306/quark_tag";
//	String DB_USER = "quark_tag";
//	String DB_PASSWORD = "zaqwsx!2#$";

}
