package statistics;
import java.util.*;


public class TruncatedNormalDistribution extends Distribution{
    
    protected double mu;
    protected double sigma;
    
    public TruncatedNormalDistribution(double mu, double sigma, Random random) {
        this.mu= mu;
        this.sigma = sigma;
        this.random = random;       
    }

    @Override
    public double expectation() {
        return mu;
    }

    @Override
    public double variance() {
        return sigma*sigma;
    }

    @Override
    public double nextRandom() {
        double U = random.nextGaussian();
        if(mu + sigma*U<0) return 0;//truncated
        return mu + sigma*U; 
    }
    
    
    
    
}