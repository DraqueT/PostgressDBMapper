/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sql.db.mapper;

/**
 *
 * @author DThompson
 */
public class TableKeyLocal extends TableKeyPattern {
    private boolean primary = false;

    public TableKeyLocal(String _keyName, String _localTable) {
        super(_keyName, _localTable);
    }
    
    @Override
    public boolean isLocal() {
        return true;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
