package org.apache.nutch.cassandra.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.apache.cassandra.thrift.*;
import org.apache.cassandra.tools.NodeProbe;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.tika.metadata.Metadata;

/**
 * Client class to interact with Cassandara cluster
 * 
 */
public class Client {
	public static final String DEFAULT_THRIFT_HOST = "192.168.1.241";
	public static final int DEFAULT_THRIFT_PORT = 9160;
	public static final int DEFAULT_JMX_PORT = 7199;
	public static final String DEFAULT_UERNAME = "";
	public static final String DEFAULT_PASSWORD = "";
	private static final String UTF8 = "UTF8";

	private static final String CQL_URL = "jdbc:cassandra:/@%s:%d/%s";

	private static final String COLUMN_FAMILY_TYPE_SUPER = "Super";

	public enum ColumnType {
		SUPER("Super"), STANDARD("Standard");

		private String type;

		private ColumnType(String type) {
			this.type = type;
		}

		public String toString() {
			return type;
		}
	}

	private TTransport transport;
	private TProtocol protocol;
	private Cassandra.Client client;
	private NodeProbe probe;

	private Connection db;
	private Statement st;

	private boolean connected = false;
	private boolean cqlConnected = false;
	private String host;
	private String username;
	private String password;
	private int thriftPort;
	private int jmxPort;

	private String keyspace;
	private String columnFamily;
	private boolean superColumn;

	public Client() {
		this(DEFAULT_THRIFT_HOST, DEFAULT_THRIFT_PORT, DEFAULT_JMX_PORT);
	}

	public Client(String host) {
		this(host, DEFAULT_THRIFT_PORT, DEFAULT_JMX_PORT);
	}

	public Client(String host, int thriftPort, int jmxPort) {
		this.host = host;
		this.thriftPort = thriftPort;
		this.jmxPort = jmxPort;
	}

	public Client(String host, String username, String password, int thriftPort, int jmxPort) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.thriftPort = thriftPort;
		this.jmxPort = jmxPort;
	}

	public void connect() throws IOException, InterruptedException, AuthenticationException, AuthorizationException, TException {
		if (!connected) {
			// Updating the transport to Framed one as it has been depreciated
			// with Cassandra 0.7.0
			transport = new TFramedTransport(new TSocket(host, thriftPort));
			protocol = new TBinaryProtocol(transport);
			client = new Cassandra.Client(protocol);
			// probe = new NodeProbe(host, thriftPort);
			transport.open();
			// client.execute_cql_query(ByteBuffer.wrap("ss".getBytes()),
			// Compression.NONE);

			if (username != null && username.trim().length() > 0 && password != null && password.trim().length() > 0) {
				Map<String, String> creds = new HashMap<String, String>();
				creds.put("username", username);
				creds.put("password", password);
				client.login(new AuthenticationRequest(creds));
			}
			connected = true;
		}
	}

	public void disconnect() {
		if (connected) {
			transport.close();
			connected = false;
		}
	}

	public void cqlConnect(String keyspace) throws ClassNotFoundException, SQLException {
		if (!cqlConnected) {
			Class.forName("org.apache.cassandra.cql.jdbc.CassandraDriver");
			db = DriverManager.getConnection(String.format(CQL_URL, host, thriftPort, keyspace), new Properties());
			st = db.createStatement();
			cqlConnected = true;
		}
	}

	public void cqlDisconnect() throws SQLException {
		if (cqlConnected) {
			st.close();
			db.close();
			cqlConnected = false;
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public String describeClusterName() throws TException {
		return client.describe_cluster_name();
	}

	public String descriveVersion() throws TException {
		return client.describe_version();
	}

	public String describeSnitch() throws TException {
		return client.describe_snitch();
	}

	public Map<String, List<String>> describeSchemaVersions() throws InvalidRequestException, TException {
		return client.describe_schema_versions();
	}

	public String describePartitioner() throws TException {
		return client.describe_partitioner();
	}

	public List<TokenRange> describeRing(String keyspace) throws TException, InvalidRequestException {
		this.keyspace = keyspace;
		return client.describe_ring(keyspace);
	}

	public List<KsDef> getKeyspaces() throws TException, InvalidRequestException {
		return client.describe_keyspaces();
	}

	public KsDef describeKeyspace(String keyspaceName) throws NotFoundException, InvalidRequestException, TException {
		return client.describe_keyspace(keyspaceName);
	}

	/**
	 * 
	 * Retrieve Column metadata from a given keyspace
	 * 
	 * @param keyspace
	 * @param columnFamily
	 * @return
	 * @throws NotFoundException
	 * @throws TException
	 * @throws InvalidRequestException
	 */
	public Map<String, String> getColumnFamily(String keyspace, String columnFamily) throws NotFoundException, TException, InvalidRequestException {
		this.keyspace = keyspace;
		this.columnFamily = columnFamily;

		for (Iterator<CfDef> cfIterator = client.describe_keyspace(keyspace).getCf_defsIterator(); cfIterator.hasNext();) {
			CfDef next = cfIterator.next();
			if (columnFamily.equalsIgnoreCase(next.getName())) {
				Map<String, String> columnMetadata = new HashMap<String, String>();

				CfDef._Fields[] fields = CfDef._Fields.values();

				for (int i = 0; i < fields.length; i++) {
					CfDef._Fields field = fields[i];
					// using string concat to avoin NPE, if the value is not
					// null
					// need to find an elegant solution
					columnMetadata.put(field.name(), next.getFieldValue(field) + "");
				}

				return columnMetadata;
			}
		}
		System.out.println("returning null");
		return null;
	}

	public Map<String, Key> listKeyAndValues(String keyspace, String columnFamily, String startKey, String endKey, int rows)
			throws InvalidRequestException, UnavailableException, TimedOutException, TException, UnsupportedEncodingException {
		this.keyspace = keyspace;
		this.columnFamily = columnFamily;

		Map<String, Key> m = new TreeMap<String, Key>();

		ColumnParent columnParent = new ColumnParent(columnFamily);

		KeyRange keyRange = new KeyRange(rows);
		keyRange.setStart_key(ByteBuffer.wrap(startKey.getBytes()));
		keyRange.setEnd_key(ByteBuffer.wrap(endKey.getBytes()));

		SliceRange sliceRange = new SliceRange();
		sliceRange.setStart(new byte[0]);
		sliceRange.setFinish(new byte[0]);

		SlicePredicate slicePredicate = new SlicePredicate();
		slicePredicate.setSlice_range(sliceRange);
		client.set_keyspace(keyspace);

		List<KeySlice> keySlices = null;
		try {
			keySlices = client.get_range_slices(columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
		} catch (UnavailableException e) {
			return m;
		}

		for (KeySlice keySlice : keySlices) {
			Key key = new Key(new String(keySlice.getKey()), new TreeMap<String, SColumn>(), new TreeMap<String, Cell>());

			for (ColumnOrSuperColumn column : keySlice.getColumns()) {
				key.setSuperColumn(column.isSetSuper_column());
				if (column.isSetSuper_column()) {
					SuperColumn scol = column.getSuper_column();
					SColumn s = new SColumn(key, new String(scol.getName(), UTF8), new TreeMap<String, Cell>());
					for (Column col : scol.getColumns()) {
						Cell c = new Cell(s, new String(col.getName(), UTF8), new String(col.getValue(), UTF8), new Date(col.getTimestamp() / 1000));
						s.getCells().put(c.getName(), c);
					}

					key.getSColumns().put(s.getName(), s);
				} else {
					Column col = column.getColumn();
					Cell c = new Cell(key, new String(col.getName(), UTF8), new String(col.getValue(), UTF8), new Date(col.getTimestamp() / 1000));
					key.getCells().put(c.getName(), c);
				}
			}

			m.put(key.getName(), key);
		}

		return m;
	}

	public Map<String, Key> getDataFromDB(String keyspace, String columnFamily, String startKey, String endKey, int rows) {
		try {
			Map<String, String> m = getColumnFamily(keyspace, columnFamily);
			if (m.get(CfDef._Fields.COLUMN_TYPE.name()).equals(COLUMN_FAMILY_TYPE_SUPER)) {
				setSuperColumn(true);
				superColumn = true;
			} else {
				setSuperColumn(false);
				superColumn = false;
			}

			Map<String, Key> list = listKeyAndValues(keyspace, columnFamily, startKey, endKey, rows);

			return list;

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	public void convertDataToMetadata(Metadata metadata, Key key) {
		if (key.isSuperColumn()) {
			for (String sName : key.getSColumns().keySet()) { // sName = h,
																// mtdt, ol ...
				if ("mtdt".equals(sName)) {
					SColumn sc = key.getSColumns().get(sName);
					for (String cName : sc.getCells().keySet()) {
						Cell c = sc.getCells().get(cName);
						metadata.set(cName, c.getValue());
					}
				}
			}
		}
	}

	public void removeSuperColumn(String keyspace, String columnFamily, String key, String superColumn) throws InvalidRequestException,
			UnavailableException, TimedOutException, TException {
		ColumnPath colPath = new ColumnPath(columnFamily);
		colPath.setSuper_column(superColumn.getBytes());
		long timestamp = System.currentTimeMillis() * 1000;

		client.set_keyspace(keyspace);
		client.remove(ByteBuffer.wrap(key.getBytes()), colPath, timestamp, ConsistencyLevel.ONE);
	}

	/**
	 * @return the keyspace
	 */
	public String getKeyspace() {
		return keyspace;
	}

	/**
	 * @param keyspace
	 *            the keyspace to set
	 */
	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	/**
	 * @return the columnFamily
	 */
	public String getColumnFamily() {
		return columnFamily;
	}

	/**
	 * @param columnFamily
	 *            the columnFamily to set
	 */
	public void setColumnFamily(String columnFamily) {
		this.columnFamily = columnFamily;
	}

	/**
	 * @return the superColumn
	 */
	public boolean isSuperColumn() {
		return superColumn;
	}

	/**
	 * @param superColumn
	 *            the superColumn to set
	 */
	public void setSuperColumn(boolean superColumn) {
		this.superColumn = superColumn;
	}

	/**
	 * @return the strategyMap
	 */
	public static Map<String, String> getStrategyMap() {
		Map<String, String> strategyMap = new TreeMap<String, String>();
		strategyMap.put("SimpleStrategy", org.apache.cassandra.locator.SimpleStrategy.class.getSimpleName());
		// strategyMap.put("LocalStrategy",
		// "org.apache.cassandra.locator.LocalStrategy");
		strategyMap.put("NetworkTopologyStrategy", org.apache.cassandra.locator.NetworkTopologyStrategy.class.getSimpleName());
		// strategyMap.put("OldNetworkTopologyStrategy",
		// "org.apache.cassandra.locator.OldNetworkTopologyStrategy");
		return strategyMap;
	}

	public static Map<String, String> getComparatorTypeMap() {
		Map<String, String> comparatorMap = new TreeMap<String, String>();
		comparatorMap.put("org.apache.cassandra.db.marshal.AsciiType", "AsciiType");
		comparatorMap.put("org.apache.cassandra.db.marshal.BytesType", "BytesType");
		comparatorMap.put("org.apache.cassandra.db.marshal.LexicalUUIDType", "LexicalUUIDType");
		comparatorMap.put("org.apache.cassandra.db.marshal.LongType", "LongType");
		comparatorMap.put("org.apache.cassandra.db.marshal.TimeUUIDType", "TimeUUIDType");
		comparatorMap.put("org.apache.cassandra.db.marshal.UTF8Type", "UTF8Type");

		return comparatorMap;
	}

	public static Map<String, String> getValidationClassMap() {
		Map<String, String> validationClassMap = new TreeMap<String, String>();
		validationClassMap.put("org.apache.cassandra.db.marshal.AsciiType", "AsciiType");
		validationClassMap.put("org.apache.cassandra.db.marshal.BytesType", "BytesType");
		validationClassMap.put("org.apache.cassandra.db.marshal.IntegerType", "IntegerType");
		validationClassMap.put("org.apache.cassandra.db.marshal.LongType", "LongType");
		validationClassMap.put("org.apache.cassandra.db.marshal.TimeUUIDType", "TimeUUIDType");
		validationClassMap.put("org.apache.cassandra.db.marshal.UTF8Type", "UTF8Type");

		return validationClassMap;
	}
}
