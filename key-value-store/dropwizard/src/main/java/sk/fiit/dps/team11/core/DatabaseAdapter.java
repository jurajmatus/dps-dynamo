package sk.fiit.dps.team11.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

import io.dropwizard.lifecycle.Managed;
import sk.fiit.dps.team11.models.VersionedValue;

public class DatabaseAdapter implements Managed {

	private Environment env;
	
	private Database databaseStore;
	
	private final static ObjectMapper MAPPER = new ObjectMapper();
	
	@Override
	public void start() throws Exception {
		EnvironmentConfig config = new EnvironmentConfig();
		config.setTransactional(true);
		config.setAllowCreate(true);
		
		Path dbPath = Paths.get("./db");
		Files.createDirectories(dbPath);
		
		env = new Environment(dbPath.toFile(), config);
	}

	@Override
	public void stop() throws Exception {
		if (databaseStore != null) {
			databaseStore.close();
		}
		
		env.close();
	}
	
	private Database getStore() throws DatabaseException {
		if (databaseStore != null) {
			return databaseStore;
		}
		
		DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(true);
        
		databaseStore = env.openDatabase(null, "store", dbConfig);
		return databaseStore;
	}
	
	public VersionedValue get(byte[] key) {
		DatabaseEntry dkey = new DatabaseEntry(key);
		DatabaseEntry dvalue = new DatabaseEntry();
		
		OperationStatus status;
		try {
			status = getStore().get(null, dkey, dvalue, LockMode.DEFAULT);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		
		if (status != OperationStatus.SUCCESS) {
			return new VersionedValue(Version.INITIAL, Arrays.asList());
		}
		
		try {
			return MAPPER.readValue(dvalue.getData(), VersionedValue.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean put(byte[] key, VersionedValue value, VersionedValue expectedOldValue) {
		
			DatabaseEntry dkey = new DatabaseEntry(key);
			DatabaseEntry doldValue = new DatabaseEntry();
			
			try {
				Transaction tr = env.beginTransaction(null, null);
				getStore().get(tr, dkey, doldValue, LockMode.RMW);
			
				if (doldValue.getData() == null
					|| Arrays.equals(MAPPER.writeValueAsBytes(expectedOldValue), doldValue.getData())) {
					
					DatabaseEntry dnewValue = new DatabaseEntry(MAPPER.writeValueAsBytes(value));
					OperationStatus status = getStore().put(tr, dkey, dnewValue);
					
					if (status == OperationStatus.SUCCESS) {
						tr.commit();
						return true;
					} else {
						tr.abort();
						return false;
					}
				} else {
					tr.abort();
					return false;
				}
				
			} catch (DatabaseException|IOException e) {
				throw new RuntimeException(e);
			}
	}
	
	public long numEntries() {
		try {
			return getStore().count();
		} catch (DatabaseException e) {
			return 0;
		}
	}
	
	public void clear() throws DatabaseException {
		DatabaseEntry _key = new DatabaseEntry();
		forEach((key, val) -> {
			_key.setData(key);
			try {
				getStore().delete(null, _key);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	/**
	 * Iterates all keys in the database
	 * @throws DatabaseException 
	 */
	public void forEach(BiConsumer<byte[], VersionedValue> consumer) throws DatabaseException {
		Cursor cursor = null;
		
		try {
			cursor = getStore().openCursor(null, null);
			
			DatabaseEntry dkey = new DatabaseEntry();
			DatabaseEntry dvalue = new DatabaseEntry();
			
			while (cursor.getNext(dkey, dvalue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				try {
					VersionedValue value = MAPPER.readValue(dvalue.getData(), VersionedValue.class);
					consumer.accept(dkey.getData(), value);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	
}
