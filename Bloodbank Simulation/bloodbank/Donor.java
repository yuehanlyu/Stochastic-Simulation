package bloodbank;

/**
 * This class represents a donor in the bloodbank system.
 *
 * @author s104454
 */
public class Donor {

    protected double arrivalTime, sojournTimePreDonation, sojournTimeDonation;
    protected int ID, donortype;

    public static final int PLASMA = 0;
    public static final int WHOLE = 1;
    // protected int packageSize;

    /**
     * Creates a donor that arrives at the bloodbank at the specified time.
     *
     * @param ID
     * @param donortype
     * @param arrivalTime 
     * @param sojournTimePreDonation 
     * @param sojournTimeDonation 
     */
    public Donor(int ID, int donortype, double arrivalTime, double sojournTimePreDonation, double sojournTimeDonation) {
        this.ID = ID;
        this.donortype = donortype;
        this.arrivalTime = arrivalTime;
        this.sojournTimePreDonation = sojournTimePreDonation;
        this.sojournTimeDonation = sojournTimeDonation;
    }

    /*Returns the arrival time of the donor */
    public int getIDdonor() {
        return ID;
    }

    public double getDonorType() {
        return donortype;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    /*Returns the sojourn time of donor from reception to the pre-donation room  */
    public double getSojournTimePreDonation() {
        return sojournTimePreDonation;
    }

    /*Returns the sojourn time of donor from pre-donation room to leaving blood bank*/
    public double getSojournTimeDonation() {
        return sojournTimeDonation;
    }

}
