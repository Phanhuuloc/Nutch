package org.apache.nutch.atexpats.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.NutchIndexWriter;
import org.apache.nutch.indexer.solr.SolrConstants;
import org.apache.nutch.indexer.solr.SolrMappingReader;
import org.apache.nutch.indexer.solr.SolrUtils;
import org.apache.nutch.parse.utils.ConstantsTikaParser;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoviesSolrWriter implements NutchIndexWriter {

	public static final Logger LOG = LoggerFactory.getLogger(MoviesSolrWriter.class);

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
		  LOG.info("MOVIES SOLR WRITER: write");
		  LOG.info("MOVIES SOLR WRITER: doc field: " + doc.getFieldNames());
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
	    
	    //TODO Insert child doc
	    boolean isInsert = true;
	   for(int i = 0; i< inputDocs.size(); i++){
	    	SolrInputDocument solrInputDoc = inputDocs.get(i);
	    	Object idElement = inputDoc.getFieldValue("id");
	    	Object idSolr = solrInputDoc.getFieldValue("id");
	    	
	    	
	    	if(idElement!= null && idSolr != null){
	    		if(idElement.toString().equals(idSolr.toString())){
	    			for(String fieldname : inputDoc.getFieldNames()){
	    				if(
	    						!"id".equals(fieldname)
	    						&& (
	    								fieldname.contains("production_index_")
//	    								|| fieldname.contains("title_index_") 
//	    								|| fieldname.contains("content_index_")
//	    								|| fieldname.contains("desc_index_")
//	    								|| fieldname.contains("genre_index_")
	    								|| fieldname.contains("director_index_")
	    								|| fieldname.contains("actor_index_")
	    								)
	    						){
	    					LOG.info("MOVIES SOLR WRITER: add to fieldName: " + fieldname);
	    					solrInputDoc.addField(fieldname, inputDoc.getFieldValues(fieldname));
	    				}
	    			}
	    			isInsert = false;
	    			break;
	    		}
	    	}
	   }
	   
	   	if(inputDoc.getFieldValue(ConstantsTikaParser.film_id) == null ||
	   			"Not Found".equals(inputDoc.getFieldValue(ConstantsTikaParser.film_id).toString()) ||
	   			StringUtils.isBlank(inputDoc.getFieldValue(ConstantsTikaParser.film_id).toString())) {
	   		isInsert = false;
	   		
	   	}
	    
	    if(isInsert){
	    	inputDocs.add(inputDoc);
	    }
	    
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
	        for(SolrInputDocument sid : inputDocs) {
	        	LOG.info("film_name = " + sid.get(ConstantsTikaParser.film_name) + "-- key: " + sid.getFieldNames());
	        }
	        solr.add(inputDocs);
	        inputDocs.clear();
	      }
	    } catch (final SolrServerException e) {
	      throw new IOException(e);
	    }
	  }

}
