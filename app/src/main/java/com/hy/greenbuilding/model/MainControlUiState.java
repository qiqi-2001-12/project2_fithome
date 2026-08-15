package com.hy.greenbuilding.model;

import com.hy.greenbuilding.protocol.ResPonseInfo.MainControlInfo;

import java.util.List;

public class MainControlUiState {
    private final int indoorTemp;
    private final int indoorHumidity;
    private final int pm25;
    private final int co2;
    private final int systemSwitch;
    private final int systemMode;
    private final int systemModeSelect;
    private final int freshAirLevel;
    private final int purifyLevel;
    private final int filterRemainingRate;
    private final boolean valid;

    public MainControlUiState(int indoorTemp,
                              int indoorHumidity,
                              int pm25,
                              int co2,
                              int systemSwitch,
                              int systemMode,
                              int systemModeSelect,
                              int freshAirLevel,
                              int purifyLevel,
                              int filterRemainingRate,
                              boolean valid) {
        this.indoorTemp = indoorTemp;
        this.indoorHumidity = indoorHumidity;
        this.pm25 = pm25;
        this.co2 = co2;
        this.systemSwitch = systemSwitch;
        this.systemMode = systemMode;
        this.systemModeSelect = systemModeSelect;
        this.freshAirLevel = freshAirLevel;
        this.purifyLevel = purifyLevel;
        this.filterRemainingRate = filterRemainingRate;
        this.valid = valid;
    }

    public static MainControlUiState empty() {
        return new MainControlUiState(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false);
    }

    public static MainControlUiState fromProject2(MainControlInfo main,
                                                  List<RoomInfo> rooms,
                                                  List<FanDataInfo> fans) {
        RoomInfo room = firstRoom(rooms);
        int freshAirLevel = fanLevelAt(fans, 0);
        int purifyLevel = fanLevelAt(fans, 2);
        int mode = main == null ? 0 : main.runMode();
        int modeSelect = main == null ? 0 : main.newControlField();
        int filter = main == null ? 0 : main.getGreenType();
        return new MainControlUiState(
                room == null ? 0 : room.getTemp(),
                room == null ? 0 : room.getHumidity(),
                room == null ? 0 : room.getPm(),
                room == null ? 0 : room.getCo2(),
                1,
                mode,
                modeSelect,
                freshAirLevel,
                purifyLevel,
                filter,
                main != null || room != null || freshAirLevel > 0 || purifyLevel > 0
        );
    }

    private static RoomInfo firstRoom(List<RoomInfo> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return null;
        }
        return rooms.get(0);
    }

    private static int fanLevelAt(List<FanDataInfo> fans, int index) {
        if (fans == null || fans.size() <= index) {
            return 0;
        }
        FanDataInfo fan = fans.get(index);
        int status = fan.getInterfaceType() == 0 ? fan.getPwmFanStatus() : fan.getFanStatus();
        if (status < 0) {
            return 0;
        }
        if (status > 3) {
            return 3;
        }
        return status;
    }

    public int getIndoorTemp() { return indoorTemp; }

    public int getIndoorHumidity() { return indoorHumidity; }

    public int getPm25() { return pm25; }

    public int getCo2() { return co2; }

    public int getSystemSwitch() { return systemSwitch; }

    public int getSystemMode() { return systemMode; }

    public int getSystemModeSelect() { return systemModeSelect; }

    public int getFreshAirLevel() { return freshAirLevel; }

    public int getPurifyLevel() { return purifyLevel; }

    public int getFilterRemainingRate() { return filterRemainingRate; }

    public boolean isValid() { return valid; }
}
