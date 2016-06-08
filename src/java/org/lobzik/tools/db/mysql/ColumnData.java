package org.lobzik.tools.db.mysql;

import java.sql.*;
/**
 * Класс содержит параметры характеризующие солбец таблицы  в БД
 */
public class ColumnData 
{
  public String columnName = "";// Наименование столбца или поля
  public int SQLTypeId = 0; // Тип столбца в соответствии с константами java.sql.Types
  public String typeName = ""; //  SQL тип, зависящий от типа БД (например image MS SQL)
  public int columnSize = 0; //длинна поля
  public boolean isNullable = true; // возможно ли NULL
  public int ordinalPosition = 0; //порядок столбца в таблице (base 1)
  /**
   * Конструктор инициализирует значения атрибутов по строке ResultSet,
   * который может быть получен при вызове метода getColumns класса DatabaseMetadata
   * @throws java.sql.SQLException
   * @param rs
   */
  public ColumnData(ResultSet rs) throws SQLException
  {
    columnName = rs.getString("COLUMN_NAME");
    SQLTypeId = rs.getInt("DATA_TYPE");
    typeName = rs.getString("TYPE_NAME");
    columnSize = rs.getInt("COLUMN_SIZE");
    isNullable = !("NO".equalsIgnoreCase(rs.getString("IS_NULLABLE")));
    ordinalPosition = rs.getInt("ORDINAL_POSITION");
  }
  /**
   * преобразует значения атрибутов в текст, для вывода
   * @return String
   */
  public String toString()
  {
    StringBuffer sbf = new StringBuffer("");
    sbf.append("COLUMN_NAME="); sbf.append(columnName);
    sbf.append(";SQL_TYPE="); sbf.append(getSqlTypeName(SQLTypeId));
    sbf.append(";INNER_SQL_TYPE="); sbf.append(typeName);
    sbf.append(";COLUMN_SIZE="); sbf.append(columnSize);
    sbf.append(";IS_NULLABLE="); sbf.append(isNullable);
    sbf.append(";ORDER="); sbf.append(ordinalPosition);
    return sbf.toString();
  }
  /**
   * Метод возвращант текстовое прндставление типов полей в таблице декларированных
   * java.sql.Types
   * 
   * @param typeId
   * @return String
   */
  public static String getSqlTypeName(int typeId )
  {
    switch(typeId)
    {
      case Types.ARRAY: return "ARRAY";
      case Types.BIGINT: return "BIGINT";
      case Types.BINARY: return "BINARY";
      case Types.BIT: return "BIT";
      case Types.BLOB: return "BLOB";
      case Types.BOOLEAN: return "BOOLEAN";
      case Types.CHAR: return "CHAR";
      case Types.CLOB: return "CLOB";
      case Types.DATALINK: return "DATALINK";
      case Types.DATE: return "DATE";
      case Types.DECIMAL: return "DECIMAL";
      case Types.DISTINCT: return "DISTINCT";
      case Types.DOUBLE: return "DOUBLE";
      case Types.FLOAT: return "FLOAT";
      case Types.INTEGER: return "INTEGER";
      case Types.JAVA_OBJECT: return "JAVA_OBJECT";
      case Types.LONGVARBINARY: return "LONGVARBINARY";
      case Types.LONGVARCHAR: return "LONGVARCHAR";
      case Types.NULL: return "NULL";
      case Types.NUMERIC: return "NUMERIC";
      case Types.OTHER: return "OTHER";
      case Types.REAL: return "REAL";
      case Types.REF: return "REF";
      case Types.SMALLINT: return "SMALLINT";
      case Types.STRUCT: return "STRUCT";
      case Types.TIME: return "TIME";
      case Types.TIMESTAMP: return "TIMESTAMP";
      case Types.TINYINT: return "TINYINT";
      case Types.VARBINARY: return "VARBINARY";
      case Types.VARCHAR: return "VARCHAR";
      
    }
    return "unknown";
  }
}
/* 
 * ВСЕ ЧТО СОДЕРЖИТ RESULTSET
 * TABLE_CAT String => table catalog (may be null) 
 * TABLE_SCHEM String => table schema (may be null) 
 * TABLE_NAME String => table name 
 * COLUMN_NAME String => column name 
 * DATA_TYPE int => SQL type from java.sql.Types 
 * TYPE_NAME String => Data source dependent type name, for a UDT the type name is fully qualified 
 * COLUMN_SIZE int => column size. For char or date types this is the maximum number of characters, for numeric or decimal types this is precision. 
 * BUFFER_LENGTH is not used. 
 * DECIMAL_DIGITS int => the number of fractional digits 
 * NUM_PREC_RADIX int => Radix (typically either 10 or 2) 
 * NULLABLE int => is NULL allowed
 * REMARKS String => comment describing column (may be null) 
 * COLUMN_DEF String => default value (may be null) 
 * SQL_DATA_TYPE int => unused 
 * SQL_DATETIME_SUB int => unused 
 * CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the column 
 * ORDINAL_POSITION int => index of column in table (starting at 1) 
 * IS_NULLABLE String => "NO" means column definitely does not allow NULL values; "YES" means the column might allow NULL values. An empty string means nobody knows. 
 * SCOPE_CATLOG String => catalog of table that is the scope of a reference attribute (null if DATA_TYPE isn't REF) 
 * SCOPE_SCHEMA String => schema of table that is the scope of a reference attribute (null if the DATA_TYPE isn't REF) SCOPE_TABLE String => table name that this the scope of a reference attribure (null if the DATA_TYPE isn't REF) 
 * SOURCE_DATA_TYPE short => source type of a distinct type or user-generated Ref type, SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated REF) 
 */