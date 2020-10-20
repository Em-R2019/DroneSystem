package nl.tue.dronesystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grid {
    // There are 6 kinds of nodes in the grid:
    // 1 normal node
    // 0 obstacle
    // 2 source 
    // 3 destination = whole height
    // 4 drone, dependent on time
    // 5 no-fly zone
    public Value[][][] nodes;

    public Grid(Value[][][] nodes) {
        this.nodes = nodes;
    }

    public List<Node> shortestPath(double time, double t) {
        // key node, value parent
        Map<Node, Node> parents = new HashMap<>();
        Node start = null;
        Node end = null;

        // find the start node
        for (int row = 0; row < nodes.length; row++) {
            for (int column = 0; column < nodes[row].length; column++) {
                for (int height = 0; height < nodes[row][column].length; height++){
                    if (nodes[row][column][height].getValue() == 2) {
                        start = new Node(row, column, height, nodes[row][column][height]);
                    break;
                    }

                    if (start != null) {
                        break;

                    }
                }
            }
        }

        if (start == null) {
          throw new RuntimeException("can't find start node");
        }

        // traverse every node using breadth first search until reaching the destination
        List<Node> temp = new ArrayList<>();
        temp.add(start);
        parents.put(start, null);
        List<Node> visitedChildren = new ArrayList<>();

        boolean reachDestination = false;
        while (temp.size() > 0 && !reachDestination) {
            time = time+t;
            Node currentNode = temp.remove(0);
            List<Node> children = getChildren(currentNode);
            for (Node child : children) {
                // Node can only be visted once
                if (!parents.containsKey(child)) {
                    parents.put(child, currentNode);
                    int value = child.getValue();

                    if (value == 1) { 
                    temp.add(child);
                    } 
                    else if (value == 3) {
                        temp.add(child);
                        reachDestination = true;
                        end = child;
                        break;
                        }
                    else if (value == 4){ // check if timeslot is open
                    List<double[]> timeslots = child.getTimeslots();
                    boolean open = true;

                    for (int i = 0;i<timeslots.size();i++){
                        double[] timeslot = timeslots.get(i);

                        if (time>timeslot[0]&&time<timeslot[1]){
                            open = false;
                        }
                    }

                        if (open){temp.add(child);}
                    }
                }
            }
        }

    List<Node> path = new ArrayList<>(0);
    
    if (end != null) {
        Node node = end;

        while (node != null) {
            path.add(0, node);
            node = parents.get(node);
        }
    }

    return path;
  }
  
  
  // find shortest path out of no-fly zone
    public List<Node> shortestPathOut(double time, double t) {

        // key node, value parent
        Map<Node, Node> parents = new HashMap<>();
        Node start = null;
        Node end = null;

        // find the start node
        for (int row = 0; row < nodes.length; row++) {
            for (int column = 0; column < nodes[row].length; column++) {
                for (int height = 0; height < nodes[row][column].length; height++){
                    if (nodes[row][column][height].getValue() == 2) {
                        start = new Node(row, column, height, nodes[row][column][height]);
                        break;
                    }

                    if (start != null) {
                        break;
                    }
                }
            }
        }

        if (start == null) {
          throw new RuntimeException("can't find start node");
        }

        // traverse every node using breadth first search until reaching the destination
        List<Node> temp = new ArrayList<>();
        temp.add(start);
        parents.put(start, null);

        boolean reachDestination = false;
        while (temp.size() > 0 && !reachDestination) {
          time = time+t;
          Node currentNode = temp.remove(0);
          List<Node> children = getChildren(currentNode);
          for (Node child : children) {
            // Node can only be visted once
            if (!parents.containsKey(child)) {
                parents.put(child, currentNode);
                int value = child.getValue();
                
                if (value == 5) { 
                    temp.add(child);
                } else if (value == 1) {
                    temp.add(child);
                    reachDestination = true;
                    end = child;
                    break;
                }
                else if (value == 4){ // check if timeslot is open
                    List<double[]> timeslots = child.getTimeslots();
                    boolean open = true;
                    for (int i = 0;i<timeslots.size();i++){
                        double[] timeslot = timeslots.get(i);
                        if (time>timeslot[0]&&time<timeslot[1]){
                            open = false;
                        }
                    }
                    if (open){temp.add(child);}
                }
            }
        }
    }

    if (end == null) {
      throw new RuntimeException("can't find end node");
    }

    // get the shortest path
    Node node = end;
    List<Node> path = new ArrayList<>();
    while (node != null) {
      path.add(0, node);
      node = parents.get(node);
    }

    return path;
  }
  
  public void updateGrid(int x, int y, int z, Value value){
      nodes[x][y][z] = value;
  }
  
  public void updateGrid(int x, int y, Value value){
      for (int i = 0; i<nodes[0][0].length;i++){
          nodes[x][y][i] = value;
      }
  }

  private List<Node> getChildren(Node parent) {
    List<Node> children = new ArrayList<>();
    int x = parent.getX();
    int y = parent.getY();
    int z = parent.getZ();

    if (x - 1 >= 0) {
      Node child = new Node(x - 1, y, z,  nodes[x - 1][y][z]);
      children.add(child);
    }
    
    if (y - 1 >= 0) {
      Node child = new Node(x, y - 1, z, nodes[x][y - 1][z]);
      children.add(child);
    }

    if (z - 1 >= 0) {
      Node child = new Node(x, y, z - 1, nodes[x][y][z - 1]);
      children.add(child);
    }
    
    if (x + 1 < nodes.length) {
      Node child = new Node(x + 1, y, z, nodes[x + 1][y][z]);
      children.add(child);
    }
    
    if (y + 1 < nodes[0].length) {
      Node child = new Node(x, y + 1, z, nodes[x][y + 1][z]);
      children.add(child);
    }
    
    if (z + 1 < nodes[0][0].length) {
      Node child = new Node(x, y, z + 1, nodes[x][y][z + 1]);
      children.add(child);
    }
    
    return children;
  }
}

class Node {
  private int x;
  private int y;
  private int z;
  private Value value;

  public Node(int x, int y, int z, Value value) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.value = value;
  }
  
 public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
  
  public int getZ() {
      return z;
  }

  public int getValue() {
    return value.getValue();
  }
  
  public List getTimeslots(){
        return value.getTimeslots();
    }

  @Override
  public String toString() {
    return "(x: " + x + " y: " + y + " z: " + z + ")";
  }

  @Override
  public int hashCode() {
    return x * y * z;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (this.getClass() != o.getClass()) return false;
    Node node = (Node) o;
    return x == node.x && y == node.y && z == node.z;
  }
}
class Value{
    public Value(int value){
        this.value = value;
    }
    
    public Value(int value, double[] timeslot){
        this.value = value;
        this.timeslots = new ArrayList<>(0);
        this.timeslots.add(timeslot);
    }
    
    int value;
    List<double[]> timeslots;
    
    public int getValue(){
        return value;
    }
    
    public void setValue(int value){
        this.value = value;
    }
    
    public void addTime(double[] timeslot){
        timeslots.add(timeslot);
    }
    
    public List getTimeslots(){
        return timeslots;
    }
}