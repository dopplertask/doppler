package com.dopplertask.doppler.domain.action.connection;

import com.dopplertask.doppler.domain.ActionResult;
import com.dopplertask.doppler.domain.StatusCode;
import com.dopplertask.doppler.domain.TaskExecution;
import com.dopplertask.doppler.domain.action.Action;
import com.dopplertask.doppler.service.TaskService;
import com.dopplertask.doppler.service.VariableExtractorUtil;
import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "MySQLAction")
@DiscriminatorValue("mysql_action")
public class MySQLAction extends Action {

    @Column
    private String hostname;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String port;

    @Column
    private String database;

    @Column
    private String timezone;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String command;

    public MySQLAction() {
    }


    @Override
    public ActionResult run(TaskService taskService, TaskExecution execution, VariableExtractorUtil variableExtractorUtil) throws IOException {
        String localHostname = variableExtractorUtil.extract(getHostname(), execution, getScriptLanguage());
        String localUsername = variableExtractorUtil.extract(getUsername(), execution, getScriptLanguage());
        String localPassword = variableExtractorUtil.extract(getPassword(), execution, getScriptLanguage());
        String localPort = variableExtractorUtil.extract(getPort(), execution, getScriptLanguage());
        String localDatabase = variableExtractorUtil.extract(getDatabase(), execution, getScriptLanguage());
        String localCommand = variableExtractorUtil.extract(getCommand(), execution, getScriptLanguage());
        String localTimezone = variableExtractorUtil.extract(getTimezone(), execution, getScriptLanguage());

        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(localUsername);
        dataSource.setPassword(localPassword);
        dataSource.setServerName(localHostname);
        dataSource.setDatabaseName(localDatabase);
        if (localPort != null && !localPort.isEmpty()) {
            try {
                dataSource.setPort(Integer.parseInt(localPort));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (!localTimezone.isEmpty()) {
            try {
                dataSource.setServerTimezone(localTimezone);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        Statement stmt = null;
        ResultSet rs = null;
        ActionResult actionResult = new ActionResult();

        try (Connection conn = dataSource.getConnection()) {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(localCommand);

            ResultSetMetaData rsmd = rs.getMetaData();
            StringBuilder builder = new StringBuilder();

            builder.append("Quering: " + localCommand + "\n");
            builder.append("Result: \n\n");
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) {
                        builder.append(",  ");
                    }
                    String columnValue = rs.getString(i);
                    builder.append(columnValue + " " + rsmd.getColumnName(i));
                }
                builder.append("\n");
            }


            actionResult.setOutput(builder.toString());
            actionResult.setStatusCode(StatusCode.SUCCESS);
            return actionResult;
        } catch (SQLException e) {
            actionResult.setErrorMsg(e.toString());
            actionResult.setStatusCode(StatusCode.FAILURE);
            return actionResult;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();

                }

            } catch (SQLException e) {

            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {

            }
        }
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}

