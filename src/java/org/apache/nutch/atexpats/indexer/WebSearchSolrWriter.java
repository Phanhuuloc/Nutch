package org.apache.nutch.atexpats.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.NutchIndexWriter;
import org.apache.nutch.indexer.solr.SolrConstants;
import org.apache.nutch.indexer.solr.SolrMappingReader;
import org.apache.nutch.indexer.solr.SolrUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSearchSolrWriter implements NutchIndexWriter {

	public static final Logger LOG = LoggerFactory.getLogger(WebSearchSolrWriter.class);

	  private SolrServer solr;
	  private SolrMappingReader solrMapping;

	  private final List<SolrInputDocument> inputDocs =
	    new ArrayList<SolrInputDocument>();

	  private int commitSize;

	  @Override
	  public void open(TaskAttemptContext job)
	  throws IOException {
	    Configuration conf = job.getConfiguration();
	    solr = SolrUtils.getCommonsHttpSolrServer(conf);
	    commitSize = conf.getInt(SolrConstants.COMMIT_SIZE, 1000);
	    solrMapping = SolrMappingReader.getInstance(conf);
	  }

	  @Override
	  public void write(NutchDocument doc) throws IOException {
	    final SolrInputDocument inputDoc = new SolrInputDocument();
	    for(final Entry<String, List<String>> e : doc) {
	      for (final String val : e.getValue()) {

	        Object val2 = val;
	        if (e.getKey().equals("content") || e.getKey().equals("title")) {
	          val2 = SolrUtils.stripNonCharCodepoints(val);
	        }

	        inputDoc.addField(solrMapping.mapKey(e.getKey()), val2);
	        String sCopy = solrMapping.mapCopyKey(e.getKey());
	        if (sCopy != e.getKey()) {
	        	inputDoc.addField(sCopy, val2);
	        }
	      }
	    }
	    inputDoc.setDocumentBoost(doc.getScore());
	    inputDocs.add(inputDoc);
	    if (inputDocs.size() >= commitSize) {
	      try {
	        LOG.info("Adding " + Integer.toString(inputDocs.size()) + " documents");
	        solr.add(inputDocs);
	      } catch (final SolrServerException e) {
	        throw new IOException(e);
	      }
	      inputDocs.clear();
	    }
	  }

	  @Override
	  public void close() throws IOException {
	    try {
	      if (!inputDocs.isEmpty()) {
	        LOG.info("Adding " + Integer.toString(inputDocs.size()) + " documents");
	        solr.add(inputDocs);
	        inputDocs.clear();
	      }
	    } catch (final SolrServerException e) {
	      throw new IOException(e);
	    }
	  }

}
