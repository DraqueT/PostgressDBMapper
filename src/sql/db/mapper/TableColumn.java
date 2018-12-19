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
public class TableColumn {
    private final String columnName;
    private final ColumnType type;
    private final int ordinalPosition;
    
    public TableColumn(String _columnName, ColumnType _type, int _ordinalPosition) {
        columnName = _columnName;
        type = _type;
        ordinalPosition = _ordinalPosition;
    }
    
    public String getColumnName() {
        return columnName;
    }

    public ColumnType getType() {
        return type;
    }

    public int getOrdinalPosition() {
        return ordinalPosition;
    }
}
