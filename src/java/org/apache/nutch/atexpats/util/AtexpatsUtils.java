package org.apache.nutch.atexpats.util;

import java.util.List;
import java.util.Map;

import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.atexpats.AtexpatsIndexerJob;
import org.apache.nutch.atexpats.MoviesIndexerJob;
import org.apache.nutch.atexpats.WebSearchIndexerJob;
import org.apache.nutch.atexpats.common.AtexpatsConstants;
import org.apache.nutch.entitybean.CinemaBean;
import org.apache.nutch.entitybean.CountryBean;
import org.apache.nutch.entitybean.LanguageBean;
import org.apache.nutch.util.NutchTool;

public class AtexpatsUtils {
	
	public static CinemaBean getCinemaFromDbAtexpats(String cinemaName, List<CinemaBean> list) {
		if(list != null && list.size() > 0) {
			double max = 0.0;
			double temp = 0.0;
			CinemaBean cinema = null;
			String cinemaNameFromDb;
			
			cinemaName = cinemaName.toLowerCase();
			cinemaName = cinemaName.replaceAll("lotte|cinema|galaxy", "");
			cinemaName = cinemaName.replaceAll("[\\p{Punct}]", ""); 
			cinemaName = cinemaName.replaceAll("\\s*", "");
			for(CinemaBean bean : list) {
				cinemaNameFromDb = bean.getCinemaName();
				cinemaNameFromDb = cinemaNameFromDb.toLowerCase();
				cinemaNameFromDb = cinemaNameFromDb.replaceAll("lotte|cinema|galaxy", "");
				cinemaNameFromDb = cinemaNameFromDb.replaceAll("[\\p{Punct}]", "");
				cinemaNameFromDb = cinemaNameFromDb.replaceAll("\\s*", "");
				
				temp = similarity(cinemaName, cinemaNameFromDb);
				if(max < temp) {
					max = temp;
					cinema = bean;
				}
				
				if(max == 1) {
					return bean;
				}
			}
			
			if(max > 0.6) {
				return cinema;
			}
		}
		return null;
	}
	
	public static StringBuffer append(String value, String regex, StringBuffer buffer) {
		if(buffer == null) {
			buffer = new StringBuffer();
			buffer.append(value);
		} else if(buffer.length() == 0) {
			buffer.append(value);
		} else {
			buffer.append(regex).append(value);
		}
		return buffer;
	}

	public static double similarity(String s1, String s2) {
		if (s1.length() < s2.length()) { // s1 should always be bigger
			String swap = s1;
			s1 = s2;
			s2 = swap;
		}
		int bigLen = s1.length();
		if (bigLen == 0) {
			return 1.0; /* both strings are zero length */
		}
		return (bigLen - computeEditDistance(s1, s2)) / (double) bigLen;
	}

	private static int computeEditDistance(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}
	
	public static CountryBean getCountryFromDbAtexpats(String countryName, List<CountryBean> list) {
		if(list == null || list.size() == 0) {
			return null;
		}
		
		double max = 0.0;
		double temp = 0.0;
		CountryBean country = null;
		String name = "";
		countryName = countryName.replaceAll("\\s*", "");
		
		for(CountryBean bean : list) {
			name = bean.getCountryTranslate();
			name = name.replaceAll("\\s*", "");
			temp = similarity(countryName, name);
			if(max < temp) {
				max = temp;
				country = bean;
			}
			
			if(max == 1.0) {
				return bean;
			}
		}
		
		if(max > 0.8) {
			return country;
		}
		
		return null;
	}
	
	public static LanguageBean getLanguageCode(String language, List<LanguageBean> list) {
		if(list == null || list.size() == 0) {
			return null;
		}
		double max = 0.0;
		double temp = 0.0;
		String languageStranlate = "";
		LanguageBean languageBean = null;
		language = language.toLowerCase();
		language = language.replaceAll("tiếng", "");
		language = language.replaceAll("\\s*", "");
		
		for(LanguageBean bean : list) {
			languageStranlate = bean.getLanguageTranslate();
			languageStranlate = languageStranlate.toLowerCase();
			languageStranlate = languageStranlate.replaceAll("tiếng", "");
			languageStranlate = languageStranlate.replaceAll("\\s*", "");
			
			temp = similarity(language, languageStranlate);
			if(max < temp) {
				max = temp;
				languageBean = bean;
			}
			
			if(max == 1.0) {
				return languageBean;
			}
		}
		
		if(max > 0.85) {
			return languageBean;
		}
		
		return null;
	}
	
	
	public static Map<String,Object> runIndex(Class<? extends NutchTool> toolClass,
			Map<String,Object> args, int type, NutchTool tool) throws Exception {
		
		String[] arr;
		if(AtexpatsConstants.TYPE_LISTING == type) {
			arr = new String[] {/*args.get(Nutch.ARG_SOLR_LISTING).toString(),*/ "-reindex"};
			ToolRunner.run(tool.getConf(),
					new AtexpatsIndexerJob(), arr);
		} else if(AtexpatsConstants.TYPE_WEB_SEARCH == type){
			arr = new String[] {/*args.get(Nutch.ARG_SOLR_WEB_SEARCH).toString(),*/ "-reindex"};
			ToolRunner.run(tool.getConf(),
					new WebSearchIndexerJob(), arr);
		} else if(AtexpatsConstants.TYPE_MOVIE == type){
			arr = new String[] {/*args.get(Nutch.ARG_SOLR_MOVIE).toString(),*/ "-reindex"};
			ToolRunner.run(tool.getConf(),
					new MoviesIndexerJob(), arr);
		}
		
		return null;
	}
}
