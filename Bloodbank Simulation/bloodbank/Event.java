package bloodbank;

/**
 * @author s104454 Michiel Paus
 */

public class Event {
    
    public static final int ARRIVAL = 1;
    public static final int REGISTRATION = 2;
    public static final int QUESTIONAIRE = 3;
    public static final int PREINTERVIEW = 4;
    public static final int DOCINTERVIEW = 5;
    public static final int PREDONATION = 6;
    public static final int ENTERDONATIONROOM = 7;
    public static final int CONNECTION = 8;
    public static final int PREDISCONNECT = 9;
    public static final int DISCONNECTION = 10;
    public static final int LEAVEDONATIONROOM=11;
    public static final int HOURSTAT=12;
    
    public static final int RECNURSEFREE=13;   
    public static final int NURSEFREE=14;

   
    protected int eventtype;
    protected int ID;
    protected double time;
    protected int flag;
    protected Donor donor;


    public Event(int ID, int eventtype, double time,int flag) {
        this.ID = ID; 
        this.eventtype = eventtype;
        this.time = time;
        this.flag=flag;
    }
    
    public int getEventID() {
        return ID;
    }

    public int getEventType() {
        return eventtype;
    }
    
    public double getTime() {
        return time;
    }
    
    public int getFlag(){
        return flag;
    }

}
