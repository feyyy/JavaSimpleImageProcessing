package com.mobge.simpleimageprocessing;

import java.util.ArrayList;

public abstract class Block {

    private Object[] _inputs;
    private ArrayList<Connection> _connections;
    private int _setInputCount;

    public Block() {
        _inputs = new Object[numberOfInputs()];
        _setInputCount = 0;
        _connections = new ArrayList<>();
    }

    public <T> T getInput(int id) {
        return (T) _inputs[id];
    }

    public int numberOfInputs() {
        return 1;
    }
    public int numberOfOutputs() {
        return 1;
    }

    public void setInput(int index, Object data){
        if(_inputs[index] == null){
            _setInputCount++;
        }
        _inputs[index] = data;
        if(_setInputCount == numberOfInputs()){
            performOperation();
            _setInputCount = 0;
            for(int i = 0; i < _inputs.length; i++) {
                _inputs[i] = null;
            }
        }
    }
    public void setInput(Object data){
        setInput(0, data);
    }
    public abstract void performOperation();

    public void connectOutput(int outputIndex, Block block, int inputIndex) {
        Connection c = new Connection();
        c.output = outputIndex;
        c.input = inputIndex;
        c.target = block;
        _connections.add(c);
    }
    public void connectOutput(Block block){
        connectOutput(0, block, 0);
    }

    public void sendOutput(Object data, int outputIndex){
        for(int i = 0; i < _connections.size(); i++){
            Connection c = _connections.get(i);
            if(c.output == outputIndex) {
                c.sendData(data);
            }
        }
    }

    void copyRedToGreenBlue(ImageData image) {

        float[] rawData = image.getRawData();
        int offset;
        // copy the value of the red channel to other channels
        for(int i = 0; i < rawData.length; i += ImageData.BYTES_PER_PIXEL){
            float nextValue = rawData[i + ImageData.OFFSET_R];
            rawData[i+ ImageData.OFFSET_G] = nextValue;
            rawData[i+ ImageData.OFFSET_B] = nextValue;
        }
    }
    private static class Connection{
        int input;
        int output;
        Block target;
        void sendData(Object data){
            target.setInput(input, data);
        }
    }
}
