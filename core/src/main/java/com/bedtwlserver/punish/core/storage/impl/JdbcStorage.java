package com.bedtwlserver.punish.core.storage.impl;

import com.bedtwlserver.punish.core.model.PunishData;
import com.bedtwlserver.punish.core.storage.Storage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public abstract class JdbcStorage extends Storage {
    protected HikariDataSource dataSource;

    @Override
    public void connect() {
        try {
            loadDriver();
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(getJdbcUrl());
            config.setMaximumPoolSize(getMaximumPoolSize());
            config.setMinimumIdle(getMinimumIdle());
            config.setPoolName(getPoolName());
            config.setConnectionTimeout(getConnectionTimeout());
            config.setLeakDetectionThreshold(getLeakDetectionThreshold());
            if (getUsername() != null) {
                config.setUsername(getUsername());
            }
            if (getPassword() != null) {
                config.setPassword(getPassword());
            }
            applyPoolProperties(config);
            dataSource = new HikariDataSource(config);
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA foreign_keys=ON");
                createTable(conn);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("無法連線資料庫", e);
        }
    }

    @Override
    public void addBan(UUID uuid, String playerName, String executor, String reason, long expireTime) {
        upsert(getBanUpsertSql(), uuid, playerName, executor, reason, expireTime);
    }

    @Override
    public void addMute(UUID uuid, String playerName, String executor, String reason, long expireTime) {
        upsert(getMuteUpsertSql(), uuid, playerName, executor, reason, expireTime);
    }

    private void upsert(String sql, UUID uuid, String playerName, String executor, String reason, long expireTime) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, playerName);
            statement.setString(3, reason);
            statement.setString(4, executor);
            statement.setLong(5, expireTime);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("新增封鎖資料失敗", e);
        }
    }

    @Override
    public boolean isBanned(UUID uuid) {
        return exists(getBanTableName(), uuid);
    }

    @Override
    public boolean isMuted(UUID uuid) {
        return exists(getMuteTableName(), uuid);
    }

    @Override
    public void removeBan(UUID uuid) {
        delete(getBanTableName(), uuid);
    }

    @Override
    public void removeMute(UUID uuid) {
        delete(getMuteTableName(), uuid);
    }

    @Override
    public PunishData getBan(UUID uuid) {
        return get(getBanTableName(), uuid);
    }

    @Override
    public PunishData getMute(UUID uuid) {
        return get(getMuteTableName(), uuid);
    }

    @Override
    public void disconnect() {
        if (dataSource == null) {
            return;
        }
        dataSource.close();
        dataSource = null;
    }

    private void createTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(getCreateBanTableSql());
            statement.executeUpdate(getCreateMuteTableSql());
        }
    }

    private boolean exists(String tableName, UUID uuid) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE uuid = ? LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("查詢資料狀態失敗", e);
        }
    }

    private void delete(String tableName, UUID uuid) {
        String sql = "DELETE FROM " + tableName + " WHERE uuid = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("移除資料失敗", e);
        }
    }

    private PunishData get(String tableName, UUID uuid) {
        String sql = "SELECT player_name, uuid, reason, executor, expireAt FROM " + tableName + " WHERE uuid = ? LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new PunishData(
                        resultSet.getString("player_name"),
                        UUID.fromString(resultSet.getString("uuid")),
                        resultSet.getString("reason"),
                        resultSet.getString("executor"),
                        resultSet.getLong("expireAt")
                );
            }
        } catch (SQLException e) {
            throw new IllegalStateException("讀取資料失敗", e);
        }
    }

    protected abstract String getJdbcUrl();

    protected String getUsername() {
        return null;
    }

    protected String getPassword() {
        return null;
    }

    protected String getPoolName() {
        return getClass().getSimpleName();
    }

    protected int getMaximumPoolSize() {
        return 10;
    }

    protected int getMinimumIdle() {
        return 2;
    }

    protected long getConnectionTimeout() {
        return 30_000L;
    }

    protected long getLeakDetectionThreshold() {
        return 0L;
    }

    protected void applyPoolProperties(HikariConfig config) {
    }

    protected abstract String getBanTableName();

    protected abstract String getMuteTableName();

    protected abstract String getCreateBanTableSql();

    protected abstract String getCreateMuteTableSql();

    protected abstract String getBanUpsertSql();

    protected abstract String getMuteUpsertSql();

    private void loadDriver() {
        String jdbcUrl = getJdbcUrl();
        try {
            if (jdbcUrl.startsWith("jdbc:sqlite:")) {
                Class.forName("org.sqlite.JDBC");
            } else if (jdbcUrl.startsWith("jdbc:mysql:")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("找不到資料庫驅動", e);
        }
    }
}
