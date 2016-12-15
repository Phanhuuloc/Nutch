/*
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
package org.apache.nutch.parse.tika;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.OutlinkExtractor;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseFilters;
import org.apache.nutch.parse.ParseStatusCodes;
import org.apache.nutch.parse.ParseStatusUtils;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.storage.ParseStatus;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.storage.WebPage.Field;
import org.apache.nutch.util.Bytes;
import org.apache.nutch.util.MimeUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.TableUtil;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

/**
 * Wrapper for Tika parsers. Mimics the HTMLParser but using the XHTML
 * representation returned by Tika as SAX events
 ***/

public class TikaParser implements org.apache.nutch.parse.Parser,Constants {

  public static final Logger LOG = LoggerFactory.getLogger(TikaParser.class);

  private static Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();

  static {
    FIELDS.add(WebPage.Field.BASE_URL);
    FIELDS.add(WebPage.Field.CONTENT_TYPE);
  }

  private Configuration conf;
  private TikaConfig tikaConfig = null;
  private DOMContentUtils utils;
  private ParseFilters htmlParseFilters;
  private String cachingPolicy;

  @Override
  public Parse getParse(String url, WebPage page) {

    String baseUrl = TableUtil.toString(page.getBaseUrl());
    URL base;
    try {
      base = new URL(baseUrl);
    } catch (MalformedURLException e) {
      return ParseStatusUtils.getEmptyParse(e, getConf());
    }

    // get the right parser using the mime type as a clue
    String mimeType = page.getContentType().toString();
    Parser parser = tikaConfig.getParser(mimeType);
    ByteBuffer raw = page.getContent();
    
    /**
	 * Quark parser
	 * */
	QuarkHTMLParser quarkParser = new QuarkHTMLParser();

    if (parser == null) {
      String message = "Can't retrieve Tika parser for mime-type " + mimeType;
      LOG.error(message);
      return ParseStatusUtils.getEmptyParse(ParseStatusCodes.FAILED_EXCEPTION,
          message, getConf());
    }

    LOG.debug("Using Tika parser " + parser.getClass().getName() + " for mime-type "
        + mimeType);

    Metadata tikamd = new Metadata();

    HTMLDocumentImpl doc = new HTMLDocumentImpl();
    doc.setErrorChecking(false);
    DocumentFragment root = doc.createDocumentFragment();
    DOMBuilder domhandler = new DOMBuilder(doc, root);
    ParseContext context = new ParseContext();
    // to add once available in Tika
    // context.set(HtmlMapper.class, IdentityHtmlMapper.INSTANCE);
    try {
      parser.parse(new ByteArrayInputStream(raw.array(), raw.arrayOffset() + raw.position(),
          raw.remaining()), domhandler, tikamd, context);

      // Logic for parsing for Quark here
      if (url.contains(Constants.INDIAMART)) {
        quarkParser.parseIndiaMart(url,
                new ByteArrayInputStream(raw.array(), raw.arrayOffset() + raw.position(), raw.remaining()), tikamd);

      } else if (url.contains(Constants.EXPORTPAGES)) {
        quarkParser.parseExportPages(url,
                new ByteArrayInputStream(raw.array(), raw.arrayOffset() + raw.position(), raw.remaining()), tikamd);

      } else if(url.contains(Constants.MADE_IN_CHINA)){
          quarkParser.readAndParseMadeInChinaSite(url,
                  new ByteArrayInputStream(raw.array(), raw.arrayOffset() + raw.position(), raw.remaining()), tikamd);
      } else if(url.contains(TRADEFORD)){
          quarkParser.readAndParseTradeFordSite(url,
                  new ByteArrayInputStream(raw.array(), raw.arrayOffset() + raw.position(), raw.remaining()), tikamd);
      } else if(url.contains(TOBOC)){
          quarkParser.readAndParseTobocSite(url,
                  new ByteArrayInputStream(raw.array(), raw.arrayOffset() + raw.position(), raw.remaining()), tikamd);
      }

    } catch (Exception e) {
      LOG.error("Error parsing "+url,e);
      //return ParseStatusUtils.getEmptyParse(e, getConf());
    }

    HTMLMetaTags metaTags = new HTMLMetaTags();
    String text = "";
    String title = "";
    Outlink[] outlinks = new Outlink[0];

    // we have converted the sax events generated by Tika into a DOM object
    // so we can now use the usual HTML resources from Nutch
    // get meta directives
    HTMLMetaProcessor.getMetaTags(metaTags, root, base);
    if (LOG.isTraceEnabled()) {
      LOG.trace("Meta tags for " + base + ": " + metaTags.toString());
    }

    // check meta directives
    if (!metaTags.getNoIndex()) { // okay to index
      StringBuffer sb = new StringBuffer();
      if (LOG.isTraceEnabled()) {
        LOG.trace("Getting text...");
      }
      utils.getText(sb, root); // extract text
      text = sb.toString();
      sb.setLength(0);
      if (LOG.isTraceEnabled()) {
        LOG.trace("Getting title...");
      }
      utils.getTitle(sb, root); // extract title
      title = sb.toString().trim();
    }

    if (!metaTags.getNoFollow()) { // okay to follow links
      ArrayList<Outlink> l = new ArrayList<Outlink>(); // extract outlinks
      URL baseTag = utils.getBase(root);
      if (LOG.isTraceEnabled()) {
        LOG.trace("Getting links...");
      }
      utils.getOutlinks(baseTag != null ? baseTag : base, l, root);
      outlinks = l.toArray(new Outlink[l.size()]);
      if (LOG.isTraceEnabled()) {
        LOG.trace("found " + outlinks.length + " outlinks in " + base);
      }
    }

    // populate Nutch metadata with Tika metadata
    String[] TikaMDNames = tikamd.names();
    for (String tikaMDName : TikaMDNames) {
      if (tikaMDName.equalsIgnoreCase(TikaCoreProperties.TITLE.toString()))
      continue;
      // TODO what if multivalued?
      page.putToMetadata(new Utf8(tikaMDName), ByteBuffer.wrap(Bytes.toBytes(tikamd
          .get(tikaMDName))));
    }

    // no outlinks? try OutlinkExtractor e.g works for mime types where no
    // explicit markup for anchors

    if (outlinks.length == 0) {
      outlinks = OutlinkExtractor.getOutlinks(text, getConf());
    }

    ParseStatus status = ParseStatusUtils.STATUS_SUCCESS;
    if (metaTags.getRefresh()) {
      status.setMinorCode(ParseStatusCodes.SUCCESS_REDIRECT);
      status.addToArgs(new Utf8(metaTags.getRefreshHref().toString()));
      status.addToArgs(new Utf8(Integer.toString(metaTags.getRefreshTime())));
    }

    Parse parse = new Parse(text, title, outlinks, status);
    parse = htmlParseFilters.filter(url, page, parse, metaTags, root);

    if (metaTags.getNoCache()) { // not okay to cache
      page.putToMetadata(new Utf8(Nutch.CACHING_FORBIDDEN_KEY), ByteBuffer.wrap(Bytes
          .toBytes(cachingPolicy)));
    }

    return parse;
  }

  public void setConf(Configuration conf) {
    this.conf = conf;
    this.tikaConfig = null;

    try {
      tikaConfig = TikaConfig.getDefaultConfig();
    } catch (Exception e2) {
      String message = "Problem loading default Tika configuration";
      LOG.error(message, e2);
      throw new RuntimeException(e2);
    }

    this.htmlParseFilters = new ParseFilters(getConf());
    this.utils = new DOMContentUtils(conf);
    this.cachingPolicy = getConf().get("parser.caching.forbidden.policy",
        Nutch.CACHING_FORBIDDEN_CONTENT);
  }

  public TikaConfig getTikaConfig(){
	  return this.tikaConfig;
  }
  
  public Configuration getConf() {
    return this.conf;
  }

  @Override
  public Collection<Field> getFields() {
    return FIELDS;
  }

  // main class used for debuggin
  public static void main(String[] args) throws Exception {
    String name = args[0];
    String url = "file:" + name;
    File file = new File(name);
    byte[] bytes = new byte[(int) file.length()];
    DataInputStream in = new DataInputStream(new FileInputStream(file));
    in.readFully(bytes);
    Configuration conf = NutchConfiguration.create();
    // TikaParser parser = new TikaParser();
    // parser.setConf(conf);
    WebPage page = new WebPage();
    page.setBaseUrl(new Utf8(url));
    page.setContent(ByteBuffer.wrap(bytes));
    MimeUtil mimeutil = new MimeUtil(conf);
    String mtype = mimeutil.getMimeType(file);
    page.setContentType(new Utf8(mtype));
    // Parse parse = parser.getParse(url, page);

    Parse parse = new ParseUtil(conf).parse(url, page);

    System.out.println("content type: " + mtype);
    System.out.println("title: " + parse.getTitle());
    System.out.println("text: " + parse.getText());
    System.out.println("outlinks: " + Arrays.toString(parse.getOutlinks()));
  }

/* (non-Javadoc)
 * @see org.apache.nutch.parse.Parser#getTextFromContent(org.apache.nutch.storage.WebPage)
 */
@Override
public Map<String, String> getTextFromContent(WebPage page) {
	// TODO Auto-generated method stub
	return null;
}
}
