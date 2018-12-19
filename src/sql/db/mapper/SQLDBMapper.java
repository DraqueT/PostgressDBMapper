/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sql.db.mapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author DThompson
 */
public class SQLDBMapper {

    final String host = "jdbc:postgresql://localhost:5432/sheep_oltp";
    final String userName = "sheep_oltp";
    final String pass = "";
    Connection con;

    /**
     * @param args the command line arguments
     */
    public static void mainX(String[] args) {
        try {
            final String host = "localhost:5432/sheep_oltp";
            final String userName = "sheep_oltp";
            final String pass = "";
            DatabaseReader myReader = DatabaseReader.create(host, userName, pass, null);
        } catch (Exception e) {
            System.out.println("LOL OOPS: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        /*try {
            Class.forName("org.postgresql.Driver");
            new SQLDBMapper().run();
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("ERROR: " + e.getLocalizedMessage());
            //e.printStackTrace();
        }

        try {
            Class.forName("org.postgresql.Driver");
            new SQLDBMapper().run();
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("ERROR: " + e.getLocalizedMessage());
            //e.printStackTrace();
        }*/
    }

    private void run() throws SQLException {
        con = DriverManager.getConnection(host, userName, pass);

        /*String[] tables = {"message"};//{"v2shift_message"};//, "shift", "message"};
        
        for (String table : tables) {
            TablePrintout(table, "BASE TABLE");
        }*/
        try (Statement stmt = con.createStatement()) {
            cycleAllTablesPrintout(stmt);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getLocalizedMessage());
            //e.printStackTrace();
        }
    }

    private void cycleAllTablesPrintout(Statement stmt) throws SQLException {
        String query = "select * from information_schema.tables";
        ResultSet rs = stmt.executeQuery(query);

        // cycle through all tables
        while (rs.next()) {
            TablePrintout(rs.getString("table_name"), rs.getString("table_type"));
        }
    }

    private void TablePrintout(String tableName, String tableType) throws SQLException {
        System.out.println(tableName + ":\t" + tableType);

        String query = "select *\n"
                + "    from INFORMATION_SCHEMA.COLUMNS \n"
                + "    where table_name = '" + tableName + "';";

        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            //printMetaData(rs.getMetaData());

            if (tableType.equals("BASE TABLE")) {
                getPrimaryKeys(tableName).forEach((key) -> {
                    System.out.println("\tPKey: " + key);
                });
            } else if (tableType.equals("VIEW")) {

            }

            printForiegnKeyInformation(getForeignKeyInformation(tableName));
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getLocalizedMessage());
            //e.printStackTrace();
        }
    }

    /**
     * Prints all metadata from a resultset
     *
     * @param metaData
     * @throws SQLException
     */
    private void printMetaData(ResultSetMetaData metaData) throws SQLException {
        int numColumns = metaData.getColumnCount();

        for (int i = 1; i <= numColumns; i++) {
            System.out.println(metaData.getColumnName(i));
        }
    }

    private List<String> getPrimaryKeys(String tableName) {
        List<String> ret = new ArrayList<>();
        String query = "SELECT a.attname\n"
                + "FROM   pg_index i\n"
                + "JOIN   pg_attribute a ON a.attrelid = i.indrelid\n"
                + "                     AND a.attnum = ANY(i.indkey)\n"
                + "WHERE  i.indrelid = '" + tableName + "'::regclass\n"
                + "AND    i.indisprimary;";

        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                ret.add(rs.getString("attname"));
            }
        } catch (SQLException e) {
            System.out.println("ERROR " + tableName + ": " + e.getLocalizedMessage());
            //e.printStackTrace();
        }

        return ret;
    }

    /**
     * Gets foreign key information and returns list of maps with column info.
     * Entry keys are colum names from SQL query constraint_name, table_name,
     * column_name, foreign_table_name, foreign_column_name
     *
     * @param tableName
     * @return
     */
    private List<Map<String, String>> getForeignKeyInformation(String tableName) {
        List<Map<String, String>> ret = new ArrayList<>();

        String query = "SELECT\n"
                + "tc.constraint_name, tc.table_name, kcu.column_name,"
                + "ccu.table_name AS foreign_table_name,"
                + "ccu.column_name AS foreign_column_name\n"
                + "FROM\n"
                + "information_schema.table_constraints AS tc\n"
                + "JOIN information_schema.key_column_usage AS kcu\n"
                + "ON tc.constraint_name = kcu.constraint_name\n"
                + "JOIN information_schema.constraint_column_usage AS ccu\n"
                + "ON ccu.constraint_name = tc.constraint_name\n"
                + "WHERE constraint_type = 'FOREIGN KEY' AND tc.table_name='" + tableName + "';";

        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Map<String, String> fk = new HashMap<>();

                fk.put("constraint_name", rs.getString("constraint_name"));
                fk.put("table_name", rs.getString("table_name"));
                fk.put("column_name", rs.getString("column_name"));
                fk.put("foreign_table_name", rs.getString("foreign_table_name"));
                fk.put("foreign_column_name", rs.getString("foreign_column_name"));

                ret.add(fk);
            }
        } catch (SQLException e) {
            System.out.println("ERROR " + tableName + ": " + e.getLocalizedMessage());
            //e.printStackTrace();
        }

        return ret;
    }

    /**
     * Prints fkey information supplied by getForeignKeyInformation()
     *
     * @param info
     */
    private void printForiegnKeyInformation(List<Map<String, String>> info) {
        for (Map<String, String> curRelMap : info) {
            System.out.println("\tConstraint Name: " + curRelMap.get("constraint_name"));
            System.out.println("\t\tLTable: " + curRelMap.get("table_name"));
            System.out.println("\t\tLColumn: " + curRelMap.get("column_name"));
            System.out.println("\t\tFTable: " + curRelMap.get("foreign_table_name"));
            System.out.println("\t\tFColumn: " + curRelMap.get("foreign_column_name"));
        }
    }

    /**
     * Gets count of rows in table
     *
     * @param tableName
     * @return
     */
    private int getRowCount(String tableName) {
        int ret = 0;
        String query = "SELECT COUNT(*) FROM " + tableName;

        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            ret = rs.getInt("count");
        } catch (SQLException e) {
            System.out.println("getRowCount() ERROR " + tableName + ": " + e.getLocalizedMessage());
            //e.printStackTrace();
        }

        return ret;
    }
}
