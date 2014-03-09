package probreasoning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ReasoningProblem {
	
	private Integer x_coord;
	private Integer y_coord ;
	private char[] robot_path;
	private Maze maze;
	
	public ReasoningProblem(Maze maze_file, Integer start_x, Integer start_y,
			char[] sequence){
		robot_path = sequence; 
		x_coord = start_x;
		y_coord = start_y;
		maze = maze_file;
		
		if (maze != null){
			start(robot_path);
		} else {
			System.out.println("Maze file not found!");
			System.exit(1);
		}
	}
	
	// main method that starts the robot with the given sequence
	public void start(char[] sequence){
		Robot robot = new Robot(this.maze);
		System.out.println("t = " + robot.move_counter);
		
		// get the initial color from the initial (x,y)
		char poss_color = robot.sense(maze.returnColor(x_coord, y_coord));
		System.out.println("first color is " + poss_color);
		
		// ASCII of solution
		System.out.println(robot.ascii(robot.generate(poss_color)));
		System.out.println(" === ");
		
		// execute each instruction in the sequence
		for (char instruction : sequence){
			System.out.println("Trying " + instruction);
			int[] direction = maze.findDirection(instruction);
			int[] locations = maze.moveRobot(x_coord, y_coord, direction);
			
			System.out.println("locations " + Arrays.toString(locations) );
			x_coord = locations[0];
			y_coord = locations[1];
			
			robot.move_counter++;
			System.out.println("t = " + robot.move_counter);
			
			
			// remap probability distribution
			poss_color = robot.sense(maze.returnColor(x_coord, y_coord));
			System.out.println("color received is " + poss_color);
			
			robot.generate(poss_color);
			System.out.println(" === ");
		}
	}
	
	
	// the starting probability 
	public Double[][] startingProb(){
		// initialize starting probability 2D array
		// one for each cell
		Double[][] startingProb = new Double[maze.width][maze.height];
		double freeSpaces = (maze.width * maze.height) - maze.obstacles;
		
		for (int i = 0; i < maze.width; i++){
			for (int j = 0; j < maze.height; j++){
				if (maze.getChar(i, j) == '#'){
					startingProb[i][j] = (double) 0;
				} else {
					// if no obstacles, then 1/16 for a 4x4 maze
					// for each probability 
					startingProb[i][j] = 1.0 / freeSpaces;
				}
			}
		}
		
		return startingProb;
	}
	
	
	private class Robot { 
		private Double[][] curr_state;
		private Maze maze;
		private Integer move_counter;

		// possible colors
		final Character[] all_colors = {'r','g','b','y'};

		public Robot(Maze m){
			move_counter = 0;
			maze = m;
		}
		
		// main probability calculation function
		public Double[][] generate(char poss_color){
			if (move_counter == 0){
				// first move -- generate basic probability
				// with obstacles heuristic
				curr_state = startingProb(); // save as first state
				return curr_state;
			} else {
				// P(R_1)
				Double[][] transitioned = calculateTransition(curr_state);
				
				System.out.println("transitioning --> ");
				System.out.println(ascii(transitioned));
				
				// update step multiplies by probability of evidence
				// for t=1
				// P(u1 | R1) * P(R1)
				// not normalized
				Double[][] prediction = updatePrediction(transitioned, poss_color);
				System.out.println("factoring in sensor -->");
				System.out.println(ascii(prediction));
				
				// normalize using alpha constant
				// make them sum up to be 1
				curr_state = normalize(prediction);
				
				System.out.println("normalizing -->");
				System.out.println(ascii(curr_state));
				return curr_state;
			}

		}
		
		public String ascii(Double[][] result){
			String string = "";
			for(int y = maze.height - 1; y > -1; y--){
				for(int x = 0; x < maze.width; x++){
					string += Math.round(result[x][y] * 1000) / 10  + "% ";
				}
				
				string += "\n";
			}
			
			return string;
		}
		
		// update prediction using sensor input 
		public Double[][] updatePrediction(Double[][] transitioned,
				char poss_color){
			Double[][] updated = new Double[maze.width][maze.height];
			for(int x = 0; x < maze.width; x++){
				for(int y = 0; y < maze.height; y++){
					// probability of being in (i,j);
					
					double chance;
					// P(u1 | R1)
					if (poss_color == maze.silentColor(x, y)){
						chance = 0.88;
					} else {
						chance = 0.04;
					}
					
					double result =  chance * transitioned[x][y];
					updated[x][y] = result;
				}
			}
			return updated;
		}
		
		public Double[][] normalize(Double[][] prediction){
			double normalizer = 0;
			for(int i = 0; i < prediction.length; i++){
				for(int j = 0; j < prediction[0].length; j++){
					normalizer += prediction[i][j];
				}
			}
			
			Double[][] updated = new Double[maze.width][maze.height];
			for(int i = 0; i < updated.length; i++){
				for(int j = 0; j < updated[0].length; j++){
					updated[i][j] = prediction[i][j] / normalizer;
				}
			}
			
			return updated;
		}
		
		// update transitions
		public Double[][] calculateTransition(Double[][] curr_state){
			Double[][] updated = new Double[maze.width][maze.height];
			for(int i = 0; i < updated.length; i++){
				for(int j = 0; j < updated[0].length; j++){
					// probability of being in (i,j);
					double sum = transitionUtil(i, j);
					updated[i][j] = sum;
				}
			}
			return updated;
			
		}
		
		// returns probability of being in (x,y) 
		// by finding the legal moves around it
		public double transitionUtil(int i, int j){
			// check all valid adjacent squares
			int legal = 0;
			double sum = 0;
			
			for (int[] move : maze.valid ){
				int new_x = i + move[0];
				int new_y = j + move[1];

				if (maze.isLegal(new_x, new_y) ){
					legal++;
				}
			}

			sum += (0.25 * legal * curr_state[i][j]);

			// loop through each legal space
			for (int[] move : maze.valid ){
				int new_x = i + move[0];
				int new_y = j + move[1];

				if (maze.isLegal(new_x, new_y) ){
					// probability of moving to (x,y) 
					// (25% since it is adjacent)
					// given probability of being on (new_x, new_y)
					sum += (0.25 * curr_state[new_x][new_y]);
				}
			}
			return sum;
			
		}
		
		// sense with 88% probability the correct color
		public char sense(char actual_color){

			// true color only 88% of the time
			if (new Random().nextDouble() <= 0.88){
				return actual_color;
			} else {
				char[] temp = new char[all_colors.length-1];

				// load all the colors NOT actual_color into an arr
				int i = 0;
				for(char color: all_colors){
					if (color != actual_color) {
						temp[i] = color;
						i++;
					}
				}
				
				// pick a random color in that arr
				return temp[new Random().nextInt(all_colors.length-1)];
			}	
		}	
		
	}
	
}
	
