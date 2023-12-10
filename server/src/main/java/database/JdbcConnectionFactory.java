package database;

import java.sql.Connection;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.ConnectionFactory;

@Slf4j
public class JdbcConnectionFactory implements ConnectionFactory {

    @Override
    public Connection openConnection() {
        return DatabaseConnection.getConnection();
    }
}
