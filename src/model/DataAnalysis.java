package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DataAnalysis {
	private Simulation sim;
	private String outputFile;
	
	public DataAnalysis(Simulation sim, String outputFile) {
		this.sim = sim;
		this.outputFile = outputFile;
	}
	
	public void analyze() throws IOException {
		
		double[] waitTimes = analyzeWaitTimes();
		double[] inducedTravel = analyzeInducedTravel();
		
		writeOut(waitTimes, inducedTravel);
	}
	
	// returns 2 double array, first is average wait time, second is standard deviation (in seconds)
	private double[] analyzeWaitTimes() {
		List<Trip> trips = sim.getTrips();
		double totalWaitTime = 0;
		double meanWaitTime = 0;
		double sumVariance = 0;
		double standardDeviation = 0;
		
		for (Trip trip : trips) {
			totalWaitTime += trip.getWaitTime();
		}
		
		meanWaitTime = totalWaitTime / trips.size();
		
		for (Trip trip : trips) {
			sumVariance += Math.pow(trip.getWaitTime() - meanWaitTime, 2);
		}
		
		standardDeviation = Math.sqrt(sumVariance / trips.size());
		
		double[] retVal = {meanWaitTime, standardDeviation};
		
		return retVal;
	}

	private double[] analyzeInducedTravel() {
		double[] inducedTravel = new double[5]; // 0-total, 1-R1, 2-R2, 3-R3, 4-R4
		double milesR1 = 0, milesR2 = 0, milesR3 = 0, milesR4 = 0, totalMiles = 0;
		List<Vehicle> vehicles = sim.getVehicleList();
		
		for (Vehicle car : vehicles) {
			totalMiles += car.getMilesDriven();
			milesR1 += car.getMilesR1();
			milesR2 += car.getMilesR2();
			milesR3 += car.getMilesR3();
			milesR4 += car.getMilesR4();
		}
		
		inducedTravel[0] = (milesR1 + milesR2 + milesR3 + milesR4) / (totalMiles) * 100;
		inducedTravel[1] = (milesR1) / (totalMiles) * 100;
		inducedTravel[2] = (milesR2) / (totalMiles) * 100;
		inducedTravel[3] = (milesR3) / (totalMiles) * 100;
		inducedTravel[4] = (milesR4) / (totalMiles) * 100;
		
		return inducedTravel;
	}

	private void writeOut(double[] waitTimes, double[] inducedTravel) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		
		out.write("Mean Wait Time: " + waitTimes[0] + "\n");
		out.write("Standard Dev : " + waitTimes[1] + "\n");
		out.write("Total Induced Travel : " + inducedTravel[0] + "\n");
		out.write("R1 Induced Travel : " + inducedTravel[1] + "\n");
		out.write("R2 Induced Travel : " + inducedTravel[2] + "\n");
		out.write("R3 Induced Travel : " + inducedTravel[3] + "\n");
		out.write("R4 Induced Travel : " + inducedTravel[4] + "\n");
		
		out.close();
	}
}
