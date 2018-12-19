/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sql.db.mapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author DThompson
 */
public class DatabaseReader {

    private final Connection con;
    private final Map<String, TableObject> tables = new HashMap();
    // reverse index has keys = table names, list of tables linking to it
    private final Map<String, List<String>> reverseIndex = new HashMap<>();
    private final JTextArea outputText;
    private final List<String> allTableNames;
    
    public static DatabaseReader create(String host,
            String userName,
            String pass,
            JTextArea _updateWindow) throws SQLException, ClassNotFoundException, Exception {
        Class.forName("org.postgresql.Driver");

        DatabaseReader ret = new DatabaseReader("jdbc:postgresql://" + host, userName, pass, _updateWindow);

        ret.populateTables();

        return ret;
    }

    private DatabaseReader(String host, String userName, String pass, JTextArea _updateWindow) throws SQLException {
        outputText = _updateWindow;
        con = DriverManager.getConnection(host, userName, pass);
        allTableNames = getAllTableNames();
    }

    private void populateTables() throws SQLException, Exception {
        for (String name : allTableNames) {
            TableObject myTable = new TableObject(name);
            myTable.setColumns(getTableColumns(name));
            myTable.setForeignKeys(getForeignKeyInformation(name));
            myTable.setLocalKeys(getLocalKeyInformation(name));
            tables.put(name, myTable);

            if (outputText != null) {
                outputText.setText(outputText.getText() + "\nFetching: " + name);
                outputText.setCaretPosition(outputText.getDocument().getLength());
            }
        }
    }

    private List<TableColumn> getTableColumns(String tableName) throws SQLException, Exception {
        List<TableColumn> ret = new ArrayList<>();
        String qry = "SELECT column_name, ordinal_position, udt_name\n"
                + "FROM information_schema.columns\n"
                + "WHERE  table_name   = '" + tableName + "'";

        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(qry);
            while (rs.next()) {
                ColumnType type;
                switch (rs.getString("udt_name")) {
                    case "int4":
                        type = ColumnType.int4;
                        break;
                    case "int2":
                        type = ColumnType.int2;
                        break;
                    case "timestamp":
                        type = ColumnType.timestamp;
                        break;
                    case "bool":
                        type = ColumnType.bool;
                        break;
                    case "text":
                        type = ColumnType.text;
                        break;
                    case "varchar":
                        type = ColumnType.varchar;
                        break;
                    case "float8":
                        type = ColumnType.float8;
                        break;
                    case "float4":
                        type = ColumnType.float4;
                        break;
                    case "char":
                        type = ColumnType.chara;
                        break;
                    case "_float4":
                    case "anyarray":
                    case "oid":
                    case "bytea":
                    case "timestamptz":
                    case "_text":
                    case "_oid":
                    case "_char":
                    case "_int2":
                    case "date":
                    case "int8":
                    case "json":
                    case "time":
                    case "jsonb":
                    case "hstore":
                    case "name":
                    case "regproc":
                    case "pg_node_tree":
                    case "aclitem":
                    case "_aclitem":
                    case "profile_service_migration_status":
                    case "period":
                    case "inet":
                    case "interval":
                    case "oidvector":
                    case "xid":
                    case "int2vector":
                    case "process_status":
                    case "uuid":
                    case "numeric":
                    case "regclass":
                    case "txid_snapshot":
                        type = ColumnType.SomeDamnedThing;
                        break;
                    default:
                        throw new Exception("Unknown type ID: " + rs.getString("udt_name"));
                }

                TableColumn myColumn = new TableColumn(rs.getString("column_name"), type, rs.getInt("ordinal_position"));

                ret.add(myColumn);
            }

        } catch (SQLException e) {
            throw new SQLException("Problem gathering column info, table "
                    + tableName + ":" + e.getLocalizedMessage());
        }

        return ret;
    }

    private List<String> getAllTableNames() throws SQLException {
        List<String> ret = new ArrayList<>();

        String qry = "SELECT table_name FROM information_schema.tables "
                + "WHERE table_type = 'BASE TABLE'"
                + "ORDER BY table_name";

        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(qry);

            while (rs.next()) {
                ret.add(rs.getString("table_name"));
            }
        } catch (SQLException e) {
            throw new SQLException("Problem populating information: " + e.getLocalizedMessage());
        }

        return ret;
    }

    private List<TableKeyLocal> getLocalKeyInformation(String tableName) throws Exception {
        List<TableKeyLocal> ret = new ArrayList<>();
        String query = "SELECT\n"
                + " t.relname as table_name, i.relname as index_name,\n"
                + " a.attname as column_name, ix.indisprimary as is_primary\n"
                + "FROM\n"
                + " pg_class t,\n"
                + " pg_class i,\n"
                + " pg_index ix,\n"
                + " pg_attribute a\n"
                + "WHERE\n"
                + " t.oid = ix.indrelid\n"
                + " AND i.oid = ix.indexrelid\n"
                + " AND a.attrelid = t.oid\n"
                + " AND a.attnum = ANY(ix.indkey)\n"
                + " AND t.relkind = 'r'\n"
                + " AND t.relname = '" + tableName + "'";

        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            Map<String, TableKeyLocal> keyMap = new HashMap<>();
            while (rs.next()) {
                String indexName = rs.getString("index_name");
                String columnName = rs.getString("column_name");

                if (!keyMap.containsKey(indexName)) {
                    keyMap.put(indexName, new TableKeyLocal(indexName, tableName));
                }

                TableKeyLocal myKey = keyMap.get(indexName);

                myKey.addLocalKeyValue(columnName);
                myKey.setPrimary(rs.getBoolean("is_primary"));
            }

            keyMap.values().forEach((key) -> {
                ret.add(key);
            });
        } catch (SQLException e) {
            throw new Exception("ERROR " + tableName + ": " + e.getLocalizedMessage());
        }

        return ret;
    }

    private List<TableKeyForeign> getForeignKeyInformation(String tableName) throws Exception {
        List<TableKeyForeign> ret = new ArrayList<>();

        String query = "SELECT\n"
                + "tc.constraint_name, kcu.column_name,"
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
                String foreignTable = rs.getString("foreign_table_name");
                TableKeyForeign key = new TableKeyForeign(rs.getString("constraint_name"),
                        tableName,
                        foreignTable);

                key.addLocalKeyValue(rs.getString("column_name"));
                key.addForeignKeyValue(rs.getString("foreign_column_name"));

                ret.add(key);

                if (reverseIndex.containsKey(foreignTable)) {
                    reverseIndex.get(foreignTable).add(tableName);
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(tableName);
                    reverseIndex.put(foreignTable, list);
                }
            }
        } catch (SQLException e) {
            throw new Exception("ERROR " + tableName + ": " + e.getLocalizedMessage());
        }

        return ret;
    }

    /**
     * *
     * Gets list of all tables dependant on table matching given table name
     *
     * @param name
     * @return
     */
    public List<String> getDependantTables(String name) {
        return reverseIndex.get(name);
    }

    public List<String> getStoredTableNames() {
        return allTableNames;
    }

    public DefaultMutableTreeNode getDependancyTree(String tableName) {
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode();
        populateDependancyTree(tableName, ret, new ArrayList<>());
        return ret;
    }

    private void populateDependancyTree(String tableName, DefaultMutableTreeNode parent, List<String> _visitedTables) {
        List<String> visitedTables = new ArrayList(_visitedTables);

        if (!visitedTables.contains(tableName) && reverseIndex.containsKey(tableName)) {
            visitedTables.add(tableName);
            
            for (String depTable : reverseIndex.get(tableName)) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode();
                TableKeyForeign curKey = tables.get(depTable).getForeignKeyForTable(tableName);
                
                if (curKey != null) {
                    newNode.setUserObject(depTable + "." 
                            + curKey.getForeignKeyValues().get(0) + " = " 
                            + tableName + "." + curKey.getLocalKeyValues().get(0));
                } else {
                    newNode.setUserObject(depTable);
                }
                
                populateDependancyTree(depTable, newNode, visitedTables);
                parent.add(newNode);
            }
        }
    }
    
    public TableObject getTable(String tableName) {
        return tables.containsKey(tableName) ? tables.get(tableName) : null;
    }
    
    public List<List<String>> getPathsToTable(String startTable, 
            String targetTable, int maxDepth, boolean reverseIndexSearch) {
        return DatabaseReader.this.getPathsToTable(startTable, targetTable, new ArrayList<>(), 
                maxDepth, reverseIndexSearch);
    }
    
    // TODO: maybe make list of keys leading from one place to the other?
    public List<List<String>> getPathsToTable(String currentPosition, 
            String targetTable, List<String> _currentPath, int maxDepth, 
            boolean reverseIndexSearch) {
        List<List<String>> ret = new ArrayList<>();
        List<String> currentPath = new ArrayList(_currentPath);
        
        if (currentPosition.equals(targetTable)) {
            currentPath.add(currentPosition);
            ret.add(currentPath);
        } else if (!currentPath.contains(currentPosition) 
                && currentPath.size() < maxDepth) {
            currentPath.add(currentPosition);            
            TableObject currentPositionTable = tables.get(currentPosition);
            
            for (TableKeyForeign fKey : currentPositionTable.getForeignKeys()) {
                ret.addAll(DatabaseReader.this.getPathsToTable(fKey.getForeignTable(), targetTable, 
                        currentPath, maxDepth, reverseIndexSearch));
            }
            
            if (reverseIndexSearch && reverseIndex.containsKey(currentPosition)) {
                for (String fTable : reverseIndex.get(currentPosition)) {
                    // make a checkbox that turns this on and off
                    ret.addAll(DatabaseReader.this.getPathsToTable(fTable, targetTable, currentPath, maxDepth, reverseIndexSearch));
                }
            }
        }        
        
        return ret;
    }
}
