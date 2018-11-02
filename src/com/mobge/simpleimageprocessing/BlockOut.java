package com.mobge.simpleimageprocessing;

public class BlockOut extends Block {
    private Listener _listener;
    @Override
    public void performOperation() {
        _listener.resultIsReady(getInput(0));
    }
    public void setListener(Listener l){
        _listener = l;
    }
    public interface Listener<T>{
        void resultIsReady(T data);
    }
}
