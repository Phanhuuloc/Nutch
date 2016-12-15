/**
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
 */
package org.apache.nutch.indexer.anchor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.TableUtil;
import org.apache.nutch.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indexing filter that offers an option to either index all inbound anchor text for 
 * a document or deduplicate anchors. Deduplication does have it's con's, 
 * @see {@code anchorIndexingFilter.deduplicate} in nutch-default.xml.
 */
public class AnchorIndexingFilter implements IndexingFilter {

  public static final Logger LOG = LoggerFactory.getLogger(AnchorIndexingFilter.class);
  private Configuration conf;
  private boolean deduplicate = false;

  private static final Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

  static {
    FIELDS.add(WebPage.Field.INLINKS);
  }
  
  /**
   * Set the {@link Configuration} object
   */
  public void setConf(Configuration conf) {
    this.conf = conf;

    deduplicate = conf.getBoolean("anchorIndexingFilter.deduplicate", false);
    LOG.info("Anchor deduplication is: " + (deduplicate ? "on" : "off"));
  }
  
  /**
   * Get the {@link Configuration} object
   */
  public Configuration getConf() {
    return this.conf;
  }
  
  public void addIndexBackendOptions(Configuration conf) {
  }
  
  /**
   * The {@link AnchorIndexingFilter} filter object which supports boolean 
   * configuration settings for the deduplication of anchors. 
   * See {@code anchorIndexingFilter.deduplicate} in nutch-default.xml.
   *  
   * @param doc The {@link NutchDocument} object
   * @param url URL to be filtered for anchor text
   * @param page {@link WebPage} object relative to the URL
   * @return filtered NutchDocument
   */
  @Override
  public NutchDocument filter(NutchDocument doc, String url, WebPage page)
      throws IndexingException {
    
    /*for(Utf8 utf8 : page.getMetadata().keySet()){
		if("name".equals(Bytes.toString(utf8.getBytes()))){
			doc.add("name", Bytes.toString(page.getMetadata().get(utf8)).trim());
		} else if("rating".equals(Bytes.toString(utf8.getBytes()))){
			doc.add("rating", Bytes.toString(page.getMetadata().get(utf8)).trim());
		} else if("trailer".equals(Bytes.toString(utf8.getBytes()))){
			doc.add("trailer", Bytes.toString(page.getMetadata().get(utf8)).trim());
		} else if("age_limit".equals(Bytes.toString(utf8.getBytes()))){
			doc.add("age_limit", Bytes.toString(page.getMetadata().get(utf8)).trim());
		} else if("photo".equals(Bytes.toString(utf8.getBytes()))){
			doc.add("photo", Bytes.toString(page.getMetadata().get(utf8)).trim());
		} else if("year_product".equals(Bytes.toString(utf8.getBytes()))){
			doc.add("year_product", Bytes.toString(page.getMetadata().get(utf8)).trim());
		} else if("duration".equals(Bytes.toString(utf8.getBytes()))){
			doc.add("film_duration", Bytes.toString(page.getMetadata().get(utf8)).trim());
		} else if("producer".equals(Bytes.toString(utf8.getBytes()))){
			doc.add("producer", Bytes.toString(page.getMetadata().get(utf8)).trim());
		} else if("sub".equals(Bytes.toString(utf8.getBytes()))){
			doc.add("sub", Bytes.toString(page.getMetadata().get(utf8)).trim());
		} else if("dimension".equals(Bytes.toString(utf8.getBytes()))){
			doc.add("dimension", Bytes.toString(page.getMetadata().get(utf8)).trim());
		} else if("actor".equals(Bytes.toString(utf8.getBytes()))){
			doc.add("actors", Bytes.toString(page.getMetadata().get(utf8)).trim());
		} else if("director".equals(Bytes.toString(utf8.getBytes()))){
			doc.add("directors", Bytes.toString(page.getMetadata().get(utf8)).trim());
		}
    }*/

    /*doc.add(
    		"nameproduct", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("nameproduct")) == null ?
    						new byte[]{} : page.getFromMetadata(new Utf8("nameproduct")).array() 
    				)
    		);*/
    /*doc.add(
    		"priceproduct", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("priceproduct")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("priceproduct")).array()
    				)
    		);*/
    
    /*doc.add(
    		"name", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("name")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("name")).array()
    				)
    		);*/
    /*doc.add(
    		"trailer", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("trailer")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("trailer")).array()
    				)
    		);*/
    /*doc.add(
    		"dimension", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("dimension")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("dimension")).array()
    				)
    		);*/
    
    /*doc.add(
    		"time", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("time")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("time")).array()
    				)
    		);*/
    /*doc.add(
    		"genre", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("genre")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("genre")).array()
    				)
    		);*/
    /*doc.add(
    		"actor", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("actor")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("actor")).array()
    				)
    		);*/
    /*doc.add(
    		"director", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("director")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("director")).array()
    				)
    		);*/
    /*doc.add(
    		"duration", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("duration")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("duration")).array()
    				)
    		);*/
   /* doc.add(
    		"photo", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("photo")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("photo")).array()
    				)
    		);*/
    /*doc.add(
    		"age_limit", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("age_limit")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("age_limit")).array()
    				)
    		);*/
    /*doc.add(
    		"movie_description", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("movie-description")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("movie-description")).array()
    				)
    		);*/
   /* doc.add(
    		"movie_sub", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("sub")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("sub")).array()
    				)
    		);*/
    /*doc.add(
    		"movie_version", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("version")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("version")).array()
    				)
    		);*/
    
    /*doc.add(
    		"movie_language", 
    		Bytes.toString(
    				page.getFromMetadata(new Utf8("language")) == null ? 
    					new byte[]{} : page.getFromMetadata(new Utf8("language")).array()
    				)
    		);*/
    
    // split string to array
    /*String schedules = Bytes.toString(
			page.getFromMetadata(new Utf8("schedule")) == null ? 
					new byte[]{} : page.getFromMetadata(new Utf8("schedule")).array()
				);
    if(schedules == null){
    	 doc.add("schedule", "");
    } else {
    	String[] scheduleArr = schedules.split("%%");
    	for(String scheduleStr : scheduleArr){
    		doc.add("schedule", scheduleStr);
    	}
    }*/
    
    /*doc.add("anchor", "");*/
    
    /*for (Entry<Utf8, Utf8> e : page.getInlinks().entrySet()) {
      String anchor = TableUtil.toString(e.getValue());

      if (deduplicate) {
        if (set == null) set = new HashSet<String>();
        String lcAnchor = anchor.toLowerCase();

        // Check if already processed the current anchor
        if (!set.contains(lcAnchor)) {
          doc.add("anchor", anchor);
          



          // Add to set
          set.add(lcAnchor);
        }
      } else {
        doc.add("anchor", anchor);
      }
    }*/
    
    return doc;
  }
  
  /**
   * Gets all the fields for a given {@link WebPage}
   * Many datastores need to setup the mapreduce job by specifying the fields
   * needed. All extensions that work on WebPage are able to specify what fields
   * they need.
   */
  @Override
  public Collection<WebPage.Field> getFields() {
    return FIELDS;
  }

}
