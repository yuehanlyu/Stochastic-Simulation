/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package statistics;
import java.util.Random;
/**
 *
 * @author mboon
 */
public class ExponentialDistribution extends Distribution {

    protected double lambda;
    
    public ExponentialDistribution(double lambda, Random random) {
        this.lambda = lambda;
        this.random = random;
    }
    
    @Override
    public double expectation() {
        return 1/lambda;
    }

    @Override
    public double variance() {
        return 1/lambda/lambda;
    }
    
    @Override
    public double standardDeviation() {
        return 1/lambda;
    }

    @Override
    public double nextRandom() {
        double u = random.nextDouble();
        return -Math.log(u)/lambda;
    }
    
}
