package autyzmsoft.pl.literowiec;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class Startowiec extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startowiec);
        new MojeTlo().execute();
    }  //onCreate()



    /************************************/
    private class MojeTlo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            //Bez tego sleep'a ponizej caly mechanizm nie dziala jak nalezy (nie pojawi sie napis 'prosze czekac'...):
            try {
              Thread.sleep(1500);
            }
            catch (InterruptedException e) {
              Thread.interrupted();
            }

            Intent mainAplikacja = new Intent("autyzmsoft.pl.literowiec.MainActivity");
            startActivity(mainAplikacja);
            return null;
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Toast.makeText(getApplicationContext(), "Dokonalo sie!", Toast.LENGTH_LONG).show();
            Startowiec.this.finish();  //zeby po zakonczeniu Aplikacji nie wisialo...
        }



        //        @Override
//        protected void doInBackground() {
//
//            for (int i = 0; i < 5; i++) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    Thread.interrupted();
//                }
//            }
//        }


    } //klasa wewnetrzna


}
