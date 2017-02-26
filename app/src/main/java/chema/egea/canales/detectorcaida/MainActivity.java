package chema.egea.canales.detectorcaida;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements SensorEventListener {

    //Detector caida

    private SensorManager sensorManager;
    TextView text_X;
    TextView text_Y;
    TextView text_Z;

    ArrayList<Float> Zvalores = new ArrayList<>(10);
    private float valoresZ[] = {0,0,0,0,0,0,0,0,0,0};
    private int i = 0;

    //Bateria
    private ProgressBar barraProgreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor( Sensor.TYPE_LINEAR_ACCELERATION ), SensorManager.SENSOR_DELAY_NORMAL );

        //RECOGEMOS VALORES DE LA INTERFAZ
        text_X=(TextView)findViewById(R.id.txtXValue);
        text_Y=(TextView)findViewById(R.id.txtYValue);
        text_Z=(TextView)findViewById(R.id.txtZValue);

        //BATERIA
        this.registerReceiver(this.bateriainfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        this.registerReceiver(this.BatteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));

        barraProgreso = (ProgressBar)findViewById(R.id.porcentajeBateria);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
        {
            //EL ACELEROMETRO HA CAMBIADO DE ESTADO
            muestraValores(event);

            if (i >= 10)
            {

                Zvalores.remove(0);
                Zvalores.add(event.values[2]);

                //Comprobamos la diferencia entre el primer y el ultimo valor, si es elevada es que caemos
                //if (Math.abs(valoresZ[0] - valoresZ[9]) > 8 )
                if (Math.abs(Zvalores.get(0)-Zvalores.get(9))>8)
                {
                    //Caemos
                    Toast.makeText(getApplicationContext(), "ESTAMOS CAYENDOOOO", Toast.LENGTH_SHORT).show();
                    i = 0;
                    Zvalores.clear();
                }

            }
            if (i < 10)
            {
                Zvalores.add(event.values[2]);
                i++;
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void muestraValores(SensorEvent event)
    {
        //ARRAY QUE CONTIENE VALORES DEL ACELEROMETRO
        float[] valores=event.values;

        //MODIFICAMOS VALORES DE LOS TEXTVIEW
        text_X.setText("Valor X: " + valores[0]);
        text_Y.setText("Valor Y: " + valores[1]);
        text_Z.setText("Valor Z: " + valores[2]);

    }

    //BATERIA

    private BroadcastReceiver bateriainfoReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);

            barraProgreso.setMax(scale);
            barraProgreso.setProgress(level);
        }
    };

    private BroadcastReceiver BatteryLevelReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action=intent.getAction();
            if (Intent.ACTION_BATTERY_LOW.equals(action))
            {
                Toast.makeText(context, "Batería BAJA - CERRAMOS LA APLICACION", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };

}