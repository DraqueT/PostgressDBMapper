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
public class TableEntry implements Comparable<TableEntry> {
    private final String tableName;
    private final TableEntryList parent;
    private final List<FKeyRelation> fKeys = new ArrayList<>();
    private final List<String> thisToForeign = new ArrayList<>();
    private final List<String> foreignToThis = new ArrayList<>();
    
    public TableEntry(TableEntryList _parent, String _tableName) {
        parent = _parent;
        tableName = _tableName;
    }
    
    @Override
    public int compareTo(TableEntry o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
        public void addFKey(FKeyRelation key) {
        fKeys.add(key);
    }
    
    public List<FKeyRelation> getFKeys() {
        return fKeys;
    }
}
