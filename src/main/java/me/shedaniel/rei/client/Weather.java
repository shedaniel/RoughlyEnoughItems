package me.shedaniel.rei.client;

public enum Weather {
    CLEAR(0, "Clear"), RAIN(1, "Rain"), THUNDER(2, "Thunder");
    
    private final int id;
    private final String name;
    
    Weather(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public static Weather byId(int int_1) {
        return byId(int_1, CLEAR);
    }
    
    public static Weather byId(int int_1, Weather gameMode_1) {
        Weather[] var2 = values();
        int var3 = var2.length;
        
        for(int var4 = 0; var4 < var3; ++var4) {
            Weather gameMode_2 = var2[var4];
            if (gameMode_2.id == int_1)
                return gameMode_2;
        }
        return gameMode_1;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
}
