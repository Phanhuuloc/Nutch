/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.apache.nutch.crawl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.avro.util.Utf8;
import org.apache.gora.mapreduce.GoraMapper;
import org.apache.gora.mapreduce.StringComparator;
import org.apache.gora.store.DataStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.nutch.atexpats.util.AtexpatsUtils;
import org.apache.nutch.entitybean.CinemaBean;
import org.apache.nutch.entitybean.CountryBean;
import org.apache.nutch.entitybean.LanguageBean;
import org.apache.nutch.indexer.IndexUtil;
import org.apache.nutch.indexer.IndexingFilters;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.parse.ParseStatusCodes;
import org.apache.nutch.parse.ParseStatusUtils;
import org.apache.nutch.parse.utils.ConstantsTikaParser;
import org.apache.nutch.scoring.ScoringFilters;
import org.apache.nutch.storage.ParseStatus;
import org.apache.nutch.storage.StorageUtils;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.apache.nutch.util.HibernateUtils;
import org.apache.nutch.util.NutchJob;
import org.apache.nutch.util.NutchTool;
import org.apache.nutch.util.StringUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*import com.gtranslate.Language;
import com.gtranslate.Translator;*/

public abstract class DbAtexpatsUpdaterJob extends NutchTool implements Tool {

	public static final Logger LOG = LoggerFactory.getLogger(DbAtexpatsUpdaterJob.class);
	private static final Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();
	
	static String PATTERN_GENRE = "[,:;]";
	static String PATTERN_SUB = "\\s+";
	static String PATTERN_QUERY_RESULT = "::";
	static String PATTERN_TIME_SCHEDULE = "";
	static String PATTERN_NAME_ACTOR = ",";
	static String PATTERN_ACTOR_LIST = ";;";
	static String PATTERN_ACTOR_FIELD = "%%";
	static String PATTERN_REVIEW_LIST = ";;";
	static String PATTERN_REVIEW_FIELD = "%%";
	static String PATTERN_NAME_SCHEDULE_CINEMA = "%%";
	static String PATTERN_NAME_SCHEDULE = "#";
	static String PATTERN_SCHEDULE = "\\$";
	static String PATTERN_DATE = ",";
	static String PATTERN_TIME = "\\;";
	
	// default value
	static String failure = "failure";
	static String partInFilmDirector = "2";
	static String partInFilmActor = "1";
	static String cmsMemberId = "1";

	private static final Utf8 REINDEX = new Utf8("-reindex");

	static {
		FIELDS.add(WebPage.Field.SIGNATURE);
		FIELDS.add(WebPage.Field.PARSE_STATUS);
		FIELDS.add(WebPage.Field.SCORE);
		FIELDS.add(WebPage.Field.MARKERS);
	}

	static enum LanguageFilm{
		ENGLISH("en" , "english"),
		VIETNAMESE("vi" , "vietnamese"),
		CANTONESEMANDARIN("zh" , "cantonesemandarin"),
		JAPANESE("ja" , "japanese"),
		HINDI("hi" , "hindi"),
		KOREAN("ko" , "korean"),
		CHINESE("zh" , "chinese");

		private String id;
		private String name;

		LanguageFilm(String id, String name) {
			this.name = name;
			this.id = id;
		}

		public java.lang.String getName() {
			return name;
		}

		public java.lang.String getId() {
			return id;
		}

		public static  List<LanguageFilm> getByNameLanguage(String subFilm){
			List<LanguageFilm> list = new ArrayList<LanguageFilm>();
			Pattern pattern = Pattern.compile(PATTERN_SUB);
			String[] subArr = pattern.split(subFilm);
			
			for(String sub : subArr){
				for(LanguageFilm e : values()) {
					if(e.name.contains(sub.trim().toLowerCase())){
						list.add(e);
					}
				}
			}
			return list;
		}
	}

	public static class IndexerMapper
	extends GoraMapper<String, WebPage, String, NutchDocument> {
		public IndexUtil indexUtil;
		public DataStore<String, WebPage> store;
		
		protected Utf8 batchId;
		Pattern pattern;

		@Override
		public void setup(Context context) throws IOException {
			Configuration conf = context.getConfiguration();
			/*batchId = new Utf8(conf.get(GeneratorJob.BATCH_ID, Nutch.ALL_BATCH_ID_STR));*/
			indexUtil = new IndexUtil(conf);
			try {
				store = StorageUtils.createWebStore(conf, String.class, WebPage.class);
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
		}

		protected void cleanup(Context context) throws IOException ,InterruptedException {
			store.close();
		};

		@Override
		public void map(String key, WebPage page, Context context)
				throws IOException, InterruptedException {
			ParseStatus pstatus = page.getParseStatus();
			if (pstatus == null || !ParseStatusUtils.isSuccess(pstatus)
					|| pstatus.getMinorCode() == ParseStatusCodes.SUCCESS_REDIRECT) {
				return; // filter urls not parsed
			}
			
			// TODO: Assign variables 
	//		String shortDes = null;
			String series = null;
			String descriptionGenre = null;
			//String descriptionDirector = null;
			//String descriptionActor = null;
			//String mainLanguage = null;
			

			String genreId = null;
			String filmId = null;
			String peopleId = null;
			String countryId = null;
			//String cinemaId = null;
			//String listingId = null;

			// TODO Variables for metadata film
			String title = null;
			String viewLanguageCode = null;
			String fullURL = null;
			String yearProduct = null;
			String showing_time = null;
			String genre = null;
			String ageLimit = null;
			String dimension = null;
			String rating = null;
			String country = null;
			String filmLanguage = null;
			String nameActors = null;
			String nameDirectors = null;
			String filmDuration = null;
			String producer = null;
			String productions = null;
			String version = null;
			String trailer = null;
			String photo = null;
			String descriptionFilm = null;
			String enDescriptionFilm = null;
			String viDescriptionFilm = null;
			String jaDescriptionFilm = null;
			String koDescriptionFilm = null;
			String nameScheduleCinemas = null;
			//String director_links = null;
			String enDirectorList = null;
			String viDirectorList = null;
			String jaDirectorList = null;
			String koDirectorList = null;
			//String actor_links = null;
			String enActorList = null;
			String viActorList = null;
			String jaActorList = null;
			String koActorList = null;
			
			String enActor = null;
			String viActor = null;
			String jaActor = null;
			String koActor = null;
			
			String enDirector = null;
			String viDirector = null;
			String jaDirector = null;
			String koDirector = null;
			
			String reviews = null;
			//String note = null;
			//String other_languge_link = null;
			String production_name = null;
			String cinema_film_name = null;
			String cinema_movie_description = null;
			String wiki_movie_description = null;
			
			for(Utf8 utf8 : page.getMetadata().keySet()){
				if(ConstantsTikaParser.film_name.equals(Bytes.toString(utf8.getBytes()))){
					title = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.language.equals(Bytes.toString(utf8.getBytes()))){
					viewLanguageCode = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.fullURL.equals(Bytes.toString(utf8.getBytes()))){
					fullURL = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.year_product.equals(Bytes.toString(utf8.getBytes()))){
					yearProduct = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.showing_time.equals(Bytes.toString(utf8.getBytes()))){
					showing_time = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.genre.equals(Bytes.toString(utf8.getBytes()))){
					genre = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.age_limit.equals(Bytes.toString(utf8.getBytes()))){
					ageLimit = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.dimension.equals(Bytes.toString(utf8.getBytes()))){
					dimension = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.rating.equals(Bytes.toString(utf8.getBytes()))){
					rating = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.film_country.equals(Bytes.toString(utf8.getBytes()))){
					country = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.film_language.equals(Bytes.toString(utf8.getBytes()))){
					filmLanguage = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.actor.equals(Bytes.toString(utf8.getBytes()))){
					nameActors = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.director.equals(Bytes.toString(utf8.getBytes()))){
					nameDirectors = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.film_duration.equals(Bytes.toString(utf8.getBytes()))){
					filmDuration = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.producer.equals(Bytes.toString(utf8.getBytes()))){
					producer = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.productions.equals(Bytes.toString(utf8.getBytes()))){
					productions = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.version.equals(Bytes.toString(utf8.getBytes()))){
					version = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.trailer.equals(Bytes.toString(utf8.getBytes()))){
					trailer = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.photo.equals(Bytes.toString(utf8.getBytes()))){
					photo = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.movie_description.equals(Bytes.toString(utf8.getBytes()))){
					descriptionFilm = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.schedule.equals(Bytes.toString(utf8.getBytes()))){
					nameScheduleCinemas = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.en_director_list.equals(Bytes.toString(utf8.getBytes()))){
					enDirectorList = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.vi_director_list.equals(Bytes.toString(utf8.getBytes()))){
					viDirectorList = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.ja_director_list.equals(Bytes.toString(utf8.getBytes()))){
					jaDirectorList = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.ko_director_list.equals(Bytes.toString(utf8.getBytes()))){
					koDirectorList = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.en_actor_list.equals(Bytes.toString(utf8.getBytes()))){
					enActorList = Bytes.toString(page.getMetadata().get(utf8)).trim();
				}  else if(ConstantsTikaParser.vi_actor_list.equals(Bytes.toString(utf8.getBytes()))){
					viActorList = Bytes.toString(page.getMetadata().get(utf8)).trim();
				}  else if(ConstantsTikaParser.ja_actor_list.equals(Bytes.toString(utf8.getBytes()))){
					jaActorList = Bytes.toString(page.getMetadata().get(utf8)).trim();
				}  else if(ConstantsTikaParser.ko_actor_list.equals(Bytes.toString(utf8.getBytes()))){
					koActorList = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.review.equals(Bytes.toString(utf8.getBytes()))){
					reviews = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.production_name.equals(Bytes.toString(utf8.getBytes()))){
					production_name = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.cinema_film_name.equals(Bytes.toString(utf8.getBytes()))){
					cinema_film_name = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.cinema_movie_description.equals(Bytes.toString(utf8.getBytes()))){
					cinema_movie_description = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.film_id.equals(Bytes.toString(utf8.getBytes()))) {
					filmId = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.director_index_en_text_multi.equals(Bytes.toString(utf8.getBytes()))) {
					enDirector = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.director_index_vi_text_multi.equals(Bytes.toString(utf8.getBytes()))) {
					viDirector = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.director_index_ja_text_multi.equals(Bytes.toString(utf8.getBytes()))) {
					jaDirector = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.director_index_ko_text_multi.equals(Bytes.toString(utf8.getBytes()))) {
					koDirector = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.actor_index_en_text_multi.equals(Bytes.toString(utf8.getBytes()))) {
					enActor = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.actor_index_vi_text_multi.equals(Bytes.toString(utf8.getBytes()))) {
					viActor = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.actor_index_ja_text_multi.equals(Bytes.toString(utf8.getBytes()))) {
					jaActor = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} else if(ConstantsTikaParser.actor_index_ko_text_multi.equals(Bytes.toString(utf8.getBytes()))) {
					koActor = Bytes.toString(page.getMetadata().get(utf8)).trim();
				} 
				
			}
			
			/*
			 * TODO Assign to another value (if null)
			 * */
			
			if(filmId == null || filmId.length() == 0){
				return;
			} else {
				
				descriptionFilm = (descriptionFilm == null ? wiki_movie_description : descriptionFilm);
				descriptionFilm = (descriptionFilm == null ? cinema_movie_description : descriptionFilm);

				LOG.info("");
				LOG.info("");
				LOG.info("idFilmFromDb - " + filmId);
				LOG.info("film_name-" + title);
				LOG.info("language-" + viewLanguageCode);
				LOG.info("fullURL-" + fullURL);
				LOG.info("year_product-" + yearProduct);
				LOG.info("showing_time-" + showing_time);
				LOG.info("genre-" + genre);
				LOG.info("age_limit-" + ageLimit);
				LOG.info("dimension-" + dimension);
				LOG.info("rating-" + rating);
				LOG.info("country-" + country);
				LOG.info("language_film-" + filmLanguage);
				LOG.info("actor-" + nameActors);
				LOG.info("director-" + nameDirectors);
				LOG.info("duration-" + filmDuration);
				LOG.info("producer-" + producer);
				LOG.info("productions-" + productions);
				LOG.info("version-" + version);
				LOG.info("trailer-" + trailer);
				LOG.info("photo-" + photo);
				LOG.info("movie_description-" + descriptionFilm);
				LOG.info("schedule-" + nameScheduleCinemas);
				LOG.info("en_director_list-" + enDirectorList);
				LOG.info("vi_director_list-" + viDirectorList);
				LOG.info("ja_director_list-" + jaDirectorList);
				LOG.info("ko_director_list-" + koDirectorList);
				LOG.info("en_actor_list-" + enActorList);
				LOG.info("vi_actor_list-" + viActorList);
				LOG.info("ja_actor_list-" + jaActorList);
				LOG.info("ko_actor_list-" + koActorList);
				LOG.info("reviews-" + reviews);
				LOG.info("production_name-" + production_name);
				LOG.info("cinema_film_name-" + cinema_film_name);
				LOG.info("cinema_movie_description-" + cinema_movie_description);
				LOG.info("wiki_movie_description-" + wiki_movie_description);
				LOG.info("");
				LOG.info("");
				
				String[] nameActorsArr = {};
				if(nameActors != null){
					nameActors = nameActors.trim();
					pattern = Pattern.compile(PATTERN_NAME_ACTOR);
					nameActorsArr = pattern.split(nameActors);
				}
				

				String[] nameDirectorsArr = {};
				if(nameDirectors != null){
					nameDirectors = nameDirectors.trim();
					pattern = Pattern.compile(PATTERN_NAME_ACTOR);
					nameDirectorsArr = pattern.split(nameDirectors);
				}
				
				// TODO: Create data atexpats
				/**
				 * Create Instance Hibernate
				 */
				HibernateUtils hibernateUtils = HibernateUtils.getInstance();
				Session session = hibernateUtils.openSession();
				Transaction transaction = session.beginTransaction();
				try{
					LOG.info("Start update or insert film to DB !!!!!");
					
					/**
					 * Create Genre - Create Film Genre Map
					 */
					try {
						if(genre != null && genre.length() > 0) {
							//pattern = Pattern.compile(PATTERN_GENRE);
							//String[] genreNameArr = pattern.split(genre);
							String[] genreArr = null;
							String result = "";
							StringBuffer genreIdString = new StringBuffer();
							
							for(String genreName : genre.split(PATTERN_GENRE)){
								genreName = genreName.trim();
								
								result = hibernateUtils.createFilmGenre(
										viewLanguageCode, genreName, descriptionGenre, cmsMemberId);
								if(result.contains(failure)){
									LOG.info("genreResult "  + result + " : " + genreName + " - " + viewLanguageCode);
									continue;
								} 
								
								pattern = Pattern.compile(PATTERN_QUERY_RESULT);
								genreArr = pattern.split(result);
								if(genreArr == null || genreArr.length != 2) {
									continue;
								}
								
								genreId = genreArr[1];
								genreIdString = AtexpatsUtils.append(genreId, ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, genreIdString);
							
								result = hibernateUtils.updatedisplayGenre(
										genreId, genreName, viewLanguageCode);
								LOG.info("genreDisplayResult: "  + result + " : " + genreId + " - " + genreName + " - " + viewLanguageCode);
								
								result = hibernateUtils.createFilmGenreMap(filmId, genreId);
								LOG.info("filmGenreMapResult: "  + result + " : " + filmId + " - " + genreId);
							}
							
							// set listId to metadata
							if(genreIdString != null) {
								page.putToMetadata(new Utf8(ConstantsTikaParser.film_genre_id), ByteBuffer.wrap(Bytes
										.toBytes(genreIdString.toString())));
							}
						}
					}catch (Exception e) {
						LOG.info("Create Genre - Create Film Genre Map: " + e.getMessage());
					}
					
					/** 
					 * update Film 
					 */
					if(ageLimit != null){
						ageLimit = ageLimit.replaceAll("\\D+","");
					}
					try {
						if(filmDuration != null && filmDuration.length() > 0) {
							filmDuration = filmDuration.replaceAll("\\D", "");
						}
						String filmResult = hibernateUtils.updateFilmInfo(filmId, viewLanguageCode, descriptionFilm, descriptionFilm, trailer, ageLimit, photo, yearProduct, filmDuration, cmsMemberId, producer, dimension, series, rating, "1");
						LOG.info("update film info: "  + filmResult);
					
					}catch (Exception e) {
						LOG.info("Update Film: " + e.getMessage());
					}
					LOG.info("");
					
					
					/**
					 *  Update description for multi_language
					 */
					try {
						String result = "";
						if(!StringUtil.isEmpty(enDescriptionFilm) && !"en".equals(viewLanguageCode)) {
							result = hibernateUtils.updateDisriptionFilm(filmId, "en", enDescriptionFilm, enDescriptionFilm, null);
							LOG.info("Update en_description_Film: " + result + " : " + filmId + " - " + enDescriptionFilm);
						}
						if(!StringUtil.isEmpty(viDescriptionFilm) && !"vi".equals(viewLanguageCode)) {
							result = hibernateUtils.updateDisriptionFilm(filmId, "vi", viDescriptionFilm, viDescriptionFilm, null);
							LOG.info("Update vi_description_Film: " + result + " : " + filmId + " - " + viDescriptionFilm);
						}
						if(!StringUtil.isEmpty(jaDescriptionFilm) && !"ja".equals(viewLanguageCode)) {
							result = hibernateUtils.updateDisriptionFilm(filmId, "ja", jaDescriptionFilm, jaDescriptionFilm, null);
							LOG.info("Update ja_description_Film: " + result + " : " + filmId + " - " + jaDescriptionFilm);
						}
						if(!StringUtil.isEmpty(koDescriptionFilm) && !"ko".equals(viewLanguageCode)) {
							result = hibernateUtils.updateDisriptionFilm(filmId, "ko", koDescriptionFilm, koDescriptionFilm, null);
							LOG.info("Update ko_description_Film: " + result + " : " + filmId + " - " + koDescriptionFilm);
						}
					}catch (Exception e) {
						LOG.info("Update Description Film: " + e.getMessage());
					}
					
					/** 
					 * Create Film Sub Map
					 * */
//					try {
//						List<LanguageFilm> subList = new ArrayList<DbAtexpatsUpdaterJob.LanguageFilm>();
//						String subFilmStr = null;
//						
//						// Get subFilm form language or version film
//						if(filmLanguage != null || version != null){
//							if(filmLanguage != null){
//								subFilmStr = filmLanguage;
//							} else{
//								subFilmStr = version;
//							}
//						}
//						
//						// Add sub film to List sub
//						if(subFilmStr != null ){
//							subList = LanguageFilm.getByNameLanguage(subFilmStr);
//						}
//						String languageFilm = null;
//						List<String> subLanguageList = new ArrayList<String>();
//						
//						// Get the language film and sub language film
//						for(int i = 0 ; i < subList.size(); i++) {
//							if(i == 0){
//								languageFilm = subList.get(i).getId();
//							} else {
//								subLanguageList.add(subList.get(i).getId());
//							}
//						}
//						
//						// Insert the language film and sub language film
//						if(subLanguageList.size() <= 0 ){
//							LOG.info(
//									"createFilmSub : " + filmId +
//									" - " + languageFilm +
//									" - " + null
//									);
//							String filmSubMapResult = hibernateUtils.createFilmSub(filmId, languageFilm, null);
//							if(filmSubMapResult.contains(failure)){
//								LOG.info("filmSubMapResult "  + filmSubMapResult);
//							} else {
//								LOG.info("filmSubMapResult "  + filmSubMapResult);
//							}
//						} else{
//							for(int i =0; i< subLanguageList.size(); i++){
//								LOG.info(
//										"createFilmSub-" + filmId +
//										"-" + languageFilm +
//										"-" + subLanguageList.get(i)
//										);
//								String filmSubMapResult = hibernateUtils.createFilmSub(filmId, languageFilm, subLanguageList.get(i));
//								if(filmSubMapResult.contains(failure)){
//									LOG.info("filmSubMapResult "  + filmSubMapResult);
//								} else {
//									LOG.info("filmSubMapResult "  + filmSubMapResult);
//								}
//							}
//						}
//					}catch (Exception e) {
//						LOG.info("Create Film Sub Map: " + e.getMessage());
//					}
//					LOG.info("");
					
					
					/** 
					 * Create Film Sub Map
					 * */
					try {
						if(!StringUtil.isEmpty(filmLanguage) || !StringUtil.isEmpty(version)) {
							List<LanguageBean> languageList = hibernateUtils.getAllLangugeFromDbAtexpats(); 
							LanguageBean[] arrayBean = new LanguageBean[2];
							LanguageBean bean = null;
							String result = "";
							if(version != null) {
								String[] array = version.split("\\s+");
								for(String a : array) {
									bean = AtexpatsUtils.getLanguageCode(a, languageList);
									if(bean == null) {
										continue;
									}
									
									if(arrayBean[0] == null) {
										arrayBean[0] = bean;
									} else if(arrayBean[1] == null) {
										arrayBean[1] = bean;
									}
									
								}
							} 
							
							if(filmLanguage != null) {
								bean = AtexpatsUtils.getLanguageCode(filmLanguage, languageList);
								if(bean != null) {
									page.putToMetadata(new Utf8(ConstantsTikaParser.film_language_code), ByteBuffer.wrap(Bytes
											.toBytes(bean.getCode())));
									
									if(arrayBean[1] != null) {
										result = hibernateUtils.createFilmSub(filmId, bean.getCode(), arrayBean[1].getCode());
										
										page.putToMetadata(new Utf8(ConstantsTikaParser.film_sub_language_code), ByteBuffer.wrap(Bytes
												.toBytes(arrayBean[1].getCode())));
										page.putToMetadata(new Utf8(ConstantsTikaParser.film_sub_language), ByteBuffer.wrap(Bytes
												.toBytes(arrayBean[1].getLanguageTranslate())));
										LOG.info("filmSubMapResult: "  + result + " : " + filmId +" - " + bean.getCode() + "/" + arrayBean[1].getCode());
									} else {
										result = hibernateUtils.createFilmSub(filmId, bean.getCode(), null);
										LOG.info("filmSubMapResult: "  + result + " : " + filmId +" - " + bean.getCode() + "/null");
									}
								}
							} else if(arrayBean[0] != null && arrayBean[1] != null) {
								result = hibernateUtils.createFilmSub(filmId, arrayBean[0].getCode(), arrayBean[1].getCode());
								
								page.putToMetadata(new Utf8(ConstantsTikaParser.film_language_code), ByteBuffer.wrap(Bytes
										.toBytes(arrayBean[0].getCode())));
								page.putToMetadata(new Utf8(ConstantsTikaParser.film_language), ByteBuffer.wrap(Bytes
										.toBytes(arrayBean[0].getLanguageTranslate())));
								page.putToMetadata(new Utf8(ConstantsTikaParser.film_sub_language_code), ByteBuffer.wrap(Bytes
										.toBytes(arrayBean[1].getCode())));
								page.putToMetadata(new Utf8(ConstantsTikaParser.film_sub_language), ByteBuffer.wrap(Bytes
										.toBytes(arrayBean[1].getLanguageTranslate())));
								
								LOG.info("filmSubMapResult: "  + result + " : " + filmId +" - " + arrayBean[0].getCode() + "/" + arrayBean[1].getCode());
							}
						}
						
					}catch (Exception e) {
						LOG.info("Create Film Sub Map: " + e.getMessage());
					}
					
					
					/** 
					 * Create Film Country Map
					 * */
					try {
						if(country != null && country.length() > 0) {
							String result = "";
							StringBuffer countryCodeBuff = new StringBuffer();
							StringBuffer countryFilmBuff = new StringBuffer();
							List<CountryBean> listCountry = hibernateUtils.getAllCountryFromDbAtexpats();
							CountryBean countryBean = null;
							for(String s : country.split(",")) {
								countryBean = AtexpatsUtils.getCountryFromDbAtexpats(s, listCountry);
								if(countryBean == null) {
									continue;
								}
								
								result = hibernateUtils.createFilmCountry(filmId, countryBean.getCountryCode());
								LOG.info("filmCountryMapResult: " + result + " : "+ countryBean.getCountryCode() + " - " + countryBean.getCountryName() + " - " + countryBean.getCountryTranslate() + " - " + countryBean.getLanguageCode());
								
								countryCodeBuff = AtexpatsUtils.append(countryBean.getCountryCode(), ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, countryCodeBuff);
								countryFilmBuff = AtexpatsUtils.append(s, ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, countryFilmBuff);
								
							}
							
							page.putToMetadata(new Utf8(ConstantsTikaParser.film_country), ByteBuffer.wrap(Bytes
									.toBytes(countryFilmBuff.toString())));
							page.putToMetadata(new Utf8(ConstantsTikaParser.film_country_code), ByteBuffer.wrap(Bytes
									.toBytes(countryCodeBuff.toString())));
						}
						
					}catch (Exception e) {
						LOG.info("Create Film Country Map: " + e.getMessage());
					}
					LOG.info("");
					
					
					/** 
					 * Create Director - Film Director Map 
					 */

					// insert for En Director :
					StringBuffer peopleIdString = new StringBuffer();
					if("en".equals(viewLanguageCode)) {
						peopleIdString = insertPeole(filmId, "en", enDirectorList, nameDirectors, partInFilmDirector, peopleIdString, hibernateUtils);
						page.putToMetadata(new Utf8(ConstantsTikaParser.director_index_en_text_multi), ByteBuffer.wrap(Bytes
								.toBytes(nameDirectors.toString())));
					} else {
						peopleIdString = insertPeole(filmId, "en", enDirectorList, enDirector, partInFilmDirector, peopleIdString, hibernateUtils);
					}
					// insert for Vi Director :
					if("vi".equals(viewLanguageCode)) {
						peopleIdString = insertPeole(filmId, "vi", viDirectorList, nameDirectors, partInFilmDirector, peopleIdString, hibernateUtils);
						page.putToMetadata(new Utf8(ConstantsTikaParser.director_index_vi_text_multi), ByteBuffer.wrap(Bytes
								.toBytes(nameDirectors.toString())));
					} else {
						peopleIdString = insertPeole(filmId, "vi", viDirectorList, viDirector, partInFilmDirector, peopleIdString, hibernateUtils);
					}
					// insert for Ja Director :
					if("ja".equals(viewLanguageCode)) {
						peopleIdString = insertPeole(filmId, "ja", jaDirectorList, nameDirectors, partInFilmDirector, peopleIdString, hibernateUtils);
						page.putToMetadata(new Utf8(ConstantsTikaParser.director_index_ja_text_multi), ByteBuffer.wrap(Bytes
								.toBytes(nameDirectors.toString())));
					} else {
						peopleIdString = insertPeole(filmId, "ja", jaDirectorList, jaDirector, partInFilmDirector, peopleIdString, hibernateUtils);
					}
					// insert for Ko Director :
					if("ko".equals(viewLanguageCode)) {
						peopleIdString = insertPeole(filmId, "ko", koDirectorList, nameDirectors, partInFilmDirector, peopleIdString, hibernateUtils);
						page.putToMetadata(new Utf8(ConstantsTikaParser.director_index_ko_text_multi), ByteBuffer.wrap(Bytes
								.toBytes(nameDirectors.toString())));
					} else {
						peopleIdString = insertPeole(filmId, "ko", koDirectorList, koDirector, partInFilmDirector, peopleIdString, hibernateUtils);
					}
					
					if(!StringUtil.isEmpty(peopleIdString.toString())) {
						page.putToMetadata(new Utf8(ConstantsTikaParser.director_id), ByteBuffer.wrap(Bytes
								.toBytes(peopleIdString.toString())));
						LOG.info("director_id : " + peopleIdString.toString());
					}
					
					
					
//					StringBuffer peopleIdString = new StringBuffer();
//					try {
//						if(enDirectorList != null){
//							pattern = Pattern.compile(PATTERN_ACTOR_LIST);
//							String[] directorArr = pattern.split(enDirectorList);
//							
//							int percen = -1;
//							int dolar = -1;
//							String result = "";
//							String nameDirector = "";
//							String avatarDirector = "";
//							String shortDesDirector = "";
//							
//							for(String directorStr : directorArr){
//								nameDirector = "";
//								avatarDirector = "";
//								shortDesDirector = "";
//								
//								percen = directorStr.indexOf("%%");
//								dolar = directorStr.indexOf("$$");
//								if(dolar > percen && percen > 0) {
//									nameDirector = directorStr.substring(0, percen);
//									avatarDirector = directorStr.substring(percen + 2, dolar);
//									shortDesDirector = directorStr.substring(dolar + 2);
//								} else {
//									continue;
//								}
//	
//								LOG.info(
//										"director, createPeople-" + viewLanguageCode + 
//										"-" + nameDirector +
//										"-" + avatarDirector +
//										"-" + shortDesDirector +
//										"-" + cmsMemberId +
//										"-" + viewLanguageCode +
//										"-" + shortDesDirector
//										);
//								result = hibernateUtils.createPeople(
//										viewLanguageCode, nameDirector, avatarDirector, shortDesDirector, 
//										cmsMemberId, viewLanguageCode, shortDesDirector);
//								if(result.contains(failure)){
//									LOG.info("Director, createPeople " + result);
//								} else {
//									pattern = Pattern.compile(PATTERN_QUERY_RESULT);
//									String[] peopleArr = pattern.split(result);
//									peopleId = peopleArr[1];
//									peopleIdString = AtexpatsUtils.append(peopleId, ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, peopleIdString);
//									
//									result  = hibernateUtils.updateDisplayPeople(
//											peopleId, nameDirector, viewLanguageCode, viewLanguageCode);
//									LOG.info("Director - peopleDisplayResult: " + result + peopleId + " - " + nameDirector + " - " + viewLanguageCode);
//		
//									result = hibernateUtils.createFilmPeopleMap(
//											filmId, peopleId, partInFilmDirector);
//									LOG.info("Director - filmPeopleMapResult: "  + result + " : " + filmId + " - " + peopleId + " - " + partInFilmDirector);
//								}
//							}
//							
//							if(!StringUtil.isEmpty(peopleIdString.toString())) {
//								page.putToMetadata(new Utf8(ConstantsTikaParser.director_id), ByteBuffer.wrap(Bytes
//										.toBytes(peopleIdString.toString())));
//								LOG.info("Director Id From DB: " + peopleIdString);
//							}
//						} else {
//							// only inser Name of director
//							String result = "";
//							for(String nameDirector : nameDirectorsArr){
//								nameDirector = nameDirector.trim();
//								
//								result = hibernateUtils.createPeople(
//										viewLanguageCode, nameDirector, null, null, 
//										cmsMemberId, viewLanguageCode, null);
//								if(result.contains(failure)){
//									LOG.info("director - createPeople " + result + " : " + viewLanguageCode + " - " + nameDirector +" - " + cmsMemberId );
//								} else {
//									pattern = Pattern.compile(PATTERN_QUERY_RESULT);
//									String[] peopleArr = pattern.split(result);
//									peopleId = peopleArr[1];
//									peopleIdString = AtexpatsUtils.append(peopleId, ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, peopleIdString);
//								
//									result  = hibernateUtils.updateDisplayPeople(
//											peopleId, nameDirector, null, viewLanguageCode);
//									LOG.info("director - peopleDisplayResult: " + result + " : " +peopleId + " - " + nameDirector + " - " + viewLanguageCode);
//									
//									result = hibernateUtils.createFilmPeopleMap(
//											filmId, peopleId, partInFilmDirector);
//									LOG.info("director - filmPeopleMapResult: "  + result + " : " + filmId + " - " + peopleId + "-" + partInFilmDirector);
//								}
//							}
//							
//							if(!StringUtil.isEmpty(peopleIdString.toString())) {
//								page.putToMetadata(new Utf8(ConstantsTikaParser.director_id), ByteBuffer.wrap(Bytes
//										.toBytes(peopleIdString.toString())));
//								LOG.info("Director Id From DB: " + peopleIdString);
//							}
//						}
//					}catch (Exception e) {
//						LOG.info("Create Director - Film Director Map: " + e.getMessage());
//					}
//					
					

					
					
					LOG.info("");
					/** 
					 * Create Actor - Film Actor Map \
					 */
					
					// insert for En Actor
					peopleIdString = new StringBuffer();
					if("en".equals(viewLanguageCode)) {
						peopleIdString = insertPeole(filmId, "en", enActorList, nameActors, partInFilmActor, peopleIdString, hibernateUtils);
						page.putToMetadata(new Utf8(ConstantsTikaParser.actor_index_en_text_multi), ByteBuffer.wrap(Bytes
								.toBytes(nameActors.toString())));
					} else {
						peopleIdString = insertPeole(filmId, "en", enActorList, enActor, partInFilmActor, peopleIdString, hibernateUtils);
					}
					// insert for Vi Actor
					if("vi".equals(viewLanguageCode)) {
						peopleIdString = insertPeole(filmId, "vi", viActorList, nameActors, partInFilmActor, peopleIdString, hibernateUtils);
						page.putToMetadata(new Utf8(ConstantsTikaParser.actor_index_vi_text_multi), ByteBuffer.wrap(Bytes
								.toBytes(nameActors.toString())));
					} else {
						peopleIdString = insertPeole(filmId, "vi", viActorList, viActor, partInFilmActor, peopleIdString, hibernateUtils);
					}
					// insert for Ja Actor
					if("ja".equals(viewLanguageCode)) {
						peopleIdString = insertPeole(filmId, "ja", jaActorList, nameActors, partInFilmActor, peopleIdString, hibernateUtils);
						page.putToMetadata(new Utf8(ConstantsTikaParser.actor_index_ja_text_multi), ByteBuffer.wrap(Bytes
								.toBytes(nameActors.toString())));
					} else {
						peopleIdString = insertPeole(filmId, "ja", jaActorList, jaActor, partInFilmActor, peopleIdString, hibernateUtils);
					}
					// insert for Ko Actor
					if("ko".equals(viewLanguageCode)) {
						peopleIdString = insertPeole(filmId, "ko", koActorList, nameActors, partInFilmActor, peopleIdString , hibernateUtils);
						page.putToMetadata(new Utf8(ConstantsTikaParser.actor_index_ko_text_multi), ByteBuffer.wrap(Bytes
								.toBytes(nameActors.toString())));
					} else {
						peopleIdString = insertPeole(filmId, "ko", koActorList, koActor, partInFilmActor, peopleIdString, hibernateUtils);
					}
					
					if(!StringUtil.isEmpty(peopleIdString.toString())) {
						page.putToMetadata(new Utf8(ConstantsTikaParser.actor_id), ByteBuffer.wrap(Bytes
								.toBytes(nameActors.toString())));
						LOG.info("actor_id : " + peopleIdString.toString());
					}
//					
//					peopleIdString = new StringBuffer();
//					try {
//						if(enActorList != null){
//							String result = "";
//							pattern = Pattern.compile(PATTERN_ACTOR_LIST);
//							String[] actorArr = pattern.split(enActorList);
//							
//							int percen = -1;
//							int dolar = -1;
//							String nameActor = "";
//							String avatarActor = "";
//							String shortDesActor = "";
//							
//							for(String actorStr : actorArr){
//								nameActor = "";
//								avatarActor = "";
//								shortDesActor = "";
//								
//								percen = actorStr.indexOf("%%");
//								dolar = actorStr.indexOf("$$");
//								if(dolar > percen && percen > 0) {
//									nameActor = actorStr.substring(0, percen);
//									avatarActor = actorStr.substring(percen + 2, dolar);
//									shortDesActor = actorStr.substring(dolar + 2);
//								} else {
//									continue;
//								}
//	
//								LOG.info(
//										"actor, createPeople-" + viewLanguageCode + 
//										"-" + nameActor +
//										"-" + avatarActor +
//										"-" + shortDesActor +
//										"-" + cmsMemberId +
//										"-" + viewLanguageCode +
//										"-" + shortDesActor
//										);
//								result = hibernateUtils.createPeople(
//										viewLanguageCode, nameActor, avatarActor, shortDesActor, 
//										cmsMemberId, viewLanguageCode, shortDesActor);
//								if(result.contains(failure)){
//									LOG.info("actor, createPeople " + result);
//								} else {
//									pattern = Pattern.compile(PATTERN_QUERY_RESULT);
//									String[] peopleArr = pattern.split(result);
//									peopleId = peopleArr[1];
//									peopleIdString = AtexpatsUtils.append(peopleId, ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, peopleIdString);
//									
//									result  = hibernateUtils.updateDisplayPeople(
//											peopleId, nameActor, shortDesActor, viewLanguageCode);
//									LOG.info("Actor, peopleDisplayResult: " + result + " : " + peopleId +
//											"-" + nameActor +
//											"-" + viewLanguageCode);
//	
//									result = hibernateUtils.createFilmPeopleMap(
//											filmId, peopleId, partInFilmActor);
//									LOG.info("filmPeopleMapResult "  + result + " : " + filmId +
//											"-" + peopleId +
//											"-" + partInFilmActor);
//								}
//							}
//							
//							if(!StringUtil.isEmpty(peopleIdString.toString())) {
//								page.putToMetadata(new Utf8(ConstantsTikaParser.actor_id), ByteBuffer.wrap(Bytes
//										.toBytes(peopleIdString.toString())));
//								LOG.info("Actor Id From DB: " + peopleIdString );
//							}
//						} else {
//							// only insert name of Actor 
//							String result = "";
//							for(String nameActor : nameActorsArr){
//								nameActor = nameActor.trim();
//								LOG.info(
//										"actor, createPeople-" + viewLanguageCode + 
//										"-" + nameActor +
//										"-" + cmsMemberId +
//										"-" + viewLanguageCode 
//										);
//								result = hibernateUtils.createPeople(
//										viewLanguageCode, nameActor, null, null, 
//										cmsMemberId, viewLanguageCode, null);
//								if(result.contains(failure)){
//	
//								} else {
//									String[] peopleArr = result.split("::");
//									peopleId = peopleArr[1];
//									peopleIdString = AtexpatsUtils.append(peopleId, ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, peopleIdString);
//									
//									result  = hibernateUtils.updateDisplayPeople(
//											peopleId, nameActor, null, viewLanguageCode);
//									LOG.info("Actor, peopleDisplayResult: " + result + " : " + peopleId +
//											"-" + nameActor +
//											"-" + viewLanguageCode);
//	
//									result = hibernateUtils.createFilmPeopleMap(
//											filmId, peopleId, partInFilmActor);
//									LOG.info("Actor, filmPeopleMapResult: "  + result + " : " +filmId +
//											" - " + peopleId +
//											" - " + partInFilmActor);
//								}
//							}
//							if(!StringUtil.isEmpty(peopleIdString.toString())) {
//								page.putToMetadata(new Utf8(ConstantsTikaParser.actor_id), ByteBuffer.wrap(Bytes
//										.toBytes(peopleIdString.toString())));
//								LOG.info("Actor Id From DB: " + peopleIdString );
//							}
//						}
//					} catch (Exception e) {
//						LOG.info("Create Actor - Film Actor Map: " + e.getMessage());
//					}

					
					LOG.info("");
					/**
					 * Create Film Review
					 * */
					try {
						if(!StringUtil.isEmpty(reviews)){
							String result = "";
							String reviewFrom = "";
							String totalReview = "";
							String contentReview = "";
							pattern = Pattern.compile(PATTERN_REVIEW_LIST);
							String[] reviewArr = pattern.split(reviews);
							String[] reviewFieldArr = null;
							
							StringBuffer reviewInfo = new StringBuffer();
							StringBuffer reviewContentInfo = new StringBuffer();

							for(String reviewString : reviewArr){pattern = Pattern.compile(PATTERN_REVIEW_FIELD);
								reviewFieldArr = pattern.split(reviewString);
	
								if (reviewFieldArr == null || reviewFieldArr.length != 3) {
									continue;
								}
								totalReview = reviewFieldArr[0];
								reviewFrom = reviewFieldArr[1];
								contentReview = reviewFieldArr[2];
	
								result = hibernateUtils.createFilmReview(viewLanguageCode, filmId, reviewFrom, totalReview, contentReview);
								LOG.info("createFilmReview: " + result + " : " + viewLanguageCode + "-" + filmId + "-" + reviewFrom + "-"
										+ totalReview + "-" + contentReview);
								
								reviewInfo = AtexpatsUtils.append(reviewFrom + "::" + totalReview, ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, reviewInfo);
								reviewContentInfo = AtexpatsUtils.append(reviewFrom + "::" + contentReview, ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, reviewContentInfo);
							}
							
							if(!StringUtil.isEmpty(reviewInfo.toString())) {
								page.putToMetadata(new Utf8(ConstantsTikaParser.review_info), ByteBuffer.wrap(Bytes
										.toBytes(reviewInfo.toString())));
							}
							
							if(!StringUtil.isEmpty(reviewContentInfo.toString())) {
								page.putToMetadata(new Utf8(ConstantsTikaParser.review_content_index_en_text_multi), ByteBuffer.wrap(Bytes
										.toBytes(reviewContentInfo.toString())));
							}
							
						}
						
					} catch (Exception e) {
						LOG.info("Create Film Review: " + e.getMessage());
					}
					LOG.info("");
					
					/** Create Film Schedule */
					try {
						/**
						 * Prepare data of schedule 
						 */	
						String dateStart = null;
						String dateEnd = null;
						showing_time = showing_time.replaceAll("\\D+","");
						if(showing_time.length() == 16){
							Calendar calendar = Calendar.getInstance();
	
							int start_day = Integer.parseInt(showing_time.substring(0, 2));
							int start_month = Integer.parseInt(showing_time.substring(2, 4));
							int start_year = Integer.parseInt(showing_time.substring(4, 8));
	
							calendar.set(start_year, start_month - 1, start_day, 0, 0);
							dateStart = String.valueOf((calendar.getTimeInMillis()/1000));
	
							int end_day = Integer.parseInt(showing_time.substring(8, 10));
							int end_month = Integer.parseInt(showing_time.substring(10, 12));
							int end_year = Integer.parseInt(showing_time.substring(12, 16));
							calendar.set(end_year, end_month - 1, end_day, 0, 0);
							dateEnd = String.valueOf((calendar.getTimeInMillis()/1000));
						}
						
						String[] nameScheduleCinemasArr = {};
						
						List<CinemaBean> listCinema = hibernateUtils.getAllCinemaFromDbAtexpats();
						
						if(!StringUtil.isEmpty(nameScheduleCinemas)) {

							pattern = Pattern.compile(PATTERN_NAME_SCHEDULE_CINEMA);
							nameScheduleCinemasArr = pattern.split(nameScheduleCinemas);

							// Loop for String to create Film Schedule
							CinemaBean cinemaBean = null;
							String cinemaId = "";
							String result = "";
							
							StringBuffer enCinemaName = new StringBuffer();
							StringBuffer viCinemaName = new StringBuffer();
							StringBuffer jaCinemaName = new StringBuffer();
							StringBuffer koCinemaName = new StringBuffer();
							
							StringBuffer cinemaIdBuffer = new StringBuffer();
							StringBuffer cinemaFilmStart = new StringBuffer();
							StringBuffer cinemaFilmEnd = new StringBuffer();
							StringBuffer cinemaFilmSchedule = new StringBuffer();

							for (String nameScheduleCinema : nameScheduleCinemasArr) {
								String filmDateTime = "";

								if (StringUtil.isEmpty(nameScheduleCinema)) {
									continue;
								}

								pattern = Pattern.compile(PATTERN_NAME_SCHEDULE);

								// [0]: nameCinema, [1]: date, [ >= 2] time
								String[] name_scheduleArr = pattern.split(nameScheduleCinema);

								// get cinema existed in DB
								cinemaBean = AtexpatsUtils.getCinemaFromDbAtexpats(name_scheduleArr[0], listCinema);
								if (cinemaBean == null) {
									continue;
								}

								// field to indexed
								if (!cinemaBean.getCinemaId().equals(cinemaId)) {
									cinemaIdBuffer = AtexpatsUtils.append(cinemaBean.getCinemaId(), ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, cinemaIdBuffer);
									cinemaFilmStart = AtexpatsUtils.append(cinemaBean.getCinemaId() + "::" + dateStart,
											ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, cinemaFilmStart);
									cinemaFilmEnd = AtexpatsUtils.append(cinemaBean.getCinemaId() + "::" + dateEnd,
											ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, cinemaFilmEnd);
									
									enCinemaName = setCinemaNameMultiLanguage(cinemaBean.getCinemaId(), "en", ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN	, enCinemaName, listCinema);
									viCinemaName = setCinemaNameMultiLanguage(cinemaBean.getCinemaId(), "vi", ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN	, viCinemaName, listCinema);
									jaCinemaName = setCinemaNameMultiLanguage(cinemaBean.getCinemaId(), "ja", ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN	, jaCinemaName, listCinema);
									koCinemaName = setCinemaNameMultiLanguage(cinemaBean.getCinemaId(), "ko", ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN	, koCinemaName, listCinema);
								}
								cinemaId = cinemaBean.getCinemaId();

								LOG.info("Cinema: " + cinemaBean.getCinemaId() + " - " + cinemaBean.getCinemaName() + " - "
										+ name_scheduleArr[0].trim());

								/**
								 * Create Film In Time
								 */
								result = hibernateUtils.createFilmTime(filmId, cinemaBean.getCinemaId(), dateStart, dateEnd);
								LOG.info("createFilmTime : " + result + " : " + filmId + " - " + cinemaBean.getCinemaId() + " - "
										+ dateStart + " - " + dateEnd);

								if (name_scheduleArr.length > 2) {
									Calendar calendar = Calendar.getInstance();
									int day = 0;
									int month = 0;
									int year = 0;
									int hour = 0;
									int minute = 0;

									String dateStr = name_scheduleArr[1];
									pattern = Pattern.compile(PATTERN_DATE);
									String[] dateArr = pattern.split(dateStr);
									String numberDate = "";
									if (dateArr.length > 1) {
										System.out.println("dateArr[1]" + dateArr[1]);
										numberDate = dateArr[1].replaceAll("\\D+", "");
									} else if (dateArr.length == 1) {
										System.out.println("dateArr[0]" + dateArr[0]);
										numberDate = dateArr[0].replaceAll("\\D+", "");
									}

									if (numberDate.length() != 8) {
										return;
									}

									day = Integer.parseInt(numberDate.substring(0, 2));
									month = Integer.parseInt(numberDate.substring(2, 4));
									year = Integer.parseInt(numberDate.substring(4, 8));

									// get time
									String numberTime = "";
									for (int i = 2; i < name_scheduleArr.length; i++) {
										numberTime = name_scheduleArr[i].replaceAll("\\D+", "");
										if (numberTime.length() == 4 && year != 0 && month != 0 && day != 0) {
											hour = Integer.parseInt(numberTime.substring(0, 2));
											minute = Integer.parseInt(numberTime.substring(2, 4));
											calendar.set(year, month - 1, day, hour, minute);
											calendar.set(Calendar.SECOND, 0);
											calendar.set(Calendar.MILLISECOND, 0);
											filmDateTime = String.valueOf(calendar.getTimeInMillis() / 1000);

											result = hibernateUtils.createFilmSchedule(cinemaBean.getCinemaId(), filmId, null, null, filmDateTime);
											LOG.info("createFilmSchedule: " + result + " : " + cinemaBean.getCinemaId() + " - " + filmId
													+ "-" + filmDateTime);

											// field to indexed
											cinemaFilmSchedule = AtexpatsUtils.append(cinemaBean.getCinemaId() + "::" + filmId + "::" + filmDateTime,
													ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, cinemaFilmSchedule);
										}
									}
								}
							}

							// set Metadata cinema_film_start, cinema_film_end,
							// cinema_film_schedule

							if(!StringUtil.isEmpty(cinemaFilmStart.toString())) {
								page.putToMetadata(new Utf8(ConstantsTikaParser.cinema_film_start), ByteBuffer.wrap(Bytes
										.toBytes(cinemaFilmStart.toString())));
							}
							
							if(!StringUtil.isEmpty(cinemaFilmEnd.toString())) {
								page.putToMetadata(new Utf8(ConstantsTikaParser.cinema_film_end), ByteBuffer.wrap(Bytes
										.toBytes(cinemaFilmEnd.toString())));
							}
							
							if(!StringUtil.isEmpty(cinemaFilmSchedule.toString())) {
								page.putToMetadata(new Utf8(ConstantsTikaParser.cinema_film_schedule), ByteBuffer.wrap(Bytes
										.toBytes(cinemaFilmSchedule.toString())));
							}
							
							if(!StringUtil.isEmpty(enCinemaName.toString())) {
								page.putToMetadata(new Utf8(ConstantsTikaParser.cinema_index_en_text_multi), ByteBuffer.wrap(Bytes
										.toBytes(enCinemaName.toString())));
							}
							
							if(!StringUtil.isEmpty(viCinemaName.toString())) {
								page.putToMetadata(new Utf8(ConstantsTikaParser.cinema_index_vi_text_multi), ByteBuffer.wrap(Bytes
										.toBytes(viCinemaName.toString())));
							}
							
							if(!StringUtil.isEmpty(jaCinemaName.toString())) {
								page.putToMetadata(new Utf8(ConstantsTikaParser.cinema_index_ja_text_multi), ByteBuffer.wrap(Bytes
										.toBytes(jaCinemaName.toString())));
							}
							
							if(!StringUtil.isEmpty(koCinemaName.toString())) {
								page.putToMetadata(new Utf8(ConstantsTikaParser.cinema_index_ko_text_multi), ByteBuffer.wrap(Bytes
										.toBytes(koCinemaName.toString())));
							}
							
							LOG.info("cinema_film_start: " + cinemaFilmStart);
							LOG.info("cinema_film_end: " + cinemaFilmEnd);
							LOG.info("cinema_film_schedule: " + cinemaFilmSchedule);
							LOG.info("enCinemaName: " + enCinemaName.toString());
							LOG.info("viCinemaName: " + viCinemaName.toString());
							LOG.info("jaCinemaName: " + jaCinemaName.toString());
							LOG.info("koCinemaName: " + koCinemaName.toString());
							
						
						}
						
					}catch (Exception e) {
						LOG.info("Create Film Schedule: " + e.getMessage());
					}

					transaction.commit();
					session.close();

				} catch(Exception e){
					e.printStackTrace();
					transaction.rollback();
					session.close();
				} finally {
					session.close();
				}
			}

			/*if(true){
				return;
			}

			Utf8 mark = Mark.UPDATEDB_MARK.checkMark(page);
			if (!batchId.equals(REINDEX)) {
				if (!NutchJob.shouldProcess(mark, batchId)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Skipping " + TableUtil.unreverseUrl(key) + "; different batch id (" + mark + ")");
					}
					return;
				}
			}

			NutchDocument doc = indexUtil.index(key, page);
			if (doc == null) {
				return;
			}
			if (mark != null) {
				Mark.INDEX_MARK.putMark(page, Mark.UPDATEDB_MARK.checkMark(page));
				store.put(key, page);
			}
			context.write(key, doc);*/
		}
	}
	
	
	private static StringBuffer setCinemaNameMultiLanguage(String cinemaId, String languageCode, String regex, StringBuffer cinemaBuffer, List<CinemaBean> list) {
		for(CinemaBean bean : list) {
			if(cinemaId.equals(bean.getCinemaId()) && languageCode.equals(bean.getLanguageCode())) {
				return AtexpatsUtils.append(bean.getCinemaName(), regex, cinemaBuffer);
			}
		}
		return cinemaBuffer;
	}
	
	private static  StringBuffer insertPeole(String filmId, String languageCode, String peopleDetailList,
							String namePepleList, String peopleType,StringBuffer peopleIdString, HibernateUtils hibernateUtils) {
		try {
			Pattern pattern = null;
			String peopleId = null;
			
			if(!StringUtil.isEmpty(peopleDetailList)){
				String result = "";
				pattern = Pattern.compile(PATTERN_ACTOR_LIST);
				String[] peopleArr = pattern.split(peopleDetailList);
				
				int percen = -1;
				int dolar = -1;
				String namePeople = "";
				String avatarPeople = "";
				String shortDesPeople = "";
				
				for(String peopleStr : peopleArr){
					namePeople = "";
					avatarPeople = "";
					shortDesPeople = "";
					
					percen = peopleStr.indexOf("%%");
					dolar = peopleStr.indexOf("$$");
					if(dolar > percen && percen > 0) {
						namePeople = peopleStr.substring(0, percen);
						avatarPeople = peopleStr.substring(percen + 2, dolar);
						shortDesPeople = peopleStr.substring(dolar + 2);
					} else {
						continue;
					}

					LOG.info(
							"createPeople with type " + peopleType + " : " + languageCode + 
							"-" + namePeople +
							"-" + avatarPeople +
							"-" + shortDesPeople +
							"-" + cmsMemberId +
							"-" + shortDesPeople
							);
					result = hibernateUtils.createPeople(
							languageCode, namePeople, avatarPeople, shortDesPeople, 
							cmsMemberId, languageCode, shortDesPeople, null);
					if(result.contains(failure)){
						LOG.info("createPeople with type " + peopleType +" :  " + result + " : " + namePeople);
					} else {
						pattern = Pattern.compile(PATTERN_QUERY_RESULT);
						String[] resultArr = pattern.split(result);
						peopleId = resultArr[1];
						
						// id to index
						if(!StringUtil.isEmpty(peopleIdString.toString()) && peopleIdString.toString().contains(peopleId)) {
							// not append if id existed 
						} else {
							peopleIdString = AtexpatsUtils.append(peopleId, ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, peopleIdString);
						}
						
						
						result  = hibernateUtils.updateDisplayPeople(
								peopleId, namePeople, shortDesPeople, null, languageCode);
						LOG.info("peopleDisplayResult: " + result + " : " + peopleId +
								"-" + namePeople +
								"-" + languageCode);

						result = hibernateUtils.createFilmPeopleMap(
								filmId, peopleId, peopleType);
						LOG.info("filmPeopleMapResult: "  + result + " : " + filmId +
								"-" + peopleId +
								"-" + peopleType);
					}
				}
				
//				if(!StringUtil.isEmpty(peopleIdString.toString())) {
//					page.putToMetadata(new Utf8(ConstantsTikaParser.actor_id), ByteBuffer.wrap(Bytes
//							.toBytes(peopleIdString.toString())));
//					LOG.info("Actor Id From DB: " + peopleIdString );
//				}
				
				
			} 
			if(!StringUtil.isEmpty(namePepleList)) {
				// only insert name of Actor 
				
				String result = "";
				String[] namePeopleArr  = namePepleList.split(",");
				
				for(String namePeople: namePeopleArr){
					//String name = nameActor;
					namePeople = namePeople.trim();
					LOG.info(
							"createPeople with type " + peopleType + " : " + languageCode + 
							" - " + namePeople + " - " + cmsMemberId );
					result = hibernateUtils.createPeople(
							languageCode, namePeople, null, null, 
							cmsMemberId, languageCode, null, null);
					if(result.contains(failure)){
						LOG.info("createPeople with type " + peopleType +" :  " + result + " : " + namePeople);
					} else {
						String[] resultArr = result.split("::");
						peopleId = resultArr[1];
						
						// id to index
						if(!StringUtil.isEmpty(peopleIdString.toString()) && peopleIdString.toString().contains(peopleId)) {
							// not append if id existed
						} else {
							peopleIdString = AtexpatsUtils.append(peopleId, ConstantsTikaParser.INDEX_MULTI_VALUES_PATTEN, peopleIdString);
						}
						
						result  = hibernateUtils.updateDisplayPeople(
								peopleId, namePeople, null, null, languageCode);
						LOG.info("peopleDisplayResult: " + result + " : " + peopleId +
								" - " + namePeople + " - " + languageCode );

						result = hibernateUtils.createFilmPeopleMap(
								filmId, peopleId, peopleType);
						LOG.info("filmPeopleMapResult: "  + result + " : " + filmId +
								" - " + peopleId + " - " + peopleType);
					}
				}
				
				
//				if(!StringUtil.isEmpty(peopleIdString.toString())) {
//					page.putToMetadata(new Utf8(ConstantsTikaParser.actor_id), ByteBuffer.wrap(Bytes
//							.toBytes(peopleIdString.toString())));
//					LOG.info("Actor Id From DB: " + peopleIdString );
//				}
				
			}
		
		}catch (Exception e) {
			LOG.info("Insert People: " + e.getMessage());
			return peopleIdString;
		}
		
		return peopleIdString;
		
	}



	private static Collection<WebPage.Field> getFields(Job job) {
		Configuration conf = job.getConfiguration();
		Collection<WebPage.Field> columns = new HashSet<WebPage.Field>(FIELDS);
		IndexingFilters filters = new IndexingFilters(conf);
		columns.addAll(filters.getFields());
		ScoringFilters scoringFilters = new ScoringFilters(conf);
		columns.addAll(scoringFilters.getFields());
		return columns;
	}

	protected Job createIndexJob(Configuration conf, String jobName, String batchId)
			throws IOException, ClassNotFoundException {
		/*conf.set(GeneratorJob.BATCH_ID, batchId);*/
		Job job = new NutchJob(conf, jobName);
		// TODO: Figure out why this needs to be here
		job.getConfiguration().setClass("mapred.output.key.comparator.class",
				StringComparator.class, RawComparator.class);

		Collection<WebPage.Field> fields = getFields(job);
		StorageUtils.initMapperJob(job, fields, String.class, NutchDocument.class,
				IndexerMapper.class);
		job.setNumReduceTasks(0);
		/*job.setOutputFormatClass(IndexerOutputFormat.class);*/
		return job;
	}
}
