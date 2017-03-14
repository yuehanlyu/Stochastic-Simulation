/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bloodbank;

import java.util.ArrayList;

/**
 *
 * @author s104454
 */
public class SimResults {
    
    protected double[] sumQL ={0,0,0,0,0,0,0,0,0,0,0,0,0};
    protected double sumSPD, sumSPD2, sumSD, sumSD2, sumW, sumW2,sumST,sumST2;
    protected double sumSpreP, sumSpreP2,sumSpreW,sumSpreW2, sumStotalP, sumStotalP2,sumStotalW,sumStotalW2,sumSpre,sumSpre2,sumStotal,sumStotal2;
    protected double nSpreP, nStotalP,nSpreW,nStotalW,nSpre,nStotal;
    protected double sumSPDP,sumSPD2P,sumSPDW,sumSPD2W,sumSDP,sumSD2P,sumSDW,sumSD2W;
    protected double[] oldTime={0,0,0,0,0,0,0,0,0,0,0,0,0};
    protected int nSPD, nSD, nW, nC,nSPDP,nSPDW,nSDP,nSDW,nST2;
    protected int nTimesWait;
    protected ArrayList<Donor> donorList;
    //protected int isrecnursebusy[]=new int[2];//nurse at reception
    protected int isdocbusy[] = new int[2];//two doctors
    protected int isnursebusy[] = new int[4];//four nurses
    protected int BOccupNumP=0;//number of occupied beds for plasma
    protected int BOccupNumW=0;//number of occupied beds for whole
    protected int questionnaireN=0;
    protected int docinterN=0;
    protected int plasmaPreDonation=0;
    protected int wholePreDonation=0;
    protected int plasmaAtDroom=0;
    protected int wholeAtDroom=0;

    public SimResults() {
        //this.sumQLreg = 0;
        //this.oldTimereg = 0;
        this.sumSPD = 0;
        this.sumSPD2 = 0;
        this.sumSD = 0;
        this.sumSD2 = 0;
        this.sumW = 0;
        this.sumST=0;
        this.sumST2=0;
        this.nST2=0;
        this.sumW2 = 0;
        this.nSPD = 0;
        this.nSD = 0;
        this.nC = 0;
        this.nTimesWait = 0;
        this.donorList = new ArrayList<Donor>();
        //for(int i=0;i<isrecnursebusy.length;i++) this.isrecnursebusy[i]=0;
        for(int i=0;i<isdocbusy.length;i++) this.isdocbusy[i]=0;
        for(int i=0;i<isnursebusy.length;i++) this.isnursebusy[i]=0;
        this.BOccupNumP=0;//should be less than or equal to 7
        this.BOccupNumW=0;
        this.sumSPDP=0;
        this.sumSPD2P=0;
        this.nSPDP=0;
        this.sumSPDW=0;
        this.sumSPD2W=0;
        this.nSPDW=0;
        this.nSDP=0;
        this.nSDW=0;
        this.questionnaireN=0;
        this.docinterN=0;
        this.plasmaPreDonation=0;
        this.wholePreDonation=0;
        this.plasmaAtDroom=0;
        this.wholeAtDroom=0;
        
        this.sumSpreP=0;
        this.sumSpreP2=0;
        this.sumSpreW=0;
        this.sumSpreW2=0;
        this.sumSpre=0;
        this.sumSpre2=0;
        this.sumStotalP=0;
        this.sumStotalP2=0;
        this.sumStotalW=0;
        this.sumStotalW2=0;
        this.sumStotal=0;
        this.sumStotal2=0;
        
        this.nSpreP=0;
        this.nSpreW=0;
        this.nSpre=0;
        this.nStotalP=0;
        this.nStotalW=0;
        this.nStotal=0;
    }

    /**
     * This function should be invoked every time the queue length changes,
     * during the simulation.
     *
     * @param time the time at which the queue length changes.
     * @param currentQueueLength the current queue length <b>right before</b>
     * the change.
     */
    /**
     * This function should be invoked every time the queue length changes,
     * during the simulation.
     *
     * @param id
     * @param donortype
     * @param arrivalTime
     */
    public void registerNewDonor(int id, int donortype, double arrivalTime) {
        Donor d = new Donor(id, donortype, arrivalTime, 0, 0);
        donorList.add(d);
    }

    public int getTypeFromList(int id) {
        return donorList.get(id).donortype;
    }

    public void registerDonorNum(int section,double time, int currentQueueLength, int flag) {
        if(section==Event.REGISTRATION){//number of donors waiting for registration
            sumQL[0] += currentQueueLength * (time - oldTime[0]);
            oldTime[0] = time;
        }
       if(section==Event.QUESTIONAIRE){//number of donors filling in questionnaire
             sumQL[1] += currentQueueLength * (time - oldTime[1]);
            oldTime[1] = time;
        }
       if(section==Event.PREINTERVIEW){//number of donors in pre-interview or interview
            if(flag==0){//plasma
           sumQL[2] += currentQueueLength * (time - oldTime[2]);
            oldTime[2] = time;                 
           }else{//whole
            sumQL[3] += currentQueueLength * (time - oldTime[3]);
            oldTime[3] = time;                
           }        
       }     
       if(section==Event.PREDONATION){
           if(flag==0){//plasma
           sumQL[4] += currentQueueLength * (time - oldTime[4]);
            oldTime[4] = time;                 
           }else{//whole
            sumQL[5] += currentQueueLength * (time - oldTime[5]);
            oldTime[5] = time;                
           }     
       }
       if(section==Event.ENTERDONATIONROOM||section==Event.LEAVEDONATIONROOM){
           if(flag==0){//plasma
           sumQL[6] += currentQueueLength * (time - oldTime[6]);
            oldTime[6] = time;                 
           }else{//whole
            sumQL[7] += currentQueueLength * (time - oldTime[7]);
            oldTime[7] = time;                
           }         
       }
       if(section==Event.DOCINTERVIEW){//number of available doctors
           currentQueueLength=isdocbusy.length;
           for(int i=0;i<isdocbusy.length;i++){
               currentQueueLength-=isdocbusy[i];
           }
            sumQL[8] += currentQueueLength * (time - oldTime[8]);
            oldTime[8] = time;      
       }
       if(section==Event.NURSEFREE){
           currentQueueLength=isnursebusy.length;
           for(int i=0;i<isnursebusy.length;i++){
               currentQueueLength-=isnursebusy[i];
           }
            sumQL[9] += currentQueueLength * (time - oldTime[9]);
            oldTime[9] = time;
       }
       if(section==Event.CONNECTION){//donors waiting for connection
           if(flag==0){//plasma
           sumQL[10] += currentQueueLength * (time - oldTime[10]);
            oldTime[10] = time;                 
           }else{//whole
            sumQL[11] += currentQueueLength * (time - oldTime[11]);
            oldTime[11] = time;                
           }
       }
       if(section==Event.DISCONNECTION){//donors waiting for disconnection
            sumQL[0] += currentQueueLength * (time - oldTime[0]);
            oldTime[0] = time;
       }
    }
    
    public double getMeanDonorNum(int section,int flag) {
        if(section==Event.REGISTRATION){
            return sumQL[0] / oldTime[0];
        }
        if(section==Event.QUESTIONAIRE){
            return sumQL[1] / oldTime[1];
        }
       if(section==Event.PREINTERVIEW){//preinterview+interview
            if(flag==0){//plasma
                return sumQL[2] / oldTime[2];                   
           }else{//whole
                return sumQL[3] / oldTime[3];                 
           }        
       }
       if(section==Event.PREDONATION){//predonation room
           if(flag==0){//plasma
                return sumQL[4] / oldTime[4];                   
           }else{//whole
                return sumQL[5] / oldTime[5];                 
           }
       }
       if(section==Event.ENTERDONATIONROOM){
           if(flag==0){//plasma
                return sumQL[6] / oldTime[6];                   
           }else{//whole
                return sumQL[7] / oldTime[7];                 
           }         
       }
       if(section==Event.DOCINTERVIEW){
            return sumQL[8] / oldTime[8];
        }
       if(section==Event.NURSEFREE){
            return sumQL[9] / oldTime[9];
       }
       if(section==Event.CONNECTION){
           if(flag==0){//plasma
                return sumQL[10] / oldTime[10];                   
           }else{//whole
                return sumQL[11] / oldTime[11];                 
           } 
       }
       if(section==Event.DISCONNECTION){
          return sumQL[12] / oldTime[12]; 
       }
        else{
             return 0;
        }
    }
    

    public Donor getDonorAt(int id) {
        return donorList.get(id);
    }

    /**
     * This function should be invoked every time a customer is taken into
     * service (and his waiting time ends), during the simulation.
     *
     * @param sojournTimePreDonation
     */
    /* Sojourn Times SimResults*/
    //pre-dontaion:PLASMA
    public void registerPlasmaPreDonationSojournTime(double sojournTimePreDonation) {
        sumSpreP += sojournTimePreDonation;
        sumSpreP2 += sojournTimePreDonation * sojournTimePreDonation;
        nSpreP++;
    }
    //pre-donation:WHOLE
    public void registerWholePreDonationSojournTime(double sojournTimePreDonation) {
        sumSpreW += sojournTimePreDonation;
        sumSpreW2 += sojournTimePreDonation * sojournTimePreDonation;
        nSpreW++;
    }
    
    public void registerPreDonationSojournTime(double sojournTimePreDonation) {
        sumSpre += sojournTimePreDonation;
        sumSpre2 += sojournTimePreDonation * sojournTimePreDonation;
        nSpreP++;
    }
    //total:PLASMA
    public void registerPlasmaSojournTime(double sojournTimeDonation) {
        sumStotalP += sojournTimeDonation;
        sumStotalP2 += sojournTimeDonation * sojournTimeDonation;
        nStotalP++;
    }
    //total:WHOLE
    public void registerWholeSojournTime(double sojournTimeDonation) {
        sumStotalW += sojournTimeDonation;
        sumStotalW2 += sojournTimeDonation * sojournTimeDonation;
        nStotalW++;
    }    
    public void registerSojournTime(double sojournTimeDonation) {
        sumStotal += sojournTimeDonation;
        sumStotal2 += sojournTimeDonation * sojournTimeDonation;
        nStotal++;
    }
    //temp
    public void registerTempSojournTime(double sojournTimeDonation) {
        sumST += sojournTimeDonation;
        sumST2 += sojournTimeDonation * sojournTimeDonation;
        nST2++;
    }
    
    public double getMeanSojournTimePlasmaPreDonation() {
        return sumSpreP / nSpreP;
    }

    public double getMeanSojournTimeWholePreDonation() {
        return sumSPDW / nSPDW;
    }
    
    public double getMeanSojournTimePreDonation() {
        return sumSPD / nSPD;
    }

    public double getMeanSojournTimePlasmaDonation() {
        return sumStotalP / nStotalP;
    }

    public double getMeanSojournTimeWholeDonation() {
        return sumSDW / nSDW;
    }    
    
    public double getMeanSojournTimeDonation() {
        return sumSD / nSD;
    }

    public double getVarianceSojournTimePlasmaPreDonation() {
        return 1.0 / (nSPDP - 1) * (sumSPD2P - nSPDP * (sumSPDP / nSPDP) * (sumSPDP / nSPDP));
    }
    
    public double getVarianceSojournTimeWholePreDonation() {
        return 1.0 / (nSPDW - 1) * (sumSPD2W - nSPDW * (sumSPDW / nSPDW) * (sumSPDW / nSPDW));
    }    
    
    public double getVarianceSojournTimePreDonation() {
        return 1.0 / (nSPD - 1) * (sumSPD2 - nSPD * (sumSPD / nSPD) * (sumSPD / nSPD));
    }
    
    public double getVarianceSojournTimePlasmaDonation() {
        return 1.0 / (nSDP - 1) * (sumSD2P - nSDP * (sumSDP / nSDP) * (sumSDP / nSDP));
    }    

    public double getVarianceSojournTimeWholeDonation() {
        return 1.0 / (nSDW - 1) * (sumSD2W - nSDW * (sumSDW / nSDW) * (sumSDW / nSDW));
    }    
    
    public double getVarianceSojournTimeDonation() {
        return 1.0 / (nSD - 1) * (sumSD2 - nSD * (sumSD / nSD) * (sumSD / nSD));
    }


    
    public void registerBed(int i,int type){ 
        //i==0:leave the bed   i==1:occupy the bed
        //type= plasma or whole
        if(type==Donor.PLASMA){
            BOccupNumP+=(i-0.5)*2;//1 or -1
        }
        else{
            BOccupNumW+=(i-0.5)*2;
        }
    }
    
    public int isBedAvailable(int type){
        if(type==Donor.PLASMA){
            if(BOccupNumP<7)
                return 1;
            else
                return 0;
        }
        else{
            if(BOccupNumW<7)
                return 1;
            else
                return 0;           
        }
    }
}
