/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sql.db.mapper;

import java.util.HashMap;

/**
 *
 * @author DThompson
 */
public class TableEntryList extends HashMap<String, TableEntry>{
    public TableEntry getOrCreateTableEntry(String tableName) {
        TableEntry ret;
        
        if (this.containsKey(tableName)) {
            ret = this.get(tableName);
        } else {
            ret = new TableEntry(this, tableName);
            super.put(tableName, ret);
        }
        
        return ret;
    }
}
