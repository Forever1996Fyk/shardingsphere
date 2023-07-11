package org.apache.shardingsphere.sql.parser.binder.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataTypeLoader {
    /**
     * Load data type.
     *
     * @param databaseMetaData database meta data
     * @return data type map
     * @throws SQLException SQL exception
     */
    public static Map<String, Integer> load(final DatabaseMetaData databaseMetaData) throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        try (ResultSet resultSet = databaseMetaData.getTypeInfo()) {
            while (resultSet.next()) {
                result.put(resultSet.getString("TYPE_NAME"), resultSet.getInt("DATA_TYPE"));
            }
        }
        return result;
    }
}
