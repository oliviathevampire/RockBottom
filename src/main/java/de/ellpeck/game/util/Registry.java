package de.ellpeck.game.util;

import de.ellpeck.game.Game;
import de.ellpeck.game.Main;
import org.newdawn.slick.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Registry<T>{

    private final String name;
    private final Map<Integer, T> map = new HashMap<>();

    public Registry(String name){
        this.name = name;
    }

    public void register(int id, T value){
        if(this.map.containsKey(id)){
            throw new RuntimeException("Cannot register "+value+" with id "+id+" twice!");
        }

        this.map.put(id, value);

        Log.info("Registered "+value+" with id "+id+" into registry "+this);
    }

    public T get(int id){
        return this.map.get(id);
    }

    public int getId(T value){
        for(Entry<Integer, T> entry : this.map.entrySet()){
            if(value.equals(entry.getValue())){
                return entry.getKey();
            }
        }
        return -1;
    }

    public int getSize(){
        return this.map.size();
    }

    public Map<Integer, T> getUnmodifiable(){
        return Collections.unmodifiableMap(this.map);
    }

    @Override
    public String toString(){
        return this.name;
    }
}
