package com.onlinestore.jdoulke.onlinestorefx.entities;

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

    public String getOperationType() {
        return operationType;
    }

    public String getTableName() {
        return tableName;
    }

    public Timestamp getOperationTime() {
        return operationTime;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public String getOldData() {
        return oldData;
    }

    public String getNewData() {
        return newData;
    }

}
