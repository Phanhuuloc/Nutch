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
import org.apache.solr.common.SolrInputField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListingSolrWriter implements NutchIndexWriter {

	public static final Logger LOG = LoggerFactory.getLogger(ListingSolrWriter.class);

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
	    
	    //TODO Insert child doc
	    boolean isInsert = true;
	   for(int i = 0; i< inputDocs.size(); i++){
	    	SolrInputDocument solrInputDoc = inputDocs.get(i);
	    	Object idElement = inputDoc.getFieldValue("id");
	    	Object idSolr = solrInputDoc.getFieldValue("id");
	    	
	    	
	    	if(idElement!= null && idSolr != null){
	    		if(idElement.toString().equals(idSolr.toString())){
	    			for(String fieldname : inputDoc.getFieldNames()){
							if (!"id".equals(fieldname)
									&& (fieldname.contains("title_ws_")
											|| fieldname.contains("content_ws_")
											|| fieldname.contains("meta_ws_") || fieldname
												.contains("web_url"))) {
								solrInputDoc.addField(fieldname,
										inputDoc.getFieldValues(fieldname));
							}
							if("sameUrlId".equalsIgnoreCase(fieldname)) {
								if(!solrInputDoc.containsKey("sameUrlId")) {
									solrInputDoc.addField(fieldname, inputDoc.getFieldValue(fieldname));
								}
							}
	    			}
	    			isInsert = false;
	    			break;
	    		}
	    	}
	   }
	   
	//   if(!inputDoc.containsKey("photo")) {
//	   	if(inputDoc.getFieldValue(ConstantsTikaParser.idFilmFromDb) == null ||
//	   			"Not Found".equals(inputDoc.getFieldValue(ConstantsTikaParser.idFilmFromDb).toString()) ||
//	   			StringUtils.isBlank(inputDoc.getFieldValue(ConstantsTikaParser.idFilmFromDb).toString())) {
//	   		isInsert = false;
//	   		
//	   	}
	    
	    if(isInsert){
	    	inputDocs.add(inputDoc);
	    }
	    
	    if (inputDocs.size() >= commitSize) {
	      try {
	    	checkForMultipleIndex(); // check for multiple index @dpqhuy
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
	    	checkForMultipleIndex(); // check for multiple index @dpqhuy
	        LOG.info("Adding " + Integer.toString(inputDocs.size()) + " documents");
	        solr.add(inputDocs);
	        inputDocs.clear();
	      }
	    } catch (final SolrServerException e) {
	      throw new IOException(e);
	    }
	  }
	  
	  private void checkForMultipleIndex() {
		  LOG.info("Before Check Multiple Index. Have " + Integer.toString(inputDocs.size()) + " documents");
		  if(!inputDocs.isEmpty()) {
			  SolrInputDocument solrDoc;
			  for(int i = 0; i< inputDocs.size(); i++){
				  solrDoc = inputDocs.get(i);
				  if(solrDoc.containsKey("sameUrlId")) {
					    SolrInputDocument cloneDoc;
						String[] idArr = solrDoc.getFieldValue("sameUrlId").toString().split(",");
						for(String id : idArr) {
							cloneDoc = new SolrInputDocument();
							cloneDoc.addField("id", id);
							for(final SolrInputField e : solrDoc) {
								if(!"sameUrlId".equalsIgnoreCase(e.getName()) && !"id".equalsIgnoreCase(e.getName())) {
							        cloneDoc.addField(e.getName(), e.getValues());
							    }
							}
							inputDocs.add(cloneDoc);
						}
						solrDoc.removeField("sameUrlId");
						solrDoc.remove("sameUrlId");
				  }
			  }
		  }
		  LOG.info("After Check Multiple Index. Have " + Integer.toString(inputDocs.size()) + " documents");
	  }

}
