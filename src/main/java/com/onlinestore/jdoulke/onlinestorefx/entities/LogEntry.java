package com.onlinestore.jdoulke.onlinestorefx.controllers;

import java.sql.Timestamp;

public class LogEntry {
    private int logId;
    private String operationType;
    private String tableName;
    private Timestamp operationTime;
    private String performedBy;
    private String oldData;
    private String newData;

    public LogEntry(int logId, String operationType, String tableName, Timestamp operationTime, String performedBy, String oldData, String newData) {
        this.logId = logId;
        this.operationType = operationType;
        this.tableName = tableName;
        this.operationTime = operationTime;
        this.performedBy = performedBy;
        this.oldData = oldData;
        this.newData = newData;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Timestamp getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Timestamp operationTime) {
        this.operationTime = operationTime;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getOldData() {
        return oldData;
    }

    public void setOldData(String oldData) {
        this.oldData = oldData;
    }

    public String getNewData() {
        return newData;
    }

    public void setNewData(String newData) {
        this.newData = newData;
    }
}
