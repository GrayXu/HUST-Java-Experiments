package sample.bean;

import db.Config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ListItemNameDoctor extends ListItem {
    public String number;
    public String departmentNumber;
    public String name;
    public boolean isSpecialist;
    public Timestamp lastLogin;
    public String password;

    @Override
    public String toString() {
        return number + " " + name + " " + (isSpecialist ? "专家" : "普通医师");
    }

    @Override
    public void fromSqlResult(ResultSet result) throws SQLException {
        number = result.getString(Config.NameTableColumnDoctorNumber);
        departmentNumber = result.getString(Config.NameTableColumnDoctorDepartmentNumber);
        name = result.getString(Config.NameTableColumnDoctorName);
        isSpecialist = result.getBoolean(Config.NameTableColumnDoctorIsSpecialist);
        lastLogin = result.getTimestamp(Config.NameTableColumnDoctorLastLogin);
        password = result.getString(Config.NameTableColumnDoctorPassword);
        pronounce = result.getString(Config.NameTableColumnDoctorPronounce);
    }
}
