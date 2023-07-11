/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sql.parser.binder.metadata.column;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.binder.metadata.DataTypeLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Column meta data loader.
 *  modify by yukai fan  @2023-06-07 优化meta加载慢的问题
 * @author Yukai Fan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnMetaDataLoader {

    private static final String ORDER_BY_ORDINAL_POSITION = " ORDER BY ORDINAL_POSITION";

    private static final String TABLE_META_DATA_NO_ORDER = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA, COLLATION_NAME, ORDINAL_POSITION FROM information_schema.columns WHERE TABLE_SCHEMA = ?";

    private static final String TABLE_META_DATA_SQL = TABLE_META_DATA_NO_ORDER + ORDER_BY_ORDINAL_POSITION;

    private static final String TABLE_META_DATA_SQL_IN_TABLES = TABLE_META_DATA_NO_ORDER + " AND TABLE_NAME IN (%s)" + ORDER_BY_ORDINAL_POSITION;

    private static final String INDEX_META_DATA_SQL = "SELECT TABLE_NAME, INDEX_NAME FROM information_schema.statistics WHERE TABLE_SCHEMA=? and TABLE_NAME IN (%s)";


    public static Map<String, TableMetaData> load(final Connection connection, final Collection<String> tables) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        Map<String, Collection<ColumnMetaData>> columnMetaDataMap = loadColumnMetaDataMap(connection, tables);
        Map<String, Collection<IndexMetaData>> indexMetaDataMap = columnMetaDataMap.isEmpty() ? Collections.emptyMap() : loadIndexMetaData(connection, columnMetaDataMap.keySet());
        for (Map.Entry<String, Collection<ColumnMetaData>> entry : columnMetaDataMap.entrySet()) {
            result.put(entry.getKey(), new TableMetaData(entry.getValue(), indexMetaDataMap.getOrDefault(entry.getKey(), Collections.emptyList())));
        }
        return result;
    }

    private static Map<String, Collection<ColumnMetaData>> loadColumnMetaDataMap(final Connection connection, final Collection<String> tables) throws SQLException {
        Map<String, Collection<ColumnMetaData>> result = new HashMap<>();
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(getTableMetaDataSQL(tables))) {
            Map<String, Integer> dataTypes = DataTypeLoader.load(connection.getMetaData());
            appendDataTypes(dataTypes);
            preparedStatement.setString(1, connection.getCatalog());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    ColumnMetaData columnMetaData = loadColumnMetaData(dataTypes, resultSet);
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    result.get(tableName).add(columnMetaData);
                }
            }
        }
        return result;
    }

    private static void appendDataTypes(final Map<String, Integer> dataTypes) {
        dataTypes.putIfAbsent("JSON", Types.LONGVARCHAR);
        dataTypes.putIfAbsent("GEOMETRY", Types.BINARY);
    }

    private static ColumnMetaData loadColumnMetaData(final Map<String, Integer> dataTypeMap, final ResultSet resultSet) throws SQLException {
        String columnName = resultSet.getString("COLUMN_NAME");
        String dataType = resultSet.getString("DATA_TYPE");
        boolean primaryKey = "PRI".equals(resultSet.getString("COLUMN_KEY"));
//        boolean generated = "auto_increment".equals(resultSet.getString("EXTRA"));
        String collationName = resultSet.getString("COLLATION_NAME");
        boolean caseSensitive = null != collationName && !collationName.endsWith("_ci");
        return new ColumnMetaData(columnName, dataTypeMap.get(dataType),"", primaryKey, false, caseSensitive);
    }

    private static String getTableMetaDataSQL(final Collection<String> tables) {
        return tables.isEmpty() ? TABLE_META_DATA_SQL : String.format(TABLE_META_DATA_SQL_IN_TABLES, tables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }

    private static Map<String, Collection<IndexMetaData>> loadIndexMetaData(final Connection connection, final Collection<String> tableNames) throws SQLException {
        Map<String, Collection<IndexMetaData>> result = new HashMap<>();
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(getIndexMetaDataSQL(tableNames))) {
            preparedStatement.setString(1, connection.getCatalog());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String indexName = resultSet.getString("INDEX_NAME");
                    String tableName = resultSet.getString("TABLE_NAME");
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    result.get(tableName).add(new IndexMetaData(indexName));
                }
            }
        }
        return result;
    }

    private static String getIndexMetaDataSQL(final Collection<String> tableNames) {
        return String.format(INDEX_META_DATA_SQL, tableNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
}
