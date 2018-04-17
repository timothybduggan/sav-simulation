package controller_view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import model.Simulation;

public class Console_Client {
	public static void main(String[] args) throws IOException {
		Scanner keyboard = new Scanner(System.in);
		Simulation sim = new Simulation();
		System.out.print("Output file name: ");
		String fileName = keyboard.next();
		keyboard.close();
		for (int i = 0; i < 100; i++) {
			System.out.print("Simulating Day : " + i);
			for (int j = 0; j < 288; j++) {
				sim.updateSimulation();
				if (j % 12 == 0) System.out.print(".");
			}
			System.out.println();
		}
		
		// data ready to be analyzed.
		sim.analyzeData(fileName);
	}
}
