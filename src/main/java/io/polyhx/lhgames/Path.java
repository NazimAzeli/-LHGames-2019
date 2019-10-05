package io.polyhx.lhgames;

import java.util.ArrayList;
import java.util.LinkedList;

import io.polyhx.lhgames.data.Point;

public class Path {
    public LinkedList<Point> moves = new LinkedList<Point>();

    public void add(Point move) {
        moves.add(move);

    }
    public void removeFirst(){
        moves.removeFirst();
    }
    public void addFirst(Point move){
        moves.addFirst(move);
    }
    public void add(Path path) {
        for(Point move : path.moves) {
            moves.add(move);
        }
    }
}
