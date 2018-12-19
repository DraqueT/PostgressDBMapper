/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sql.db.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DThompson
 */
public class TableObject {
    private final String name;
    private List<TableKeyLocal> localKeys = new ArrayList<>();
    private Map<String, TableKeyForeign> foreignKeys = new HashMap<>();
    private List<TableColumn> columns = new ArrayList<>();
    
    public TableObject(String _name) {
        name = _name;
    }
    
    public List<TableKeyLocal> getLocalKeys() {
        return localKeys;
    }
    
    public List<TableKeyForeign> getForeignKeys() {
        return new ArrayList(foreignKeys.values());
    }
    
    public TableKeyForeign getForeignKeyForTable(String tableName) {
        TableKeyForeign ret = null;
        
        for (TableKeyForeign curKey : foreignKeys.values()) {
            if (curKey.getForeignTable().equals(tableName)) {
                ret = curKey;
                break;
            }
        }
        
        return ret;
    }
    
    public String getName() {
        return name;
    }
    
    public List<TableColumn> getColumns() {
        return columns;
    }
    
    public void addColumn(TableColumn column) {
        columns.add(column);
    }
    
    public void setColumns(List<TableColumn> _columns) {
        columns = _columns;
    }
    
    public void addLocalKey(TableKeyLocal key) {
        localKeys.add(key);
    }
    
    private void addForeignKey(TableKeyForeign key) {
        if (!foreignKeys.containsKey(key.getKeyName())) {
            foreignKeys.put(key.getKeyName(), key);
        }
    }
    
    public void setForeignKeys(List<TableKeyForeign> _foreignKeys) {
        foreignKeys = new HashMap<>();
        for (TableKeyForeign key : _foreignKeys) {
            addForeignKey(key);
        }
    }
    
    public void setLocalKeys(List<TableKeyLocal> _localKeys) {
        localKeys = _localKeys;
    } 
    
    public TableKeyForeign getForeignKeyByForeignTable(String foreignTableName) {
        TableKeyForeign ret = null;
        
        for (TableKeyForeign testKey : foreignKeys.values()) {
            if (testKey.getForeignTable().equals(foreignTableName)) {
                ret = testKey;
                break;
            }
        }
        
        return ret;
    }
}
