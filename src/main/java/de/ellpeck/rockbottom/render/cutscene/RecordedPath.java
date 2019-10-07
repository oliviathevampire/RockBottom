package de.ellpeck.rockbottom.render.cutscene;

import de.ellpeck.rockbottom.api.data.set.DataSet;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class RecordedPath {
    private final Vector3f[] data;

    public RecordedPath(Vector3f[] data) {
        this.data = data;
    }

    public float getX(int pos) {
        if (pos >= data.length) {
            return getX(data.length - 1);
        }
        return data[pos].x;
    }

    public float getY(int pos) {
        if (pos >= data.length) {
            return getY(data.length - 1);
        }
        return data[pos].y;
    }

    public float getScale(int pos) {
        if (pos >= data.length) {
            return getScale(data.length - 1);
        }
        return data[pos].z;
    }

    public int getLength() {
        return data.length;
    }

    public static class Builder {
        private final List<Vector3f> data;

        public Builder() {
            this.data = new ArrayList<>();
        }

        public Builder with(float x, float y, float scale) {
            data.add(new Vector3f(x, y, scale));
            return this;
        }

        public DataSet serialize() {
            DataSet set = new DataSet();
            set.addInt("length", data.size());
            for (int i = 0; i < data.size(); i++) {
                DataSet entry = new DataSet();
                entry.addFloat("x", data.get(i).x);
                entry.addFloat("y", data.get(i).y);
                entry.addFloat("scale", data.get(i).z);
                set.addDataSet("entry_" + i, entry);
            }
            return set;
        }

        public RecordedPath build() {
            Vector3f[] positions = new Vector3f[this.data.size()];
            for (int i = 0; i < this.data.size(); i++) {
                positions[i] = this.data.get(i);
            }
            return new RecordedPath(positions);
        }
    }
}