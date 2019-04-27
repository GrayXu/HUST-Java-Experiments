package sample.bean;

import db.Config;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ListItemNameDepartment extends ListItem {
    public String number;
    public String name;

    @Override
    public String toString() {
        return number + " " + name + " ";
    }

    @Override
    public void fromSqlResult(ResultSet result) throws SQLException {
        number = result.getString(Config.NameTableColumnDepartmentNumber);
        name = result.getString(Config.NameTableColumnDepartmentName);
        pronounce = result.getString(Config.NameTableColumnDepartmentPronounce);
    }
}
