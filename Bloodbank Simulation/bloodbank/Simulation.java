/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bloodbank;

import bloodbank.Event;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import statistics.Distribution;
import statistics.TruncatedNormalDistribution;
import statistics.ExponentialDistribution;
import statistics.DiscreteUniformDistribution;
import statistics.BernoulliDistribution;
import java.util.Random;
import java.util.Set;

public class Simulation{

    protected Distribution plasmaInterarrivalDistribution;
    protected Distribution[] wholeInterarrivalDistribution = new Distribution[24]; //12 hours
    protected Distribution[] procedureDistributions = new Distribution[10];

    public Simulation(Distribution plasmaInterarrivalDistribution, Distribution[] wholeInterarrivalDistribution, Distribution[] procedureDistributions) {
        this.plasmaInterarrivalDistribution = plasmaInterarrivalDistribution;
        this.wholeInterarrivalDistribution = wholeInterarrivalDistribution;
        this.procedureDistributions = procedureDistributions;
    }

    public static int randomOutOfArray(Set set) {
        int size = set.size();
        int item = new Random().nextInt(size); // In real life, the Random object should be rather more shared than this
        int i = 0;
        for (Object obj : set) {
            if (i == item) {
                return (int) obj;
            }
            i = i + 1;
        }
        return 0;
    }

    public static void constructDistribution(Distribution[] wholeInter, Distribution[] procedures, Random rng) {
        procedures[0] = new TruncatedNormalDistribution(2, 0.5, rng);//Registration 
        procedures[1] = new ExponentialDistribution(1.0 / 1.5, rng);//QuestionNaire
        procedures[2] = new TruncatedNormalDistribution(6.0, 1.0, rng);//Interview 
        procedures[3] = new ExponentialDistribution(1.0 / 3.0, rng);//Nurse:Connect to device
        procedures[4] = new TruncatedNormalDistribution(8.5, 0.6, rng);//Whole blood donation
        procedures[5] = new TruncatedNormalDistribution(45.0, 6.0, rng);//Plasma donation
        procedures[6] = new ExponentialDistribution(1.0 / 2.0, rng);//Nurse:Disconnect from device
        procedures[7] = new ExponentialDistribution(1.0 / 4.0, rng);//Recover

        double[] parameters = {5.76, 5.94, 7.20, 7.56, 8.28, 7.56, 5.94, 5.40, 5.22, 5.76, 6.66, 7.56, 7.74, 6.84, 6.12, 6.30, 6.84, 6.66, 10.44, 8.64, 9.18, 12.24, 12.6, 7.56};
        for (int i = 0; i < 24; i++) {
            wholeInter[i] = new ExponentialDistribution(parameters[i]/30, rng);
        }
    }

    public void simulate(Sheet sheet,int runs) {
        FES fes = new FES();
        //Donor donor=new donor();
        SimResults results = new SimResults();
        //Queue queues[];
        //queues = new Queue[8];
        //Exception occurs if I use above code, I don't know why....       
        Queue queue0=new Queue();Queue queue1=new Queue();Queue queue2=new Queue();
        Queue queue3=new Queue();Queue queue4=new Queue();Queue queue5=new Queue();
        Queue queue6=new Queue();Queue queue7=new Queue();
//0.reception 1.preinterview-plasma 2.preinterview-whole 
//3.predonation-plasma 4.predonation-whole
//5. Disconnect (not distinguishing donation type)	 
//6. Plasma connecting  7. Whole connecting

        Donor cDonor;
        int cID = 0;//current id
        int cType = 0;//current type
        int listID = 0;//ID in the list
        double OldhourBedOccPl = 0;
        double OldhourBedOccWh = 0;
        double OldhourQueue0 = 0;
        double OldhourQueueDocPl = 0;
        double OldhourQueueDocWh = 0;
        double OldsojournPreDonPl = 0;
        double OldsojournPreDonWh = 0;
        double OldsojournTotDonPl = 0;
        double OldsojournTotDonWh = 0;
        double OldhourQueuePreDonPl = 0;
        double OldhourQueuePreDonWh = 0;
        double OldhourQuestionnaireN=0;
        double OldavailableNurse=0;
        double OldPwaitforconnect=0;
        double OldWwaitforconnect=0;
        double Oldwaitfordisconnect=0;
        
        //double number is the arbitrary number 35, (this was initially the plan as I constructed half hour statistics)
        double endtime=0;
        double[] hourBedOccPl = new double[35];        // make array for hourBedOccPl
        double[] hourBedOccWh = new double[35];        // make array for hourBedOccWh               
        double[] hourQueue0 = new double[35];        // make array for queue 0
        double[] hourQueueDocPl = new double[35];        // make array for queue docpl
        double[] hourQueueDocWh = new double[35];        // make array for queue docwh
        double[] hourQueuePreDonPl = new double[35];  
        double[] hourQueuePreDonWh = new double[35];  
        
        double[] sojournPreDonPl = new double[35]; 
        double[] sojournPreDonWh = new double[35]; 
        double[] sojournTotDonPl = new double[35]; 
        double[] sojournTotDonWh = new double[35]; 
        double[] questionnaireN= new double[35];
        double[] availableNurse=new double[35];
        double[] Pwaitforconnect=new double[35];
        double[] Wwaitforconnect=new double[35];
        double[] waitfordisconnect=new double[35];
        
        
        double t = 0; 
        Event firstPlasma = new Event(0, Event.ARRIVAL, t, 0);
        fes.addEvent(firstPlasma);
        
        Event firsthour = new Event(0, Event.HOURSTAT, t+60, 0);
        fes.addEvent(firsthour);
        
        results.registerNewDonor(listID++, Donor.PLASMA, t);//ID=0
        double it=wholeInterarrivalDistribution[0].nextRandom();
        Event firstWhole = new Event(1, Event.ARRIVAL, t + it, 0);
        fes.addEvent(firstWhole);
        results.registerNewDonor(listID++, Donor.WHOLE, it);// ID=1
        while (1==1) {
            Event e = fes.nextEvent();
            if(e==null) break;
            
            
            t = e.getTime();// tNurse=eNurse.getTime();
            cID = e.getEventID(); 
            cType = results.getTypeFromList(cID);
            
            if(fes.getSizeFES()==1){
               endtime = t;
            }
            
            if (e.getEventType() == e.HOURSTAT) {
                int hst = 0;
                hst= (int) Math.floor(t/60); 
                if(hst<=14){              
                results.registerDonorNum(Event.REGISTRATION, t,queue0.getSize(),0);
                results.registerDonorNum(Event.QUESTIONAIRE, t, results.questionnaireN,0);
                results.registerDonorNum(Event.PREINTERVIEW, t, queue1.getSize(),0);
                results.registerDonorNum(Event.PREINTERVIEW, t, queue2.getSize(),1);
                results.registerDonorNum(Event.DOCINTERVIEW, t,0,0);
                results.registerDonorNum(Event.PREDONATION,t,queue3.getSize(), 0);
                results.registerDonorNum(Event.PREDONATION,t,queue4.getSize(), 1);
                results.registerDonorNum(Event.ENTERDONATIONROOM,t, results.plasmaAtDroom,0);
                results.registerDonorNum(Event.ENTERDONATIONROOM,t, results.wholeAtDroom,1);
                results.registerDonorNum(Event.NURSEFREE,t, 0,0);
                results.registerDonorNum(Event.DISCONNECTION, t, queue5.getSize(), 0);
                results.registerDonorNum(Event.CONNECTION, t, queue6.getSize(), 0);
                results.registerDonorNum(Event.CONNECTION, t, queue7.getSize(), 1);
                   
                            
                hourBedOccPl[hst] = (results.getMeanDonorNum(Event.ENTERDONATIONROOM,0)*hst - OldhourBedOccPl*(hst-1));
                hourBedOccWh[hst] = (results.getMeanDonorNum(Event.ENTERDONATIONROOM,1)*hst - OldhourBedOccWh*(hst-1));
                hourQueue0[hst] = (results.getMeanDonorNum(Event.REGISTRATION,0)*hst - OldhourQueue0*(hst-1));
                hourQueueDocPl[hst] = (results.getMeanDonorNum(Event.PREINTERVIEW,0)*hst - OldhourQueueDocPl*(hst-1));
                hourQueueDocWh[hst] = (results.getMeanDonorNum(Event.PREINTERVIEW,1)*hst - OldhourQueueDocWh*(hst-1));
                hourQueuePreDonPl[hst] = (results.getMeanDonorNum(Event.PREDONATION,0)*hst - OldhourQueuePreDonPl*(hst-1));
                hourQueuePreDonWh[hst] = (results.getMeanDonorNum(Event.PREDONATION,1)*hst - OldhourQueuePreDonWh*(hst-1));
                questionnaireN[hst] = (results.getMeanDonorNum(Event.QUESTIONAIRE, 0)*hst-OldhourQuestionnaireN*(hst-1));
                availableNurse[hst]=(results.getMeanDonorNum(Event.NURSEFREE, 0)*hst-OldavailableNurse*(hst-1));
                Pwaitforconnect[hst]=(results.getMeanDonorNum(Event.CONNECTION, 0)*hst-OldPwaitforconnect*(hst-1));
                Wwaitforconnect[hst]=(results.getMeanDonorNum(Event.CONNECTION, 1)*hst-OldWwaitforconnect*(hst-1));
                waitfordisconnect[hst]=(results.getMeanDonorNum(Event.DISCONNECTION, 0)*hst-Oldwaitfordisconnect*(hst-1));
                
                sojournPreDonPl[hst] = (results.getMeanSojournTimePlasmaPreDonation()*hst - OldsojournPreDonPl*(hst-1));
                sojournTotDonPl[hst] = (results.getMeanSojournTimePlasmaDonation()*hst - OldsojournTotDonPl*(hst-1));
                sojournPreDonWh[hst] = (results.getMeanSojournTimeWholePreDonation()*hst - OldsojournPreDonWh*(hst-1));
                sojournTotDonWh[hst] = (results.getMeanSojournTimeWholeDonation()*hst - OldsojournTotDonWh*(hst-1));
                
                OldhourBedOccPl = results.getMeanDonorNum(Event.ENTERDONATIONROOM,0);
                OldhourBedOccWh = results.getMeanDonorNum(Event.ENTERDONATIONROOM,1);
                OldhourQueue0 = results.getMeanDonorNum(Event.REGISTRATION,0);
                OldhourQueueDocPl= results.getMeanDonorNum(Event.PREINTERVIEW,0);
                OldhourQueueDocWh = results.getMeanDonorNum(Event.PREINTERVIEW,1);
                OldhourQueuePreDonPl = results.getMeanDonorNum(Event.PREDONATION,0);
                OldhourQueuePreDonWh = results.getMeanDonorNum(Event.PREDONATION,1);
                OldsojournPreDonPl = results.getMeanSojournTimePlasmaPreDonation();
                OldsojournTotDonPl = results.getMeanSojournTimePlasmaDonation();
                OldsojournPreDonWh = results.getMeanSojournTimeWholePreDonation();
                OldsojournTotDonWh = results.getMeanSojournTimeWholeDonation();
                OldhourQuestionnaireN=results.getMeanDonorNum(Event.QUESTIONAIRE, 0);
                OldavailableNurse=results.getMeanDonorNum(Event.NURSEFREE, 0);
                OldPwaitforconnect=results.getMeanDonorNum(Event.CONNECTION, 0);
                OldWwaitforconnect=results.getMeanDonorNum(Event.CONNECTION, 1);
                Oldwaitfordisconnect=results.getMeanDonorNum(Event.DISCONNECTION, 0);
                }
                if(hst<=16){
                Event hourstat = new Event(0, Event.HOURSTAT, t + 60, 0);
                fes.addEvent(hourstat);
                }
                if(hst==15){
                continue;
                }
                else    continue;
            }
            
            if (e.getEventType() == e.ARRIVAL) {
                if(t>=12*60) continue;//original setting:t>=12*60   
                if (cType == Donor.PLASMA) {
                    if(t+6<655){//only 110 plasma donors
                        results.registerNewDonor(listID, Donor.PLASMA, t + 6);
                        Event nextPlasma = new Event(listID++, Event.ARRIVAL, t + 6, 0);
                        fes.addEvent(nextPlasma);
                    }
                    Random rnd = new Random();
                    double s = rnd.nextDouble();
                    if (s < 0.15) {
                        continue;//skip current iteration
                    }
                    cDonor = results.getDonorAt(cID);
                    queue0.addDonor(cDonor);
                    if (queue0.getSize() <= 1) {
                        queue0.removeFirstDonor();
                        Event registration = new Event(cID, Event.REGISTRATION, t, 0);
                        fes.addEvent(registration);
                    }
                } else {//cType==WHOLE
                    cDonor = results.getDonorAt(cID);
                    queue0.addDonor(cDonor);
                    if (queue0.getSize() <= 1) {
                        Event registration = new Event(cID, Event.REGISTRATION, t, 0);
                        fes.addEvent(registration);
                    }
                    int ti = 0;//time interval
                    ti = (int)(Math.floor(t/ 30));
                    double tt = wholeInterarrivalDistribution[ti].nextRandom();
                    if(t+tt>((ti+1)*30)){
                        ti++; 
                        t=(Math.floor(t/30)+1)*30;
                        if(ti==24){
                            tt=1;//add 1 more minute to judge the end point
                        }
                        else{
                            tt = wholeInterarrivalDistribution[ti].nextRandom();
                        }
                    }                        
                    results.registerNewDonor(listID, Donor.WHOLE, t + tt);
                    Event nextWhole = new Event(listID++,Event.ARRIVAL,t + tt, 0);
                    fes.addEvent(nextWhole);
                }
            }
            if (e.getEventType() == Event.REGISTRATION) {
                results.registerDonorNum(e.getEventType(), t,1+queue0.getSize(),0);
                Event questionnaire = new Event(cID, Event.QUESTIONAIRE, t + procedureDistributions[0].nextRandom(), 0); // start questionnaire after reg.service complete
                fes.addEvent(questionnaire);
            }
            if (e.getEventType() == Event.QUESTIONAIRE) {//departure from register
                results.questionnaireN++;
                results.registerDonorNum(e.getEventType(), t, results.questionnaireN,0);
                if (queue0.getSize() > 0) {
                    Event registration = new Event(queue0.getFirstDonor().getIDdonor(), Event.REGISTRATION, t, 0);
                    fes.addEvent(registration);
                    queue0.removeFirstDonor();
                }else{//no one waits for registration
                    results.registerDonorNum(Event.REGISTRATION, t,0,0);
                }
                Event preinterview = new Event(cID, Event.PREINTERVIEW, t + procedureDistributions[1].nextRandom(), 0);
                fes.addEvent(preinterview);
            }
            if (e.getEventType() == Event.PREINTERVIEW) {
                results.questionnaireN--;
                results.registerDonorNum(Event.QUESTIONAIRE, t, results.questionnaireN,0); //questionnaire-1donor
               //results.docinterN++;
                if (cType == Donor.PLASMA) {
                    //results.registerDonorNum(Event.PREINTERVIEW, t, queue1.getSize(),0);//preinterview
                    queue1.addDonor(results.getDonorAt(cID));                   
                    if (queue1.getSize() <= 1) {
                        for (int i = 0; i < results.isdocbusy.length; i++) {
                            if (results.isdocbusy[i] == 0) {
                                Event docinterview = new Event(cID, Event.DOCINTERVIEW, t, i);
                                fes.addEvent(docinterview);
                                queue1.removeFirstDonor();
                                //results.registerDonorNum(Event.PREINTERVIEW, t, queue1.getSize(),0);
                                break;
                            }
                        }
                    }
                    results.registerDonorNum(Event.PREINTERVIEW, t, queue1.getSize(),0);
                } else {//whole donor
                    //results.registerDonorNum(Event.PREINTERVIEW, t, queue2.getSize(),1);
                    queue2.addDonor(results.getDonorAt(cID));                
                    if (queue2.getSize() <= 1 && queue1.getSize() == 0) {
                        for (int i = 0; i < results.isdocbusy.length; i++) {
                            if (results.isdocbusy[i] == 0) {
                                Event docinterview = new Event(cID, Event.DOCINTERVIEW, t, i);
                                fes.addEvent(docinterview);
                                queue2.removeFirstDonor();
                                //results.registerDonorNum(Event.PREINTERVIEW, t, queue2.getSize(),1);
                                break;
                            }
                        }
                    }
                    results.registerDonorNum(Event.PREINTERVIEW, t, queue2.getSize(),1); //newplace of registration
                }
            }
            if (e.getEventType() == Event.DOCINTERVIEW) {
                results.registerDonorNum(Event.DOCINTERVIEW, t,0,0);
                results.isdocbusy[e.getFlag()] = 1;
                double itt = procedureDistributions[2].nextRandom();//interview time
                Event predonation = new Event(cID, Event.PREDONATION, t + itt, e.getFlag());//i:doctor i
                fes.addEvent(predonation);
                           
            }
            if (e.getEventType() == Event.PREDONATION) {
                //results.registerTempSojournTime(t-);
                results.registerDonorNum(Event.DOCINTERVIEW, t,0,0);
                results.isdocbusy[e.flag] = 0;
                if (queue1.getSize() > 0) {//plasma donor's waiting for interview
                    Event docinterview = new Event(queue1.getFirstDonor().getIDdonor(), Event.DOCINTERVIEW, t, 0);
                    fes.addEvent(docinterview);
                    queue1.removeFirstDonor();
                    results.registerDonorNum(Event.PREINTERVIEW, t, queue1.getSize(),0);
                } else if (queue2.getSize() > 0) {//check whole donor's waiting
                    Event docinterview = new Event(queue2.getFirstDonor().getIDdonor(), Event.DOCINTERVIEW, t, 0);
                    fes.addEvent(docinterview);
                    queue2.removeFirstDonor();
                    results.registerDonorNum(Event.PREINTERVIEW, t, queue2.getSize(),1);
                }
                Random rnd = new Random();
                double s = rnd.nextDouble();
                if (s < 0.05)//ineligible
                {
                    continue;//skip current iteration
                }
                if (cType == Donor.PLASMA) {
                    queue3.addDonor(results.getDonorAt(cID));//add plasma donor to pre-donation queue
                    results.registerDonorNum(Event.PREDONATION,t,queue3.getSize(), 0);//register 
                    if (queue3.getSize() <= 1) {//also needs to check any bed and nurse available or not
                        if(results.isBedAvailable(Donor.PLASMA)==1){
                            queue3.removeFirstDonor();
                            Event donation = new Event(cID, Event.ENTERDONATIONROOM, t, 0);
                            fes.addEvent(donation);
                        }
                    }
                }
                else{
                    queue4.addDonor(results.getDonorAt(cID));//add whole donor to pre-donation queue
                    results.registerDonorNum(Event.PREDONATION,t,queue4.getSize(), 1);//register 
                    if (queue4.getSize() <= 1) {//also needs to check any bed is available or not
                        if(results.isBedAvailable(Donor.WHOLE)==1){
                            queue4.removeFirstDonor();
                            Event donation = new Event(cID, Event.ENTERDONATIONROOM, t, 0);
                            fes.addEvent(donation);
                        }
                    }
                }           
        }
            if(e.getEventType() == Event.ENTERDONATIONROOM){
                if(cType==Donor.PLASMA){
                    results.registerPlasmaPreDonationSojournTime(t-results.getDonorAt(cID).arrivalTime);
                    results.registerDonorNum(Event.PREDONATION,t,queue3.getSize(), 0);
                    results.plasmaAtDroom++;
                    results.registerDonorNum(e.getEventType(),t, results.plasmaAtDroom,0);
                    results.registerDonorNum(Event.CONNECTION, t, queue6.getSize(), 0);
                }
                else{
                    results.registerWholePreDonationSojournTime(t-results.getDonorAt(cID).arrivalTime);
                    results.registerDonorNum(Event.PREDONATION,t,queue4.getSize(), 1);
                    results.wholeAtDroom++;
                    results.registerDonorNum(e.getEventType(),t, results.wholeAtDroom,1);
                    results.registerDonorNum(Event.CONNECTION, t, queue7.getSize(), 1);
                }
                results.registerPreDonationSojournTime(t-results.getDonorAt(cID).arrivalTime);
                //check any nurse is available
                results.registerBed(1, cType);
                int iflag=0;
                int i=0;
                do{
                    if(results.isnursebusy[i]==0){
                        //results.isnursebusy[i]=1;
                        Event connection = new Event(cID, Event.CONNECTION, t, i);//i:nurse num i
                        fes.addEvent(connection);
                        iflag=1;
                        break;
                    }
                    i++;
                }while(i<results.isnursebusy.length);
                //if no nurse available, enter queue
                if(iflag==0){
                    if(cType==Donor.PLASMA){
                        queue6.addDonor(results.getDonorAt(cID));
                    }
                    else{
                        queue7.addDonor(results.getDonorAt(cID));
                    }
                }
            }
            if(e.getEventType()==Event.CONNECTION){
                results.registerDonorNum(Event.NURSEFREE,t,0,0);
                results.isnursebusy[e.getFlag()]=1;//nursebusy0->1
                double nt;
                nt=procedureDistributions[3].nextRandom();//nurse connection time
                //we can simply check three queues when this nurse finishes connection here,
                //but to simplify the code, we create event.NURSEFREE
                //Similarly, we can create event.DOCFREE, but we will only use it once, ...so ,..
                Event nurseFree= new Event(0,Event.NURSEFREE,t+nt,e.getFlag());
                fes.addEvent(nurseFree);
                if(cType==Donor.PLASMA){
                    results.registerDonorNum(Event.CONNECTION, t, queue6.getSize(), 0);
                    Event predisconnect=new Event(cID,Event.DISCONNECTION,t+nt+procedureDistributions[5].nextRandom(),0);                    
                    fes.addEvent(predisconnect);
                }
                else{
                    results.registerDonorNum(Event.CONNECTION, t, queue7.getSize(), 1);
                    Event predisconnect=new Event(cID,Event.DISCONNECTION,t+nt+procedureDistributions[4].nextRandom(),0);                                        
                    fes.addEvent(predisconnect);
                }               
            }
            if(e.getEventType()==Event.PREDISCONNECT){
                results.registerDonorNum(Event.DISCONNECTION, t, queue5.getSize(), 0);
                int iflag=0;
                for(int i=0;i<=results.isnursebusy.length;i++){//check if any nurse is available
                    if(results.isnursebusy[i]==0){
                        //results.isnursebusy[i]=1;
                        Event disconnection = new Event(cID, Event.DISCONNECTION, t, i);//i:nurse num i
                        fes.addEvent(disconnection);
                        iflag=1;
                        break;
                    }
                }
                if(iflag==0){//no nurse is available at this time
                     queue5.addDonor(results.getDonorAt(cID));
                }
            }
            if(e.getEventType()==Event.DISCONNECTION){
                results.registerDonorNum(Event.DISCONNECTION, t, queue5.getSize(), 0);
                results.registerDonorNum(Event.NURSEFREE,t,0,0);
                results.isnursebusy[e.getFlag()]=1;//nursebusy0->1
                double nt;
                nt=procedureDistributions[6].nextRandom();//nurse disconnection time
                Event nurseFree= new Event(0,Event.NURSEFREE,t+nt,e.getFlag());
                fes.addEvent(nurseFree);
                //schedule recovery and leave
                Event leavedonationroom=new Event(cID,Event.LEAVEDONATIONROOM,t+nt+procedureDistributions[7].nextRandom(),0);
                fes.addEvent(leavedonationroom);
            }
            if(e.getEventType()==Event.NURSEFREE){
                results.registerDonorNum(Event.DISCONNECTION, t, queue5.getSize(), 0);
                results.registerDonorNum(Event.CONNECTION, t, queue6.getSize(), 0);
                results.registerDonorNum(Event.CONNECTION, t, queue7.getSize(), 1);
                //flag1->0
                //check three queues.
                results.registerDonorNum(Event.NURSEFREE,t,0,0);
                results.isnursebusy[e.getFlag()]=0;
                if(queue5.getSize()>0){//disconnection
                    cDonor=queue5.getFirstDonor();
                    Event disconnection = new Event(cDonor.getIDdonor(), Event.DISCONNECTION, t, e.getFlag());//i:nurse num i
                    fes.addEvent(disconnection);
                    queue5.removeFirstDonor();
                }else
                    if(queue6.getSize()>0){//connect plasma
                        cDonor=queue6.getFirstDonor();
                        Event connection = new Event(cDonor.getIDdonor(), Event.CONNECTION, t, e.getFlag());//i:nurse num i
                        fes.addEvent(connection);
                        queue6.removeFirstDonor();
                    }else
                        if(queue7.getSize()>0){//connect whole
                            cDonor=queue7.getFirstDonor();
                            Event connection = new Event(cDonor.getIDdonor(), Event.CONNECTION, t, e.getFlag());//i:nurse num i
                            fes.addEvent(connection);
                            queue7.removeFirstDonor();
                        }          
            }
            if(e.getEventType()==Event.LEAVEDONATIONROOM){
                if(cType==Donor.PLASMA){
                    results.registerPlasmaSojournTime(t-results.getDonorAt(cID).arrivalTime);
                    results.plasmaAtDroom--;
                    results.registerDonorNum(e.getEventType(),t, results.plasmaAtDroom,0);
                }
                else{
                    results.registerWholeSojournTime(t-results.getDonorAt(cID).arrivalTime);
                    results.wholeAtDroom--;
                    results.registerDonorNum(e.getEventType(),t, results.wholeAtDroom,1);
                }
                results.registerSojournTime(t-results.getDonorAt(cID).arrivalTime);
                results.registerBed(0, cType);//1 bed becomes available
                
                if(cType==Donor.PLASMA&&queue3.getSize()>0){ //queue3.getSize()>0  //cType==Donor.PLASMA&&queue3.getSize()>0
                    Event donation = new Event(cID, Event.ENTERDONATIONROOM, t, 0);
                    fes.addEvent(donation);
                    queue3.removeFirstDonor();
                    results.registerDonorNum(Event.PREDONATION,t,queue3.getSize(), 0);
                }

                if(cType==Donor.WHOLE&&queue4.getSize()>0){//queue4.getSize()>0  //cType==Donor.WHOLE&&queue4.getSize()>0
                    Event donation = new Event(cID, Event.ENTERDONATIONROOM, t, 0);
                    fes.addEvent(donation);
                    queue4.removeFirstDonor();
                    results.registerDonorNum(Event.PREDONATION,t,queue4.getSize(), 1);
                }
            }
            
        }
        
            Row row = sheet.createRow((short)runs);
        Cell cell=row.createCell(0);cell.setCellValue(endtime);
        cell=row.createCell(1);cell.setCellValue(results.getMeanSojournTimePlasmaPreDonation());
        cell=row.createCell(2);cell.setCellValue(results.getMeanSojournTimeWholePreDonation());
        cell=row.createCell(3);cell.setCellValue(results.getMeanSojournTimePlasmaDonation());
        cell=row.createCell(4);cell.setCellValue(results.getMeanSojournTimeWholeDonation());
        cell=row.createCell(5);cell.setCellValue(results.getMeanDonorNum(Event.REGISTRATION,0));//
        cell=row.createCell(6);cell.setCellValue(results.getMeanDonorNum(Event.QUESTIONAIRE,0));
        cell=row.createCell(7);cell.setCellValue(results.getMeanDonorNum(Event.PREINTERVIEW,0));//preinterview plasma
        cell=row.createCell(8);cell.setCellValue(results.getMeanDonorNum(Event.PREINTERVIEW,1));//preinterview whole
        cell=row.createCell(9);cell.setCellValue(results.getMeanDonorNum(Event.DOCINTERVIEW, 0));//number of available doctors
        cell=row.createCell(10);cell.setCellValue(results.getMeanDonorNum(Event.PREDONATION,0));//
        cell=row.createCell(11);cell.setCellValue(results.getMeanDonorNum(Event.PREDONATION,1));//
        cell=row.createCell(12);cell.setCellValue(results.getMeanDonorNum(Event.ENTERDONATIONROOM,0));//donor room
        cell=row.createCell(13);cell.setCellValue(results.getMeanDonorNum(Event.ENTERDONATIONROOM,1));//donor room
        
        for(int i = 0; i < 16; i++) {
        /*cell=row.createCell(13+i);cell.setCellValue(Math.round(hourBedOccPl[i]* 10000.0) / 10000.0);//donor room Plasma
        cell=row.createCell(13+1*16+i);cell.setCellValue(Math.round(hourBedOccWh[i]* 10000.0) / 10000.0);//donor room Whole
        cell=row.createCell(13+2*16+i);cell.setCellValue(Math.round(hourQueue0[i]* 10000.0) / 10000.0);//Queue lenght reception
        cell=row.createCell(13+3*16+i);cell.setCellValue(Math.round(hourQueueDocPl[i]* 10000.0) / 10000.0);//Queue length doctor plasma
        cell=row.createCell(13+4*16+i);cell.setCellValue(Math.round(hourQueueDocWh[i]* 10000.0) / 10000.0);//Queue length doctor whole
        cell=row.createCell(13+5*16+i);cell.setCellValue(Math.round(sojournPreDonPl[i]* 10000.0) / 10000.0);
        cell=row.createCell(13+6*16+i);cell.setCellValue(Math.round(sojournTotDonPl[i]* 10000.0) / 10000.0);
        cell=row.createCell(13+7*16+i);cell.setCellValue(Math.round(sojournPreDonWh[i]* 10000.0) / 10000.0);
        cell=row.createCell(13+8*16+i);cell.setCellValue(Math.round(sojournTotDonWh[i]* 10000.0) / 10000.0);
        cell=row.createCell(13+9*16+i);cell.setCellValue(Math.round(hourQueuePreDonPl[i]* 10000.0) / 10000.0);
        cell=row.createCell(13+10*16+i);cell.setCellValue(Math.round(hourQueuePreDonWh[i]* 10000.0) / 10000.0);
        cell=row.createCell(13+11*16+i);cell.setCellValue(Math.round(questionnaireN[i]* 10000.0) / 10000.0);*/
        cell=row.createCell(13+1*16+i);cell.setCellValue(Math.round(availableNurse[i]* 10000.0) / 10000.0);
        cell=row.createCell(13+2*16+i);cell.setCellValue(Math.round(Pwaitforconnect[i]* 10000.0) / 10000.0);
        cell=row.createCell(13+3*16+i);cell.setCellValue(Math.round(Wwaitforconnect[i]* 10000.0) / 10000.0);
        cell=row.createCell(13+4*16+i);cell.setCellValue(Math.round(waitfordisconnect[i]* 10000.0) / 10000.0);
        cell=row.createCell(13+5*16);cell.setCellValue(Math.round(results.donorList.size()) *10000.0/ 10000.0);
        }
      
        //other measures can be added
    }
            
            
 
        

        
    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        //from 8am to 20pm 
        Random rng = new Random();     
   
        Distribution plasmaInter =new DiscreteUniformDistribution(6,6,rng); 
        Distribution[] wholeInter=new Distribution[24];
        Distribution[] procedures=new Distribution[10];
        constructDistribution(wholeInter,procedures,rng);

        Simulation sim = new Simulation(plasmaInter,wholeInter,procedures);       
    // Create the sheet
        Workbook wb = new HSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();
        Sheet sheet = wb.createSheet("new sheet");
        
        int runs=1;
                Row row = sheet.createRow((short)0);
        Cell cell=row.createCell(0);cell.setCellValue(createHelper.createRichTextString("Total running time"));
        cell=row.createCell(1);cell.setCellValue(createHelper.createRichTextString("P pre-donation sojourn time"));
        cell=row.createCell(2);cell.setCellValue(createHelper.createRichTextString("W pre-donation sojourn time"));
        cell=row.createCell(3);cell.setCellValue(createHelper.createRichTextString("P total sojourn time"));
        cell=row.createCell(4);cell.setCellValue(createHelper.createRichTextString("W total sojourn time"));
        cell=row.createCell(5);cell.setCellValue(createHelper.createRichTextString("Qmean # P&W at registration"));
        cell=row.createCell(6);cell.setCellValue(createHelper.createRichTextString("Qmean # P&W at questionnaire"));
        cell=row.createCell(7);cell.setCellValue(createHelper.createRichTextString("Qmean # P at (pre-)interview"));
        cell=row.createCell(8);cell.setCellValue(createHelper.createRichTextString("Qmean # W at (pre-)interview"));
        cell=row.createCell(9);cell.setCellValue(createHelper.createRichTextString("# of available doctors"));
        
        cell=row.createCell(10);cell.setCellValue(createHelper.createRichTextString("Qmean # P at pre-donation room"));
        cell=row.createCell(11);cell.setCellValue(createHelper.createRichTextString("Qmean # W at pre-donation room"));
        cell=row.createCell(12);cell.setCellValue(createHelper.createRichTextString("Qmean # P at donation room"));
        cell=row.createCell(13);cell.setCellValue(createHelper.createRichTextString("Qmean # W at donation room"));
        
        for(int i = 0; i < 16; i++) {
        /*cell=row.createCell(13+i);cell.setCellValue(createHelper.createRichTextString("BedOcc.Pl " + "hr " + (8 + i)));//donor room Plasma
        cell=row.createCell(13+1*16+i);cell.setCellValue(createHelper.createRichTextString("BedOcc.Wh " + "hr" + (8 + i)));//donor room Whole
        cell=row.createCell(13+2*16+i);cell.setCellValue(createHelper.createRichTextString("Queue0" + "hr" + (8 + i)));//Queue lenght reception
        cell=row.createCell(13+3*16+i);cell.setCellValue(createHelper.createRichTextString("QueueDocPl" + "hr" + (8 + i)));//Queue length doctor plasma
        cell=row.createCell(13+4*16+i);cell.setCellValue(createHelper.createRichTextString("QueueDocWh" + "hr" + (8 + i)));//Queue length doctor whole
        cell=row.createCell(13+5*16+i);cell.setCellValue(createHelper.createRichTextString("SJT Pl PreDon" + "hr" + (8 + i)));
        cell=row.createCell(13+6*16+i);cell.setCellValue(createHelper.createRichTextString("SJT Pl TotDon" + "hr" + (8 + i)));
        cell=row.createCell(13+7*16+i);cell.setCellValue(createHelper.createRichTextString("SJT Wh PreDon" + "hr" + (8 + i)));
        cell=row.createCell(13+8*16+i);cell.setCellValue(createHelper.createRichTextString("SJT Wh TotDon" + "hr" + (8 + i)));
        cell=row.createCell(13+9*16+i);cell.setCellValue(createHelper.createRichTextString("QueuePreDonPl" + "hr" + (8 + i)));
        cell=row.createCell(13+10*16+i);cell.setCellValue(createHelper.createRichTextString("QueuePreDonWh" + "hr" + (8 + i)));   
        cell=row.createCell(13+11*16+i);cell.setCellValue(createHelper.createRichTextString("QuestionNaire" + "hr" + (8 + i))); */
        cell=row.createCell(13+1*16+i);cell.setCellValue(createHelper.createRichTextString("AvailableNurse" + "hr" + (8 + i))); 
        cell=row.createCell(13+2*16+i);cell.setCellValue(createHelper.createRichTextString("P Wait for connect" + "hr" + (8 + i)));
        cell=row.createCell(13+3*16+i);cell.setCellValue(createHelper.createRichTextString("W Wait for connect" + "hr" + (8 + i)));
        cell=row.createCell(13+4*16+i);cell.setCellValue(createHelper.createRichTextString("Wait for disconnect" + "hr" + (8 + i)));
        }

        
        //other measures can be added, see all measures in line 364-379, as well as variance  
        while(runs<=10000){//runs=10000 costs 9 seconds
            sim.simulate(sheet,runs);
            runs++;
        }
        FileOutputStream fileOut = new FileOutputStream("correct.xls");//name of the excel file
        wb.write(fileOut);
        fileOut.close(); 
    }
    
}
