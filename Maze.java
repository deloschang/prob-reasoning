package probreasoning;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Maze {
	final static Charset ENCODING = StandardCharsets.UTF_8;

	// A few useful constants to describe actions
	public static int[] NORTH = {0, 1};
	public static int[] EAST = {1, 0};
	public static int[] SOUTH = {0, -1};
	public static int[] WEST = {-1, 0};
	public static int[][] valid = new int[][]{NORTH,EAST,SOUTH,WEST};
	
	public int width;
	public int height;
	
	private char[][] grid;
	public int obstacles;

	public static Maze readFromFile(String filename) {
		Maze m = new Maze();

		try {
			List<String> lines = readFile(filename);
			m.height = lines.size();

			int y = 0;
			int obstacles = 0;
			m.grid = new char[m.height][];
			for (String line : lines) {
				m.width = line.length();
				m.grid[m.height - y - 1] = new char[m.width];
				for (int x = 0; x < line.length(); x++) {
					// (0, 0) should be bottom left, so flip y as 
					//  we read from file into array:
					m.grid[m.height - y - 1][x] = line.charAt(x);
					
					// locate the obstacles
					if (m.grid[m.height - 1 -y][x] == '#'){
						obstacles++;
					}
				}
				y++;

				// System.out.println(line.length());
			}

			m.obstacles = obstacles;
			return m;
		} catch (IOException E) {
			return null;
		}
	}

	private static List<String> readFile(String fileName) throws IOException {
		Path path = Paths.get(fileName);
		return Files.readAllLines(path, ENCODING);
	}
	
	// returns the color for the given coordinate
	public char returnColor(int x_coord, int y_coord){
		if (this.isLegal(x_coord, y_coord)){
			char actual_color = this.getChar(x_coord, y_coord);
			System.out.println("true color is " + actual_color);
			
			return actual_color;
		} 
		// illegal coordinate
		return '#';
	}
	
	// returns the color for the given coordinate
	public char silentColor(int x_coord, int y_coord){
		if (this.isLegal(x_coord, y_coord)){
			char actual_color = this.getChar(x_coord, y_coord);
			
			return actual_color;
		} 
		// illegal coordinate
		return '#';
	}

	public char getChar(int x, int y) {
		return grid[y][x];
	}
	
	// return new locations 
	public int[] moveRobot(int x_coord, int y_coord, 
			int[] direction){
		int poss_x = x_coord + direction[0];
		int poss_y = y_coord + direction[1];
		
		if (isLegal(poss_x, poss_y) ){
			return new int[]{poss_x, poss_y};
		}
		return new int[]{x_coord,y_coord};
	}

	
	// find the coordinates
	public int[] findDirection(char instruction){
		switch( Character.toLowerCase(instruction) ){
			case 'n':
				return NORTH;
			case 'w':
				return WEST;
			case 'e':
				return EAST;
			case 's':
				return SOUTH;
		}
		// error checking
		System.out.println("Not a valid direction!");
		System.exit(1);
		return null;
	}

	
	// is the location x, y on the map, and also a legal floor tile (not a wall)?
	public boolean isLegal(int x, int y) {
		// on the map
		if(x >= 0 && x < width && y >= 0 && y < height) {
			// and it's a floor tile, not a wall tile:
			// modified so we only check to make sure it's not a wall
			return getChar(x, y) != '#';
		}
		return false;
	}
	
	
	public String toString() {
		String s = "";
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				s += grid[y][x];
			}
			s += "\n";
		}
		return s;
	}

	public static void main(String args[]) {
		Maze m = Maze.readFromFile("simple.maz");
		System.out.println(m);
	}

}
