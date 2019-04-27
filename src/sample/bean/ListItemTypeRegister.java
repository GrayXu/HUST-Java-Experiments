package sample.bean;

import java.sql.ResultSet;

public class ListItemTypeRegister extends ListItem {
    public boolean isSpecialist;

    @Override
    public String toString() {
        return isSpecialist ? "专家号" : "普通号";
    }

    @Override
    public void fromSqlResult(ResultSet result) {

    }
}
