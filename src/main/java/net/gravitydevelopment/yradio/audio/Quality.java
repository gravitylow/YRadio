package net.gravitydevelopment.yradio.audio;

public enum Quality {
    
    p480(18),
    p720(22),
    p1080(37);
    
    private int value;

    private Quality(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return this.name().substring(1)+"p";
    }
    
    public static Quality fromName(String name) {
        for(Quality q : values()) {
            if(q.toString().equals(name)) {
                return q;
            }
        }
        return null;
    }
    
    public static Quality fromValue(int value) {
        for(Quality q : values()) {
            if(q.getValue() == value) {
                return q;
            }
        }
        return null;
    }

    public Quality getNextLowestQuality() {
        if(this == p1080) return p720;
        else if (this == p720) return p480;
        else return null;
    }
    
}
