import java.util.ArrayList;

public class Map {
	// This is the map space
	
	private ArrayList<Vehicle>[][] map; // each space has a list of what cars are in it.
	static int width = 40;	// number of east/west blocks
	static int height = 40;  // number of north/south blocks
	
	// Bottom left corner is (1,1)
	
	public Map(ArrayList<Vehicle> cars) {
		
		for (Vehicle car : cars) {
			
		}
	}
}
