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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avro.util.Utf8;
import org.apache.commons.io.FileUtils;
import org.apache.gora.mapreduce.GoraMapper;
import org.apache.hadoop.util.StringUtils;
import org.apache.nutch.atexpats.common.AtexpatsConstants;
import org.apache.nutch.scoring.ScoreDatum;
import org.apache.nutch.scoring.ScoringFilterException;
import org.apache.nutch.scoring.ScoringFilters;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.apache.nutch.util.TableUtil;
import org.apache.nutch.util.WebPageWritable;
import org.slf4j.Logger;

public class DbUpdateMapper
extends GoraMapper<String, WebPage, UrlWithScore, NutchWritable> {
  public static final Logger LOG = DbUpdaterJob.LOG;

  private ScoringFilters scoringFilters;

  private final List<ScoreDatum> scoreData = new ArrayList<ScoreDatum>();
  
  //reuse writables
  private UrlWithScore urlWithScore = new UrlWithScore();
  private NutchWritable nutchWritable = new NutchWritable();
  private WebPageWritable pageWritable;
  
  private HashMap<String, String> urlMap;

  @Override
  public void map(String key, WebPage page, Context context)
  throws IOException, InterruptedException {
	  
	// TODO
	  Integer urlId = null;
	  String sameUrlId = "";
	  String urlStatus = AtexpatsConstants.STATUS_NEW;
	  for(Utf8 utf8 : page.getMetadata().keySet()){
		  if("urlId".equals(Bytes.toString(utf8.getBytes()))){
			  try{
				  urlId = Integer.parseInt(Bytes.toString(page.getMetadata().get(utf8)).trim());
			  } catch (Exception e){
				  urlId = null;
			  }
		  }
		  if("sameUrlId".equals(Bytes.toString(utf8.getBytes()))){
			  try{
				  sameUrlId = Bytes.toString(page.getMetadata().get(utf8)).trim();
			  } catch (Exception e){
				  sameUrlId = "";
			  }
		  }
		  
		  if("urlStatus".equals(Bytes.toString(utf8.getBytes()))){
			  try{
				  urlStatus = Bytes.toString(page.getMetadata().get(utf8)).trim();
			  } catch (Exception e){
				  urlStatus = AtexpatsConstants.STATUS_NEW;
			  }
		  }
	  }
	  
	 if(urlId != null) {
		 if(urlMap != null && urlMap.containsKey(urlId.toString())) {
			 urlStatus = urlMap.get(urlId.toString());
		 }
	 }

    String url = TableUtil.unreverseUrl(key);
    
    LOG.info("DbUpdateMapper : url = " + url);
    LOG.info("DbUpdateMapper : sameUrlId = " + sameUrlId);
    LOG.info("DbUpdateMapper : urlStatus = " + urlStatus);

    scoreData.clear();
    Map<Utf8, Utf8> outlinks = page.getOutlinks();
    if (outlinks != null) {
      for (Entry<Utf8, Utf8> e : outlinks.entrySet()) {
                int depth=Integer.MAX_VALUE;
        Utf8 depthUtf8=page.getFromMarkers(DbUpdaterJob.DISTANCE);
        if (depthUtf8 != null) depth=Integer.parseInt(depthUtf8.toString());
        scoreData.add(new ScoreDatum(0.0f, e.getKey().toString(), 
            e.getValue().toString(), depth));
      }
    }

    // TODO: Outlink filtering (i.e. "only keep the first n outlinks")
    try {
    	scoringFilters.distributeScoreToOutlinks(url, page, scoreData, (outlinks == null ? 0 : outlinks.size()));

    } catch (ScoringFilterException e) {
      LOG.warn("Distributing score failed for URL: " + key +
          " exception:" + StringUtils.stringifyException(e));
    }
    
    urlWithScore.setUrl(key);
    urlWithScore.setScore(Float.MAX_VALUE);
 // TODO Set urlId
    if(urlId != null){
    	urlWithScore.setUrlId(urlId);
    }
    
    if(sameUrlId != null){
    	urlWithScore.setSameUrlId(sameUrlId);
    }
    
    if(urlStatus != null){
    	urlWithScore.setUrlStatus(urlStatus);
    }
    
    // reset fetch time if status is UP
    if(AtexpatsConstants.STATUS_UPDATE.equals(urlStatus)) {
    	resetFetchTime(page);
    }
    
    pageWritable.setWebPage(page);
    nutchWritable.set(pageWritable);
    
    context.write(urlWithScore, nutchWritable);

    for (ScoreDatum scoreDatum : scoreData) {
    	try {
      String reversedOut = TableUtil.reverseUrl(scoreDatum.getUrl());
      scoreDatum.setUrl(url);
      urlWithScore.setUrl(reversedOut);
      urlWithScore.setScore(scoreDatum.getScore());
      nutchWritable.set(scoreDatum);
   // TODO Set urlId
      if(urlId != null){
    	  urlWithScore.setUrlId(urlId);
      }
      if(sameUrlId != null){
      	urlWithScore.setSameUrlId(sameUrlId);
      }
      if(urlStatus != null){
        	urlWithScore.setUrlStatus(urlStatus);
        }
      
      context.write(urlWithScore, nutchWritable);
    } catch(Exception e) {
    	LOG.error("Failed with out link: " + scoreDatum.getUrl());
    	LOG.error(e.getMessage());
    }
    
    }
  }

  @Override
  public void setup(Context context) {
    scoringFilters = new ScoringFilters(context.getConfiguration());
    pageWritable = new WebPageWritable(context.getConfiguration(), null);
    try {
    //String dir = context.getConfiguration().get("mapred.input.dir");
    String dir = context.getWorkingDirectory().toUri().getPath() + File.separator + "urls";
    File file = new File(dir + File.separator + "url.txt");
    if(file.exists()) {
    	
			List<String> list = FileUtils.readLines(file, "UTF-8");
			if(list.size() > 0) {
				if(urlMap == null) {
					urlMap = new HashMap<String, String>();
				}
				String[] arr;
				for(String value : list) {
					arr = value.split("##");
					if(arr.length > 2) {
						urlMap.put(arr[1].trim(), arr[2].trim());
					}
				}
			}
    }
    
    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
  }
  
  private void resetFetchTime(WebPage page) {
    int fetchInterval = page.getFetchInterval();
    LOG.info("after get fetch interval=" + fetchInterval);
    page.setFetchTime(System.currentTimeMillis() - fetchInterval);
  }

}
