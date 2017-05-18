package kr.ac.jbnu.ssel.instantfeedback.tool.db;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.hsqldb.Server;

/**
 * A class to handle hsqldb server start/stop and get server instance
 *
 */
public class HSQLDBClass {
    // to store the database server instance
    private static Map<String, Server> _dbMap = new HashMap<String, Server>();
    private static Lock lock = new ReentrantLock();

    public static Server startServer (String dbName, String path, PrintWriter logWriter) {
        lock.lock();
        // 'Server' is a class of HSQLDB representing
        // the database server
        Server hsqlServer = null;
        try {
            if (isServerExist(dbName)) {
                stopServer(dbName);
            } else {
                hsqlServer = new Server();
    
                hsqlServer.setLogWriter(logWriter);
                hsqlServer.setDatabaseName(0, dbName);
                hsqlServer.setDatabasePath(0, "file:"+path);
    
                // Start the database!
                hsqlServer.start();
                _dbMap.put(dbName, hsqlServer);
            }
        } finally {
            lock.unlock();
        }
        return hsqlServer;
    }

    public static void stopServer (String dbName) {
        lock.lock();
        try {
            Server hsqlServer = _dbMap.get(dbName);
            if (hsqlServer != null) {
                System.out.println("stop server");
                hsqlServer.stop();
                _dbMap.remove(dbName);
            }
        } finally {
            lock.unlock();
        }
    }
    
    public static Server getServer (String dbName) {
        lock.lock();
        Server hsqlServer = null;
        try {
            hsqlServer = _dbMap.get(dbName);
        } finally {
            lock.unlock();
        }
        return hsqlServer;
    }
    
    private static boolean isServerExist (String dbName) {
        if (_dbMap.containsKey(dbName))
            return true;
        return false;
    }
}
