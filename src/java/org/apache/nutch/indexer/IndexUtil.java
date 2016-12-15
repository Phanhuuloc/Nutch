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
package org.apache.nutch.indexer;

import org.apache.avro.util.Utf8;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.atexpats.common.AtexpatsConstants;
import org.apache.nutch.parse.utils.ConstantsTikaParser;
import org.apache.nutch.scoring.ScoringFilterException;
import org.apache.nutch.scoring.ScoringFilters;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.apache.nutch.util.TableUtil;

/**
 * Utility to create an indexed document from a webpage.  
 *
 */
public class IndexUtil {
  private static final Log LOG = LogFactory.getLog(new Object() {
  }.getClass().getEnclosingClass());
  
  
  private IndexingFilters filters;
  private ScoringFilters scoringFilters;
  
  public IndexUtil(Configuration conf) {
    filters = new IndexingFilters(conf);
    scoringFilters = new ScoringFilters(conf);
  }
  
  /**
   * Index a {@link Webpage}, here we add the following fields:
   * <ol>
   * <li><tt>id</tt>: default uniqueKey for the {@link NutchDocument}.</li>
   * <li><tt>digest</tt>: Digest is used to identify pages (like unique ID) and is used to remove
   * duplicates during the dedup procedure. It is calculated using {@link org.apache.nutch.crawl.MD5Signature} or
   * {@link org.apache.nutch.crawl.TextProfileSignature}.</li>
   * <li><tt>batchId</tt>: The page belongs to a unique batchId, this is its identifier.</li>
   * <li><tt>boost</tt>: Boost is used to calculate document (field) score which can be used within
   * queries submitted to the underlying indexing library to find the best results. It's part of the scoring algorithms. 
   * See scoring.link, scoring.opic, scoring.tld, etc.</li>
   * </ol>
   * 
   * @param key The key of the page (reversed url).
   * @param page The {@link Webpage}.
   * @return The indexed document, or null if skipped by index filters.
   */
  public NutchDocument index(String key, WebPage page) {
    NutchDocument doc = new NutchDocument();
    doc.add("id", key);
   /* doc.add("digest", StringUtil.toHexString(page.getSignature()));
    if (page.getBatchId() != null) {
      doc.add("batchId", page.getBatchId().toString());
    }*/
    
    String url = TableUtil.unreverseUrl(key);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Indexing URL: " + url);
    }

    try {
      doc = filters.filter(doc, url, page);
    } catch (IndexingException e) {
      LOG.warn("Error indexing "+key+": "+e);
      return null;
    }

    // skip documents discarded by indexing filters
    if (doc == null) return null;

    float boost = 1.0f;
    // run scoring filters
    try {
      boost = scoringFilters.indexerScore(url, doc, page, boost);
    } catch (final ScoringFilterException e) {
      LOG.warn("Error calculating score " + key + ": " + e);
      return null;
    }

    doc.setScore(boost);
    // store boost for use by explain and dedup
    /*doc.add("boost", Float.toString(boost));*/

    return doc;
  }
  
  // TODO add new index
  public NutchDocument index(
		  String key, WebPage page,
		  IndexInfos infos
		  ) {
	    NutchDocument doc = new NutchDocument();
	    String url = TableUtil.unreverseUrl(key);
	    
	    // add index
	    doc.add("id", infos.getUrlId());
	    
	    //doc.add("web_url", url);
	    //doc.add("producer", metadata);
	    //doc.add("film_duration", content);
	    //page.getMetadata();
//	    Map<Utf8, ByteBuffer> mt = page.getMetadata();
//	    doc.add(ConstantsTikaParser.idFilmFromDb, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.idFilmFromDb))));
//	    doc.add(ConstantsTikaParser.rating, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.rating))));
//	    doc.add(ConstantsTikaParser.producer, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.producer))));
//	    doc.add(ConstantsTikaParser.trailer, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.trailer))));
//	    doc.add(ConstantsTikaParser.age_limit, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.age_limit))));
//	    doc.add(ConstantsTikaParser.photo, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.photo))));
//	    doc.add(ConstantsTikaParser.year_product, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.year_product))));
//	    doc.add("film_duration", Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.duration))));
//	    doc.add(ConstantsTikaParser.producer, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.producer))));
	    //main_language_code
	    //language_code
	    //sub
//	    doc.add(ConstantsTikaParser.dimension, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.dimension))));
	    //delete flag
	    // country code
//	    String actorStr = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.actor)));
//	    if(actorStr != null) {
//		    String[] actorArr = actorStr.split(",");
//		    for(String actor : actorArr) {
//		    	doc.add("actors", actor.trim());
//		    }
//	    }
//	    String directorStr = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.director)));
//	    if(directorStr != null) {
//		    String[] directorArr = directorStr.split(",");
//		    for(String director : directorArr) {
//		    	doc.add("directors",director.trim());
//		    }
//	    }
	    
	    String urlStatus = AtexpatsConstants.STATUS_NEW;
	    if(page.getMetadata().containsKey(new Utf8("urlStatus"))) {
	    	urlStatus = Bytes.toString(page.getFromMetadata(new Utf8("urlStatus")));
	    	/*if(!StringUtils.isBlank(sameUrlId)) {
	    		doc.add("sameUrlId", sameUrlId);
	    	}*/
	    }
	    
	    String title = url + "::" + infos.getTitle();
	    String descMeta = (StringUtils.isBlank(infos.getDescMeta()) ? null : url + "::" + infos.getDescMeta());
	    String kwMeta = (StringUtils.isBlank(infos.getKwMeta()) ? null : url + "::" + infos.getKwMeta());
	    String content = url + "::" + infos.getText();
	    
	    if(AtexpatsConstants.STATUS_DELETE.equals(urlStatus)) {
	    	title = "";
	    	descMeta = "";
	    	kwMeta = "";
	    	content = "";
	    }
	    
	    addIndexByLanguage(doc, infos,
	    		title, descMeta, kwMeta, content);
	    
	    /* doc.add("digest", StringUtil.toHexString(page.getSignature()));
	    if (page.getBatchId() != null) {
	      doc.add("batchId", page.getBatchId().toString());
	    }*/

	    if (LOG.isDebugEnabled()) {
	      //LOG.debug("Indexing URL: " + url);
	    }
	    
	    String sameUrlId;
	    if(page.getMetadata().containsKey(new Utf8("sameUrlId"))) {
	    	sameUrlId = Bytes.toString(page.getFromMetadata(new Utf8("sameUrlId")));
	    	if(!StringUtils.isBlank(sameUrlId)) {
	    		doc.add("sameUrlId", sameUrlId);
	    	}
	    }

	    try {
	      doc = filters.filter(doc, url, page);
	    } catch (IndexingException e) {
	      LOG.warn("Error indexing "+key+": "+e);
	      return null;
	    }

	    // skip documents discarded by indexing filters
	    if (doc == null) return null;

	    float boost = 1.0f;
	    // run scoring filters
	    try {
	      boost = scoringFilters.indexerScore(url, doc, page, boost);
	    } catch (final ScoringFilterException e) {
	      LOG.warn("Error calculating score " + key + ": " + e);
	      return null;
	    }

	    doc.setScore(boost);
	    // store boost for use by explain and dedup
	    /*doc.add("boost", Float.toString(boost));*/

	    return doc;
	  }
  
  /**
   * index method for movies
   * @param key
   * @param page
   * @param infos
   * @return
   */
  public NutchDocument indexV2(
		  String key, WebPage page,
		  IndexInfos infos
		  ) {
	    NutchDocument doc = new NutchDocument();
	    String url = TableUtil.unreverseUrl(key);
	    
	    // add index
	    doc.add("id", infos.getUrlId());
	    
	    //doc.add("web_url", url);
	    //doc.add("producer", metadata);
	    //doc.add("film_duration", content);
	    //page.getMetadata();
//	    Map<Utf8, ByteBuffer> mt = page.getMetadata();
	    
	    doc.add(ConstantsTikaParser.film_id, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.film_id))));
	    doc.add(ConstantsTikaParser.total_rated, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.total_rated))));
	    doc.add(ConstantsTikaParser.rating, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.rating))));
	    doc.add(ConstantsTikaParser.trailer, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.trailer))));
	    doc.add(ConstantsTikaParser.age_limit, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.age_limit))));
	    doc.add(ConstantsTikaParser.photo, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.photo))));
	    doc.add(ConstantsTikaParser.year_product, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.year_product))));
	    doc.add(ConstantsTikaParser.film_duration, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.film_duration))));
	    doc.add(ConstantsTikaParser.producer, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.producer))));
	    doc.add(ConstantsTikaParser.dimension, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.dimension))));
	    
	    //title
	    doc.add(ConstantsTikaParser.title_index_en_text, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.title_index_en_text))));
	    doc.add(ConstantsTikaParser.title_index_vi_text, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.title_index_vi_text))));
	    doc.add(ConstantsTikaParser.title_index_ja_text, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.title_index_ja_text))));
	    doc.add(ConstantsTikaParser.title_index_ko_text, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.title_index_ko_text))));
	    
	    //description
	    doc.add(ConstantsTikaParser.desc_index_en_text, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.desc_index_en_text))));
	    doc.add(ConstantsTikaParser.desc_index_vi_text, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.desc_index_vi_text))));
	    doc.add(ConstantsTikaParser.desc_index_ja_text, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.desc_index_ja_text))));
	    doc.add(ConstantsTikaParser.desc_index_ko_text, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.desc_index_ko_text))));
	    
	    //content
	    doc.add(ConstantsTikaParser.content_index_en_text, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.content_index_en_text))));
	    doc.add(ConstantsTikaParser.content_index_vi_text, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.content_index_vi_text))));
	    doc.add(ConstantsTikaParser.content_index_ja_text, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.content_index_ja_text))));
	    doc.add(ConstantsTikaParser.content_index_ko_text, Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.content_index_ko_text))));
	    
	    //official_movie
	    addMultiValueField(doc, page, ConstantsTikaParser.official_movie);
	    
	    // production
	    addMultiValueField(doc, page, ConstantsTikaParser.production_id);
	    addMultiValueField(doc, page, ConstantsTikaParser.production_index_en_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.production_index_ja_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.production_index_ko_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.production_index_vi_text_multi);
	    
	    // film language	    
	    addMultiValueField(doc, page, ConstantsTikaParser.film_language_code);
	    addMultiValueField(doc, page, ConstantsTikaParser.film_language);
	    addMultiValueField(doc, page, ConstantsTikaParser.film_sub_language_code);
	    addMultiValueField(doc, page, ConstantsTikaParser.film_sub_language);
	    
	    // film country
	    addMultiValueField(doc, page, ConstantsTikaParser.film_country_code);
	    addMultiValueField(doc, page, ConstantsTikaParser.film_country);
	    
	    // film genre
	    addMultiValueField(doc, page, ConstantsTikaParser.film_genre_id);
	    addMultiValueField(doc, page, ConstantsTikaParser.genre_index_en_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.genre_index_ja_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.genre_index_ko_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.genre_index_vi_text_multi);
	    
	    // director
	    addMultiValueField(doc, page, ConstantsTikaParser.director_id);
	    addMultiValueField(doc, page, ConstantsTikaParser.director_index_en_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.director_index_ja_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.director_index_ko_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.director_index_vi_text_multi);
	    
	    // actor
	    addMultiValueField(doc, page, ConstantsTikaParser.actor_id);
	    addMultiValueField(doc, page, ConstantsTikaParser.actor_index_en_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.actor_index_ja_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.actor_index_ko_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.actor_index_vi_text_multi);
	   
	    // cinema
	    addMultiValueField(doc, page, ConstantsTikaParser.cinema_id);
	    addMultiValueField(doc, page, ConstantsTikaParser.cinema_index_en_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.cinema_index_ja_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.cinema_index_ko_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.cinema_index_vi_text_multi);
	    
	    // film schedule
	    addMultiValueField(doc, page, ConstantsTikaParser.cinema_film_start);
	    addMultiValueField(doc, page, ConstantsTikaParser.cinema_film_end);
	    addMultiValueField(doc, page, ConstantsTikaParser.cinema_film_schedule);
	    
	    // film review
	    addMultiValueField(doc, page, ConstantsTikaParser.review_info);
	    addMultiValueField(doc, page, ConstantsTikaParser.review_content_index_en_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.review_content_index_ja_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.review_content_index_ko_text_multi);
	    addMultiValueField(doc, page, ConstantsTikaParser.review_content_index_vi_text_multi);
	    
	    //addMovieIndexByLanguage(doc, page);

	    if (LOG.isDebugEnabled()) {
	      //LOG.debug("Indexing URL: " + url);
	    }

	    try {
	      doc = filters.filter(doc, url, page);
	    } catch (IndexingException e) {
	      LOG.warn("Error indexing "+key+": "+e);
	      return null;
	    }

	    // skip documents discarded by indexing filters
	    if (doc == null) return null;

	    float boost = 1.0f;
	    // run scoring filters
	    try {
	      boost = scoringFilters.indexerScore(url, doc, page, boost);
	    } catch (final ScoringFilterException e) {
	      LOG.warn("Error calculating score " + key + ": " + e);
	      return null;
	    }

	    doc.setScore(boost);
	    // store boost for use by explain and dedup
	    /*doc.add("boost", Float.toString(boost));*/

	    return doc;
	  }
  
  public NutchDocument indexWebSearch(
		  String key, WebPage page,
		  IndexInfos infos
		  ) {
	  NutchDocument doc = new NutchDocument();
	    String url = TableUtil.unreverseUrl(key);
	    
	    // add index
	    //doc.add("id", infos.getUrlId());
	    
	    //doc.add("web_url", url);
	    doc.add("id", url);
	    //doc.add("listing_id", infos.getListingId());
	    if(!StringUtils.isBlank(infos.getListingId())) {
	    	String[] idArr = infos.getListingId().split(",");
	    	for(String id : idArr) {
	    		if(!StringUtils.isBlank(id)) {
	    			doc.add("listing_id", id.trim());
	    		}
	    	}
	    }
	    
	    String urlStatus = AtexpatsConstants.STATUS_NEW;
	    if(page.getMetadata().containsKey(new Utf8("urlStatus"))) {
	    	urlStatus = Bytes.toString(page.getFromMetadata(new Utf8("urlStatus")));
	    }
	    
	    String title = infos.getTitle();
	    String descMeta = infos.getDescMeta();
	    String kwMeta = infos.getKwMeta();
	    String content = infos.getText();
	    
	    if(AtexpatsConstants.STATUS_DELETE.equals(urlStatus)) {
	    	title = "";
	    	descMeta = "";
	    	kwMeta = "";
	    	content = "";
	    }
	    
	    addIndexByLanguage(doc, infos,
	    		title, descMeta, kwMeta, content);
	    
	    /* doc.add("digest", StringUtil.toHexString(page.getSignature()));
	    if (page.getBatchId() != null) {
	      doc.add("batchId", page.getBatchId().toString());
	    }*/

	    if (LOG.isDebugEnabled()) {
	      //LOG.debug("Indexing URL: " + url);
	    }

	    try {
	      doc = filters.filter(doc, url, page);
	    } catch (IndexingException e) {
	      LOG.warn("Error indexing "+key+": "+e);
	      return null;
	    }

	    // skip documents discarded by indexing filters
	    if (doc == null) return null;

	    float boost = 1.0f;
	    // run scoring filters
	    try {
	      boost = scoringFilters.indexerScore(url, doc, page, boost);
	    } catch (final ScoringFilterException e) {
	      LOG.warn("Error calculating score " + key + ": " + e);
	      return null;
	    }

	    doc.setScore(boost);
	    // store boost for use by explain and dedup
	    /*doc.add("boost", Float.toString(boost));*/

	    return doc;
  }
  
  private boolean addIndexByLanguage(
		  NutchDocument doc, IndexInfos infos,
		  String title, String descMeta, String kwMeta, String content
		  ){
	  boolean result = false;
	  
	  if(!StringUtils.isBlank(title)) {
		  if(infos.getLanguageCodeTitle() == null || "".equals(infos.getLanguageCodeTitle()) || "en".equals(infos.getLanguageCodeTitle())){
			  doc.add("title_ws_en", title);
		  } else if("vi".equals(infos.getLanguageCodeTitle().trim())) {
			  doc.add("title_ws_vi", title);
		  } else if("ko".equals(infos.getLanguageCodeTitle().trim())) {
			  doc.add("title_ws_ko", title);
		  } else if("ja".equals(infos.getLanguageCodeTitle().trim())) {
			  doc.add("title_ws_ja", title);
		  } else if("zh".equals(infos.getLanguageCodeTitle().trim())) {
			  doc.add("title_ws_zh", title);
		  }
	  }
	  
	  if(!StringUtils.isBlank(content)) {
		  if(infos.getLanguageCodeText() == null || "".equals(infos.getLanguageCodeText()) || "en".equals(infos.getLanguageCodeText())){
			  doc.add("content_ws_en", content);
		  } else if("vi".equals(infos.getLanguageCodeText().trim())) {
			  doc.add("content_ws_vi", content);
		  } else if("ko".equals(infos.getLanguageCodeText().trim())) {
			  doc.add("content_ws_ko", content);
		  } else if("ja".equals(infos.getLanguageCodeText().trim())) {
			  doc.add("content_ws_ja", content);
		  } else if("zh".equals(infos.getLanguageCodeText().trim())) {
			  doc.add("content_ws_zh", content);
		  }
	  }
	  
	  if(!StringUtils.isBlank(descMeta)) {
		  if(infos.getLanguageCodeDescMeta() == null || "".equals(infos.getLanguageCodeDescMeta()) || "en".equals(infos.getLanguageCodeDescMeta())){
			  doc.add("meta_desc_ws_en", descMeta);
		  } else if("vi".equals(infos.getLanguageCodeDescMeta().trim())) {
			  doc.add("meta_desc_ws_vi", descMeta);
		  } else if("ko".equals(infos.getLanguageCodeDescMeta().trim())) {
			  doc.add("meta_desc_ws_ko", descMeta);
		  } else if("ja".equals(infos.getLanguageCodeDescMeta().trim())) {
			  doc.add("meta_desc_ws_ja", descMeta);
		  } else if("zh".equals(infos.getLanguageCodeDescMeta().trim())) {
			  doc.add("meta_desc_ws_zh", descMeta);
		  }
	  }
	  
	  if(!StringUtils.isBlank(kwMeta)) {
		  if(infos.getLanguageCodeKwMeta() == null || "".equals(infos.getLanguageCodeKwMeta()) || "en".equals(infos.getLanguageCodeKwMeta())){
			  doc.add("meta_kw_ws_en", kwMeta);
		  } else if("vi".equals(infos.getLanguageCodeKwMeta().trim())) {
			  doc.add("meta_kw_ws_vi", kwMeta);
		  } else if("ko".equals(infos.getLanguageCodeKwMeta().trim())) {
			  doc.add("meta_kw_ws_ko", kwMeta);
		  } else if("ja".equals(infos.getLanguageCodeKwMeta().trim())) {
			  doc.add("meta_kw_ws_ja", kwMeta);
		  } else if("zh".equals(infos.getLanguageCodeKwMeta().trim())) {
			  doc.add("meta_kw_ws_zh", kwMeta);
		  }
	  }
	  
	  return result;
  }
  
	private boolean addMovieIndexByLanguage(NutchDocument doc, WebPage page) {
		boolean result = false;

		String lang = Bytes.toString(page.getFromMetadata(new Utf8(
				ConstantsTikaParser.language)));

		String[] genreArr = null;
		String[] productionArr = null;
		String[] actorArr = null;
		String[] directorArr = null;
		String[] cinemaArr = null;
		
		String genreStr = Bytes.toString(page.getFromMetadata(new Utf8(
				ConstantsTikaParser.genre)));
		if (genreStr != null) {
			productionArr = genreStr.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
		}
		
		String productionStr = Bytes.toString(page.getFromMetadata(new Utf8(
				ConstantsTikaParser.productions)));
		if (productionStr != null) {
			productionArr = productionStr.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
		}
		
		String actorStr = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.actor)));
	    if(actorStr != null) {
		    actorArr = actorStr.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    }
	    
	    String directorStr = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.director)));
	    if(directorStr != null) {
		    directorArr = directorStr.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    }
	    
	    String cinemaStr = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.cinema_site)));
	    if(cinemaStr != null) {
	    	cinemaArr = cinemaStr.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    }
	    
	    String titleKey = "title_index_:lang_text";
	    String contentKey = "content_index_:lang_text";
	    String descKey = "desc_index_:lang_text";
	    String genreKey = "genre_index_:lang_text_multi";
	    String productionKey = "production_index_:lang_text_multi";
	    String actorKey = "actor_index_:lang_text_multi";
	    String directorKey = "director_index_:lang_text_multi";
	    String cinemaKey = "cinema_index_:lang_text_multi";
	    
	    
		if (lang == null || "".equals(lang) 
				|| (!"en".equals(lang) && !"vi".equals(lang) &&
						!"ko".equals(lang) && !"ja".equals(lang) &&
						!"zh".equals(lang))) {
			lang = "en";
		}
		
		doc.add(titleKey.replace(":lang", lang), Bytes.toString(page
				.getFromMetadata(new Utf8(ConstantsTikaParser.film_name))));
		doc.add(contentKey.replace(":lang", lang), Bytes.toString(page
				.getFromMetadata(new Utf8(
						ConstantsTikaParser.cinema_movie_description))));
		doc.add(descKey.replace(":lang", lang), Bytes.toString(page
				.getFromMetadata(new Utf8(
						ConstantsTikaParser.cinema_movie_description))));

		if (productionArr != null && productionArr.length > 0) {
			for (String production : productionArr) {
				doc.add(productionKey.replace(":lang", lang), production.trim());
			}
		}

		if (genreArr != null && genreArr.length > 0) {
			for (String genre : genreArr) {
				doc.add(genreKey.replace(":lang", lang), genre.trim());
			}
		}
		
		if(actorArr != null && actorArr.length > 0) {
			for(String actor : actorArr) {
		    	doc.add(actorKey.replace(":lang", lang), actor.trim());
		    }
		}
		
		if(directorArr != null && directorArr.length > 0) {
			for(String director : directorArr) {
		    	doc.add(directorKey.replace(":lang", lang), director.trim());
		    }
		}
		
		if(cinemaArr != null && cinemaArr.length > 0) {
			for(String cinema : cinemaArr) {
		    	doc.add(cinemaKey.replace(":lang", lang), cinema.trim());
		    }
		}

		return result;
	}
	
	private void addMovieMultiValueIndex(NutchDocument doc, WebPage page) {
		String value = null;
		String[] arr = null;
		
		// cinema_film_start
		value = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.cinema_film_start)));
		if(value != null) {
	    	arr = value.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    	for(String val : arr) {
	    		doc.add(ConstantsTikaParser.cinema_film_start, val);
	    	}
	    }
		value = null;
		arr = null;
		
		// cinema_film_end
		value = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.cinema_film_end)));
		if(value != null) {
	    	arr = value.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    	for(String val : arr) {
	    		doc.add(ConstantsTikaParser.cinema_film_end, val);
	    	}
	    }
		value = null;
		arr = null;
		
		// cinema_film_schedule
		value = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.cinema_film_schedule)));
		if(value != null) {
	    	arr = value.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    	for(String val : arr) {
	    		doc.add(ConstantsTikaParser.cinema_film_schedule, val);
	    	}
	    }
		value = null;
		arr = null;
		
		// review_from
		value = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.review_from)));
		if(value != null) {
	    	arr = value.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    	for(String val : arr) {
	    		doc.add(ConstantsTikaParser.review_from, val);
	    	}
	    }
		value = null;
		arr = null;
		
		// review_total
		value = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.review_total)));
		if(value != null) {
	    	arr = value.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    	for(String val : arr) {
	    		doc.add(ConstantsTikaParser.review_total, val);
	    	}
	    }
		value = null;
		arr = null;
		
		// review_content
		value = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.review_content)));
		if(value != null) {
	    	arr = value.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    	for(String val : arr) {
	    		doc.add(ConstantsTikaParser.review_content, val);
	    	}
	    }
		value = null;
		arr = null;
		
		// film_genre_id
		value = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.film_genre_id)));
		if(value != null) {
	    	arr = value.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    	for(String val : arr) {
	    		doc.add(ConstantsTikaParser.film_genre_id, val);
	    	}
	    }
		value = null;
		arr = null;
		
		// cinema_id
		value = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.cinema_id)));
		if(value != null) {
	    	arr = value.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    	for(String val : arr) {
	    		doc.add(ConstantsTikaParser.cinema_id, val);
	    	}
	    }
		value = null;
		arr = null;
		
		// actor_id
		value = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.actor_id)));
		if(value != null) {
	    	arr = value.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    	for(String val : arr) {
	    		doc.add(ConstantsTikaParser.actor_id, val);
	    	}
	    }
		value = null;
		arr = null;
		
		// director_id
		value = Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.director_id)));
		if(value != null) {
	    	arr = value.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    	for(String val : arr) {
	    		doc.add(ConstantsTikaParser.director_id, val);
	    	}
	    }
		
	}
	
	private void addMultiValueField(NutchDocument doc, WebPage page, String fieldName) {
		String value = null;
		String[] arr = null;
		
		value = Bytes.toString(page.getFromMetadata(new Utf8(fieldName)));
		if(value != null && !StringUtils.isBlank(value)) {
	    	arr = value.split(ConstantsTikaParser.REGEX_INDEX_MULTI_VALUES_PATTEN);
	    	for(String val : arr) {
	    		doc.add(fieldName, val);
	    	}
	    }
	}
  
  public static class IndexInfos {
		private String languageCodeTitle = null;
		private String languageCodeText = null;
		private String languageCodeDescMeta = null;
		private String languageCodeKwMeta = null;
		private String title = null;
		private String descMeta = null; // description metadata
		private String kwMeta = null; // keyword metadata
		private String text = null;
		private String urlId = null;
		private String webUrl = null; // this is id of schema websearch
		private String listingId = null; // list id injected with same url, separate by ',' character
		
		public String getLanguageCodeTitle() {
			return languageCodeTitle;
		}
		public void setLanguageCodeTitle(String languageCodeTitle) {
			this.languageCodeTitle = languageCodeTitle;
		}
		public String getLanguageCodeText() {
			return languageCodeText;
		}
		public void setLanguageCodeText(String languageCodeText) {
			this.languageCodeText = languageCodeText;
		}
		public String getLanguageCodeDescMeta() {
			return languageCodeDescMeta;
		}
		public void setLanguageCodeDescMeta(String languageCodeDescMeta) {
			this.languageCodeDescMeta = languageCodeDescMeta;
		}
		public String getLanguageCodeKwMeta() {
			return languageCodeKwMeta;
		}
		public void setLanguageCodeKwMeta(String languageCodeKwMeta) {
			this.languageCodeKwMeta = languageCodeKwMeta;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getDescMeta() {
			return descMeta;
		}
		public void setDescMeta(String descMeta) {
			this.descMeta = descMeta;
		}
		public String getKwMeta() {
			return kwMeta;
		}
		public void setKwMeta(String kwMeta) {
			this.kwMeta = kwMeta;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public String getUrlId() {
			return urlId;
		}
		public void setUrlId(String urlId) {
			this.urlId = urlId;
		}
		public String getWebUrl() {
			return webUrl;
		}
		public void setWebUrl(String webUrl) {
			this.webUrl = webUrl;
		}
		public String getListingId() {
			return listingId;
		}
		public void setListingId(String listingId) {
			this.listingId = listingId;
		}
		
  }
}
