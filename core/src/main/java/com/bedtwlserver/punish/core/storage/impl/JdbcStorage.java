package com.bedtwlserver.punish.core.storage.impl;

import com.bedtwlserver.punish.api.event.ServerEvent;
import com.bedtwlserver.punish.core.event.BanServerEvent;
import com.bedtwlserver.punish.core.event.CacheUpdateServerEvent;
import com.bedtwlserver.punish.core.event.MuteServerEvent;
import com.bedtwlserver.punish.core.event.PunishStepServerEvent;
import com.bedtwlserver.punish.core.model.PunishData;
import com.bedtwlserver.punish.core.storage.Storage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                if (getJdbcUrl().startsWith("jdbc:sqlite:")) {
                    stmt.execute("PRAGMA journal_mode=WAL");
                    stmt.execute("PRAGMA foreign_keys=ON");
                }
                createTable(conn);
                migrateTable(conn);
            }
        } catch (Exception e) {
            throw new IllegalStateException("無法連線資料庫: " + e.getMessage(), e);
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
    public Map<UUID, PunishData> loadAllBans() {
        return loadAll(getBanTableName());
    }

    @Override
    public Map<UUID, PunishData> loadAllMutes() {
        return loadAll(getMuteTableName());
    }

    private Map<UUID, PunishData> loadAll(String tableName) {
        Map<UUID, PunishData> result = new java.util.HashMap<>();
        String sql = "SELECT player_name, uuid, reason, executor, expireAt FROM " + tableName;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                PunishData data = new PunishData(
                        resultSet.getString("player_name"),
                        uuid,
                        resultSet.getString("reason"),
                        resultSet.getString("executor"),
                        resultSet.getLong("expireAt")
                );
                result.put(uuid, data);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("載入快取資料失敗: " + tableName, e);
        }
        return result;
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
            statement.executeUpdate(getCreateServerEventTableSql());
        }
    }

    private void migrateTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            migrateServerEventTable(statement);
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

    @Override
    public void addServerEvent(ServerEvent event) {
        String sql = "INSERT INTO server_events (event_type, event_data, source_server, processed_by) VALUES (?, ?, ?, '')";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, event.getEventType());
            statement.setString(2, event.toJson());
            statement.setString(3, event.sourceServer());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("新增伺服器事件失敗", e);
        }
    }

    @Override
    public List<ServerEvent> getServerEvents(String serverId) {
        String sql = "SELECT id, event_type, event_data, source_server, processed_by FROM server_events " +
                "WHERE source_server != ? AND (" +
                "processed_by IS NULL OR processed_by = '' OR " +
                "(processed_by != ? AND processed_by NOT LIKE ? AND processed_by NOT LIKE ? AND processed_by NOT LIKE ?)) " +
                "ORDER BY id ASC";
        List<ServerEvent> events = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, serverId);
            statement.setString(2, serverId);
            statement.setString(3, serverId + ",%");
            statement.setString(4, "%," + serverId);
            statement.setString(5, "%," + serverId + ",%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    String eventType = resultSet.getString("event_type");
                    String eventData = resultSet.getString("event_data");

                    ServerEvent event = deserializeEvent(id, eventType, eventData);
                    if (event != null) {
                        events.add(event);
                    }
                }
            }
            return events;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("讀取伺服器事件失敗", e);
        }
    }

    @Override
    public void markServerEventProcessed(long id, String serverId) {
        String sql = "UPDATE server_events SET processed_by = CASE " +
                "WHEN processed_by IS NULL OR processed_by = '' THEN ? " +
                "WHEN processed_by = ? THEN processed_by " +
                "WHEN processed_by LIKE ? THEN processed_by " +
                "WHEN processed_by LIKE ? THEN processed_by " +
                "WHEN processed_by LIKE ? THEN processed_by " +
                "ELSE CONCAT(processed_by, ',', ?) END WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, serverId);
            statement.setString(2, serverId);
            statement.setString(3, serverId + ",%");
            statement.setString(4, "%," + serverId);
            statement.setString(5, "%," + serverId + ",%");
            statement.setString(6, serverId);
            statement.setLong(7, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("標記伺服器事件失敗", e);
        }
    }

    private ServerEvent deserializeEvent(long id, String eventType, String eventData) {
        try {
            JsonObject json = JsonParser.parseString(eventData).getAsJsonObject();
            
            if ("ban".equals(eventType)) {
                return new BanServerEvent(
                        id,
                        json.get("source_server").getAsString(),
                        UUID.fromString(json.get("player_uuid").getAsString()),
                        json.get("player_name").getAsString(),
                        json.get("executor").getAsString(),
                        json.get("reason").getAsString(),
                        json.get("expire_time").getAsLong()
                );
            } else if ("mute".equals(eventType)) {
                return new MuteServerEvent(
                        id,
                        json.get("source_server").getAsString(),
                        UUID.fromString(json.get("player_uuid").getAsString()),
                        json.get("player_name").getAsString(),
                        json.get("executor").getAsString(),
                        json.get("reason").getAsString(),
                        json.get("expire_time").getAsLong()
                );
            } else if ("punish_step".equals(eventType)) {
                return new PunishStepServerEvent(
                        id,
                        json.get("source_server").getAsString(),
                        json.get("step_name").getAsString(),
                        UUID.fromString(json.get("player_uuid").getAsString()),
                        json.get("player_name").getAsString(),
                        json.get("executor").getAsString(),
                        json.get("timestamp").getAsLong()
                );
            } else if ("cache_update".equals(eventType)) {
                return CacheUpdateServerEvent.fromJson(
                        id,
                        json.get("source_server").getAsString(),
                        UUID.fromString(json.get("player_uuid").getAsString()),
                        json
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    protected abstract String getCreateServerEventTableSql();

    protected abstract String getBanUpsertSql();

    protected abstract String getMuteUpsertSql();

    protected void migrateServerEventTable(Statement statement) {
    }

    @Override
    public void deleteServerEventBySource(String sourceServer, String eventType) {
        String sql = "DELETE FROM server_events WHERE source_server = ? AND event_type = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sourceServer);
            statement.setString(2, eventType);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("刪除伺服器事件失敗", e);
        }
    }

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
