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

package org.apache.shardingsphere.sql.parser.binder.metadata.table;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaDataLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaDataLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Table meta data loader.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataLoader {
    
    /**
     * modify by yukai fan @2023-06-07
     *
     * Load table meta data.
     *
     * @param dataSource data source
     * @param table table name
     * @param databaseType database type
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public static TableMetaData load(final DataSource dataSource, final String table, final String databaseType) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, TableMetaData> metaDataMap = ColumnMetaDataLoader.load(connection, Lists.newArrayList(table));
            if (Objects.isNull(metaDataMap.get(table))) {
                try {
                    log.warn("load metadata from database:{}, table:{} failed, table not exist.", connection.getCatalog(), table);
                } catch (Exception e) {
                    log.warn("load metadata table:{} failed, table not exist.", table);
                }
            }
            return Objects.isNull(metaDataMap.get(table)) ? new TableMetaData(Collections.emptyList(), Collections.emptyList()) : metaDataMap.get(table);
//            return new TableMetaData(ColumnMetaDataLoader.load(connection, table, databaseType), IndexMetaDataLoader.load(connection, table, databaseType));
        }
    }
}
