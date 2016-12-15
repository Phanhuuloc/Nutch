package org.apache.nutch.atexpats;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.util.Utf8;
import org.apache.gora.mapreduce.GoraMapper;
import org.apache.gora.mapreduce.StringComparator;
import org.apache.gora.store.DataStore;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.atexpats.common.AtexpatsConstants;
import org.apache.nutch.atexpats.indexer.MoviesSolrWriter;
import org.apache.nutch.crawl.CrawlStatus;
import org.apache.nutch.crawl.GeneratorJob;
import org.apache.nutch.indexer.IndexUtil;
import org.apache.nutch.indexer.IndexUtil.IndexInfos;
import org.apache.nutch.indexer.IndexerAtexpatsJob;
import org.apache.nutch.indexer.IndexerOutputFormat;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.NutchIndexWriterFactory;
import org.apache.nutch.indexer.solr.SolrConstants;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.parse.Parser;
import org.apache.nutch.parse.ParserFactory;
import org.apache.nutch.parse.ParserNotFound;
import org.apache.nutch.parse.utils.ConstantsTikaParser;
import org.apache.nutch.storage.Mark;
import org.apache.nutch.storage.StorageUtils;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.apache.nutch.util.TableUtil;
import org.apache.nutch.util.ToolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoviesIndexerJob extends IndexerAtexpatsJob {

	public static Logger LOG = LoggerFactory.getLogger(MoviesIndexerJob.class);

	@Override
	public Map<String,Object> run(Map<String,Object> args) throws Exception {
		//String solrUrl = (String)args.get(Nutch.ARG_SOLR);
		String solrUrl = getConf().get(AtexpatsConstants.CONFIG_SOLR_URL_MOVIE);
		String batchId = (String)args.get(Nutch.ARG_BATCH);
		NutchIndexWriterFactory.addClassToConf(getConf(), MoviesSolrWriter.class);
		
		if(solrUrl == null) {
			//solrUrl = (String)args.get(Nutch.ARG_SOLR_MOVIE);
			solrUrl = getConf().get(AtexpatsConstants.CONFIG_SOLR_URL_MOVIE);
		}
		
		getConf().set(SolrConstants.SERVER_URL, solrUrl);
		
		LOG.info("MoviesIndexerJob: solrUrl: " + solrUrl);
		LOG.info("MoviesIndexerJob: batchId: " + batchId);
		if(batchId == null) {
			batchId = "-reindex";
		}

		currentJob = createIndexJobV2(getConf(), "solr-index", batchId);

		currentJob.waitForCompletion(true);
		ToolUtil.recordJobStatus(null, currentJob, results);
		return results;
	}

	/*public void indexSolr(String solrUrl, String batchId) throws Exception {
		LOG.info("MoviesIndexerJob: starting");
		
		// not use solrUrl anymore, get config
		String solrUrl = getConf().get(AtexpatsConstants.CONFIG_SOLR_URL_MOVIE);

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
	}*/

	public int run(String[] args) throws Exception {
		LOG.info("MoviesIndexerJob: start run.");
		if (args.length < 1) {
			System.err.println("Usage: MoviesIndexerJob (<batchId> | -all | -reindex) [-crawlId <id>]");
			return -1;
		}

		if (args.length == 4 && "-crawlId".equals(args[2])) {
			getConf().set(Nutch.CRAWL_ID_KEY, args[3]);
		}
		try {
			LOG.info("MoviesIndexerJob: begin index.");
//			indexSolr(args[0], args[1]);
			indexSolr(getConf().get(AtexpatsConstants.CONFIG_SOLR_URL_MOVIE), args[0]);
			LOG.info("MoviesIndexerJob: end run.");
			return 0;
		} catch (final Exception e) {
			LOG.error("SolrIndexerJob: " + StringUtils.stringifyException(e));
			return -1;
		}
		
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
			LOG.info("MoviesIndexerJob : Map : " + key);
			if(page.getStatus() != CrawlStatus.STATUS_FETCHED){
				return;
			}
			
			Map<String, String> htmlMap = new HashMap<String, String>();
			IndexInfos infos = new IndexInfos();
			
			/*if(page.getContent() != null){
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
			infos.setText(htmlMap.get("text"));*/
			if(page.getFromMetadata(new Utf8(ConstantsTikaParser.film_id)) != null){
				infos.setUrlId(Bytes.toString(page.getFromMetadata(new Utf8(ConstantsTikaParser.film_id))));
			}
			
			/*if(
					infos.getUrlId() == null 
					|| (infos.getTitle() == null && infos.getDescMeta() == null && infos.getKwMeta() == null &&  infos.getText() == null)
					){
				return;
			}*/
			
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
					indexUtil.indexV2(key, page, 
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
	
	
	protected Job createIndexJobV2(Configuration conf, String jobName,
			String batchId) throws IOException, ClassNotFoundException {
		conf.set(GeneratorJob.BATCH_ID, batchId);
		Job job = new NutchJob(conf, jobName);
		// TODO: Figure out why this needs to be here
		job.getConfiguration().setClass("mapred.output.key.comparator.class",
				StringComparator.class, RawComparator.class);

		Collection<WebPage.Field> fields = getFields(job);
		StorageUtils.initMapperJob(job, fields, String.class,
				NutchDocument.class, IndexerMapper.class);
		job.setNumReduceTasks(0);
		job.setOutputFormatClass(IndexerOutputFormat.class);
		return job;
	}

	public static void main(String[] args) throws Exception {
		LOG.info("MoviesIndexerJob : start main ");
		final int res = ToolRunner.run(NutchConfiguration.create(),
				new MoviesIndexerJob(), args);
		LOG.info("MoviesIndexerJob : end main ");
		System.exit(res);
	}
	
	
}
