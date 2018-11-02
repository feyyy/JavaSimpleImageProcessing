package com.mobge.simpleimageprocessing;

public class BlockIn extends Block{
    @Override
    public void performOperation() {
        sendOutput(getInput(0), 0);
    }
}
