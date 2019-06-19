package br.usp.poli.labsoft.safemeet;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class TimerLocation extends IntentService {

    public static final int MSG_END_SERV = 0;
    public static final int MSG_REG_LOCAL = 1;

    private int count;
    private boolean stopServ;

    public TimerLocation(){
        super("Timer teste");
        this.count = 0;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.v("Timer", "Timer iniciado");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //Código (modelo) de inspiração
        //https://www.youtube.com/watch?v=PppcJOOrHHo

        ResultReceiver receiver = intent.getParcelableExtra("receiver");
        Bundle bdlLocal = new Bundle();
        Bundle bdlStop = new Bundle();

        int maxCount = intent.getIntExtra("maxCount", 0);

        while(count < maxCount && !stopServ){
            Log.v("Timer", "i: " + count);
            receiver.send(MSG_REG_LOCAL, bdlLocal);

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            count++;
        }

        bdlStop.putString("message", "Contagem finalizada!");
        receiver.send(MSG_END_SERV, bdlStop);
    }

    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
    */
}
