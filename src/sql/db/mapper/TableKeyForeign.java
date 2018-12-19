/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sql.db.mapper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author DThompson
 */
public class TableKeyForeign extends TableKeyPattern {
    private final List<String> foreignKeyValues = new ArrayList<>();    
    private final String foreignTable;
    
    public TableKeyForeign(String _keyName, String _localTable, String _foreignTable) {
        super(_keyName, _localTable);
        foreignTable = _foreignTable;
    }
    
    @Override
    public boolean isLocal() {
        return false;
    }
    
    public void addForeignKeyValue(String keyValue) {
        foreignKeyValues.add(keyValue);
    }
    
    public List<String> getForeignKeyValues() {
        return foreignKeyValues;
    }
    
    public String getForeignTable() {
        return foreignTable;
    }
}
