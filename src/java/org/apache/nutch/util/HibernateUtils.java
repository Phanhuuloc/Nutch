package org.apache.nutch.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.nutch.entitybean.CinemaBean;
import org.apache.nutch.entitybean.CountryBean;
import org.apache.nutch.entitybean.FilmBean;
import org.apache.nutch.entitybean.LanguageBean;
import org.apache.nutch.entitybean.PeopleBean;
import org.apache.nutch.entitybean.ProductionBean;
import org.apache.nutch.entitybean.StationBean;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class HibernateUtils {
	private static HibernateUtils instance;
	private static org.hibernate.cfg.Configuration cfg;
	private static SessionFactory factory;
	private final static String FILE_HIBERNATE_CONFIG = "hibernate.cfg.xml";
	private Session session;

	static{
		instance = new HibernateUtils();
		cfg = new org.hibernate.cfg.Configuration();
		cfg.configure(FILE_HIBERNATE_CONFIG);
		factory = cfg.buildSessionFactory();
	}
	private HibernateUtils(){

	}

	public static HibernateUtils getInstance(){
		return instance;
	}

	public Session openSession(){
		session = factory.openSession();
		return session;
	}

	public void closeSession(){
		try {
			if (session != null && session.isOpen()) {
				session.close();
			}
		} catch (Exception e) {
			//no need write to log
		}
	}
	
	public void beginTransaction() {
		if (session != null && session.getTransaction() != null && !session.getTransaction().isActive()) {
			session.getTransaction().begin();
			return ;
		} else if (session != null && session.getTransaction() != null && session.getTransaction().isActive()) {
			return ;
		} else if (session != null) {
			session.beginTransaction();
		}
	}

	
	/*
	 * Update Query 
	 */
	public String createFilmGenre(
			String viewLanguageCode, String genre,
			String description, String cmsMemberId){
		String result ="";
		Query query = session.createSQLQuery(
				"CALL StpCMSCreateGenre ("
						+ ":ViewLanguageCode, "
						+ ":Genre, "
						+ ":Description, "
						+ ":CmsMemberId"
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("ViewLanguageCode", viewLanguageCode)
				.setParameter("Genre", genre)
				.setParameter("Description", description)
				.setParameter("CmsMemberId", cmsMemberId);

		result = String.valueOf(query.uniqueResult());
		return result;
	}

	public String createFilm(
			String viewLanguageCode, String title, String content, String description,
			String trailer, String ageLimit, String photo,
			String yearProduct, String filmDuration, String cmsMemberId, String producer,
			String demension, String series, String rating
			){
		String result = "";
		Query query = session.createSQLQuery(
				"CALL StpCMSCreateFilm ("
						+ ":ViewLanguageCode, "
						+ ":Title, "
						+ ":Content, "
						+ ":Description, "
						+ ":Trailer, "
						+ ":AgeLimit, "
						+ ":Photo, "
						+ ":YearProduct, "
						+ ":FilmDuration, "
						+ ":CmsMemberId, "
						+ ":Producer, "
						+ ":Demension, "
						+ ":Series, "
						+ ":Rating"
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("ViewLanguageCode", viewLanguageCode)
				.setParameter("Title", title)
				.setParameter("Content", content)
				.setParameter("Description", description)
				.setParameter("Trailer", trailer)
				.setParameter("AgeLimit", ageLimit)
				.setParameter("Photo", photo)
				.setParameter("YearProduct", yearProduct)
				.setParameter("FilmDuration", filmDuration)
				.setParameter("CmsMemberId", cmsMemberId)
				.setParameter("Producer", producer)
				.setParameter("Demension", demension)
				.setParameter("Series", series)
				.setParameter("Rating", rating);

		result = String.valueOf(query.uniqueResult());
		return result;
	}

	public String createPeople(
			String viewLanguageCode, String name, String avatar,
			String description, String cmsMemberId, String mainLanguage,
			String shortDes, String sourceRef
			){
		String result = "";
		Query query = session.createSQLQuery(
				"CALL StpCrawlerCreatePeople ("
						+ ":ViewLanguageCode, "
						+ ":Name, "
						+ ":Avatar, "
						+ ":Description, "
						+ ":CmsMemberId, "
						+ ":MainLanguage, "
						+ ":ShortDes,"
						+ ":SourceRef"
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("ViewLanguageCode", viewLanguageCode)
				.setParameter("Name", name)
				.setParameter("Avatar", avatar)
				.setParameter("Description", description)
				// default value
				.setParameter("CmsMemberId", cmsMemberId)
				.setParameter("MainLanguage", mainLanguage)
				.setParameter("ShortDes", shortDes)
				.setParameter("SourceRef", sourceRef);

		result = String.valueOf(query.uniqueResult());
		return result;
	}

	public String createFilmGenreMap(String filmId, String genreId){
		String result ="";
		Query query = session.createSQLQuery(
				"CALL StpCMSCreateFilmGenreMap ("
						+ ":FilmId, "
						+ ":GenreId"
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("FilmId", filmId)
				.setParameter("GenreId", genreId);

		result = String.valueOf(query.uniqueResult());
		return result;
	}

	public String createFilmPeopleMap(String filmId, String peopleId, String partInFilm){
		String result = "";
		Query query = session.createSQLQuery(
				"CALL StpCrawlerCreateFilmPeopleMap ("
						+ ":FilmId, "
						+ ":PeopleId, "
						+ ":PartInFilm"
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("FilmId", filmId)
				.setParameter("PeopleId", peopleId)
				.setParameter("PartInFilm", partInFilm);

		result = String.valueOf(query.uniqueResult());
		return result;
	}

	public String createFilmTime(String filmId, String cinemaId, String dateStart, String dateEnd){
		String result = "";
		Query query = session.createSQLQuery(
				"CALL StpCMSCreateFilmTime ("
						+ ":FilmId, "
						+ ":CinemaId, "
						+ ":DateStart, "
						+ ":DateEnd"
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("FilmId", filmId)
				.setParameter("CinemaId", cinemaId)
				.setParameter("DateStart", dateStart)
				.setParameter("DateEnd", dateEnd);

		result = String.valueOf(query.uniqueResult());
		return result;
	}

	public String createFilmSchedule(
			String cinemaId, String filmId, String dateStart,
			String timeStart, String filmDateTime
			){
		String result = "";
		Query query = session.createSQLQuery(
				"CALL StpCMSCreateFilmSchedule ("
						+ ":CinemaId, "
						+ ":FilmId, "
						+ ":DateStart, "
						+ ":TimeStart, "
						+ ":FilmDateTime"
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("CinemaId", cinemaId)
				.setParameter("FilmId", filmId)
				.setParameter("DateStart", dateStart)
				.setParameter("TimeStart", timeStart)
				.setParameter("FilmDateTime", filmDateTime);

		result = String.valueOf(query.uniqueResult());
		return result;
	}

	public String[] getUniqueIdFilmAndFilmName (String displayFilmName) {
		try {
			openSession();
			beginTransaction();
			Query query = session.createSQLQuery(
					"CALL StpCMSCheckUniqueFilm ("
							+ ":filmTitle"
							+ ")"
					)
					.addScalar("FilmId", Hibernate.STRING)
					.addScalar("FilmTlt", Hibernate.STRING)
					.setParameter("filmTitle", displayFilmName);
			
			List result = query.list();
			if(result != null && result.size() == 1) {
				Object[] row = ((List<Object[]>) result).get(0);
				Object id = row[0];
			    Object name = row[1];
			    if("Not Found".equals(id) || "Not Found".equals(name)) {
			    	return null;
			    }
			    String[] value = new String[]{(String)id, (String)name};
			    return value;
			} else {
				return null;
			}
		}catch (Exception e) {
			System.out.println("getUniqueIdFilmAndFilmName:  " + e.getMessage());
			return null;
		} finally {
			closeSession();
		}
		
	}
	
	
	public String checkUniqueFilm(String filmTitle, String actors,
			String directors, String series, String yearProduct){
		String result = "";
		Query query = session.createSQLQuery(
				"CALL StpCMSCheckUniqueFilm ("
						+ ":FilmTitle, "
						+ ":Actors, "
						+ ":Directors, "
						+ ":Series, "
						+ ":YearProduct "
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("FilmTitle", filmTitle)
				.setParameter("Actors", actors)
				.setParameter("Directors", directors)
				.setParameter("Series", series)
				.setParameter("YearProduct", yearProduct);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	public String createFilmReview(String viewLanguageCode, String filmId,
			String reviewFrom, String totalReview, String content){
		String result ="";
		Query query = session.createSQLQuery(
				"CALL StpCMSCreateFilmReview ("
						+ ":ViewLanguageCode, "
						+ ":FilmId, "
						+ ":ReviewFrom, "
						+ ":TotalReview, "
						+ ":Content "
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("ViewLanguageCode", viewLanguageCode)
				.setParameter("FilmId", filmId)
				.setParameter("ReviewFrom", reviewFrom)
				.setParameter("TotalReview", totalReview)
				.setParameter("Content", content);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	
	
	public String createCountry(
			String viewLanguageCode, String countryCode, String country, String description,
			String tel, String currency, String active, String addLevel1Title,
			String addLevel2Title, String addLevel3Title, String addLevel4Title, String addLevel5Title,
			String nationality, String colorCode, String flagId
			){
		String result ="";
		Query query = session.createSQLQuery(
				"CALL StpCMSCreateCountry ("
						+ ":ViewLanguageCode, "
						+ ":CountryCode, "
						+ ":Country, "
						+ ":Description, "
						+ ":Tel, "
						+ ":Currency, "
						+ ":Active, "
						+ ":AddLevel1Title, "
						+ ":AddLevel2Title, "
						+ ":AddLevel3Title, "
						+ ":AddLevel4Title, "
						+ ":AddLevel5Title, "
						+ ":Nationality, "
						+ ":ColorCode "
						+ ":flagId "
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("ViewLanguageCode", viewLanguageCode)
				.setParameter("CountryCode", countryCode)
				.setParameter("Country", country)
				.setParameter("Description", description)
				.setParameter("Tel", tel)
				.setParameter("Currency", currency)
				.setParameter("Active", active)
				.setParameter("AddLevel1Title", addLevel1Title)
				.setParameter("AddLevel2Title", addLevel2Title)
				.setParameter("AddLevel3Title", addLevel3Title)
				.setParameter("AddLevel4Title", addLevel4Title)
				.setParameter("AddLevel5Title", addLevel5Title)
				.setParameter("Nationality", nationality)
				.setParameter("ColorCode", colorCode)
				.setParameter("flagId", flagId);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	public String createFilmCountry(String filmId, String countryCode){
		String result ="";
		Query query = session.createSQLQuery(
				"CALL StpCMSCreateFilmCountryMap ("
						+ ":FilmId, "
						+ ":CountryCode "
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("FilmId", filmId)
				.setParameter("CountryCode", countryCode);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	public String createFilmSub(String filmId, String languageCode, String sub){
		String result ="";
		Query query = session.createSQLQuery(
				"CALL StpCrawlerCreateFilmLanguageMap ("
						+ ":FilmId, "
						+ ":LanguageCode, "
						+ ":Sub "
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("FilmId", filmId)
				.setParameter("LanguageCode", languageCode)
				.setParameter("Sub", sub);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	public String createCinema(String listingId, String cmsMemberId, String cinema){
		String result ="";
		Query query = session.createSQLQuery(
				"CALL StpCMSCreateCinema ("
						+ ":ListingId, "
						+ ":CmsMemberId, "
						+ ":Cinema "
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("ListingId", listingId)
				.setParameter("CmsMemberId", cmsMemberId)
				.setParameter("Cinema", cinema);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	/*
	 * Update Query 
	 */
	
	public String updateDisplayFilm(String filmId, String viewLanguageCode, String displayFilm,
			String displayContent, String displayDescription, String displaySeries){
		String result ="";
		Query query = session.createSQLQuery(
				"CALL StpCMSUpdateDisplayFilm ("
						+ ":FilmId, "
						+ ":ViewLanguageCode, "
						+ ":DisplayFilm, "
						+ ":DisplayContent, "
						+ ":DisplayDescription, "
						+ ":DisplaySeries "
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("FilmId", filmId)
				.setParameter("ViewLanguageCode", viewLanguageCode)
				.setParameter("DisplayFilm", displayFilm)
				.setParameter("DisplayContent", displayContent)
				.setParameter("DisplayDescription", displayDescription)
				.setParameter("DisplaySeries", displaySeries);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	public String updateFilmInfo (String idFilm, String viewLanguageCode, String content, String description, 
						 String trailer, String ageLimit, String photo,
						String yearProduct, String filmDuration, String cmsMemberId, String producer,
						 String demension, String series, String rating, String total_rated) {
		
		String result = "";
		Query query = session.createSQLQuery(
				"CALL StpCrawlerUpdateFilm ("
				+":FilmId, "
				+":ViewLanguageCode, "
				+":Content, "
				+":Description, "
				+":Trailer, "
				+":AgeLimit, "
				+":Photo, "
				+":YearProduct, "
				+":FilmDuration,"
				+ ":CmsMemberId, "
				+ ":Producer, "
				+ ":Demension, "
				+ ":Series, "
				+ ":Rating "
				+ ")")
				.addScalar("result", Hibernate.STRING)
				.setParameter("FilmId", idFilm )
				.setParameter("ViewLanguageCode", viewLanguageCode)
				.setParameter("Content", content)
				.setParameter("Description", description)
				.setParameter("Trailer", trailer)
				.setParameter("AgeLimit", ageLimit)
				.setParameter("Photo", photo)
				.setParameter("YearProduct", yearProduct )
				.setParameter("FilmDuration", filmDuration)
				.setParameter("CmsMemberId", cmsMemberId)
				.setParameter("Producer", producer)
				.setParameter("Demension", demension)
				.setParameter("Series", series)
				.setParameter("Rating", rating);
		
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	public String updatedisplayGenre(String genreId, String displayGenre, String viewLanguageCode){
		String result ="";
		Query query = session.createSQLQuery(
				"CALL StpCMSUpdateDisplayGenre ("
						+ ":GenreId, "
						+ ":DisplayGenre, "
						+ ":ViewLanguageCode "
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("GenreId", genreId)
				.setParameter("DisplayGenre", displayGenre)
				.setParameter("ViewLanguageCode", viewLanguageCode);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	public String updateDisplayPeople(
			String peopleId, String displayName, String displayDescription, String sourceRef, String viewLanguageCode){
		String result ="";
		Query query = session.createSQLQuery(
				"CALL StpCrawlerUpdateDisplayPeople ("
						+ ":PeopleId, "
						+ ":DisplayName, "
						+ ":DisplayDescription, "
						+ ":SourceRef,"
						+ ":ViewLanguageCode "
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("PeopleId", peopleId)
				.setParameter("DisplayName", displayName)
				.setParameter("DisplayDescription", displayDescription)
				.setParameter("SourceRef", sourceRef)
				.setParameter("ViewLanguageCode", viewLanguageCode);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	/*
	 * Delete Query 
	 */
	
	public String deleteFilmReview(String filmId, String viewLanguageCode){
		String result ="";
		Query query = session.createSQLQuery(
				"CALL StpCMSDeleteFilmReview ("
						+ ":FilmId, "
						+ ":ViewLanguageCode "
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("FilmId", filmId)
				.setParameter("ViewLanguageCode", viewLanguageCode);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	public String deleteFilmTime(String filmId, String cinemaId){
		String result = "";
		Query query = session.createSQLQuery(
				"CALL StpCMSDeleteFilmTime ("
						+ ":FilmId, "
						+ ":CinemaId "
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("FilmId", filmId)
				.setParameter("CinemaId", cinemaId);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	public String deleteFilmSchedule(String filmId, String cinemaId){
		String result = "";
		Query query = session.createSQLQuery(
				"CALL StpCMSDeleteFilmSchedule ("
						+ ":FilmId, "
						+ ":CinemaId "
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("FilmId", filmId)
				.setParameter("CinemaId", cinemaId);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	/*
	 * List integer Id and String url
	 * */
	public Map<String, Integer> getListingProduction(){
		Map< String, Integer> hashUrl = new HashMap<String, Integer>();
		
		Query query = session.createSQLQuery("CALL StpCMSGetListingProduction ()")
				.addScalar("id", Hibernate.INTEGER)
				.addScalar("website", Hibernate.STRING);
		List result = query.list();
		for (Object[] row: (List<Object[]>) result ) {
		    Object id = row[0];
		    Object url = row[1];
		    hashUrl.put((String)url, (Integer)id);
		}
		
		return hashUrl;
	}
	
	public List<FilmBean> getAllFilmFromDbAtexpats() {
		List<FilmBean> list = new ArrayList<FilmBean>();
		try {
			openSession();
			beginTransaction();
			Query query = session.createSQLQuery(
					"CALL StpCrawlerGetMovieTitle ()"
					)
					.addScalar("film_id", Hibernate.STRING)
					.addScalar("film_title", Hibernate.STRING)
					.addScalar("display_film", Hibernate.STRING)
					.addScalar("official_website", Hibernate.STRING)
					.addScalar("language_code", Hibernate.STRING);
			
			List result = query.list();
			if(result != null && result.size() > 0) {
				FilmBean film = null;
				for(Object[] row : ((List<Object[]>) result)) {
					film = new FilmBean();
					film.setFilmId((String)row[0]);
					film.setFilmName((String)row[1]);
					film.setDisplayFilm((String)row[2]);
					film.setOfficialUrl((String)row[3]);
					film.setLanguageCode((String)row[4]);
					
					list.add(film);
				}
				
			    return list;
			    
			} else {
				return null;
			}
		} catch (Exception e) {
			System.out.println("HibernateUtils.getAllFilmFromDbAtexpats(): " + e.getMessage());
			return null;
		} finally {
			closeSession();
		}
		
	}
	
	public List<ProductionBean> getAllProductionFromDbAtexpats() {
		try {
			openSession();
			beginTransaction();
			List<ProductionBean> list = new ArrayList<ProductionBean>();
			Query query = session.createSQLQuery("CALL StpCrawlerGetFilmProduction()")
							.addScalar("production_id", Hibernate.STRING)
							.addScalar("film_id", Hibernate.STRING)
							.addScalar("film_name", Hibernate.STRING)
							.addScalar("production_name", Hibernate.STRING)
							.addScalar("display_production_name", Hibernate.STRING)
							.addScalar("website", Hibernate.STRING)
							.addScalar("extra_website", Hibernate.STRING)
							.addScalar("language_code", Hibernate.STRING);
			
			List result = query.list();
			if(result != null && result.size() > 0) {
				ProductionBean production = null;
				for(Object[] row : ((List<Object[]>)result)) {
					production = new ProductionBean();
					production.setProductionId((String) row[0]);
					production.setFilmId((String) row[1]);
					production.setFilmName((String)row[2]);
					production.setProductionName((String)row[3]);
					production.setDisplayProductionName((String)row[4]);
					production.setProductionUrl((String)row[5]);
					production.setExtraProductionUrl((String)row[6]);
					production.setLanguageCode((String)row[7]);
					
					list.add(production);
				}
				return list;
			} else {
				return null;
			}
		}catch (Exception e) {
			System.out.println("HibernateUtils.getAllProductionFromDbAtexpats: " + e.getMessage());
			return null;
		} finally {
			closeSession();
		}
	}
	
	
	/*
	 * List integer Id and String url and State
	 * */
	public Map<String, Integer> getListingWebsite(){
		Map< String, Integer> hashUrl = new HashMap<String, Integer>();
		
		Query query = session.createSQLQuery("CALL StpCrawlerGetListingWebsite(" + (System.currentTimeMillis()/1000) + ")")
				.addScalar("listing_id", Hibernate.INTEGER)
				.addScalar("website", Hibernate.STRING)
				.addScalar("website_status", Hibernate.STRING);
		List result = query.list();
		for (Object[] row: (List<Object[]>) result ) {
		    Object id = row[0];
		    Object url = row[1];
		    hashUrl.put((String)url, (Integer)id);
		}
		
		return hashUrl;
	}
	
	/*
	 * List integer Id and String url and State
	 * */
	public Map<Integer, String> getListingWebsiteV2(){
		Map<Integer, String> hashUrl = new HashMap<Integer, String>();
		
		Query query = session.createSQLQuery("CALL StpCrawlerGetListingWebsite (-1)")// + (System.currentTimeMillis()/1000) + ")")
				.addScalar("listing_id", Hibernate.INTEGER)
				.addScalar("website", Hibernate.STRING)
				.addScalar("website_status", Hibernate.STRING);
		List result = query.list();
		for (Object[] row: (List<Object[]>) result ) {
		    Object id = row[0];
		    Object url = row[1];
		    Object status = row[2];
		    if(status == null) {
		    	status = "NE";
		    }
		    if(url == null || (url != null && StringUtils.isBlank(url.toString()))) {
		    	continue;
		    }
		    hashUrl.put((Integer)id, (String)status + ";" + (String)url);
		}
		
		return hashUrl;
	}
	
	/*
	 * List integer Id and String url
	 * */
	public Map<String, Integer> getListingCinema(){
		Map< String, Integer> hashUrl = new HashMap<String, Integer>();
		
		Query query = session.createSQLQuery("CALL StpCrawlerGetListCinema ()")
				.addScalar("cinema_id", Hibernate.INTEGER)
				.addScalar("cinema_url", Hibernate.STRING);
		List result = query.list();
		for (Object[] row: (List<Object[]>) result ) {
		    Object id = row[0];
		    Object url = row[1];
		    hashUrl.put((String)url, (Integer)id);
		}
		
		return hashUrl;
	}
	
	/*
	 * List integer Id and String url
	 * */
	public Map<Integer, String> getListingCinemaV2(){
		Map<Integer, String> hashUrl = new HashMap<Integer, String>();
		
		Query query = session.createSQLQuery("CALL StpCrawlerGetListCinema ()")
				.addScalar("cinema_id", Hibernate.INTEGER)
				.addScalar("cinema_url", Hibernate.STRING);
		List result = query.list();
		for (Object[] row: (List<Object[]>) result ) {
		    Object id = row[0];
		    Object url = row[1];
		    hashUrl.put((Integer)id, (String)url);
		}
		
		return hashUrl;
	}
	
	public List<CinemaBean> getAllCinemaFromDbAtexpats() {
		try {
			List<CinemaBean> list = new ArrayList<CinemaBean>();
			Query query = session.createSQLQuery("CALL StpCrawlerGetListCinema()")
					.addScalar("cinema_id", Hibernate.STRING)
					.addScalar("cinema_name", Hibernate.STRING)
					.addScalar("cinema_des", Hibernate.STRING)
					.addScalar("cinema_url", Hibernate.STRING)
					.addScalar("cinema_extra_url", Hibernate.STRING)
					.addScalar("language_code", Hibernate.STRING);
			
			List result = query.list();
			CinemaBean cinema = null;
			for(Object[] row : (List<Object[]>)result) {
				cinema = new CinemaBean();
				cinema.setCinemaId((String)row[0]);
				cinema.setCinemaName((String)row[1]);
				cinema.setCinemaDes((String)row[2]);
				cinema.setCinemaUrl((String)row[3]);
				cinema.setCinemaExtraUrl((String)row[4]);
				cinema.setLanguageCode((String)row[5]);
				list.add(cinema);
			}
			
			return list;
			
		}catch (Exception e) {
			System.out.println("getAllCinemaFromDbAtexpats: "+e.getMessage());
			return null;
		}
	}
	
	public List<CountryBean> getAllCountryFromDbAtexpats() {
		try {
			List<CountryBean> list = new ArrayList<CountryBean>();
			
			Query query = session.createSQLQuery("CALL StpCrawlerGetFilmCountry()")
							.addScalar("code", Hibernate.STRING)
							.addScalar("country", Hibernate.STRING)
							.addScalar("country_translate", Hibernate.STRING)
							.addScalar("language_code", Hibernate.STRING);
			
			List result = query.list();
			CountryBean country = null;
			for(Object[] row : (List<Object[]>)result) {
				country = new CountryBean();
				country.setCountryCode((String)row[0]);
				country.setCountryName((String)row[1]);
				country.setCountryTranslate((String)row[2]);
				country.setLanguageCode((String)row[3]);
				list.add(country);
			}
			return list;
		}catch (Exception e) {
			System.out.println("getAllCountryFromDbAtexpats: " + e.getMessage());
			return null;
		}
		
	}
	
	public List<LanguageBean> getAllLangugeFromDbAtexpats() {
		try {
			List<LanguageBean> list = new ArrayList<LanguageBean>();
			
			Query query = session.createSQLQuery("CALL StpCrawlerGetListLanguage()")
							.addScalar("code", Hibernate.STRING)
							.addScalar("language", Hibernate.STRING)
							.addScalar("language_translate", Hibernate.STRING)
							.addScalar("language_code_translate", Hibernate.STRING);
			
			List result = query.list();
			LanguageBean language = null;
			for(Object[] row : (List<Object[]>)result) {
				language = new LanguageBean();
				language.setCode((String)row[0]);
				language.setLanguage((String)row[1]);
				language.setLanguageTranslate((String)row[2]);
				language.setLanguageTranslateCode((String) row[3]);
				
				list.add(language);
			}
			return list;
		}catch (Exception e) {
			System.out.println("getAllLangugeFromDbAtexpats: " + e.getMessage());
			return null;
		}
		
	}
	
	public String updateDisriptionFilm(String filmId, String languageCode, String content, String description, String series) {
		String result ="";
		Query query = session.createSQLQuery(
				"CALL StpCrawlerUpdateDisplayFilm ("
						+ ":FilmId, "
						+ ":ViewLanguageCode, "
						+ ":Content, "
						+ ":Description, "
						+ ":Series"
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("FilmId", filmId)
				.setParameter("ViewLanguageCode", languageCode)
				.setParameter("Content", content)
				.setParameter("Description", description)
				.setParameter("Series", series);
		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
	public List<PeopleBean> getAllPeopSameNames(String names) {
		List<PeopleBean> list = new ArrayList<PeopleBean>();
		Query query = session.createSQLQuery("CALL StpCrawlerCheckExistsPeopel(" 
				+ ":Name )")
				.addScalar("people_id", Hibernate.STRING)
				.addScalar("people_name", Hibernate.STRING)
				.addScalar("source_ref", Hibernate.STRING)
				.addScalar("language_code", Hibernate.STRING)
				.setParameter("Name", names);
		
		List result = query.list();
		if(result != null && result.size() > 0) {
			PeopleBean people = null;
			for(Object[] row : (List<Object[]>)result) {
				people = new PeopleBean();
				people.setPeopleId((String) row[0]);
				people.setName((String) row[1]);
				people.setDetailUrl((String) row[2]);
				people.setLanguageCode((String) row[3]);
				list.add(people);
			}
			return list;
		} 
		return null;
	}
	
	public HashMap<String, String> getTrainId() {
		Query query = session.createSQLQuery("CALL StpCrawlerGetTrainId()")
				.addScalar("train_id", Hibernate.STRING)
				.addScalar("website", Hibernate.STRING);
		List result = query.list();
		HashMap<String, String> hashMap = new HashMap<String, String>();
		if(result != null && result.size() > 0) {
			for(Object[] row : (List<Object[]>)result) {
				hashMap.put((String) row[0], (String) row[1]);
			}
			return hashMap;
		} 
		return null;
	}
	
	public HashMap<String, String> getTrainDisplay(String trainId) {
		Query query = session.createSQLQuery("CALL StpCrawlerGetTrainDisplay(:TrainId )")
				.addScalar("train", Hibernate.STRING)
				.addScalar("language_code", Hibernate.STRING)
				.setParameter("TrainId", trainId);
		List result = query.list();
		HashMap<String, String> hashMap = new HashMap<String, String>();
		if (result != null && result.size() > 0) {
			for (Object[] row : (List<Object[]>) result) {
				hashMap.put((String) row[1], (String) row[0]);
			}
			return hashMap;
		} 
		return null;
	}
	
	public List<StationBean> getAllTrainStation() {
		List<StationBean> list = new ArrayList<StationBean>();
		Query query = session.createSQLQuery("CALL StpCrawlerGetTrainStation()")
				.addScalar("listing_id", Hibernate.STRING)
				.addScalar("train_station", Hibernate.STRING)
				.addScalar("language_code", Hibernate.STRING);
		
		List result = query.list();
		if(result != null && result.size() > 0) {
			StationBean station = null;
			for(Object[] row : (List<Object[]>)result) {
				station = new StationBean();
				station.setStationId((String) row[0]);
				station.setStationName((String) row[1]);
				station.setLanguageCode((String) row[2]);
				list.add(station);
			}
			return list;
		} 
		return null;
	}
	
	public String createScheduleOfTrain(String trainId, String codeOfTrain, String departureStation, String arrivalStation, String departureTime, String arrivalTime, String distance, String dateAdd) {
		String result = "";
		Query query = session.createSQLQuery(
				"CALL StpCrawlerCreateTrainSchedule ("
						+ ":TrainCode, "
						+ ":TrainId, "
						+ ":DepartureStation, "
						+ ":ArrivalStation, "
						+ ":DepartureTime, "
						+ ":ArrivalTime, "
						+ ":Distance, "
						+ ":ArrivalAddDate"
						+ ")"
				)
				.addScalar("result", Hibernate.STRING)
				.setParameter("TrainCode", codeOfTrain)
				.setParameter("TrainId", trainId)
				.setParameter("DepartureStation", departureStation)
				.setParameter("ArrivalStation", arrivalStation)
				.setParameter("DepartureTime", departureTime)
				.setParameter("ArrivalTime", arrivalTime)
				.setParameter("Distance", distance)
				.setParameter("ArrivalAddDate", dateAdd);

		result = String.valueOf(query.uniqueResult());
		return result;
	}
	
}
