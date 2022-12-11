/*
 * This file is part of the OdinMS MapleStory Private Server
 * Copyright (C) 2011 Patrick Huy and Matthias Butz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import server.config.ServerConfig;

import java.sql.Connection;
import java.sql.SQLException;

@lombok.extern.slf4j.Slf4j
public class DatabaseConnection {

    public static final int RETURN_GENERATED_KEYS = 1;
    private static ConnectionPool pool;


    public static Connection getConnection() {
        try {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            log.info(stackTraceElements[2].toString());
            return pool.getConnection();
        } catch (SQLException ex) {
            log.error("Could not get connection. Error: ", ex);
            return null;
        }
    }

    public static void initConfig(ServerConfig config) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Unable to find JDBC library. Do you have MySQL Connector/J (if using default JDBC driver)?");
        }

        String password = config.getProperty("database.password");
        if (password == null) {
            throw new DatabaseException("Database password not provided.");
        }

        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getProperty("database.url"));
        hikariConfig.setUsername(config.getProperty("database.user"));
        hikariConfig.setPassword(config.getProperty("database.password"));

        hikariConfig.setConnectionTimeout(30000);

        hikariConfig.setIdleTimeout(10000);

        hikariConfig.setMaximumPoolSize(30);

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setConnectionTestQuery("SELECT 1");


        pool = new HikariConnectionPool(new HikariDataSource(hikariConfig));
    }


    interface ConnectionPool {
        Connection getConnection() throws SQLException;
    }

    static class HikariConnectionPool implements ConnectionPool {


        private final HikariDataSource hikariDataSource;

        public HikariConnectionPool(HikariDataSource hikariDataSource) {
            this.hikariDataSource = hikariDataSource;
        }


        @Override
        public Connection getConnection() throws SQLException {
            return hikariDataSource.getConnection();
        }
    }
}

