package sample.tool;

import db.DataBase;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.HashMap;

public class TimeManager {

    private static HashMap<String, Thread> threadHashMap = new HashMap<>();
    private static TimeManager timeManager = null;

    public static void bindLableThread(String mode, Label label){
        Thread tempThread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> label.setText(DataBase.getInstance().getTimeFromDB()));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        threadHashMap.put(mode, tempThread);
        tempThread.start();
        tempThread.suspend();
    }

    public static TimeManager getInstance() {
        if (timeManager == null){
            timeManager = new TimeManager();
        }
        return timeManager;
    }


    public void start(String mode) {
        threadHashMap.get(mode).resume();
    }

    public void stop(String mode) {
        threadHashMap.get(mode).suspend();
    }

    public static void stopAllThread(){
        System.out.println("关闭程序");
        for(String key : threadHashMap.keySet()){
            System.out.println("关闭"+key);
            threadHashMap.get(key).stop();
        }
    }

}
