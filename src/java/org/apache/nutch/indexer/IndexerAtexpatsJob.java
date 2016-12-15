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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.avro.util.Utf8;
import org.apache.commons.lang.StringUtils;
import org.apache.gora.mapreduce.GoraMapper;
import org.apache.gora.mapreduce.StringComparator;
import org.apache.gora.store.DataStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.nutch.crawl.CrawlStatus;
import org.apache.nutch.crawl.GeneratorJob;
import org.apache.nutch.indexer.IndexUtil.IndexInfos;
import org.apache.nutch.indexer.solr.SolrConstants;
import org.apache.nutch.indexer.solr.SolrUtils;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.parse.Parser;
import org.apache.nutch.parse.ParserFactory;
import org.apache.nutch.parse.ParserNotFound;
import org.apache.nutch.scoring.ScoringFilters;
import org.apache.nutch.storage.Mark;
import org.apache.nutch.storage.StorageUtils;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.apache.nutch.util.NutchJob;
import org.apache.nutch.util.NutchTool;
import org.apache.nutch.util.TableUtil;
import org.apache.nutch.util.ToolUtil;
import org.apache.solr.client.solrj.SolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IndexerAtexpatsJob extends NutchTool implements Tool {

	public static final Logger LOG = LoggerFactory.getLogger(IndexerAtexpatsJob.class);

	private static final Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

	public static final Utf8 REINDEX = new Utf8("-reindex");
	
	

	static {
		FIELDS.add(WebPage.Field.SIGNATURE);
		FIELDS.add(WebPage.Field.SCORE);
		FIELDS.add(WebPage.Field.MARKERS);
		FIELDS.add(WebPage.Field.CONTENT);
		FIELDS.add(WebPage.Field.CONTENT_TYPE);
		FIELDS.add(WebPage.Field.BASE_URL);
		FIELDS.add(WebPage.Field.STATUS);
		FIELDS.add(WebPage.Field.METADATA);
		FIELDS.add(WebPage.Field.BATCH_ID);
		FIELDS.add(WebPage.Field.P_BATCH_ID);
	}

	public static class IndexerMapper
	extends GoraMapper<String, WebPage, String, NutchDocument> {
		public IndexUtil indexUtil;
		public DataStore<String, WebPage> store;

		protected Utf8 batchId;
		
		ParserFactory parserFactory = null;
		Parser parser = null;
		
		@Override
		public void setup(Context context) throws IOException {
			Configuration conf = context.getConfiguration();
			batchId = new Utf8(conf.get(GeneratorJob.BATCH_ID, Nutch.ALL_BATCH_ID_STR));
			indexUtil = new IndexUtil(conf);
			try {
				store = StorageUtils.createWebStore(conf, String.class, WebPage.class);
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
			
			ParserFactory parserFactory = new ParserFactory(conf);
			try {
				this.parser = parserFactory.getParserById("org.apache.nutch.parse.tika.TikaParser");
			} catch (ParserNotFound e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void cleanup(Context context) throws IOException ,InterruptedException {
			store.close();
		};

		@Override
		public void map(String key, WebPage page, Context context)
				throws IOException, InterruptedException {
			/*ParseStatus pstatus = page.getParseStatus();
			if (pstatus == null || !ParseStatusUtils.isSuccess(pstatus)
					|| pstatus.getMinorCode() == ParseStatusCodes.SUCCESS_REDIRECT) {
				return; // filter urls not parsed
			}*/
			if(page.getStatus() != CrawlStatus.STATUS_FETCHED){
				return;
			}
			
			Map<String, String> htmlMap = new HashMap<String, String>();
			IndexInfos infos = new IndexInfos();
			
			if(page.getContent() != null){
				htmlMap = parser.getTextFromContent(page);
			}
			
			// Get data from Map
			infos.setLanguageCodeTitle(htmlMap.get("languageCodeTitle"));
			infos.setLanguageCodeText(htmlMap.get("languageCodeText"));
			infos.setLanguageCodeDescMeta(htmlMap.get("languageCodeDescriptionMeta"));
			infos.setLanguageCodeKwMeta(htmlMap.get("languageCodeKeywordsMeta"));
			infos.setTitle(htmlMap.get("title"));
			infos.setDescMeta(htmlMap.get("descriptionMeta"));
			infos.setKwMeta(htmlMap.get("keywordsMeta"));
			infos.setText(htmlMap.get("text"));
			if(page.getFromMetadata(new Utf8("urlId")) != null){
				infos.setUrlId(Bytes.toString(page.getFromMetadata(new Utf8("urlId"))));
			}
			
			if(
					StringUtils.isBlank(infos.getUrlId()) 
					|| (StringUtils.isBlank(infos.getTitle()) && StringUtils.isBlank(infos.getDescMeta()) && StringUtils.isBlank(infos.getKwMeta()) &&  StringUtils.isBlank(infos.getText()))
					){
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
			
			// LOG for testing
			/*LOG.info("URLID: " + urlId);
			LOG.info("LANGUAGE: " + languageCode);
			LOG.info("TITLE: " + title);
			LOG.info("META: " + metadata);
			LOG.info("TEXT: " + text);*/
			
			NutchDocument doc = 
					indexUtil.index(key, page, 
							infos);
			if (doc == null) {
				return;
			}
			if (mark != null) {
				Mark.INDEX_MARK.putMark(page, Mark.UPDATEDB_MARK.checkMark(page));
				store.put(key, page);
			}
			
			context.write(key, doc);
		}
	}


	public static Collection<WebPage.Field> getFields(Job job) {
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
		conf.set(GeneratorJob.BATCH_ID, batchId);
		Job job = new NutchJob(conf, jobName);
		// TODO: Figure out why this needs to be here
		job.getConfiguration().setClass("mapred.output.key.comparator.class",
				StringComparator.class, RawComparator.class);

		Collection<WebPage.Field> fields = getFields(job);
		StorageUtils.initMapperJob(job, fields, String.class, NutchDocument.class,
				IndexerMapper.class);
		job.setNumReduceTasks(0);
		job.setOutputFormatClass(IndexerOutputFormat.class);
		return job;
	}
	
	public void indexSolr(String solrUrl, String batchId) throws Exception {
		LOG.info("MoviesIndexerJob: starting");
		
		// not use solrUrl anymore, get config
		//String solrUrl = getConf().get(AtexpatsConstants.CONFIG_SOLR_URL_MOVIE);

		run(ToolUtil.toArgMap(
				Nutch.ARG_SOLR, solrUrl,
				Nutch.ARG_BATCH, batchId));
		// do the commits once and for all the reducers in one go
		getConf().set(SolrConstants.SERVER_URL,solrUrl);
		SolrServer solr = SolrUtils.getCommonsHttpSolrServer(getConf());
		if (getConf().getBoolean(SolrConstants.COMMIT_INDEX, true)) {
			solr.commit();
		}
		LOG.info("MoviesIndexerJob: done.");
	}
}
