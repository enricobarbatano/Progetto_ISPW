package com.ispw.bean;

import java.util.ArrayList;
import java.util.List;

public class LogsBean {

    private List<LogEntryBean> logs = new ArrayList<>();

    public LogsBean() {
        // costruttore di default
    }

    public List<LogEntryBean> getLogs() {
        return logs;
    }

    public void setLogs(List<LogEntryBean> logs) {
        this.logs = (logs == null) ? new ArrayList<>() : logs;
    }
}
