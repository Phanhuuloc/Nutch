package org.apache.nutch.parse.tika;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.nutch.parse.tika.model.Quark;
import org.apache.nutch.parse.tika.model.ValueBand;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by laphuoc on 9/27/2016.
 */
public class SolrUtils {

	public static Logger LOG = LoggerFactory.getLogger(SolrUtils.class);

	public static final int QUARK_CORE = 0;
	public static final int VALUEBAND_CORE = 1;

	public static CommonsHttpSolrServer getCommonsHttpSolrServer(String serverUrl, String username, String password)
			throws MalformedURLException {
		/*DefaultHttpClient client = new DefaultHttpClient();

		// Check for username/password
		if (Constants.IS_AUTH_SOLR) {
			LOG.info("Authenticating as: " + username);
			AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
					AuthScope.ANY_REALM, AuthScope.ANY_SCHEME);
			client.getCredentialsProvider().setCredentials(scope,
					new UsernamePasswordCredentials(username, password));
			HttpParams params = client.getParams();
			org.apache.http.client.params.HttpClientParams.setAuthenticating(params, true);
			client.setParams(params);
		}

		return new CommonsHttpSolrServer(serverUrl, client);*/
		HttpClient client = new HttpClient();

		// Check for username/password
		if (Constants.IS_AUTH_SOLR) {
			LOG.info("Authenticating as: " + username);
			AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
					AuthScope.ANY_REALM, AuthScope.ANY_SCHEME);
			
		      client.getState().setCredentials(scope,
		    		  new UsernamePasswordCredentials(username, password));

		      HttpClientParams params = client.getParams();
		      params.setAuthenticationPreemptive(true);

		      client.setParams(params);
		}

		return new CommonsHttpSolrServer(serverUrl, client);
		
	}

	/**
	 * Get HttpSolrServer
	 *
	 * @param solrCore
	 * @return
	 * @throws IOException
	 */
	public static synchronized CommonsHttpSolrServer getCommonsHttpSolrServer(int solrCore)
			throws IOException {
		HttpClient client = new HttpClient();
		
		Map<String, String> quarkPropValues = ParserUtils.getQuarkPropValues();
		String serverUrl = quarkPropValues.get("solr.url");
		String quarkCore = quarkPropValues.get("solr.quark_core");
		String valueBandCore = quarkPropValues.get("solr.value_band_core");
		if (solrCore == QUARK_CORE) {
			serverUrl += quarkCore;
		} else if (solrCore == VALUEBAND_CORE) {
			serverUrl += valueBandCore;
		}

		String auth = quarkPropValues.get("solr.auth");
		String username = quarkPropValues.get("solr.user");
		String password = quarkPropValues.get("solr.password");
		LOG.info("Solr info: url=" + serverUrl + " user=" + username + " pass=" + password);

		// Check for username/password
		if (auth != null && auth.equals("true")) {
			LOG.info("Authenticating as: " + username);
			AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
					AuthScope.ANY_REALM, AuthScope.ANY_SCHEME);
			
		      client.getState().setCredentials(scope,  new UsernamePasswordCredentials(username, password));

		      HttpClientParams params = client.getParams();
		      params.setAuthenticationPreemptive(true);

		      client.setParams(params);
		}

		return new CommonsHttpSolrServer(serverUrl, client);
	}


//	public static HttpSolrServer getCommonsHttpSolrServer(int solrCore)
//			throws IOException {
//		DefaultHttpClient client = new DefaultHttpClient();
//
//		String serverUrl = "";
//		if (solrCore == QUARK_CORE) {
//			serverUrl = Constants.QUARK_SOLR_URL;
//		} else if (solrCore == VALUEBAND_CORE) {
//			serverUrl = Constants.VALUEBAND_SOLR_URL;
//		}
//		return new HttpSolrServer(serverUrl, client);
//	}

	public static String stripNonCharCodepoints(String input) {
		StringBuilder retval = new StringBuilder();
		char ch;

		for (int i = 0; i < input.length(); i++) {
			ch = input.charAt(i);

			// Strip all non-characters
			// http://unicode.org/cldr/utility/list-unicodeset.jsp?a=[:Noncharacter_Code_Point=True:]
			// and non-printable control characters except tabulator, new line
			// and carriage return
			if (ch % 0x10000 != 0xffff && // 0xffff - 0x10ffff range step
											// 0x10000
					ch % 0x10000 != 0xfffe && // 0xfffe - 0x10fffe range
					(ch <= 0xfdd0 || ch >= 0xfdef) && // 0xfdd0 - 0xfdef
					(ch > 0x1F || ch == 0x9 || ch == 0xa || ch == 0xd)) {

				retval.append(ch);
			}
		}

		return retval.toString();
	}

	/**
	 * Insert ValueBand to Solr
	 * @param valueBand
	 * @return
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public static synchronized boolean insertValueBand(ValueBand valueBand) throws IOException, SolrServerException {
		boolean result = false;
		LOG.info("insertValueBand to Solr");
		LOG.info("insertValueBand ValueBand info:" + " name=" + valueBand.getName()
				+ " point=" + valueBand.getPoint() + " count=" + valueBand.getCount()
		+ " num_view=" + valueBand.getNum_view()) ;
		SolrServer solrServer = getCommonsHttpSolrServer(VALUEBAND_CORE);

		solrServer.addBean(valueBand);
		//solrServer.commit();
		result = true;
		LOG.info("insertValueBand to Solr successfully!");
		return result;
	}

	/**
	 * Update ValueBand to Solr
	 *
	 * @param valueBand
	 * @return
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public static synchronized boolean updateValueBand(ValueBand valueBand) throws SolrServerException, IOException {
		LOG.info("updateValueBand to Solr");
		LOG.info("updateValueBand ValueBand info:" + " id=" + valueBand.getId()
				+ " name=" + valueBand.getName()
				+ " point=" + valueBand.getPoint() + " count=" + valueBand.getCount()
				+ " num_view=" + valueBand.getNum_view()) ;
		SolrServer solrServer = getCommonsHttpSolrServer(VALUEBAND_CORE);
        SolrInputDocument doc = buildDocForInsertUpdateValueBand(valueBand);
        if (doc == null) return false;
		solrServer.add(doc);
		//solrServer.commit();
		LOG.info("updateValueBand to Solr successfully!");
		return true;
	}

	/**
	 * Insert or Update ValueBand to Solr
	 *
	 * @param valueBand
	 * @return
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public static synchronized boolean insertUpdateValueBand(ValueBand valueBand) throws SolrServerException, IOException {
		LOG.info("insertUpdateValueBand to Solr");
		SolrServer solrServer = getCommonsHttpSolrServer(VALUEBAND_CORE);
		SolrInputDocument doc = buildDocForInsertUpdateValueBand(valueBand);
		if (doc == null) return false;
		solrServer.add(doc);
		//solrServer.commit();
		LOG.info("insertUpdateValueBand to Solr successfully!");
		return true;
	}

	/**
	 * Insert Quarks to Solr
	 * @param quarks
	 * @return
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public static synchronized boolean insertQuarks(List<Quark> quarks) throws IOException, SolrServerException {
		LOG.info("insertQuarks to Solr");
		SolrServer solrServer = getCommonsHttpSolrServer(QUARK_CORE);
		if (quarks == null || quarks.size() < 0) {
			return false;
		}
		for (Quark quark : quarks) {
			solrServer.addBean(quark);
		}
		//solrServer.commit();
		LOG.info("insertQuarks to Solr successfully!");

		return true;
	}

	/**
	 * Insert or Update Quarks to Solr
	 * @param quarks
	 * @return
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public static synchronized boolean insertUpdateQuarks(List<Quark> quarks) throws IOException, SolrServerException {
		LOG.info("insertUpdateQuarks to Solr");
		if (quarks == null || quarks.size() < 0) return false;
		SolrServer solrServer = getCommonsHttpSolrServer(QUARK_CORE);
		for (Quark quark : quarks) {
			SolrInputDocument doc = buildSolrDocForInsertUpdateQuark(quark);
			if (doc != null) solrServer.add(doc);
		}
		//solrServer.commit();
		LOG.info("insertUpdateQuarks to Solr successfully!");

		return true;
	}

	/**
	 * Build Solr Document for insert or update Quark
	 *
	 * @param quark
	 * @return
	 */
	public static SolrInputDocument buildSolrDocForInsertUpdateQuark(Quark quark) {
		if (quark == null) return null;
		SolrInputDocument doc = new SolrInputDocument();

		doc.addField("id", quark.getId());
		doc.addField("quark_type", quark.getQuark_type());
		doc.addField("link_external", quark.getLink_external());
		doc.addField("photos", quark.getPhotos());
		doc.addField("has_photo", quark.getHas_photo());
		doc.addField("user_id", quark.getUser_id());
		doc.addField("brand_id", quark.getBrand_id());
		doc.addField("language", quark.getLanguage());
		doc.addField("list_languages", quark.getList_languages());
		doc.addField("country_code", quark.getCountry_code());
		doc.addField("country_code_en", quark.getCountry_code_en());
		doc.addField("country_en", quark.getCountry_en());
		doc.addField("title_en", quark.getTitle_en());
		doc.addField("description_en", quark.getDescription_en());
		doc.addField("manufacturer_en", quark.getManufacturer_en());
		doc.addField("address_lv1_en", quark.getAddress_lv1_en());
		doc.addField("created_time", quark.getCreated_time());
		doc.addField("updated_time", quark.getUpdated_time());
		doc.addField("delete_flag", quark.getDelete_flag());
		doc.addField("status", quark.getStatus());
		doc.addField("price", quark.getPrice());
		doc.addField("value_bands_en", quark.getValue_bands_en());
		doc.addField("vb_raw_name_en", quark.getVb_raw_name_en());
		doc.addField("vb_category_en", quark.getVb_category_en());
		doc.addField("value_bands_data_en", quark.getValue_bands_data_en());
		doc.addField("model_name_en", quark.getModel_name_en());

		return doc;
	}

    /**
     * Build SolrDoc From ValueBand
     *
     * @param valueBand
     * @return
     */
	public static SolrInputDocument buildDocForInsertUpdateValueBand(ValueBand valueBand) {
		if (valueBand == null) return null;

		SolrInputDocument doc = new SolrInputDocument();

		doc.addField("id", valueBand.getId());
		doc.addField("raw_name", valueBand.getRaw_name());
		doc.addField("category", valueBand.getCategory());
		doc.addField("category_id", valueBand.getCategory_id());
		doc.addField("type", valueBand.getType());
		doc.addField("language", valueBand.getLanguage());
		doc.addField("name", valueBand.getName());
		doc.addField("search_name", valueBand.getSearch_name());
		doc.addField("num_view", valueBand.getNum_view());
		doc.addField("point", valueBand.getPoint());
		doc.addField("users_subscribed", valueBand.getUsers_subscribed());
		doc.addField("count", valueBand.getCount());
		doc.addField("count_brand", valueBand.getCount_brand());

		return doc;
	}

	/**
	 * Get ValueBand By Id
	 * @param id
	 * @return
	 * @throws MalformedURLException
	 * @throws SolrServerException
	 */
	public static ValueBand getValueBandById(String id) throws IOException, SolrServerException {
		SolrServer solrServer = getCommonsHttpSolrServer(VALUEBAND_CORE);

		SolrQuery solrQuery = new  SolrQuery().setQuery("id:" + ClientUtils.escapeQueryChars(id));

		QueryResponse rsp = solrServer.query(solrQuery);
		List<ValueBand> docs = rsp.getBeans(ValueBand.class);
		if (docs != null) return docs.get(0);

		return null;
	}

	/**
	 * Get Quark By Id
	 * @param id
	 * @return
	 * @throws MalformedURLException
	 * @throws SolrServerException
	 */
	public static Quark getQuarkById(String id) throws IOException, SolrServerException {
		SolrServer solrServer = getCommonsHttpSolrServer(QUARK_CORE);

		SolrQuery solrQuery = new  SolrQuery().setQuery("id:" + ClientUtils.escapeQueryChars(id));

		QueryResponse rsp = solrServer.query(solrQuery);
		List<Quark> docs = rsp.getBeans(Quark.class);
		if (docs != null) return docs.get(0);

		return null;
	}


	/**
	 * Generate random int number
	 * @param min
	 * @param max
	 * @return
	 */
	public static int randInt(int min, int max) {
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

	/**
	 * Get Quarks By ValueBand
	 * @param valueBand
	 * @return
	 * @throws MalformedURLException
	 * @throws SolrServerException
	 */
	public static List<Quark> getQuarksByValueBand(String valueBand) throws IOException, SolrServerException {
		SolrServer solrServer = getCommonsHttpSolrServer(QUARK_CORE);

		int numberOfQuarks = getNumberOfQuarksByValueBand(valueBand);

		SolrQuery solrQuery = new SolrQuery()
				.setQuery("value_bands_data_en:" + valueBand)
				.setRows(numberOfQuarks);

		QueryResponse rsp = solrServer.query(solrQuery);
		List<Quark> docs = rsp.getBeans(Quark.class);
		if (docs != null) {
			return docs;
		}

		return null;
	}

	/**
	 * Get Number Of Quarks By ValueBand
	 *
	 * @param valueBand
	 * @return
	 * @throws MalformedURLException
	 * @throws SolrServerException
	 */
	public static int getNumberOfQuarksByValueBand(String valueBand) throws IOException, SolrServerException {
		SolrServer solrServer = getCommonsHttpSolrServer(QUARK_CORE);

		SolrQuery solrQuery = new SolrQuery().setQuery("value_bands_data_en:" + valueBand);

		QueryResponse rsp = solrServer.query(solrQuery);
		SolrDocumentList results = rsp.getResults();
		if (results != null) {
			return (int) results.getNumFound();
		}
		return 0;
	}

	/**
	 * Get Number Of Brands By ValueBand
	 *
	 * @param valueBand
	 * @return
	 * @throws MalformedURLException
	 * @throws SolrServerException
	 */
	public static int getNumberOfBrandsByValueBand(String valueBand) throws IOException, SolrServerException {
		Set<Integer> brands = new HashSet<>();
		List<Quark> quarks = getQuarksByValueBand(valueBand);

		if (quarks != null) {
			for (Quark quark : quarks) {
				if (quark.getBrand_id() > 0) {
					brands.add(quark.getBrand_id());
				}
			}
		}

		return brands.size();
	}

	/**
	 * Set values for point, count, count_brand fields of Value Band
	 *
	 * @return
	 */
	public static void setFieldsOfValueBand(ValueBand valueBand) throws IOException, SolrServerException {
		if (valueBand == null) return;

		String valueBandString = "\"" + valueBand.getRaw_name() + "::" + valueBand.getCategory() + "\"";

		double point = 0;

		double views_point = 0;
		double total_brands_point = 0;
		double total_quarks_point = 0;

		int numView = valueBand.getNum_view();

		views_point = ParserUtils.getPoints(numView);

		int numberOfQuarks = SolrUtils.getNumberOfQuarksByValueBand(valueBandString);
		total_quarks_point = ParserUtils.getPoints(numberOfQuarks);

		int numberOfBrands = SolrUtils.getNumberOfBrandsByValueBand(valueBandString);
		total_brands_point = ParserUtils.getPoints(numberOfBrands);

		int total_followers = 0;
		if (valueBand.getUsers_subscribed() != null) {
			total_followers = valueBand.getUsers_subscribed().size();
		}

		point = views_point + total_brands_point + total_quarks_point + total_followers * 3;

		// set values for point, count, count_brand fields of Value Band
		valueBand.setPoint(point);
		valueBand.setCount(numberOfQuarks);
		valueBand.setCount_brand(numberOfBrands);
	}

	/**
	 * Check ValueBand  exist in Solr
	 *
	 * @param valueBandId
	 * @return
	 */
	public static boolean checkValueBandExist(int valueBandId) throws IOException, SolrServerException {
		SolrServer solrServer = getCommonsHttpSolrServer(VALUEBAND_CORE);

		SolrQuery solrQuery = new SolrQuery().setQuery("id:" + valueBandId);

		QueryResponse rsp = solrServer.query(solrQuery);
		SolrDocumentList results = rsp.getResults();
		if (results != null) {
			return results.getNumFound() > 0;
		}
		return false;
	}

}