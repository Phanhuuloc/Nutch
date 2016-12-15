package org.apache.nutch.parse.utils;

public class ConstantsTikaParser {
	// time out
	public static int TIMEOUT_CONNECTION = 3000;

	// Path
	public static final String PROFILES_PATH = "conf/profiles";
	public static final String MOVIE_MAP_PATH = "conf/map-file/movie_map.txt";
	public static final String PRODUCTION_MAP_PATH = "conf/map-file/movie_map.txt";
	public static final String VERSION_MAP_PATH = "conf/map-file/version.txt";
	public static final String STOPWORD_FILM_NAME = "conf/map-file/stopword_film_name.txt";
	public static final String FILM_NAME_LANGUAGE_MAP = "conf/map-file/film_name_language.txt";

	//public static final String idFilmFromDb = "idFilmFromDb";
	public static final String film_name = "film_name";
	public static final String film_id = "film_id";
	public static final String language = "language";
	public static final String fullURL = "fullURL";
	public static final String year_product = "year_product";
	// From date To date
	public static final String showing_time = "showing_time";
	public static final String genre = "genre";
	public static final String age_limit = "age_limit";
	public static final String dimension = "dimension";
	public static final String rating = "rating";
	public static final String total_rated = "total_rated";
	public static final String film_country = "film_country";
	public static final String film_language = "film_language";
	public static final String actor = "actor";
	public static final String director = "director";
	public static final String film_duration = "film_duration";
	public static final String producer = "producer";
	public static final String version = "version";
	public static final String trailer = "trailer";
	public static final String photo = "photo";
	public static final String movie_description = "movie_description";
	public static final String schedule = "schedule";
	public static final String official_movie = "official_movie";
	public static final String film_language_code = "film_language_code";
	public static final String film_country_code = "film_country_code";
	public static final String film_sub_language = "film_sub_language";
	public static final String film_sub_language_code = "film_sub_language_code";
	

	// links that contain detail of director
	public static final String en_director_links = "en_director_links";
	public static final String vi_director_links = "vi_director_links";
	public static final String ja_director_links = "ja_director_links";
	public static final String ko_director_links = "ko_director_links";
	// detail of director
	public static final String en_director_list = "en_director_list";
	public static final String vi_director_list = "vi_director_list";
	public static final String ja_director_list = "ja_director_list";
	public static final String ko_director_list = "ko_director_list";
	// links that contain detail of actor
	public static final String en_actor_links = "en_actor_links";
	public static final String vi_actor_links = "vi_actor_links";
	public static final String ja_actor_links = "ja_actor_links";
	public static final String ko_actor_links = "ko_actor_links";
	// detail of actor
	public static final String en_actor_list = "en_actor_list";
	public static final String vi_actor_list = "vi_actor_list";
	public static final String ja_actor_list = "ja_actor_list";
	public static final String ko_actor_list = "ko_actor_list";
	
	public static final String review = "review";
	public static final String note = "note";
	public static final String other_languge_link = "other_languge_link";
	public static final String production_name = "production_name"; //production to get detail of film
	public static final String productions = "productions"; // list production of film

	// cinema
	public static final String cinema_site = "cinema_site";
	public static final String cinema_film_name = "cinema_film_name";
	public static final String cinema_showing_time = "cinema_showing_time";
	public static final String cinema_genre = "cinema_genre";
	public static final String cinema_dimension = "cinema_dimension";
	public static final String cinema_actor = "cinema_actor";
	public static final String cinema_director = "cinema_director";
	public static final String cinema_duration = "cinema_duration";
	public static final String cinema_productions = "cinema_productions";
	public static final String cinema_version = "cinema_version";
	public static final String cinema_trailer = "cinema_trailer";
	public static final String cinema_photo = "cinema_photo";
	public static final String cinema_movie_description = "cinema_movie_description";

	// wikipedia
	//public static final String wiki_movie_description = "wiki_movie_description";
	//public static final String en_wiki_movie_description = "en_wiki_movie_description";
	//public static final String vi_wiki_movie_description = "vi_wiki_movie_description";
	//public static final String ja_wiki_movie_description = "ja_wiki_movie_description";
	//public static final String ko_wiki_movie_description = "ko_wiki_movie_description";
	
	public static final String en_wiki_link = "en_wiki_link";
	public static final String vi_wiki_link = "vi_wiki_link";
	public static final String ja_wiki_link = "ja_wiki_link";
	public static final String ko_wiki_link = "ko_wiki_link";
	
	public static final String vi_wiki_director_name = "vi_wiki_director_name";
	public static final String vi_wiki_actor_name = "vi_wiki_actor_name";

	// check same film
	//public static final String is_exist_from_db = "is_exist_from_db";
	//public static final String url_exist_from_db = "url_exist_from_db";

	// Cassandra Database
	public static final String KEYSPACE = "webpage";
	public static final String COLUMN_FAMILY = "sc";
	public static final String HOST = "host";
	public static final String THRIFT_PORT = "thrift_port";
	public static final String USERNAME_CASSANDRA = "username_cassandra";
	public static final String PASSWORD_CASSANDRA = "password_cassandra";

	public static final int ROWS = 10000;
	public static final int JMX_PORT = 7199;
	
	// Id from MySQL
	public static final String film_genre_id = "film_genre_id";
	public static final String director_id = "director_id";
	public static final String actor_id = "actor_id";
	public static final String cinema_id = "cinema_id";
	public static final String production_id = "production_id";
	
	
	//////////////// format data to index ///////////////////////
	public static final String cinema_film_start = "cinema_film_start";
	public static final String cinema_film_end = "cinema_film_end";
	public static final String cinema_film_schedule = "cinema_film_schedule";
	public static final String review_info = "review_info";
	public static final String review_content = "review_content";
	
	public static final String review_from = "review_from";
	public static final String review_total = "review_total";
	
	public static final String title_index_en_text = "title_index_en_text";
	public static final String content_index_en_text  = "content_index_en_text";
	public static final String desc_index_en_text  = "desc_index_en_text";
	
	public static final String  title_index_ja_text = "title_index_ja_text";
	public static final String  content_index_ja_text = "content_index_ja_text";
	public static final String  desc_index_ja_text = "desc_index_ja_text";
	
	public static final String  title_index_ko_text = "title_index_ko_text";
	public static final String  content_index_ko_text = "content_index_ko_text";
	public static final String  desc_index_ko_text = "desc_index_ko_text";
	
	public static final String  title_index_vi_text = "title_index_vi_text";
	public static final String  content_index_vi_text = "content_index_vi_text";
	public static final String  desc_index_vi_text = "desc_index_vi_text";
	
	// production
	public static final String  production_index_en_text_multi = "production_index_en_text_multi";
	public static final String  production_index_ja_text_multi = "production_index_ja_text_multi";
	public static final String  production_index_ko_text_multi = "production_index_ko_text_multi";
	public static final String  production_index_vi_text_multi = "production_index_vi_text_multi";
	
	// genre
	public static final String  genre_index_en_text_multi = "genre_index_en_text_multi";
	public static final String  genre_index_ja_text_multi = "genre_index_ja_text_multi";
	public static final String  genre_index_ko_text_multi = "genre_index_ko_text_multi";
	public static final String  genre_index_vi_text_multi = "genre_index_vi_text_multi";
	
	// crew & cast
	public static final String  director_index_en_text_multi = "director_index_en_text_multi";
	public static final String  director_index_ja_text_multi = "director_index_ja_text_multi";
	public static final String  director_index_ko_text_multi = "director_index_ko_text_multi";
	public static final String  director_index_vi_text_multi = "director_index_vi_text_multi";
	public static final String  actor_index_en_text_multi = "actor_index_en_text_multi";
	public static final String  actor_index_ja_text_multi = "actor_index_ja_text_multi";
	public static final String  actor_index_ko_text_multi = "actor_index_ko_text_multi";
	public static final String  actor_index_vi_text_multi = "actor_index_vi_text_multi";
	
	// cinema
	public static final String  cinema_index_en_text_multi = "cinema_index_en_text_multi";
	public static final String  cinema_index_ja_text_multi = "cinema_index_ja_text_multi";
	public static final String  cinema_index_ko_text_multi = "cinema_index_ko_text_multi";
	public static final String  cinema_index_vi_text_multi = "cinema_index_vi_text_multi";
	
	// review
	public static final String  review_content_index_en_text_multi = "review_content_index_en_text_multi";
	public static final String  review_content_index_ja_text_multi = "review_content_index_ja_text_multi";
	public static final String  review_content_index_ko_text_multi = "review_content_index_ko_text_multi";
	public static final String  review_content_index_vi_text_multi = "review_content_index_vi_text_multi";
	
	
	/**
	 *  Constanst OF Train
	 */
	
	
	public static final String route_train = "route_train";
	public static final String original_url = "original_url";
	public static final String default_code_train = "default_code_train";
	public static final String remaining_code_train = "remaining_code_train";
	public static final String default_direction_train = "default_direction_train";
	public static final String remaining_direction_train = "remaining_direction_train";
	
	// index
	public static final String train_id = "_train_id";
	public static final String train_code_list = "train_code_list";
	public static final String train_index_ = "_train_index_";
	public static final String train_line_index_vi_text = "train_line_index_vi_text";
	public static final String text = "_text";
	public static final String train_code = "_train_code";
	public static final String station_id = "_station_id";
	public static final String station_name_vi = "_station_name_vi";
	public static final String station_name_en = "_station_name_en";
	public static final String station_name_ja = "_station_name_ja";
	public static final String station_name_ko = "_station_name_ko";
	public static final String station_name_zh = "_station_name_zh";
	
	public static final String departure_time = "_departure_time";
	public static final String arrival_time = "_arrival_time";
	public static final String distance = "_distance";
	public static final String arrival_add_date = "_arrival_add_date";
	
	
	
	// Patten
	public static final String INDEX_MULTI_VALUES_PATTEN = "$$";
	public static final String REGEX_INDEX_MULTI_VALUES_PATTEN = "\\$\\$";

}
