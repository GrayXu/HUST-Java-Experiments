package sample.bean;

import db.Config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public abstract class ListItem {
    public String pronounce;

    @Override
    public abstract String toString();

    public abstract void fromSqlResult(ResultSet result) throws SQLException;

    public String getPronounce() {
        return pronounce;
    }
}

