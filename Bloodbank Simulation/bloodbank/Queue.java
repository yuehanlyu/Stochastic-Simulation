package bloodbank;

import java.util.ArrayList;


public class Queue {

  /* List of donor in the queue */
  protected ArrayList<Donor> donor;  

  
  
  /**
   * Constructs an empty single-server Queue
   */
   
  public Queue() {
    this.donor = new ArrayList<Donor>();
  }
  
  /** 
   * Returns the number of donor in the queue (including the person in service)
   * @return the number of donor
   */
   
  public int getSize() {
    return donor.size();
  }
  
  /**
   * Adds a customer to this queue
   * @param d the new customer
   */
   
  public void addDonor(Donor d) {
    donor.add(d);
  }
  
  /** 
   * Returns the first customer in the queue.
   * @return the first customer
   */
  
  public Donor getFirstDonor() {
      return donor.get(0);
  }
  
  public Donor removeFirstDonor() {
      return donor.remove(0);
  }
  
  public void removeDonor (Donor d){
      donor.remove(d);
  }
  
  public Donor getDonorAt(int i){
      return donor.get(i);
  }
  
  /**
   * Removes the first customer from the queue at time <i>t</i>
   * @param t the time that the customer is removed
   * @return the customer that is removed from the queue
   */
   

  
} 