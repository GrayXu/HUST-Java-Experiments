package sample.bean;

import db.Config;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ListItemNameRegister extends ListItem {
    public String number;
    public String name;
    public Float fee;
    public String department;
    public boolean isSpecialist;
    public int maxNumber;

    @Override
    public String toString() {
        return number + " " + name + " " + (isSpecialist ? "专家号" : "普通号") + " ¥" + fee;
    }

    @Override
    public void fromSqlResult(ResultSet result) throws SQLException {
        number = result.getString(Config.NameTableColumnCategoryRegisterNumber);
        name = result.getString(Config.NameTableColumnCategoryRegisterName);
        pronounce = result.getString(Config.NameTableColumnCategoryRegisterPronounce);
        department = result.getString(Config.NameTableColumnCategoryRegisterDepartment);
        isSpecialist = result.getBoolean(Config.NameTableColumnCategoryRegisterIsSpecialist);
        maxNumber = result.getInt(Config.NameTableColumnCategoryRegisterMaxRegisterNumber);
        fee = result.getFloat(Config.NameTableColumnCategoryRegisterFee);
    }
}
