package com.gooddata.qa.utils.snowflake;

import net.snowflake.client.jdbc.SnowflakeConnectionV1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class SnowflakeUtils {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeUtils.class);
    ConnectionInfo snowflakeConnectionInfo;

    public SnowflakeUtils(ConnectionInfo connectionInfo) {
        snowflakeConnectionInfo = connectionInfo;
    }

    /**
     * Create database witch specific name on the Snowflake.
     *
     * @param databaseName
     */
    public void createDatabase(String databaseName) throws SQLException {
        // create or replace new database
        executeSql("CREATE OR REPLACE DATABASE " + databaseName + " comment='This database is using by ATT team'");
        logger.info("Created database with specific name is: " + databaseName);
    }

    /**
     * Drop database on the snowflake.
     *
     * @param databaseName name of database will be dropped.
     */
    public void dropDatabaseIfExists(String databaseName) throws SQLException {
        executeSql("DROP DATABASE IF EXISTS " + databaseName);
        logger.info("Dropped the database with name is: " + databaseName);
    }

    /**
     * Upload CSV to Snowflake stage.
     *
     * @param stageName
     * @param tableName
     * @param csvFullPath
     */
    public void uploadCsv2Snowflake(String stageName, String prefix, String tableName, String csvFullPath)
            throws SQLException, FileNotFoundException {
        createSnowflakeLocalStage(stageName);
        uploadCsvToSnowflakeStage(stageName, prefix, tableName, csvFullPath);
        loadCsvDataFromStage(stageName, tableName);
    }

    /**
     * Create a local stage with specific name on Snowflake.
     *
     * @param stageName
     */
    public void createSnowflakeLocalStage(String stageName) throws SQLException {
        executeSql("CREATE OR REPLACE STAGE " + stageName);
        logger.info("Created Snowflake stage with name is: " + stageName);
    }

    /**
     * Upload a CSV file to stage on Snowflake.
     *
     * @param stageName
     * @param csvPath
     */
    public void uploadCsvToSnowflakeStage(String stageName, String prefix, String desFileName, String csvPath)
            throws SQLException, FileNotFoundException {
        // Put local csv to InputStream
        File file = new File(csvPath);
        InputStream fileInputStream = new FileInputStream(file);

        // Upload CSV files as an InputStream
        Connection connection = buildConnection(snowflakeConnectionInfo);
        ((SnowflakeConnectionV1) connection).compressAndUploadStream(stageName, prefix, fileInputStream, desFileName);
        logger.info("Uploaded CSV files to stage: " + stageName);
    }

    /**
     * Copy CSV data from a stage to table.
     *
     * @param stageName
     * @param tableName
     */
    public void loadCsvDataFromStage(String stageName, String tableName) throws SQLException {
        executeSql(String.format(
                "COPY INTO %s FROM @%s file_format=(type=csv error_on_column_count_mismatch=false skip_header = 1)",
                tableName, stageName));
        logger.info("Loaded CSV data from stage " + stageName + "to table " + tableName);
    }

    /**
     * Create or replace table if exists on Snowflake database.
     *
     * @param tableName     name of table.
     * @param listOfColumns need at least one column to create table.
     */
    public void createTable(String tableName, List<DatabaseColumn> listOfColumns) throws SQLException {
        // setup columns
        String columnsOfTable = setupColumnsOfTable(listOfColumns);

        // create table
        executeSql("CREATE TABLE " + tableName + "(" + columnsOfTable + ")");
        logger.info("Created table with name: " + tableName);
    }

    /**
     * Setup columns for table structure.
     *
     * @return columns will be created along with table.
     */
    public String setupColumnsOfTable(List<DatabaseColumn> listOfColumns) {
        // add comma between columns to applicable to SQL format
        return listOfColumns
                .stream()
                .map(DatabaseColumn::toString)
                .collect(Collectors.joining(", "));
    }

    /**
     * Drop tables on Snowflake.
     *
     * @param tableNames
     */
    public void dropTables(String... tableNames) throws SQLException {
        for (String willDeleteTableName : tableNames) {
            executeSql("DROP TABLE IF EXISTS " + willDeleteTableName + " CASCADE;");
            logger.info("Dropped table with name: " + willDeleteTableName);
        }
    }

    /**
     * Execute SQL statement on the snowflake via given connection info.
     *
     * @param sqlStr SQL command or a script (multiple colon-separated commands) to execute.
     */
    public void executeSql(String sqlStr) throws SQLException {
        // create JDBC connection to Snowflake
        Connection connection = buildConnection(snowflakeConnectionInfo);

        // create statements
        Statement statement = connection.createStatement();

        // execute SQL commands
        logger.info("Executing the SQL statements");
        System.out.println(sqlStr);
        statement.executeUpdate(sqlStr);
        logger.info("Done executing the SQL statements");

        // close JDBC connection
        statement.close();
        connection.close();
    }

    /**
     * Build JDBC connection to snowflake.
     *
     * @param connectionInfo snowflake connection information.
     * @return a connection point to Snowflake database schema.
     */
    private static Connection buildConnection(ConnectionInfo connectionInfo) throws SQLException {
        try {
            Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Snowflake driver not found" + ex);
        }

        // build connection properties
        Properties properties = new Properties();
        properties.put("user", connectionInfo.getUserName());
        properties.put("password", connectionInfo.getPassword());
        properties.put("warehouse", connectionInfo.getWarehouse());
        properties.put("db", connectionInfo.getDatabase());
        properties.put("schema", connectionInfo.getSchema());

        String connectStr = connectionInfo.getUrl();
        return DriverManager.getConnection(connectStr, properties);
    }
}