package com.bedtwlserver.punish.core.storage.impl;

import com.bedtwlserver.punish.core.Punish;

public class SQLiteStorage extends JdbcStorage {
    private final String filePath;

    public SQLiteStorage(String filePath) {
        this.filePath = Punish.instance.getDataFolder() + "/" +filePath;
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:sqlite:" + filePath;
    }

    @Override
    protected void applyPoolProperties(com.zaxxer.hikari.HikariConfig config) {
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        config.setConnectionTestQuery("SELECT 1");
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
                "uuid TEXT PRIMARY KEY, " +
                "player_name TEXT NOT NULL, " +
                "reason TEXT NOT NULL, " +
                "executor TEXT NOT NULL, " +
                "expireAt INTEGER NOT NULL" +
                ")";
    }

    @Override
    protected String getCreateMuteTableSql() {
        return "CREATE TABLE IF NOT EXISTS punish_mutes (" +
                "uuid TEXT PRIMARY KEY, " +
                "player_name TEXT NOT NULL, " +
                "reason TEXT NOT NULL, " +
                "executor TEXT NOT NULL, " +
                "expireAt INTEGER NOT NULL" +
                ")";
    }

    @Override
    protected String getBanUpsertSql() {
        return "INSERT OR REPLACE INTO punish_bans (uuid, player_name, reason, executor, expireAt) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getMuteUpsertSql() {
        return "INSERT OR REPLACE INTO punish_mutes (uuid, player_name, reason, executor, expireAt) VALUES (?, ?, ?, ?, ?)";
    }
}
