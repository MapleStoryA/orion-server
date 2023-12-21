package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Slf4JSqlLogger;
import server.base.config.Config;
import server.base.config.ServerConfig;

@Slf4j
public class DatabaseConnection {

    public static final int RETURN_GENERATED_KEYS = 1;
    private static ConnectionPool pool;

    public static Connection getConnection() {
        try {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            log.debug(stackTraceElements[2].toString());
            return pool.getConnection();
        } catch (SQLException ex) {
            log.error("Could not get connection. Error: ", ex);
            return null;
        }
    }

    public static Jdbi getConnector() {
        return pool.getJdbi();
    }

    public static void initConfig(ServerConfig config) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "Unable to find JDBC library. Do you have MySQL Connector/J (if using default" + " DBC driver)?");
        }
        Config.Database database = ServerConfig.serverConfig().getConfig().getDatabase();
        String password = database.getPassword();
        if (password == null) {
            throw new DatabaseException("Database password not provided.");
        }

        var hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(database.getUrl());
        hikariConfig.setUsername(database.getUser());
        hikariConfig.setPassword(database.getPassword());
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setMinimumIdle(10);
        hikariConfig.setMaximumPoolSize(30);

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "25");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("leakDetectionThreshold", "60000");
        hikariConfig.setConnectionTestQuery("SELECT 1");

        pool = new HikariConnectionPool(new HikariDataSource(hikariConfig));
    }

    interface ConnectionPool {
        Connection getConnection() throws SQLException;

        Jdbi getJdbi();
    }

    static class HikariConnectionPool implements ConnectionPool {

        private final HikariDataSource hikariDataSource;
        private final Jdbi jdbi;

        public HikariConnectionPool(HikariDataSource hikariDataSource) {
            this.hikariDataSource = hikariDataSource;
            this.jdbi = Jdbi.create(hikariDataSource);
            this.jdbi.setSqlLogger(new Slf4JSqlLogger());
        }

        @Override
        public Connection getConnection() throws SQLException {
            return hikariDataSource.getConnection();
        }

        @Override
        public Jdbi getJdbi() {
            return jdbi;
        }
    }
}
