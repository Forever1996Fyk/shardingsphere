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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.EncryptRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.statement.EncryptPreparedStatement;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.statement.EncryptStatement;
import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * Encrypt connection.
 */
@RequiredArgsConstructor
@Getter
public final class EncryptConnection extends AbstractUnsupportedOperationConnection {
    
    private final Connection connection;
    
    private final EncryptRuntimeContext runtimeContext;
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return connection.getMetaData();
    }
    
    @Override
    public Statement createStatement() throws SQLException {
        return new EncryptStatement(this);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return new EncryptStatement(this, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return new EncryptStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return new EncryptPreparedStatement(this, sql);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return new EncryptPreparedStatement(this, sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return new EncryptPreparedStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        return new EncryptPreparedStatement(this, sql, autoGeneratedKeys);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        return new EncryptPreparedStatement(this, sql, columnIndexes);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        return new EncryptPreparedStatement(this, sql, columnNames);
    }

    @Override
    public boolean isValid(final int timeout) throws SQLException {
        return connection.isValid(timeout);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return connection.getAutoCommit();
    }
    
    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        connection.setAutoCommit(autoCommit);
    }
    
    @Override
    public void commit() throws SQLException {
        connection.commit();
    }
    
    @Override
    public void rollback() throws SQLException {
        connection.rollback();
    }
    
    @Override
    public void close() throws SQLException {
        connection.close();
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return connection.isClosed();
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        return connection.isReadOnly();
    }
    
    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        connection.setReadOnly(readOnly);
    }
    
    @Override
    public int getTransactionIsolation() throws SQLException {
        return connection.getTransactionIsolation();
    }
    
    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        connection.setTransactionIsolation(level);
    }
    
    @Override
    public SQLWarning getWarnings() {
        return null;
    }
    
    @Override
    public void clearWarnings() {
    }
    
    @Override
    public int getHoldability() {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }
    
    @Override
    public void setHoldability(final int holdability) {
    }
}
