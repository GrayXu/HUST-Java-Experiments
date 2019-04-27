package sample.tool;

import db.Config;
import db.DataBase;
import db.RegisterException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TryRegisterService extends Service {
    String registerCategoryNumber;
    String doctorNumber;
    String patientNumber;
    double registerFee;
    boolean deductFromBalance;
    double addToBalance;
    int retry = 5;

    public int registerNumber;
    public RegisterException.ErrorCode returnCode;
    public double updatedBalance;

    public TryRegisterService(
            String regCatNum,
            String docNum,
            String patientNum,
            double regFee,
            boolean deduct,
            double add) {
        registerCategoryNumber = regCatNum;
        doctorNumber = docNum;
        patientNumber = patientNum;
        registerFee = regFee;
        deductFromBalance = deduct;
        addToBalance = add;
    }

    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                boolean retryFlag = false;
                do {
                    try {
                        // try register
                        registerNumber = DataBase.getInstance().tryRegister(
                                registerCategoryNumber,
                                doctorNumber,
                                patientNumber,
                                registerFee,
                                deductFromBalance,
                                addToBalance);
                    } catch (RegisterException e) {
                        // retry on fail
                        retryFlag = true;
                        switch (e.error) {
                            case sqlException:
                                returnCode = RegisterException.ErrorCode.sqlException;
                                break;
                            case registerNumberExceeded:
                            case registerCategoryNotFound:
                            case patientNotExist:
                                returnCode = e.error;
                                return null;
                        }
                    }
                } while (retryFlag && --retry > 0);

                if (retry == 0)
                    returnCode = RegisterException.ErrorCode.retryTimeExceeded;
                else {
                    returnCode = RegisterException.ErrorCode.noError;
                    try {
                        ResultSet patientInfo = DataBase.getInstance().getPatientInfo(patientNumber);
                        if (!patientInfo.next())
                            returnCode = RegisterException.ErrorCode.patientNotExist;
                        updatedBalance = patientInfo.getDouble(Config.NameTableColumnPatientBalance);
                    } catch (SQLException e) {
                        returnCode = RegisterException.ErrorCode.sqlException;
                        return null;
                    }
                }
                return null;
            }
        };
    }
}
