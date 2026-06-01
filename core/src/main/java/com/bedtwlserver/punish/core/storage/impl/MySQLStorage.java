package com.bedtwlserver.punish.core.storage.impl;

public class MySQLStorage extends JdbcStorage {
    private final String address;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public MySQLStorage(String address, int port, String database, String username, String password) {
        this.address = address;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:mysql://" + address + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Taipei";
    }

    @Override
    protected String getUsername() {
        return username;
    }

    @Override
    protected String getPassword() {
        return password;
    }

    @Override
    protected String getPoolName() {
        return "Punish-MySQL";
    }

    @Override
    protected int getMaximumPoolSize() {
        return 10;
    }

    @Override
    protected int getMinimumIdle() {
        return 2;
    }

    @Override
    protected void applyPoolProperties(com.zaxxer.hikari.HikariConfig config) {
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
    }

    @Override
    protected String getBanTableName() {
        return "punish_bans";
    }

    @Override
    protected String getMuteTableName() {
        return "punish_mutes";
    }

    @Override
    protected String getCreateBanTableSql() {
        return "CREATE TABLE IF NOT EXISTS punish_bans (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "reason TEXT NOT NULL, " +
                "executor VARCHAR(16) NOT NULL, " +
                "expireAt BIGINT NOT NULL" +
                ")";
    }

    @Override
    protected String getCreateMuteTableSql() {
        return "CREATE TABLE IF NOT EXISTS punish_mutes (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "reason TEXT NOT NULL, " +
                "executor VARCHAR(16) NOT NULL, " +
                "expireAt BIGINT NOT NULL" +
                ")";
    }

    @Override
    protected String getBanUpsertSql() {
        return "INSERT INTO punish_bans (uuid, player_name, reason, executor, expireAt) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE reason = VALUES(reason), executor = VALUES(executor), expireAt = VALUES(expireAt)";
    }

    @Override
    protected String getMuteUpsertSql() {
        return "INSERT INTO punish_mutes (uuid, player_name, reason, executor, expireAt) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE reason = VALUES(reason), executor = VALUES(executor), expireAt = VALUES(expireAt)";
    }
}
