package sk.fiit.dps.team11.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import io.dropwizard.lifecycle.Managed;
import sk.fiit.dps.team11.models.VersionedValue;

public class DatabaseAdapter implements Managed {

	private Environment env;
	
	private Database databaseStore;
	
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
		try {
			getStore();
			
			// TODO
			return null;
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean put(byte[] key, VersionedValue value) {
		try {
			getStore();
			
			// TODO
			return true;
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}
	
	/*public boolean put(byte[] key, byte[] value) throws DatabaseException {
		OperationStatus status = getStore().put(null, new DatabaseEntry(key), new DatabaseEntry(value));
		return status == OperationStatus.SUCCESS;
	}
	
	public Optional<byte[]> get(byte[] key) throws DatabaseException {
		DatabaseEntry ret = new DatabaseEntry();
		OperationStatus status = getStore().get(null, new DatabaseEntry(key), ret, LockMode.RMW);
		
		if (status == OperationStatus.SUCCESS) {
			return Optional.of(ret.getData());
		} else {
			return Optional.empty();
		}
	}*/

}
