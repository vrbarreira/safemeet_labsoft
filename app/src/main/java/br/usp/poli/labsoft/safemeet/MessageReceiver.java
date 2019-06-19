package br.usp.poli.labsoft.safemeet;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

@SuppressLint("ParcelCreator")
public class MessageReceiver extends ResultReceiver {
    private TrackerActivity.Message msg;

    public MessageReceiver(TrackerActivity.Message msg) {
        super(new Handler());
        this.msg = msg;
    }

    @Override
    protected void onReceiveResult(int msgType, Bundle resultData){
        msg.displayMessage(msgType, resultData);
    }

}
