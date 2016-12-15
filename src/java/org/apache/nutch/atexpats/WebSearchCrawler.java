package org.apache.nutch.atexpats;

import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.atexpats.common.AtexpatsConstants;
import org.apache.nutch.atexpats.util.AtexpatsUtils;
import org.apache.nutch.crawl.DbUpdaterJob;
import org.apache.nutch.crawl.GeneratorJob;
import org.apache.nutch.crawl.InjectorJob;
import org.apache.nutch.fetcher.FetcherJob;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.parse.ParserJob;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchTool;
import org.apache.nutch.util.ToolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSearchCrawler extends NutchTool implements Tool {
	private static final Logger LOG = LoggerFactory.getLogger(WebSearchCrawler.class);

	private boolean cleanSeedDir = false;
	private String tmpSeedDir = null;
	private HashMap<String,Object> results = new HashMap<String,Object>();
	private Map<String,Object> status =
			Collections.synchronizedMap(new HashMap<String,Object>());
	private NutchTool currentTool = null;
	private boolean shouldStop = false;

	@Override
	public Map<String,Object> getStatus() {
		return status;
	}

	private Map<String,Object> runTool(Class<? extends NutchTool> toolClass,
			Map<String,Object> args) throws Exception {
		currentTool = (NutchTool)ReflectionUtils.newInstance(toolClass, getConf());
		return currentTool.run(args);
	}
	
	/*private Map<String,Object> runIndex(Class<? extends NutchTool> toolClass,
			Map<String,Object> args, int type) throws Exception {
		
		String[] arr;
		if(AtexpatsConstants.TYPE_LISTING == type) {
			arr = new String[] {args.get(Nutch.ARG_SOLR_LISTING).toString(), "-reindex"};
			ToolRunner.run(getConf(),
					new AtexpatsIndexerJob(), arr);
		} else if(AtexpatsConstants.TYPE_WEB_SEARCH == type){
			arr = new String[] {args.get(Nutch.ARG_SOLR_WEB_SEARCH).toString(), "-reindex"};
			ToolRunner.run(getConf(),
					new WebSearchIndexerJob(), arr);
		}
		
		return null;
	}*/

	@Override
	public boolean stopJob() throws Exception {
		shouldStop = true;
		if (currentTool != null) {
			return currentTool.stopJob();
		}
		return false;
	}

	@Override
	public boolean killJob() throws Exception {
		shouldStop = true;
		if (currentTool != null) {
			return currentTool.killJob();
		}
		return false;
	}

	@Override
	public Map<String,Object> run(Map<String, Object> args) throws Exception {
		results.clear();
		status.clear();
		String crawlId = (String)args.get(Nutch.ARG_CRAWL);
		if (crawlId != null) {
			getConf().set(Nutch.CRAWL_ID_KEY, crawlId);
		}
		String seedDir = null;
		String seedList = (String)args.get(Nutch.ARG_SEEDLIST);    
		if (seedList != null) { // takes precedence
			String[] seeds = seedList.split("\\s+");
		// create tmp. dir
			String tmpSeedDir = getConf().get("hadoop.tmp.dir") + "/seed-" +
					System.currentTimeMillis();
			FileSystem fs = FileSystem.get(getConf());
			Path p = new Path(tmpSeedDir);
			fs.mkdirs(p);
			Path seedOut = new Path(p, "urls");
			OutputStream os = fs.create(seedOut);
			for (String s : seeds) {
				os.write(s.getBytes());
				os.write('\n');
			}
			os.flush();
			os.close();
			cleanSeedDir = true;
			seedDir = tmpSeedDir;
		} else {
			seedDir = (String)args.get(Nutch.ARG_SEEDDIR);
		}
		Integer depth = (Integer)args.get(Nutch.ARG_DEPTH);
		if (depth == null) depth = 1;
		boolean parse = getConf().getBoolean(FetcherJob.PARSE_KEY, false);
		String solrUrlListing = (String)args.get(Nutch.ARG_SOLR_LISTING);
		String solrUrlWebSearch = (String) args.get(Nutch.ARG_SOLR_WEB_SEARCH);
		int onePhase = 3;
		if (!parse) onePhase++;
		float totalPhases = depth * onePhase;
		if (seedDir != null) totalPhases++;
		float phase = 0;
		Map<String,Object> jobRes = null;
		LinkedHashMap<String,Object> subTools = new LinkedHashMap<String,Object>();
		status.put(Nutch.STAT_JOBS, subTools);
		results.put(Nutch.STAT_JOBS, subTools);
		// inject phase
		if (seedDir != null) {
			status.put(Nutch.STAT_PHASE, "inject");
			jobRes = runTool(InjectorJob.class, args);
			if (jobRes != null) {
				subTools.put("inject", jobRes);
			}
			status.put(Nutch.STAT_PROGRESS, ++phase / totalPhases);
			if (cleanSeedDir && tmpSeedDir != null) {
				LOG.info(" - cleaning tmp seed list in " + tmpSeedDir);
				FileSystem.get(getConf()).delete(new Path(tmpSeedDir), true);
			}
		}
		if (shouldStop) {
			return results;
		}
		// run "depth" cycles
		for (int i = 0;; i++) {
			status.put(Nutch.STAT_PHASE, "generate " + i);
			jobRes = runTool(GeneratorJob.class, args);
			if (jobRes != null) {
				subTools.put("generate " + i, jobRes);
			}
			status.put(Nutch.STAT_PROGRESS, ++phase / totalPhases);
			if (shouldStop) {
				return results;
			}
			status.put(Nutch.STAT_PHASE, "fetch " + i);
			jobRes = runTool(FetcherJob.class, args);
			if (jobRes != null) {
				subTools.put("fetch " + i, jobRes);
			}
			status.put(Nutch.STAT_PROGRESS, ++phase / totalPhases);
			if (shouldStop) {
				return results;
			}
			if (!parse) {
				status.put(Nutch.STAT_PHASE, "parse " + i);
				jobRes = runTool(ParserJob.class, args);
				if (jobRes != null) {
					subTools.put("parse " + i, jobRes);
				}
				status.put(Nutch.STAT_PROGRESS, ++phase / totalPhases);
				if (shouldStop) {
					return results;
				}
			}
			status.put(Nutch.STAT_PHASE, "updatedb " + i);
			jobRes = runTool(DbUpdaterJob.class, args);
			if (jobRes != null) {
				subTools.put("updatedb " + i, jobRes);
			}
			status.put(Nutch.STAT_PROGRESS, ++phase / totalPhases);
			if (shouldStop) {
				return results;
			}
			
			// TODO Index to listing solr
			//if (solrUrlListing != null) {
				status.put(Nutch.STAT_PHASE, "indexListing" + i);
				jobRes = AtexpatsUtils.runIndex(AtexpatsIndexerJob.class, args, AtexpatsConstants.TYPE_LISTING, this);
				if (jobRes != null) {
					subTools.put("indexListing", jobRes);
				}
			//}
			
			// TODO Index to web search solr
			//if (solrUrlWebSearch != null) {
				status.put(Nutch.STAT_PHASE, "indexWebSearch" + i);
				//jobRes = runTool(WebSearchIndexerJob.class, args);
				jobRes = AtexpatsUtils.runIndex(WebSearchIndexerJob.class, args, AtexpatsConstants.TYPE_WEB_SEARCH, this);
				if (jobRes != null) {
					subTools.put("indexWebSearch", jobRes);
				}
			//}
			if (shouldStop) {
				return results;
			}
			
		}
		
		
		/*if (solrUrl != null) {
			status.put(Nutch.STAT_PHASE, "index");
			jobRes = runTool(SolrIndexerJob.class, args);
			if (jobRes != null) {
				subTools.put("index", jobRes);
			}
		}*/
		//return results;
	}

	@Override
	public float getProgress() {
		Float p = (Float)status.get(Nutch.STAT_PROGRESS);
		if (p == null) return 0;
		return p;
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("Usage: WebSearchCrawler (<seedDir> | -continue) [-solrlisting <solrURLListing>] [-solrws <solrURLWebSearch>] [-threads n] [-depth i] [-topN N] [-numTasks N]");
			return -1;
		}
		// parse most common arguments here
		String seedDir = null;
		int threads = getConf().getInt("fetcher.threads.fetch", 10);    
		int depth = 5;
		long topN = Long.MAX_VALUE;
		String solrUrlListing = null;
		String solrUrlWebSearch = null;
		Integer numTasks = null;

		for (int i = 0; i < args.length; i++) {
			if ("-threads".equals(args[i])) {
				threads = Integer.parseInt(args[i+1]);
				i++;
			} else if ("-depth".equals(args[i])) {
				depth = Integer.parseInt(args[i+1]);
				i++;
			} else if ("-topN".equals(args[i])) {
				topN = Integer.parseInt(args[i+1]);
				i++; 
			} else if ("-solrlisting".equals(args[i])) {
				solrUrlListing = StringUtils.lowerCase(args[i + 1]);
				i++;
			} else if ("-solrws".equals(args[i])) {
				solrUrlWebSearch = StringUtils.lowerCase(args[i + 1]);
				i++;
			} else if ("-numTasks".equals(args[i])) {
				numTasks = Integer.parseInt(args[i+1]);
				i++;
			} else if ("-continue".equals(args[i])) {
				// skip
			} else if (args[i] != null) {
				seedDir = args[i];
			}
		}
		Map<String,Object> argMap = ToolUtil.toArgMap(
				Nutch.ARG_THREADS, threads,
				Nutch.ARG_DEPTH, depth,
				Nutch.ARG_TOPN, topN,
				Nutch.ARG_SOLR_LISTING, solrUrlListing,
				Nutch.ARG_SOLR_WEB_SEARCH, solrUrlWebSearch,
				Nutch.ARG_SEEDDIR, seedDir,
				Nutch.ARG_NUMTASKS, numTasks);
		run(argMap);
		return 0;
	}

	public static void main(String[] args) throws Exception {
		WebSearchCrawler c = new WebSearchCrawler();
		Configuration conf = NutchConfiguration.create();
		int res = ToolRunner.run(conf, c, args);
		System.exit(res);
	}
}
