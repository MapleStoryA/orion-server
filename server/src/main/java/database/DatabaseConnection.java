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

import server.ServerProperties;
import tools.LockableList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseConnection {

    public static final int RETURN_GENERATED_KEYS = 1;
    private static ConnectionPool pool;


    public static Connection getConnection() {
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            System.out.println("Could not get connection. Error: " + e);
            e.printStackTrace();
            return null;
        }
    }

    public static void setProps() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Unable to find JDBC library. Do you have MySQL Connector/J (if using default JDBC driver)?");
        }
        String url = ServerProperties.getProperty("url");
        String user = ServerProperties.getProperty("user");
        String password = System.getenv("MYSQL_ROOT_PASSWORD");
        if (password == null) {
            throw new DatabaseException("Database password not provide.");
        }
        pool = new ThreadLocalConnections(url, user, password);
    }

    public static Map<Connection, SQLException> closeAll() {
        Map<Connection, SQLException> exceptions = new HashMap<>();
        LockableList<Connection> allConnections = pool.allConnections();
        allConnections.lockWrite();
        try {
            for (Iterator<Connection> iter = allConnections.iterator(); iter.hasNext(); ) {
                Connection con = iter.next();
                try {
                    con.close();
                    iter.remove();
                } catch (SQLException e) {
                    exceptions.put(con, e);
                    e.printStackTrace();
                }
            }
        } finally {
            allConnections.unlockWrite();
        }
        return exceptions;
    }

    private static boolean connectionCheck(Connection con) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("/* ping */ SELECT 1");
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException ex) {
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    if (ps != null) {
                        ps.close();
                    }
                } catch (SQLException ex) {
                    //nothing we can do!
                    ex.printStackTrace();
                }
            }
        }
    }

    private interface ConnectionPool {

        Connection getConnection() throws SQLException;

        void returnConnection(Connection con);

        LockableList<Connection> allConnections();

        int connectionsInUse();

        int totalConnections();
    }

    private static class ThreadLocalConnections extends ThreadLocal<Connection> implements ConnectionPool {

        private final LockableList<Connection> allConnections;
        private final AtomicInteger taken;
        private final ThreadLocal<SQLException> exceptions;
        private final String url, user, password;

        protected ThreadLocalConnections(String url, String user, String password) {
            allConnections = new LockableList<>(new LinkedList<Connection>());
            taken = new AtomicInteger(0);
            exceptions = new ThreadLocal<>();
            this.url = url;
            this.user = user;
            this.password = password;
        }

        @Override
        protected Connection initialValue() {
            try {
                Connection con = DriverManager.getConnection(url, user, password);
                allConnections.addWhenSafe(con);
                return con;
            } catch (SQLException e) {
                exceptions.set(/*new SQLException("Could not connect to database.", */e/*)*/);
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public Connection getConnection() throws SQLException {
            Connection con = get();
            if (con == null) {
                remove();
                SQLException ex = exceptions.get();
                exceptions.remove();
                throw ex;
            }
            if (connectionCheck(con)) {
                taken.incrementAndGet();
                return con;
            } else {
                try {
                    con.close();
                    allConnections.removeWhenSafe(con);
                } catch (SQLException e) {
                    throw new SQLException("Could not remove invalid connection to database.", e);
                }
                remove();
                taken.incrementAndGet();
                return get();
            }
        }

        @Override
        public void returnConnection(Connection con) {
            taken.decrementAndGet();
        }

        @Override
        public LockableList<Connection> allConnections() {
            return allConnections;
        }

        @Override
        public int connectionsInUse() {
            return taken.get();
        }

        @Override
        public int totalConnections() {
            return allConnections.size();
        }
    }
}

