package statistics;
import java.util.*;

public class UniformDistribution extends Distribution{
    
    protected double a; // lower bound
    protected double b; // upper bound 
    
    public UniformDistribution(double a, double b, Random random){
        this.a = a;
        this.b = b;
        this.random = random;
    }

    //public UniformDistribution(double delta, double delta0, Random rng) {
      // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    //}

    @Override
    public double expectation() {
        return (a+b)/2;
    }

    @Override
    public double variance() {
        return (b-a)*(b-a)/12;
    }

    @Override
    public double nextRandom() {
        double U = random.nextDouble();
        return a + U*(b-a);
    }
}