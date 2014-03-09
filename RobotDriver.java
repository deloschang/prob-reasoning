package probreasoning;

// driver class
class RobotDriver {
	public static void main(String[] args){
		Maze maze = Maze.readFromFile("simple.maz");
		ReasoningProblem reasoning = new ReasoningProblem(maze, 0, 0,
				new char[]{'e','e','e','e'});
	}
	
}