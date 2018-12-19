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
public abstract class TableKeyPattern {
    protected final String keyName;
    protected boolean isPrimary = false;
    protected final List<String> localKeyValues = new ArrayList<>();
    protected final String localTable;
    
    public abstract boolean isLocal();
    
    public TableKeyPattern(String _keyName, String _localTable) {
        localTable = _localTable;
        keyName = _keyName;
    }
    
    public List<String> getLocalKeyValues() {
        return localKeyValues;
    }
    
    public String getLocalTable() {
        return localTable;
    }
    
    public void addLocalKeyValue(String value) {
        localKeyValues.add(value);
    }
    
    public String getKeyName() {
        return keyName;
    }
}
