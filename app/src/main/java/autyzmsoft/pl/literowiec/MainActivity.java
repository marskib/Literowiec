package autyzmsoft.pl.literowiec;

import static android.graphics.Color.RED;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import static autyzmsoft.pl.literowiec.ZmienneGlobalne.LATWE;
import static autyzmsoft.pl.literowiec.ZmienneGlobalne.SREDNIE;
import static autyzmsoft.pl.literowiec.ZmienneGlobalne.TRUDNE;
import static autyzmsoft.pl.literowiec.ZmienneGlobalne.WSZYSTKIE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

//import android.util.Log;


//Prowadzenie litery po ekranie Wykonalem na podstawie: https://github.com/delaroy/DragNDrop
//YouTube: https://www.youtube.com/watch?v=H3qr1yK6u3M   szukać:android drag and drop delaroy

public class MainActivity extends Activity implements View.OnLongClickListener {

    public static final int MAXL = 12;          //maxymalna dopuszczalna liczba liter w wyrazie

    public static final int DELAY_EXERC = 1000; //opoznienie w pokazywaniu rozrzuconych liter i podpisu pod Obrazkiem

    public static final long DELAY_ORDER = 600; //opoznienie uporządkowania Obszaru po Zwyciestwie


    Intent intModalDialog;  //Na okienko dialogu 'modalnego' orzy starcie aplikacji
    Intent intUstawienia;   //Na przywolanie ekranu z ustawieniami

    static MediaPlayer mp = null;

    private ViewGroup rootLayout;

    //Obrazek i nazwa pod obrazkiem:
    private  ImageView imageView;
    TextView tvNazwa;
    //Kontener z obrazkiem (do [long]klikania (lepiej niz na image - rozwiazuje problem z niewidzialnym obrazkiem):
    RelativeLayout l_imageContainer;

    //Placeholders'y na etykiety (12 szt.):
    MojTV L00, L01, L02, L03,
          L04, L05, L06, L07,
          L08, L09, L10, L11;


    public  static MojTV[] lbs;     //tablica zawierajaca (oryginalne) litery wyrazu; onomastyka: lbs = 'labels'
    private static MojTV[] lbsRob;  //tablica robocza, do dzialań (m.in. latwego wykrycia prawidlowego porzadku ulozenia etykiet w Obszarze); podzbior tab. lbs


    TextView tvInfo, tvInfo1, tvInfo2, tvInfo3;

    TextView tvShownWord; //na umieszczenie wyrazu po Zwyciestwie

    private int sizeH, sizeW;    //wymiary Urzadzenia

    private int _xDelta;
    private int _yDelta;

    private int yLg,yLd,xLl,xLp; //wspolrzedne pionowe ygrek Linij Górnej i Dolnej oraz wspolrzedne poziome x linij Lewej i Prawej obszaru 'gorącego'
    private int yLtrim;          ///polozenie  linii 'Trimowania' - srodek Obszaru, do tej linii dosuwam etykiety (kosmetyka znaczaca)

    private RelativeLayout.LayoutParams lParams, layoutParams;

    public  boolean toUp = false; //czy jestesmy w trybie duzych/malych liter
    private Button bUpperLower;   //wielkie/male litery
    private Button bHint;         //klawisz podpowiedzi
    private Button bAgain;        //wymieszanie liter; klawisz pod Obszarem
    private Button bAgain1;       //wymieszanie liter; klawisz podbDalej

    private LinearLayout lObszar;
    private Button bDalej;                              //button na przechodzenie po kolejne cwiczenie
    private Button bPomin;                              //na pominiecie/ucieczke z cwiczenia nie konczac go

    public static File     katalogSD;                 //katalog z obrazkami na SD (internal i external)
    public static String[] listaObrazkowSD = null;    //lista obrazkow w katalogu na SD (internal i externa)

    public static String   katalogAssets = null;               //Katalogu w Assets, w ktorym trzymane beda obrazki
    public static String[] listaObrazkowAssets = null;         //lista obrazkow z Assets/obrazki - dla wersji demo (i nie tylko...)

    public static String[] listaOper = null; //listas 'operacyjna', z niej ostateczne pobieranie obrazkow

    boolean nieGraj = true;    //przelacznik(semafar) : grac/nie grac - jesli start apk. to ma nie grac slowa (bo glupio..)

    public static int currImage = -1;      //indeks biezacego obrazka
    public String     currWord  = "*";     //bieżacy, wygenerowany wyraz, wziety z currImage; sluzy do porownan; nie jest wyswietlany (w starych wersjach byl...)

    Button bDajGestosc; //sledzenie
    public static int density;          //gestosc ekranu - przydatne system-wide

    public static boolean PW = true;    //Pierwsze Wejscie do aplikacji

    private Pamietacz mPamietacz;       //do pamietania przydzielonych obrazkow, zeby w miare mozliwosci nie powtarzaly sie

    ZmienneGlobalne mGlob;                          //'m-member' na zmienne globalne - obiekt singleton klasy ZmienneGlobalne
    KombinacjaOpcji currOptions, newOptions;        //biezace (obowiazujace do chwili wywolania UstawieniaActivity) ustawienia i najnowsze, ustawione w UstawieniaActivity)

    Animation animShakeShort, animShakeLong;  //potrzasanie litera[mi] - definuje 'wysoko' - wydajnosc



    /* eksperymenty ze status barem - 2018.08.11 */
/*
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Toast.makeText(this, "pkt1", Toast.LENGTH_SHORT).show();
        if (hasFocus) {
            Toast.makeText(this, "pkt2", Toast.LENGTH_SHORT).show();
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        Toast.makeText(this, "pkt3", Toast.LENGTH_SHORT).show();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
*/

    /* eksperymenty ze status barem - 2018.08.11 - KONIEC*/


    /*Ponizej, do konca metody onRequestPermissionResult() kod zapewniajacy dostep do kart SD: */
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private float tvWyrazSize;  //rozmiar wyrazu pod obrazkiem
    private double screenInches;

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        /* Wywolywana po udzieleniu/odmowie zezwolenia na dostęp do karty (od API 23 i wyzej) */
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //reload my activity with permission granted or use the features what required the permission
                } else {
                    //toast("Nie udzieliłeś zezwolenia na odczyt. Opcja 'obrazki z mojego katalogu' nie będzie działać. Możesz zainstalować aplikacje ponownie lub zmienić zezwolenie w Menadżerze aplikacji.");
                    wypiszOstrzezenie(
                            "Nie udzieliłeś zezwolenia na odczyt. Opcja 'mój katalogAssets' nie będzie działać. Możesz zainstalować aplikację ponownie lub zmienić zezwolenie w Menadżerze aplikacji.");
                    mGlob.ODMOWA_DOST = true;  //dajemy znać, ze odmowiono dostepu; bedzie potrzebne na Ustawieniach przy próbie wybrania wlasnych zasobow
                }
            }
        }
    } //koniec Metody

    @Override
    public void onCreate(Bundle savedInstanceState) {

        /* ZEZWOLENIA NA KARTE _ WERSJA na MARSHMALLOW, jezeli dziala na starszej wersji, to ten kod wykona sie jako dummy */
        int jestZezwolenie = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (jestZezwolenie != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    REQUEST_CODE_ASK_PERMISSIONS);
        }
        /* KONIEC **************** ZEZWOLENIA NA KARTE _ WERSJA na MARSHMALLOW */

        super.onCreate(savedInstanceState);

        //Na caly ekran:
        //1.Remove title bar:
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //2.Remove notification bar:
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //3.Set content view AFTER ABOVE sequence (to avoid crash):

        setContentView(R.layout.activity_main);


//ski ski 2018.09.10   ****proby z 'progress' barem:

//        final Handler hRefresh = new Handler(){
//
//            @Override
//            public void handleMessage(Message msg) {
//                switch(msg.what){
//                    case 1000:
//                        //Refreshing UI:
//                        break;
//                }
//            }
//        };
//
//         final ProgressDialog mProgressDlg = ProgressDialog.show(this, "App_Name", "Loading data...",
//         true, false);
//         new Thread(new Runnable(){
//         public void run() {
//         //Loading Data:
//
//             //ladowanieDanych();
//
//             try {
//                 Thread.sleep(5000);
//             } catch (InterruptedException e) {
//                 e.printStackTrace();
//             }
//
//             mProgressDlg.dismiss();
//        hRefresh.sendEmptyMessage(1000);
//    }
//}).start();

/****************** ski ski koniec prob z PRogres 'barem' ************************/



        mGlob = (ZmienneGlobalne) getApplication();

        rootLayout = (ViewGroup) findViewById(R.id.view_root);

        l_imageContainer = (RelativeLayout) findViewById(R.id.l_imgContainer);
        imageView = (ImageView) rootLayout.findViewById(R.id.imageView);

        tvNazwa = (TextView) findViewById(R.id.tvNazwa);
        lObszar = (LinearLayout) findViewById(R.id.l_Obszar);
        bDalej  = (Button) findViewById(R.id.bDalej);
        bPomin  = (Button) findViewById(R.id.bPomin);
        bAgain  = (Button) findViewById(R.id.bAgain);
        bAgain1 = (Button) findViewById(R.id.bAgain1);
        tvShownWord = (TextView) findViewById(R.id.tvShownWord);
        bUpperLower =(Button) findViewById(R.id.bUpperLower);
        bHint = (Button) findViewById(R.id.bHint);

        //kontrolki do sledzenia:
        tvInfo = (TextView) findViewById(R.id.tvInfo);
        tvInfo1 = (TextView) findViewById(R.id.tvInfo1);
        tvInfo2 = (TextView) findViewById(R.id.tvInfo2);
        tvInfo3 = (TextView) findViewById(R.id.tvInfo3);
        bDajGestosc = (Button) findViewById(R.id.bDajGestosc);


        przypiszLabelsyAndListenery();

        //animacj3 na potrzasanie litera[mi]:
        animShakeShort = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shaking_short);
        animShakeLong  = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shaking_long);

        //Poprawienie wydajnosci? (zeby w onTouch nie tworzyc stale obiektow) L01 - placeholder
        lParams = (RelativeLayout.LayoutParams) L01.getLayoutParams();
        layoutParams = (RelativeLayout.LayoutParams) L01.getLayoutParams();

        //ustalam polozenie obrazkow - przy pelnej wersji - duuzo więcej... ;):
        katalogAssets = "obrazki_demo_ver";
        if (mGlob.PELNA_WERSJA) {
            katalogAssets = "obrazki_pelna_ver";
        }


        dostosujDoUrzadzen();

        dajWspObszaruInfo();

        pokazUkryjEtykietySledzenia(false);

        resetujLabelsy();

        //Trzeba czekac, bo problemy (doswiadczalnie):
        lObszar.post(new Runnable() {
            @Override
            public void run() {
                ustawLadnieEtykiety();
                ustawWymiaryKlawiszy();
            }
        });

        //Stworzenie statycznej, raz na zawsze listy z Assets:
        tworzListeFromAssets();
        listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowAssets, mGlob.POZIOM);
        //gdyby byly jakies problemy, to na WSZYSTKIE... :
        if (listaOper.length==0) {
            mGlob.POZIOM = WSZYSTKIE;
            listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowAssets, mGlob.POZIOM);
        }

        //ewentualna lista z SD (jezeli ma byc na starcie):
        if (mGlob.ZRODLEM_JEST_KATALOG ) {
            tworzListeFromKatalog();
            listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowSD, mGlob.POZIOM);
            //gdyby byly jakies problemy (cos. nie ok. w np. SharedPref), to na WSZYSTKIE...:
            if (listaOper.length==0) {
                mGlob.POZIOM = WSZYSTKIE;
                listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowSD, mGlob.POZIOM);
            }
        }

        mPamietacz = new Pamietacz(listaOper); //do pamietania przydzielonych obrazkow

        //Zapamietanie ustawien:
        currOptions = new KombinacjaOpcji();
        newOptions  = new KombinacjaOpcji();


        dajNextObrazek();                   //daje index currImage obrazka do prezentacji oraz wyraz currWord odnaleziony pod indeksem currImage
        setCurrentImage();                  //wyswietla currImage i odgrywa słowo okreslone przez currImage
        rozrzucWyraz();                     //rozrzuca litery wyrazu okreslonego przez currImage

        pokazModal();                       //startowe okienko modalne z logo i objasnieniami 'klikologii'

    }  //koniec onCreate()


    private void ladowanieDanych() {
    } //koniec metody ladowanieDanych()



    private void tworzListeFromAssets() {
        //Pobranie listy obrazkow z Assets (statyczna, raz na zawsze, wiec najlepiej tutaj):
        AssetManager mgr = getAssets();
        try {
            listaObrazkowAssets =  mgr.list(katalogAssets);  //laduje wszystkie obrazki z Assets
        } catch (IOException e) {
            e.printStackTrace();
        }
    }  //koniec Metody()

    private static String[] listaOgraniczonaDoPoziomuTrudnosci(String[] lista, int poziom) {
    /*************************************************************************************/
    /* Ograniczenie Listy obrazkow (Assets bądź SD) do wybranego poziomu.                */
    /* (tworze tablice robocza, a nastepnie lista obrazkow wskaze na tę tablicę roboczą) */
    /*************************************************************************************/

        int dlug_min = 1;
        int dlug_max = MAXL;

        switch (poziom) {
            case LATWE     : dlug_min = 1; dlug_max = 4; break;
            case SREDNIE   : dlug_min = 5; dlug_max = 7; break;
            case TRUDNE    : dlug_min = 8; dlug_max = MAXL; break;
            case WSZYSTKIE : dlug_min = 1; dlug_max = Integer.MAX_VALUE; break; //nazwa dluzsza niz 12 (MAXL) znakow - trzeba ja uwzglednic, bo inaczej pozniej exception.. (potem i tak przytne do 12)
        }

        //Tworze liste robocza:
        //dzieki temu okresle ile jest wymaganych obrazkow, a tym samym bede mial rozmiar tablicy roboczej
        ArrayList<String> lRob =  new ArrayList<String>();
        for (String el : lista) {
            String elTmp = getRemovedExtensionName(el);
            int dlug0 = elTmp.length();         //uwaga na kot1
            elTmp = usunLastDigitIfAny(elTmp);  //gdyby byl kot1 to kot1->kot
            int dlug = elTmp.length();

            //Czysta sytuacja - wyraz miesci sie w kryterium:
            if ( (dlug >= dlug_min) && (dlug <= dlug_max) ) {
                lRob.add(el); //dodajemy z rozszerzeniem - pelna nazwa pliku!!!
            }
            //Sprawdzamy, bo moze byc 'kot1', 'kot2' .... - taki wyraz, chc dluzszy, trzeba wziac, bo last digit bedzie w ptzyszlosci wyciety i zostanie 3-literowe kot, tak jak trzeba...
            else {
              if (dlug0 == dlug_max+1) {
                int idxEnd = dlug-1;
                Character lastChar = elTmp.charAt(idxEnd);
                if (Character.isDigit(lastChar)) {
                    lRob.add(el);
                }
              }
            }
        } //for

        //Przepisanie lRob -> tabRob:
        String[] tabRob = new String[lRob.size()];
        int i = 0;
        for (String s : lRob) {
            tabRob[i] = s;
            i++;
        }

        return tabRob;

    }  //koniec Metody()


    public void setCurrentImage() {
    /* Wyrysowanie Obrazka; Odegranie dźwieku; Animacja */

        String nazwaObrazka; //zawiera rozszerzenie (.jpg , .bmp , ...)
        Bitmap bitmap;       //nie trzeba robic bitmapy, mozna bezposrednio ze strumienia, ale bitmap pozwala uzyc bitmat.getWidth() (patrz setCornerRadius())

        if (mGlob.BEZ_OBRAZKOW)
            imageView.setVisibility(INVISIBLE);
        else
            imageView.setVisibility(VISIBLE);

        nazwaObrazka = listaOper[currImage];
        try {
            if (mGlob.ZRODLEM_JEST_KATALOG) { //pobranie z Directory

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                String robAbsolutePath = katalogSD + "/" + nazwaObrazka;
                bitmap = BitmapFactory.decodeFile(robAbsolutePath, options);
                //Wykrycie orientacji i ewentualny obrot obrazka:
                //bitmap = obrocJesliTrzeba(bitmap, robAbsolutePath); - wylaczam, bo chyba(?) nie dziala
            } else {  //pobranie obrazka z Assets
                InputStream streamSki = getAssets().open(katalogAssets + "/" + nazwaObrazka);
                bitmap = BitmapFactory.decodeStream(streamSki);

                /********* obrazek "klasyczny", bez rounded corners (wersja poprzednia): ***********
                a) - z Assets:
                Drawable drawable = Drawable.createFromStream(stream, null);
                imageView.setImageDrawable(drawable);

                b) - z SD:
                imageView.setImageBitmap(bitmap);
                ******* obrazek "klasyczny" - koniec ******************/
            }

            /******* rounded corners 2018.08.03 *************/
            RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(), bitmap); //ostatnim parametrem moglby byc stremSki (patrz wyzej)
            dr.setCornerRadius(Math.max(bitmap.getWidth(), bitmap.getHeight()) / 20.0f);
            imageView.setImageDrawable(dr);
            /******* rounded corners koniec *************/

            //Pokazania obrazka z 'efektem' (efekciarstwo):
            if (!mGlob.BEZ_OBRAZKOW) { //trzeba sprawdzic warunek, bo animacja pokazalaby obrazek na chwile...
                Animation a = AnimationUtils.loadAnimation(this, R.anim.skalowanie);
                imageView.startAnimation(a);
            }

            //Ewentualna nazwa pod obrazkiem (robie tutaj, bo lepszy efekt wizualny niż gdzie indziej):
            if (mGlob.Z_NAZWA) {
                pokazUkryjNazwe();
                Animation b = AnimationUtils.loadAnimation(this, R.anim.skalowanie);
                tvNazwa.startAnimation(b);
            }

        } catch (Exception e) {
            Log.e("4321", e.getMessage());
            Toast.makeText(this, "Problem z wyswietleniem obrazka...", Toast.LENGTH_SHORT).show();
        }

        //ODEGRANIE DŹWIĘKU
        odegrajWyraz(400);

    }  //koniecMetody()


    private void odegrajWyraz(int opozniacz) {
        /*************************************************/
        /* Odegranie prezentowanego wyrazu               */
        /*************************************************/
        //najpierw sprawdzam, czy trzeba:
        //Jezeli w ustawieniech jest, zeby nie grac - to wychodzimy:
        if (mGlob.BEZ_DZWIEKU == true) {
            return;
        }
        //zeby nie gral zaraz po po starcie apki:
        if (nieGraj) {
            nieGraj = false;
            return;
        }
        //Granie wlasciwe:

        String nazwaObrazka = listaOper[currImage];
        String rdzenNazwy = usunLastDigitIfAny(getRemovedExtensionName(nazwaObrazka));
        if (!mGlob.ZRODLEM_JEST_KATALOG) {
            //odeggranie z Assets (tam TYLKO ogg):
            String sciezka_do_pliku_dzwiekowego = "nagrania/" + rdzenNazwy + ".ogg";
            odegrajZAssets(sciezka_do_pliku_dzwiekowego, opozniacz);
        } else {  //pobranie nagrania z directory
            //odegranie z SD (na razie nie zajmujemy sie rozszerzeniem=typ pliku dzwiekowego jest (prawie) dowolny):
            String sciezka_do_pliku_dzwiekowego = katalogSD + "/" + rdzenNazwy; //tutaj przekazujemy rdzen nazwy, bez rozszerzenia, bo mogą być różne (.mp3, ogg, .wav...)
            odegrajZkartySD(sciezka_do_pliku_dzwiekowego, opozniacz);
        }
    }  //koniec Metody()

    private void odegrajZAssets(final String sciezka_do_pliku_parametr, int delay_milisek) {
        /* ***************************************************************** */
        // Odegranie dzwieku umieszczonego w Assets (w katalogu 'nagrania'):
        /* ***************************************************************** */

        if (mGlob.nieGrajJestemW105) return; //na czas developmentu....

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                try {
                    if (mp != null) {
                        mp.release();
                        mp = new MediaPlayer();
                    } else {
                        mp = new MediaPlayer();
                    }
                    final String sciezka_do_pliku = sciezka_do_pliku_parametr; //udziwniam, bo klasa wewn. i kompilator sie czepia....
                    AssetFileDescriptor descriptor = getAssets().openFd(sciezka_do_pliku);
                    mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                    descriptor.close();
                    mp.prepare();
                    mp.setVolume(1f, 1f);
                    mp.setLooping(false);
                    mp.start();
                    //Toast.makeText(getApplicationContext(),"Odgrywam: "+sciezka_do_pliku,Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    //Toast.makeText(getApplicationContext(), "Nie można odegrać pliku z dźwiękiem.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, delay_milisek);
    } //koniec Metody()


    private void odegrajZkartySD(final String sciezka_do_pliku_parametr, int delay_milisek) {
        /* ************************************** */
        /* Odegranie pliku dzwiekowego z karty SD */
        /* ************************************** */

        if (mGlob.nieGrajJestemW105) return; //na czas developmentu....

        //Na pdst. parametru metody szukam odpowiedniego pliku do odegrania:
        //(typuję, jak moglby sie nazywac plik i sprawdzam, czy istbieje. jezeli istnieje - OK, wychodze ze sprawdzania majac wytypowaną nazwe pliku)
        String pliczek;
        pliczek = sciezka_do_pliku_parametr + ".m4a";
        File file = new File(pliczek);
        if (!file.exists()) {
            pliczek = sciezka_do_pliku_parametr + ".mp3";
            file = new File(pliczek);
            if (!file.exists()) {
                pliczek = sciezka_do_pliku_parametr + ".ogg";
                file = new File(pliczek);
                if (!file.exists()) {
                    pliczek = sciezka_do_pliku_parametr + ".wav";
                    file = new File(pliczek);
                    if (!file.exists()) {
                        pliczek = sciezka_do_pliku_parametr + ".amr";
                        file = new File(pliczek);
                        if (!file.exists()) {
                            pliczek = ""; //to trzeba zrobic, zeby 'gracefully wyjsc z metody (na Android 4.4 sie wali, jesli odgrywa plik nie istniejacy...)
                            //dalej nie sprawdzam/nie typuję... (na razie) (.wma nie sa odtwarzane na Androidzie)
                        }
                    }
                }
            }
        }
        //Odegranie znalezionego (if any) poliku:
        if (pliczek.equals("")) {
            return;  //bo Android 4.2 wali sie, kiedy próbujemy odegrac plik nie istniejący
        }
        Handler handler = new Handler();
        final String finalPliczek = pliczek; //klasa wewnetrzna ponizej - trzeba "kombinowac"...
        handler.postDelayed(new Runnable() {
            public void run() {
                try {
                    Uri u = Uri.parse(finalPliczek); //parse(file.getAbsolutePath());
                    mp = MediaPlayer.create(getApplicationContext(), u);
                    mp.start();
                } catch (Exception e) {
                    //toast("Nie udalo się odegrać pliku z podanego katalogu...");
                    Log.e("4321", e.getMessage()); //"wytłumiam" komunikat
                } finally {
                    //Trzeba koniecznie zakonczyc Playera, bo inaczej nie slychac dzwieku:
                    //mozna tak:
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });
                    //albo mozna tak:
                    //mPlayer.setOnCompletionListener(getApplicationContext()); ,
                    //a dalej w kodzie klasy zdefiniowac tego listenera, czyli public void onCompletion(MediaPlayer xx) {...}
                }
            }
        }, delay_milisek);
    } //koniec metody odegrajZkartySD


      private void dajNextObrazek() {
     //Daje index currImage obrazka do prezentacji oraz wyraz currWord odnaleziony pod indeksem currImage;
     //Na podst. currImage ustawia nazwe currWord.

        currImage = dajLosowyNumerObrazka();

        //Nazwe odpowiadajacego pliku oczyszczamy z nalecialosci:

        String nazwaPliku = listaOper[currImage];

        nazwaPliku = getRemovedExtensionName(nazwaPliku);
        nazwaPliku = usunLastDigitIfAny(nazwaPliku);

        //Uwaga - Uwaga : przyciecie do 12 liter !!!!
        currWord  = nazwaPliku.substring(0, Math.min(MAXL,nazwaPliku.length()) );

        //Odsiewam/zamieniam ewentualne spacje z wyrazu bo problemy:
        if (currWord.contains(" ")) {
            //currWord = currWord.replaceAll("\\s","");
            currWord = currWord.replaceAll("\\s", "_");
        }

      } //koniec Metody()


  private void pokazUkryjNazwe() {
  //Umieszcza nazwę pod obrazkiem (jesli ustawiono w ustawieniach)

    tvNazwa.setVisibility(INVISIBLE);  //wymazanie (rowniez) ewentualnej poprz. nazwy

    if (!mGlob.Z_NAZWA) return;

    tvNazwa.setText(currWord);
      if (toUp) {
          tvNazwa.setText(tvNazwa.getText().toString().toUpperCase(Locale.getDefault()));
      }
      tvNazwa.setVisibility(VISIBLE);

  }  //koniec Metedy()


  private void rozrzucWyraz() {
   /* Rozrzucenie currWord po tablicy lbs (= po Ekranie)              */
   //Wyswietla tez nazwe pod obrazkiem

       //bDajGestosc.setText("TV :   Ol: "); //sledzenie

       //currWord = "ABCDEFGHIJKL";
       //currWord = "cytryna";
       //currWord = "************";
       //currWord   = "abcdefghijkl";
       //currWord   = "pomarańczowy";
       //currWord   = "niedźwiedzie";
       //currWord   = "rękawiczki";
       //currWord   = "jękywiłzkóśp";
       //currWord   = "wwwwwwwwwwww";
       //currWord   = "mmmmmmmmmmmm";
       //currWord   = "tikjńfźlóśżk";
       //currWord   = "mikrofalówka";
       //currWord   = "pies";
       //currWord   = "mmmm";
       //currWord   = "Mikołaj";
       //currWord   = "Mikołajm";
       //currWord   = "lalka";
       //currWord   = "jabłko";
       //currWord   = "słoneczniki";
       //currWord   = "podkoszulek";
       //currWord   = "ogórek";
       //currWord   = "makaron";
       //currWord   = "zegar";
       //currWord   = "nóż";
       //currWord   = "kot";
       //currWord   = "huśtawka";
       //currWord   = "buty";
       //currWord   = "W";
       //currWord   = "ze spacjom";


      //Pobieramy wyraz do rozrzucenia:
      final char[] wyraz = currWord.toCharArray();       //bo latwiej operowac na Char'ach

      final Random rand = new Random();

      final Animation a = AnimationUtils.loadAnimation(MainActivity.this, R.anim.skalowanie);
      a.setDuration(500);

      //Pokazujemy z lekkim opoznieniem (efekciarstwo...):
      Handler mHandl = new Handler();
      mHandl.postDelayed(new Runnable() {
           @Override
           public void run() {
               //Kazda litera wyrazu ląduje w losowej komorce tablicy lbs :
               for (int i = 0; i < wyraz.length; i++) {
                   String z = Character.toString(wyraz[i]); //pobranie litery z wyrazu

                   //Losowanie pozycji w tablicy lbs:
                   int k;  //na losową pozycję
                   do {
                      k = rand.nextInt(lbs.length);
                   }
                   while (lbs[k].getVisibility() == VISIBLE); //petla gwarantuje, ze trafiamy tylko w puste (=niewidoczne) etykiety

                   //Umieszczenie litery na wylosowanej pozycji (i w strukturze obiektu MojTV) + pokazanie:
                   lbs[k].setOrigL(z);
                   lbs[k].setText(z);
                   lbs[k].setTextColor(Color.BLACK);  //kosmetyka, ale wazna...
                   lbs[k].setVisibility(VISIBLE);
                   /******/
                   //podpiecie animacji:
                   lbs[k].startAnimation(a);
               }
               if (toUp)             //ulozylismy z malych (oryginalnych) liter. Jesli trzeba - podnosimy
                   podniesLabels();
           }  //run()
      }, DELAY_EXERC);

      //Odblokowanie dodatkowych klawiszy - chwilke po pokazaniu liter (lepszy efekt):
      Handler mH2 = new Handler();
      mH2.postDelayed(new Runnable() {
          @Override
          public void run() {
            odblokujZablokujKlawiszeDodatkowe();
          }
      }, 2*DELAY_EXERC);

   } //koniecMetody();


    private void resetujLabelsy() {
    //Resetowanie tablicy i tym samym zwiazanycyh z nia kontrolek ekranowych:
        for (MojTV lb : lbs) {
            lb.setText("*");
            lb.setOrigL("*");
            lb.setInArea(false);
            lb.setVisibility(INVISIBLE);
        }
    }


    public void bDalejOnClick(View v) {
    /* ***************************** */
    /* Przejscie do nowego cwiczenia */
    /* ***************************** */
        //sledzenie:
        //bUpperLower.setText(sizeW+"x"+sizeH);

        //Usuniecie Grawitacji z lObszar, bo mogla byc ustawiona w korygujJesliWystaje() ):
        usunGrawitacje();

        blokujKlawiszeDodatkowe();

        resetujLabelsy();
        ustawLadnieEtykiety();
        dajNextObrazek();                   //daje indeks currImage obrazka do prezentacji oraz currWord = nazwa obrazka bez nalecialosci)
        setCurrentImage();                  //wyswietla currImage i odgrywa słowo okreslone przez currImage
        rozrzucWyraz();                     //rozrzuca litery wyrazu okreslonego przez currWord

        tvShownWord.setVisibility(INVISIBLE);

        bDalej.setVisibility(INVISIBLE);
        bAgain1.setVisibility(INVISIBLE);

    } //koniecMetody()



    public void bAgainOnClick(View v) {
    //bAgain -  kl. pod Obszarem
    //bAgain1 - kl. pod bDalej

        //Usuniecie Grawitacji z lObszar, bo mogla byc ustawiona w korygujJesliWystaje() ):
        usunGrawitacje();

        blokujKlawiszeDodatkowe();

        ustawLadnieEtykiety();
        resetujLabelsy();
        rozrzucWyraz();

        tvShownWord.setVisibility(INVISIBLE);
        bDalej.setVisibility(INVISIBLE); //gdyby byl widoczny

        if (v==bAgain1) {   //pod klawiszem bDalej
            bAgain1.setVisibility(INVISIBLE);
            Handler mHandl = new Handler();
            mHandl.postDelayed(new Runnable() {
                @Override
                public void run() {
                    odblokujZablokujKlawiszeDodatkowe(); //pokazanie z opoznieniem, zeby nie klikal za wczesnie, bo 'zawiecha'
                }
            },2*DELAY_EXERC);
        }
        else {
            bAgain1.setVisibility(INVISIBLE); //bo w pewnych warunkach pozostaje zapalony...
        }
    }  //koniec Metody()

    private void blokujKlawiszeDodatkowe() {
        bPomin.setEnabled(false);
        bUpperLower.setEnabled(false);

        bAgain.setEnabled(false);
        bAgain.setText(""); //czyszcze, bo cos moze zostac po animacji.... (patrz opi MakeMeBlink()

        bHint.setEnabled(false);
    }


    public void bPominOnClick(View v) {
        bDalej.callOnClick();
    }


    public void bUpperLowerOnClick(View v) {
    //Zmiana male/duze litery (w obie strony)

        toUp = !toUp;

        //Kosmetyka - zmiana symbolu na buttonie:
        if (!toUp)
            ((Button) v).setText("-----");
        else
            ((Button) v).setText("|");


        //1.Wyraz juz ulozony:
        if (tvShownWord.getVisibility()== VISIBLE) {
            if (toUp) podniesWyraz();
            else restoreOriginalWyraz();
        }
        //2.Wyraz jeszcze nie ulozony:
        else {
            if (toUp) podniesLabels();
            else restoreOriginalLabels();
        }

    } //koniec Metody()

    private void podniesLabels() {
    //Etykiety podnoszone są do Wielkich liter
    //Oryginałów l.origL - nie ruszam!!!

        String coWidac;
        for (MojTV lb : lbs) {
            if (!lb.getOrigL().equals("*")) { //(lb.getVisibility()== View.VISIBLE) {
                coWidac = lb.getText().toString();
                coWidac = coWidac.toUpperCase(Locale.getDefault());
                lb.setText(coWidac);
            }
        }
        tvNazwa.setText(tvNazwa.getText().toString().toUpperCase(Locale.getDefault())); //podniesienie nazwy pod Obrazkiem
    } //koniec Metody()

    private void restoreOriginalLabels() {
    //Etykiety przywracane są do oryginalnych (małych) liter
    //Uwzględnia to problem MIKOŁAJ->Mikołaj

        String coPokazac;
        for (MojTV lb : lbs) {
            if (!lb.getOrigL().equals("*")) { //(lb.getVisibility()==View.VISIBLE) {
                coPokazac = lb.getOrigL();
                lb.setText(coPokazac);
            }
        }
        tvNazwa.setText(currWord);  //nazwa pod Obrazkiem wraca do oryginalnych liter
    } //koniec Metody()

    private void podniesWyraz() {
    //Poprawny wyraz z Obszaru podnoszony do Wielkich liter
    //Ewentualne odsuniecie, jezeli wyraz po powiekszeniu wychodzi za prawa krawedz Obszaru

        String coWidac = tvShownWord.getText().toString();
        coWidac = coWidac.toUpperCase(Locale.getDefault());
        tvShownWord.setText(coWidac);

        tvNazwa.setText(tvNazwa.getText().toString().toUpperCase(Locale.getDefault())); //podniesienie nazwy pod Obrazkiem

        ewentualnieSciesnij(); //Sciesniam jezeli b.dlugi wyraz z letterSpacing>0 , bo wychodzi za Obszar no mater what...
        korygujJesliWystaje();

    }  //koniec Metody()


    private void korygujJesliWystaje() {
    //Robimy w post bo trzeba zaczekac, az tvShownWord pojawi sie na ekranie.

        tvShownWord.post(new Runnable() {
            @Override
            public void run() {
                int rightT = tvShownWord.getRight();  //T - od 'textV..'
                int rightL = lObszar.getRight();      //L - od 'lOb...'

                //bDajGestosc.setText("TV : "+Integer.toString(rightT)+" Ol :"+Integer.toString(rightL)); //sledzenie

                if (rightT >= rightL) {
                    addGravityToParent();
                }
            }
        });

    } //koniec Metody()


    private void restoreOriginalWyraz() {
    //Wyraz z Obszaru zmniejszany jest do małych (scislej: oryginalnych) liter.
    //Uwzględnia to problem MIKOŁAJ->Mikołaj
    //Wywolywane w kontekscie zmiany z Wielkich->małe, wiec staram sie, zeby wyraz z malymi literami
    //rozpoczynal sie tam, gdzie zaczynal sie wyraz z "macierzysty" (jezeli wyraz<MAXL znakow)

        String coPokazac = currWord;
        restoreApplyLetterSpacing(tvShownWord);
        tvShownWord.setText(coPokazac);

        //Jezeli wyraz nie jest zbyt dlugi, to wyraz zacznie sie tam, gdzie zaczynal sie wyraz z Wielimi literami
        //(przy b.dlugich wyrazach nie mozna sobie na to pozwolic - patrz 'niedziedzie' przy zmianie Wielki->male nie miesci sie w Obszarze(!)
        //(wieloliterowy wyraz malymi literami moze byc dluzszy niz ten sam wyraz Wielkimi, bo wielki ma usuniety letterSpacing(!)):
        final int pocz = tvShownWord.getLeft();
        if (currWord.length()<MAXL) {
            tvShownWord.post(new Runnable() {
                @Override
                public void run() {
                    tvShownWord.setLeft(pocz);
                }
            });
        }
        tvNazwa.setText(currWord); //nazwa pod Obrazkiem wraca do malyh liter
    } //koniec Metody()



    public void bDajWielkoscEkranuOnClick(View v) {

//        bDalej.getLayoutParams().height = yLg;
//        bDalej.requestLayout();

        int screenSize = getResources().getConfiguration().screenLayout &
            Configuration.SCREENLAYOUT_SIZE_MASK;

        String toastMsg;
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                toastMsg = "XLarge screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                toastMsg = "Large screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                toastMsg = "Normal screen";
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                toastMsg = "Small screen";
                break;
            default:
                toastMsg = "Screen size is neither xlarge, large, normal or small";
        }
        Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
    } //koniec Metody()



    private void dajWspObszaruInfo() {
    //Daje wspolrzedne prostokatnego Obszaru, gdzie ukladany jest wyraz

        lObszar.post(new Runnable() { //czekanie az obszar sie narysuje
            @Override
            public void run() {
                int[] location = new int[2];
                lObszar.getLocationOnScreen(location);
                int x = location[0];
                int y = location[1];

                //Przekazanie do zmiennych Klasy parametrow geograficznych Obszaru:
                xLl = x;
                yLg = y;
                xLp = xLl + lObszar.getWidth();
                yLd = yLg + lObszar.getHeight();
                //Przekazanie do zmiennek klasy współrzędnej y linii 'Trymowania':
                yLtrim = yLg+ ((int) ((yLd-yLg)/2.0));
            }
        });
   } //koniec Metody()



    private void pokazUkryjEtykietySledzenia(boolean czyPokazac) {
        int rob;
        rob = INVISIBLE;
        if (czyPokazac) rob = VISIBLE;
        tvInfo.setVisibility(rob);
        tvInfo1.setVisibility(rob);
        tvInfo2.setVisibility(rob);
        tvInfo3.setVisibility(rob);
    } //koniec Metody();


    public void bDajGestoscOnClick(View view) {
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        int density= getResources().getDisplayMetrics().densityDpi;
        switch(density)
        {
            case DisplayMetrics.DENSITY_LOW:
                Toast.makeText(this, "LDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                Toast.makeText(this, "MDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_HIGH:
                Toast.makeText(this, "HDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                Toast.makeText(this, "XHDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                Toast.makeText(this, "XXHDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                Toast.makeText(this, "XXXHDPI", Toast.LENGTH_SHORT).show();
                break;
            case DisplayMetrics.DENSITY_560:
                Toast.makeText(this, "560 ski ski", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "nie znalazłem...", Toast.LENGTH_SHORT).show();
        }
    } //koniec Metody()



    @Override
    /**
     * dotyczy: imageView
     * Co na dlugim kliknieciu na obrazku - powolanie ekranu z opcjami
     */
    public boolean onLongClick(View view) {
        intUstawienia = new Intent("autyzmsoft.pl.literowiec.UstawieniaActivity");
        startActivity(intUstawienia);
        return true;
    } //koniec Metody()


    private final class ChoiceTouchListener implements OnTouchListener {
        public boolean onTouch(View view, MotionEvent event) {
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_MOVE:
                    layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    layoutParams.leftMargin = X - _xDelta;
                    layoutParams.topMargin = Y - _yDelta;
                    layoutParams.rightMargin = -250;
                    layoutParams.bottomMargin = -250;
                    view.setLayoutParams(layoutParams);
                    break;
                case MotionEvent.ACTION_DOWN:
                    lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;

                    //sledzenie:
                    //Pokazanie szerokosci kontrolki:
                    //tvInfo.setText(Integer.toString(view.getWidth()));

                    ((MojTV) view).setTextColor(RED); //zmiana koloru przeciaganej litery - kosmetyka

                    //action_down wykonuje sie (chyba) ZAWSZE, wiec zakladam:
                    ((MojTV) view).setInArea(false);
                    //policzInAreasy(); -> sledzenie
                    //a potem sie to ww. zmodyfikuje w action up....

                    break;
                case MotionEvent.ACTION_UP:

                    ((MojTV) view).setTextColor(Color.BLACK); //przywroceni koloru przeciaganej litery - kosmetyka

                    //sledzenie:
                    int Xstop = X;
                    tvInfo.setText("xKontrolki=" + Integer.toString(layoutParams.leftMargin));
                    tvInfo1.setText("xPalca=" + Integer.toString(Xstop));

                    /* Sprawdzenie, czy srodek etykiety jest w Obszarze; Jezeli tak - dosuniecie do lTrim. : */
                    //1.Policzenie wspolrzednych srodka Litery: (zakladam, ze srodek litery jest w srodku kontrolki o szer w i wys. h)
                    int w  = view.getWidth();
                    int lm = layoutParams.leftMargin;
                    int h = view.getHeight();
                    int tm = layoutParams.topMargin;

                    //srodek litery:
                    int xLit = lm + (int) (w/2.0);
                    int yLit = tm + (int) (h/2.0);
                    //2.Dosunirecie Litery na poziomy srodek Obszaru (linia yLtrim); srodek etykiety ma wypasc na yLtrim:
                    if ((yLit>yLg && yLit<yLd) && (xLit>xLl && xLit<xLp)) {
                        layoutParams.topMargin = yLtrim - (int) (h/2.0);  //odejmowanie zeby srodek etykiety wypadl na lTrim

                        //Bylo 'trimowanie' a wiec na pewno jestesmy w Obszarze- dajemy znac i badanie ewentualnego ZWYCIESTWA :
                        ((MojTV) view).setInArea(true);

                        if (policzInAreasy() == currWord.length()) {  //wszystkie litery wyrazu znalazly sie w Obszarze
                            if (poprawnieUlozono()) {
                                view.startAnimation(animShakeLong);  //ostatnio polozona litera podskoczy z 'radosci' - efekciarstwo
                                Zwyciestwo();
                            }
                            else
                              reakcjaNaBledneUlozenie();
                        }
                        //Wyraz jeszcze nie dokonczony, badamy, czy poprawna kolejnosc liter:
                        else {
                            String whatSeen = coWidacInObszar();
                            String mCurrWord = currWord;
                            if (toUp)
                              mCurrWord = currWord.toUpperCase(Locale.getDefault());

                            if (!mCurrWord.startsWith(whatSeen))
                              reakcjaNaBledneUlozenie();
                            else
                              view.startAnimation(animShakeLong);
                        }

                    }
                    //3.Jesli srodek litery zostala wyciagnieta za bande - dosuwam z powrotem:
                    if (xLit < xLl) {   //dosuniecie w prawo
                        //Toast.makeText(MainActivity.this, "Wyszedl za bande...", Toast.LENGTH_SHORT).show();
                        layoutParams.leftMargin = xLl - view.getPaddingLeft()+2 ; //dosuniecie w prawo
                        rootLayout.invalidate();
                        //Ponowne wywolanie eventa - spowoduje, ze wykona sie onTouch na tym samym view z zastanym (=ACTION_UP) eventem/parametrem, ale na innym polozeniu litery,
                        //litera bedzie w Obszarze i zostanie 'dotrimowana'"
                        view.dispatchTouchEvent(event); // Dispatch touch event to view
                    }
                    if (xLit > xLp) {   //dosuniecie w lewo
                        //Toast.makeText(MainActivity.this, "Wyszedl za bande...", Toast.LENGTH_SHORT).show();
                        layoutParams.leftMargin = xLp - w + view.getPaddingRight(); //dosuniecie w lewo
                        rootLayout.invalidate();
                        view.dispatchTouchEvent(event);
                    }
                    //3.Jezeli srodek litery za górnym lub dolnym brzegiem ekranu - dosuwam z powrotem:
                    if (yLit<0) {
                        //layoutParams.topMargin += Math.abs(layoutParams.topMargin);
                        layoutParams.topMargin = 0;
                    }
                    if (yLit>sizeH) {
                        layoutParams.topMargin = sizeH - (int) (0.7*h);
                    }

                    //sledzenie:
                    //tvInfo2.setText("xLit="+Integer.toString(xLit)+" yLit="+Integer.toString(yLit));
                    break;
                /*
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                */
            }
            rootLayout.invalidate();
            return true;
        }

        private void reakcjaNaBledneUlozenie() {
        /* Ostatnią wolną litere polozone źle; ewentualne 'brrr...' na klawiszu + 'shaking_short' animacja */

            //Potrzasniecie blednie ulozonymi literami:
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (MojTV lb : lbs) {
                        if (lb.isInArea())
                            lb.startAnimation(animShakeShort);
                    }
                }
            },150);

            if (mGlob.CISZA)
                return;
            odegrajZAssets("nagrania/komentarze/zle.mp3",20);
            if (mGlob.BEZ_KOMENT)
                return;
            odegrajZAssets("nagrania/komentarze/negatywy/male/nie-e2.m4a", 320);  //"y-y" męski glos dezaprobaty
        }
    } //koniec Metody()

    private void Zwyciestwo() {
    /* **************************************************************** */
    /* Dzialania po Zwyciestwie = poprawnym polozeniu ostatniej litery: */
    /* Porzadkowanie Obszaru, blokowanie klawiszy, dzwieki              */
    /* **************************************************************** */
        if (!mGlob.CISZA) odegrajZAssets("nagrania/komentarze/ding.mp3",10);
        dajNagrode(); //nagroda dzwiekowa (ewentualna)
        //
        //Zeby w (krotkim) czasie DELAY_ORDER nie mogl naciskac - bo problemy(!) :
        blokujKlawiszeDodatkowe();
        //
        //uporzadkowanie w Obszarze z lekkim opoznieniem:
        Handler mHandl = new Handler();
        mHandl.postDelayed(new Runnable() {
            @Override
            public void run() {
                uporzadkujObszar();
                 } }, DELAY_ORDER);
    }  //koniec Metody()

    private void dajNagrode() {
    /* ****************************** */
    /* nagroda dzwiekowa (ewentualna) */
    /* ****************************** */
        if (mGlob.CISZA) return;
        if (mGlob.BEZ_KOMENT) return;
        if (mGlob.TYLKO_OKLASKI) {
            odegrajZAssets("nagrania/komentarze/oklaski.ogg", 400);
            return;
        }

        //Teraz mamy pewnosc, ze to Glos [+Oklaski], losujemy plik z mową:
        String komcie_path = "nagrania/komentarze/pozytywy/female";
        //facet, czy kobieta:
        Random rand = new Random();
        int n = rand.nextInt(4); // Gives n such that 0 <= n < 4
        if (n == 3) komcie_path = "nagrania/komentarze/pozytywy/male"; //kobiecy glos 3 razy czesciej
        //teraz konkretny (losowy) plik:
        String doZagrania = dajLosowyPlik(komcie_path);

        odegrajZAssets(komcie_path + "/" + doZagrania, 400);    //pochwala glosowa

        if (mGlob.TYLKO_GLOS) return;

        //teraz Oklaski (bo to jeszcze pozostalo):
        odegrajZAssets("nagrania/komentarze/oklaski.ogg", 2900); //oklaski

    } //koniec Metody()

    private String dajLosowyPlik(String aktywa) {
        //Zwraca losowy plik z podanego katalogu w Assets; używana do generowania losowej pochwały/nagany

        String[] listaKomciow = null;
        AssetManager mgr = getAssets();
        try {
            listaKomciow = mgr.list(aktywa);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int licznosc = listaKomciow.length;
        int rnd = (int) (Math.random() * licznosc);
        int i = -1;
        String plik = null;
        for (String file : listaKomciow) {
            i++;
            if (i == rnd) {
                plik = file;
                break;
            }
        }
        return plik;
    } //koniec metody()

    private int dajLeftmostX() {
    //Daje wspolrzedną X najbardziej na lewo polozonej przez usera etykiety z Obszaru; pomocnicza

        int min = Integer.MAX_VALUE;
        for (MojTV lb : lbs) {
            if (lb.isInArea())
              if (lb.getLeft() < min) min = lb.getLeft();
        }
        return min;
    }



    private void uporzadkujObszar() {
    /* ******************************************************************************************* */
    /* Po Zwyciestwie:                                                                             */
    /* Gasimy wszysko (litery w obszarze); wyswietlamy zwycieski wyraz, przywracamy klawisz bDalej */
    /* Jesli trzeba - robimy korekcje miejsca wyswietlania (zeby wyraz sie miescil w Obszarze)     */
    /* Gasimy niektore klawisze pod Obszarem.                                                      */
    /* ******************************************************************************************* */

        //Przywracamy wielkosc letterSpacing, bo mogly byc zmienione przy b. dlugich wyrazach (length>10) o wielkich literach:
        restoreApplyLetterSpacing(tvShownWord);

        //Wyswietlenie wyrazu rozpoczynam od miejsca, gdzie user umiescil 1-sza litere (z ewentualnymi poprawkami):
        LinearLayout.LayoutParams lPar;
        lPar = (LinearLayout.LayoutParams) tvShownWord.getLayoutParams();
        int leftMost = dajLeftmostX();
        //Gdyby mialo wypasc troche przed poczatkiem lObszar'u:
        if (leftMost+lbs[0].getPaddingLeft() <= xLl)
            leftMost = xLl;

        lPar.leftMargin = leftMost;

        //ski ski ++
        // 2018.09.06 - kod poniżej zapewnia, ze tvShownWord zostanie pokazane "równo" z ulozonymi literami, nie będzie "podskoku"
        //Trudno wyelimimnowac ten 'podskok' w xml, ale kod ponizej wydaje sie troche 'nadmiarowy' i niepewny (lbs[0])
        //Sprawdzic na tablecie Marcina - tam widac wysokie 'skoki' ;)
        int h = lbs[0].getHeight(); //wys=sokosc litery (? czy aby na pewno -> wielka vs. mala)
        int lSrWz = (int) lObszar.getHeight()/2;  //linia Srodkowa Wzgledna (w przestrzeni wspolrzednych lObszar)
        ////rownowazne getHeightt90 -> int lSrWz = ((int) ((yLd-yLg)/2.0));
        lPar.topMargin = lSrWz - (int) (h/2.0) -6;  //odejmowanie zeby srodek etykiety wypadl na lTrim; -6 bo 'y' jest ucinane od dolu...
        //ski ski --

        tvShownWord.setLayoutParams(lPar);

        if (toUp) ewentualnieSciesnij();  //reakcja na b.dlugi wyraz wielkimi literami (>10)

        pokazWyraz();                     //w Obszarze pokazany zostaje ulozony wyraz (umieszczaam w tvSHownWord; + ewentualna korekcja polozenia)

        //Gasimy wszystkie etykiety:
        for (MojTV lb : lbs) { lb.setVisibility(INVISIBLE);}

        //Przywrocenie/pokazanie klawisza bDalej i bAgain1 oraz niektorych dodatkowych (z lekkim opoznieniem):
        Handler mHandl = new Handler();
        mHandl.postDelayed(new Runnable() {
            @Override
            public void run() {
                bDalej.setVisibility(VISIBLE);
                bAgain1.setVisibility(VISIBLE);
                if(mGlob.BUPLOW_ALL)
                  bUpperLower.setEnabled(true);
            } },2000); //zeby dziecko mialo czas na 'podziwianie' ;)

        //Animacja w 'nagrode':
        if (!mGlob.BEZ_OBRAZKOW) {
            Handler mHandl1 = new Handler();
            mHandl1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation a = AnimationUtils.loadAnimation(MainActivity.this, R.anim.obrot);
                    //Animation a = AnimationUtils.loadAnimation(MainActivity.this, R.anim.puls);
                    imageView.startAnimation(a);
                }
            }, DELAY_ORDER + DELAY_ORDER / 2);
        }

    } //koniec Metody()


    private void usunGrawitacje() {
    //Usuwa grawitacje z lObszar
        RelativeLayout.LayoutParams lPar = (RelativeLayout.LayoutParams) lObszar.getLayoutParams();
        lObszar.setGravity(Gravity.NO_GRAVITY);
        lObszar.setLayoutParams(lPar);
    }



    private void restoreApplyLetterSpacing(TextView mTV) {
    //Przywracamy wielkosc letterSpacing (po zmianie wymuszonej przez dlugi wyraz) + ewentualna reakcja na ustawienia globalne

        if (Build.VERSION.SDK_INT >= 21) {
            //float lspacing = getResources().getDimension(R.dimen.lspacing_ski); nie dziala, ustawiam na żywca....
            //tvShownWord.setLetterSpacing(lspacing);
            if (mGlob.ZE_SPACING) {
                if (mTV.length() >= MAXL && toUp) return; //za dlugich wielkich nie rozszerzamy, bo moga sie nie zmiescic na wielu urzadzeniach...
                mTV.setLetterSpacing((float) 0.1);   //UWAGA!!! - na "żywca"... patrz wyżej
                //!!! BARDZO WAZNE: !!!
                korygujJesliWystaje();
            }
            else {
                mTV.setLetterSpacing((float) 0.0);   //UWAGA!!! - na "żywca"... patrz wyżej
            }
        }
    }

    private void pokazWyraz() {
    //Pokazanie ulozonego wyrazu w Obszarze;
    //Wyraz skladam z tego, co widac na ekranie, nie uzywając currWord (bo problemy z duze/male litery)

      tvShownWord.setText(coWidacInObszar());

      //Kolor biore z etykiet, bo fabryczny jest troche za jasny... kosmetyka
      ColorStateList kolor = null;
      for (MojTV lb : lbs) {
        if (lb.isInArea()) {
          kolor = lb.getTextColors();
          break;
        }
      }

      tvShownWord.setTextColor(kolor);

      tvShownWord.setVisibility(VISIBLE);

      //!!! BARDZO WAZNE: !!!
      korygujJesliWystaje();

    } //koniec Metody()


    private void addGravityToParent() {
    //Dodanie grawitacji sciagajacej do prawego boku do lObszar;
    //Dzieki temu, ze mamy gwarancje, jezeli wyraz wystaje za lObszar, to zostanie "cofnięty"
    //i pokazany w całości w lObszar.

        RelativeLayout.LayoutParams lPar = (RelativeLayout.LayoutParams) lObszar.getLayoutParams();

        //"usuniecie" marginesu z TextView'a (bo mogl byc programowo ustawiony i w takim wypadku grawitacja by nie zadzialala):
        LinearLayout.LayoutParams lTV = (LinearLayout.LayoutParams) tvShownWord.getLayoutParams();
        lTV.leftMargin = 0;
        tvShownWord.setLayoutParams(lTV);

        //Teraz ustawienie grawitacji u parenta:
        lObszar.setGravity(Gravity.END);
        lObszar.setLayoutParams(lPar);
    } //koniec Metody()


    private void ewentualnieSciesnij() {
    //Jesli wyraz dluzszy niz 11, to sciesniam (jesli szeroki)
    //Sciesnianie jest API dependent, wiec badam.
    //Jezeli API<21 nie robie nic, bo taki wyraz nie jest sciesniony i na pewno(?) sie miesci....
    //Zakladam, ze wywolywana tylko, gdy duze litery; przy malych- wszystko sie miesci

        if (currWord.length()<MAXL) return;

        int versionINT = Build.VERSION.SDK_INT;

//        String manufacturer = Build.MANUFACTURER;
//        String model = Build.MODEL;
//        String versionRelease = Build.VERSION.RELEASE;
//
//        Log.e("MyActivity", "manufacturer " + manufacturer
//                + " \n model " + model
//                + " \n version " + versionINT
//                + " \n version " + versionREL
//                + " \n versionRelease " + versionRelease
//        );

        if (versionINT >= 21) tvShownWord.setLetterSpacing(0);
//
    } //koniec Metody()


    private boolean poprawnieUlozono() {
    /* **************************************** */
    /* zalozenie wejsciowe:                     */
    // Wszystkie litery znajduja sie w Obszarze */
    /* Sprawdzenie, czy poprawnie ulozone.      */
    /* **************************************** */

        String coUlozyl = coWidacInObszar();

        //Uwaga - nie nalezy podnosic do upperCase obydwu stron "równania" i porownywac bez warunku 'if' (jak ponizej) --> problemy (Mikolaj-Mikolaj):
        if (!toUp) {
            return coUlozyl.equals(currWord);
        }
        else {
            return coUlozyl.equals(currWord.toUpperCase(Locale.getDefault()));
        }

    } //koniec Metody();




    private int policzInAreasy() {
    //Zlicza, ile elementow znajduje sie aktualnie w Obszarze
        int licznik = 0;
        for (MojTV lb : lbs) {
            if (lb.isInArea()) licznik++;
        }
        return licznik;
    }


    @Override protected void onResume() {
    /* *************************************   */
    /* Aplikowanie zmian wprowadzonych w menu  */
    /* Bądż pierwsze uruchomienie (po splashu) */
    /* *************************************   */
        super.onResume();


        //Kwestia pierwszego wejscia:
        if (PW) {
            PW = false;
            return;
        }

        if (mGlob.PO_DIALOGU_MOD) {
            mGlob.PO_DIALOGU_MOD = false;
            odegrajWyraz(200);
        }

        //Pokazujemy cwiczenie z parametrami ustawionymi na Zmiennych Glob. (np. poprzez UstawieniaActivity):

        //Jezeli bez obrazkow - gasimy biezacy obrazek, z obrazkami - pokazujemy (gdyby byl niewidoczny):
        if (mGlob.BEZ_OBRAZKOW) {
            imageView.setVisibility(INVISIBLE);
            l_imageContainer.setBackgroundResource(R.drawable.border_skib_gray);
        }else {
            imageView.setVisibility(VISIBLE);
            l_imageContainer.setBackgroundColor(Color.TRANSPARENT);
            //l_imageContainer.setBackgroundResource(0); - alternatywa do Color.TRANSPARENT (podobno TRANSPARENT lepszy)
        }
        odblokujZablokujKlawiszeDodatkowe();    //pokaze/ukryje klawisze zgodnie z sytuacja na UstawieniaActivity = w obiekcie mGlob
        pokazUkryjNazwe();                      //j.w. - nazwa pod obrazkiem
        restoreApplyLetterSpacing(tvShownWord); //reakcja na ewentualna zmiane odstepu miedzy literami w ulozonym wyrazie

        //Badamy najistotniejsze opcje; Gdyby zmieniono Katalog lub poziom, to naczytanie na nowo:
        newOptions.pobierzZeZmiennychGlobalnych();           //jaki byl wynik ostatniej 'wizyty' w UstawieniaActivity
        if (!newOptions.takaSamaJak(currOptions)) {          //musimy naczytac ponownie, bo zmieniono zrodlo obrazkow (chocby poprzez zmiane poziomu trudnosci)
            currOptions.pobierzZeZmiennychGlobalnych();      //zapamietanie na przyszlosc
            if (!mGlob.ZRODLEM_JEST_KATALOG) {
                listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowAssets, mGlob.POZIOM); //nie trzeba tworzyc listy z Assets - jest stworzona raz na zawsze w onCreate()
            } else {
                tworzListeFromKatalog();
                listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowSD, mGlob.POZIOM);
            }
            //Gdyby okazalo sie, ze nie ma obrazkow o wybranym poziomie trudnosci, bierzemy wszystkie (list zrodlowych tworzyc w tym punkcie sterowania nie trzeba):
            if (listaOper.length == 0) {
                wypiszOstrzezenie("Brak ćwiczeń o wybranym poziomie trudności. Zostaną pokazane wszystkie ćwiczenia.");
                mGlob.POZIOM = WSZYSTKIE;
                currOptions.pobierzZeZmiennychGlobalnych();      //bo sie zmienily linie wyzej...
                if (!mGlob.ZRODLEM_JEST_KATALOG)
                    listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowAssets,mGlob.POZIOM);
                else
                    listaOper = listaOgraniczonaDoPoziomuTrudnosci(listaObrazkowSD, mGlob.POZIOM);
            }
            mPamietacz = new Pamietacz(listaOper); //nowa lista, wiec Pamietacz na nowo....
            bDalej.callOnClick();
        }

    } //koniec onResume()


    private void tworzListeFromKatalog() {
    //Tworzenie listy obrazków z Katalogu:

        katalogSD = new File(mGlob.WYBRANY_KATALOG);
        listaObrazkowSD = findObrazki(katalogSD);

    } //koniec Metody()


//    public static ArrayList<File> findObrazki(File katalog) {
    public static String[] findObrazki(File katalog) {
        /* ******************************************************************************************************************* */
        /* Zwraca liste-tablice obrazkow (plikow graficznych .jpg .bmp .png) z katalogu katalogAssets - uzywana tylko dla przypadku SD karty */
        /* ******************************************************************************************************************* */

//      Wersja ok, ale na <ArrayList<File>:

        ArrayList<File> al = new ArrayList<File>(); //al znaczy "Array List"
        File[] files = katalog.listFiles(); //w files WSZYSTKIE pliki z katalogu (rowniez nieporządane)
        if (files != null) { //lepiej sprawdzic, bo wali sie w petli for na niektorych emulatorach...
            for (File singleFile : files) {
                if ((singleFile.getName().toUpperCase().endsWith(".JPG"))
                        || (singleFile.getName().toUpperCase().endsWith(".PNG"))
                        || (singleFile.getName().toUpperCase().endsWith(".BMP"))
                        || (singleFile.getName().toUpperCase().endsWith(".WEBP"))
                        || (singleFile.getName().toUpperCase().endsWith(".JPEG"))) {
                        al.add(singleFile);
                }
            }
        }
        //return al;

        //Przepisanie na tablice stringow:

        String[] wynikowa = new String[al.size()];
        int i =0;
        for (File file : al) {
            wynikowa[i] = file.getName();
            i++;
        }

        return wynikowa;

/*
        List<String> zawartosc = new ArrayList<String>();
        File[] files = katalog.listFiles(); //w files WSZYSTKIE pliki z katalogu (rowniez nieporządane)
        if (files != null) { //lepiej sprawdzic, bo wali sie w petli for na niektorych emulatorach...
            for (File singleFile : files) {
                String plikPath = singleFile.getName();
                if ((plikPath.toUpperCase().endsWith(".JPG"))
                        || (plikPath.toUpperCase().endsWith(".PNG"))
                        || (plikPath.toUpperCase().endsWith(".BMP"))
                        || (plikPath.toUpperCase().endsWith(".WEBP"))
                        || (plikPath.toUpperCase().endsWith(".JPEG"))) {
                    zawartosc.add(plikPath);
                }
            }
        }

        return zawartosc;
*/

    }  //koniec Matody()


    private int dajLosowyNumerObrazka() {

        if (mGlob.ROZNICUJ_OBRAZKI) {
            return mPamietacz.dajSwiezyZasob();
        }
        //Nie korzystamy z Pamietacza:
        else {
            int rob;
            int rozmiar_tab = listaOper.length;
            //Generujemy losowy numer, ale tak, zeby nie wypadl ten sam:
            if (rozmiar_tab != 1) { //przy tylko jednym obrazku kod ponizej jest petla nieskonczona, więc if
                do {
                    rob = (int) (Math.random() * rozmiar_tab);
                } while (rob == currImage);
            } else
                rob = 0; //bo 0-to jest de facto numer obrazka
            return rob;  //105-rzeka 33=lalendarz
        }

    } //koniec Metody()



    private void przypiszLabelsyAndListenery() {

        L00 = (MojTV) findViewById(R.id.L00);
        L01 = (MojTV) findViewById(R.id.L01);
        L02 = (MojTV) findViewById(R.id.L02);
        L03 = (MojTV) findViewById(R.id.L03);
        L04 = (MojTV) findViewById(R.id.L04);
        L05 = (MojTV) findViewById(R.id.L05);
        L06 = (MojTV) findViewById(R.id.L06);
        L07 = (MojTV) findViewById(R.id.L07);
        L08 = (MojTV) findViewById(R.id.L08);
        L09 = (MojTV) findViewById(R.id.L09);
        L10 = (MojTV) findViewById(R.id.L10);
        L11 = (MojTV) findViewById(R.id.L11);

        //ustawienie tablicy do operowania na ww. etykietach:
        lbs = new MojTV[] {L00, L01, L02, L03, L04, L05, L06, L07, L08, L09, L10, L11};

        //podpiecie listenerow pod labelsy:
        for (MojTV lb : lbs) {
            lb.setOnTouchListener(new ChoiceTouchListener());
        }

        //Listenery na obszarze na Obrazek:
        l_imageContainer.setOnLongClickListener(this);
        l_imageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                odegrajWyraz(0);
            }
        });

    } //koniec Metody()


    private void dostosujDoUrzadzen() {
        RelativeLayout.LayoutParams lPar;

        //Pobieram wymiary ekranu na potrzeby dostosowania wielkosci Obrazka i Prostokata/Obszaru 'gorącego' do ekranu urządzenia:
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        //przekazanie na zewnatrz:
        sizeW = displaymetrics.widthPixels;
        sizeH = displaymetrics.heightPixels;

        //sledzenie - pokazania wymiarow urządzenia i rozdzielczosci dpi
        tvInfo3.setText(Integer.toString(sizeW) + "x" + Integer.toString(sizeH)+" dpi="+Integer.toString(displaymetrics.densityDpi));

        //Obrazek - ustawiam w lewym górnym rogu:
        lPar = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        lPar.width = sizeW / 3;
        lPar.height = sizeH / 2;
        lPar.topMargin = 5;
        lPar.leftMargin = 10;
        imageView.setLayoutParams(lPar);

        //nazwa pod obrazkiem - szerokosc jak Obrazek:
        tvNazwa.getLayoutParams().width = lPar.width;

        //Obszar-Prostokat na ukladanie wyrazu:
        RelativeLayout.LayoutParams lPar1 = (RelativeLayout.LayoutParams) lObszar.getLayoutParams();
        lPar1.topMargin = (int) (sizeH/1.6);
        lPar1.height = sizeH/4;
        lObszar.setLayoutParams(lPar1);

        //gestosc ektranu:
        density = getResources().getDisplayMetrics().densityDpi;

    } //koniec Metody()



    public static String getRemovedExtensionName(String name){
        /**
         * Pomocnicza, widoczna wszedzie metodka na pozbycie sie rozszerzenia z nazwy pliku - dostajemy "goly" wyraz
         */
        String baseName;
        if(name.lastIndexOf(".")==-1){
            baseName=name;
        }else{
            int index=name.lastIndexOf(".");
            baseName=name.substring(0,index);
        }
        return baseName;
    }  //koniec metody()

    public static String usunLastDigitIfAny(String name) {
        /**
         * Pomocnicza, widoczna wszedzie, usuwa ewentualna ostatnia cyfre w nazwie zdjecia (bo moze byc pies1.jpg, pies1.hjpg. pies2.jpg - rozne psy)
         * Zakladamy, ze dostajemy nazwe bez rozszerzenia i bez kropki na koncu
         */
        int koniec = name.length()-1;
        if (Character.isDigit(name.charAt(koniec))) {

            return name.substring(0,koniec);
        }
        else {

            return name;
        }
    } //koniec Metody()


    private boolean pokazModal() {

        if (!mGlob.POKAZ_MODAL) return true;

        //Pokazanie modalnego okienka.
        //Okienko realizowane jest jako Activity  o nazwie DialogModalny
        intModalDialog = new Intent(getApplicationContext(), DialogModalny.class);
        startActivity(intModalDialog);
        return true;
    }


    private void ustawLadnieEtykiety() {
        /* *************************************************************************************************** */
        /* Ustawiam Literki/Etykiety L0n wzgledem obrazka i wzgledem siebie - na lewo od obrazka               */
        /* Kazdy rząd (3 rzedy) ustawiam niejako osobno, poczynajac od 1-go elementu w rzedzie jako od wzorca. */
        /* *************************************************************************************************** */



        //wstawka dla duzych ekranow - powiekszam litery:
//        if (sizeW>1100) {
//            int litera_size = (int) getResources().getDimension(R.dimen.litera_size);
//            float wsp = 1.07f;
//            if (sizeW>1900) wsp = 1.2f;
//            for (MojTV lb : lbs) lb.setTextSize(TypedValue.COMPLEX_UNIT_PX, wsp*litera_size);
//            tvShownWord.setTextSize(TypedValue.COMPLEX_UNIT_PX, wsp*litera_size); //wyraz wyswietlany nie powinien roznic sie od liter
//        }


        final int odstepWpionie = yLg /4; //od gory ekranu do Obszaru sa 3 wiersze etykiet, wiec 4 przerwy

        int od_obrazka = (int) getResources().getDimension(R.dimen.od_obrazka); //odstep 1-szej litery 1-go rzedu od obrazka

        RelativeLayout.LayoutParams lPar;
        //L00 (1-szy rząd):
        lPar = (RelativeLayout.LayoutParams) L00.getLayoutParams();

        //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_1st_row);
        int marginesTop = 1*odstepWpionie - L00.getHeight() /2;  //*1 - bo 1-szy wiersz

        final int poprPion = 25;
        lPar.topMargin = marginesTop - poprPion;
        int poprPoziom = (rootLayout.getRight()-imageView.getRight())/MAXL;
        //troche w prawo, jesli dobre urzadzenie:

        if (sizeW>1100) poprPoziom = (int) (1.24*poprPoziom);

        lPar.leftMargin = imageView.getRight()-L00.getPaddingLeft() + poprPoziom;

        L00.setLayoutParams(lPar);

        final int poprawka = (int) getResources().getDimension(R.dimen.poprawka);
        //Toast.makeText(this,"poprawka: "+pxToDp(poprawka),Toast.LENGTH_SHORT).show();
        int lsize = (int) getResources().getDimension(R.dimen.litera_size);
        //Toast.makeText(this,"litera_size: "+pxToSp(lsize),Toast.LENGTH_SHORT).show();

        //L01:  //dalej trzeba uzywac Runnable - czekanie az obiekt L00 'usadowi' sie - inaczej wartosci nieustalobe, czyli ok. 0....
        L00.post(new Runnable() {
            @Override
            public void run() { //czekanie aż policzy/usadowi sie L01
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L01.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L00.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_1st_row);
                int marginesTop = 1*odstepWpionie  - L00.getHeight() /2 - poprPion;
                lParX.topMargin = marginesTop;
                L01.setLayoutParams(lParX); //n
            }
        });

        //L02:
        L01.post(new Runnable() {
            @Override
            public void run() { //czekanie aż policzy/usadowi się L01
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L02.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L01.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_1st_row);
                int marginesTop = 1*odstepWpionie  - L00.getHeight() /2 - poprPion;
                lParX.topMargin = marginesTop;
                L02.setLayoutParams(lParX); //n
            }
        });

        //L03:
        L02.post(new Runnable() {
            @Override
            public void run() { //czekanie aż policzy/usadowi się L02
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L03.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L02.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_1st_row);
                int marginesTop = 1*odstepWpionie - L00.getHeight() /2 - poprPion;
                lParX.topMargin = marginesTop;
                L03.setLayoutParams(lParX); //n
            }
        });


        //L04: (2-gi rząd):
        lPar = (RelativeLayout.LayoutParams) L04.getLayoutParams();
        lPar.leftMargin = ((RelativeLayout.LayoutParams) imageView.getLayoutParams()).leftMargin + imageView.getLayoutParams().width +
                od_obrazka/4;
        //marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_2nd_row);
        marginesTop = 2*odstepWpionie - L00.getHeight() /2; //2- bo 2-gi wiersz
        lPar.topMargin = marginesTop;
        lPar.leftMargin = imageView.getRight() + poprPoziom/4;
        L04.setLayoutParams(lPar);

        //L05
        L04.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L05.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L04.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_2nd_row);
                int marginesTop = 2*odstepWpionie - L00.getHeight() /2;
                lParX.topMargin = marginesTop;
                L05.setLayoutParams(lParX);
            }
        });

        //L06:
        L05.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L06.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L05.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_2nd_row);
                int marginesTop = 2*odstepWpionie - L00.getHeight() /2;
                lParX.topMargin = marginesTop;
                L06.setLayoutParams(lParX); //n
            }
        });

        //L07:
        L06.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L07.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L06.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_2nd_row);
                int marginesTop = 2*odstepWpionie - L00.getHeight() /2;
                lParX.topMargin = marginesTop;
                L07.setLayoutParams(lParX); //n
            }
        });


        //L08: (3-ci rząd):
        lPar = (RelativeLayout.LayoutParams) L08.getLayoutParams();
        lPar.leftMargin = ((RelativeLayout.LayoutParams) imageView.getLayoutParams()).leftMargin + imageView.getLayoutParams().width +
                od_obrazka/2;
        //marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_3rd_row);
        marginesTop = 3*odstepWpionie - L00.getHeight() /2; //3- bo 3-szy wiersz
        lPar.topMargin = marginesTop + poprPion;
        lPar.leftMargin = imageView.getRight()-L00.getPaddingLeft() + poprPoziom;
        L08.setLayoutParams(lPar);

        //L09:
        L08.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L09.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L08.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_3rd_row);
                int marginesTop = 3*odstepWpionie - L00.getHeight() /2 + poprPion;
                lParX.topMargin = marginesTop;
                L09.setLayoutParams(lParX); //n
            }
        });

        //L10:
        L09.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L10.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L09.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_3rd_row);
                int marginesTop = 3*odstepWpionie - L00.getHeight() /2 + poprPion;
                lParX.topMargin = marginesTop;
                L10.setLayoutParams(lParX); //n
            }
        });

        //L11:
        L10.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams lParX = (RelativeLayout.LayoutParams) L11.getLayoutParams();
                lParX.leftMargin = ((RelativeLayout.LayoutParams) L10.getLayoutParams()).leftMargin + poprawka;
                //int marginesTop = (int) getResources().getDimension(R.dimen.margin_top_size_3rd_row);
                int marginesTop = 3*odstepWpionie - L00.getHeight() /2 + poprPion;
                lParX.topMargin = marginesTop;
                L11.setLayoutParams(lParX); //n
            }
        });


        //Dodatkowe przemieszanie wyzej-nizej po kazdej etykiecie:
        for (final MojTV lb : lbs) {
            lb.post(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout.LayoutParams lParX =
                            (RelativeLayout.LayoutParams) lb.getLayoutParams();
                    Random rand = new Random();
                    int k = rand.nextInt(3);
                    if (k == 0) k =   0;
                    if (k == 1) k = +15;
                    if (k == 2) k = -15;

                    //Zmieniamy w 2-gim wierszu :
                    if (lb==lbs[4] || lb==lbs[5] || lb==lbs[6] || lb==lbs[7]) {
                        lParX.topMargin += k;
                    }
                    //Zmieniamy w 1-szym wierszu; w 1-szym wierszu pozwalam tylko w dol :
                    if (lb==lbs[0] || lb==lbs[1] || lb==lbs[2] || lb==lbs[3]) {
                        //w 1-szym wierszu zmieniamy tylko przy duzych gestosciach, inaczej główki liter wystają poza górną krawędź ekranu:
                        if (density > DisplayMetrics.DENSITY_MEDIUM) {
                            k = -Math.abs(k);
                            if (density>DisplayMetrics.DENSITY_HIGH)
                                k = 2*k;
                            lParX.topMargin += k;
                        }
                    }
                    //Zmieniamy w 3-cim wierszu :
                    if (lb==lbs[8] || lb==lbs[9] || lb==lbs[10] || lb==lbs[11]) { //w 3-cim wierszu pozwalam tylko w gore
                        k = Math.abs(k);
                        lParX.topMargin -= k;
                    }

                    lb.setLayoutParams(lParX);
                }
            });
        }

    }  //koniec Metody()

    private void ustawWymiaryKlawiszy() {
    //Wymiarowuje klawisze bDalej, bPomin, bAgain, bHint, bUpperLOwer
        //bDalej zajmuje przestrzen od gory do gornej krawedzi Obszaru, ale zostawia 2/3 swojej wysokosci miejsce na bAgain1:

        bDalej.getLayoutParams().height = (int) (0.66*yLg);
        bDalej.requestLayout();

        bAgain1.getLayoutParams().height = (int) (0.32*yLg);
        bAgain1.getLayoutParams().width = bDalej.getWidth();
        int lsize = (int) getResources().getDimension(R.dimen.litera_size); //30% rozmiaru liter-etykiet
        bAgain1.setTextSize(pxToSp(lsize/3));
        bAgain1.requestLayout();

        //cala przestrzen od dolnej krawedzi Obszaru do konca ekranu:
        bPomin.getLayoutParams().height = sizeH - yLd;
        bPomin.requestLayout();

        bAgain.getLayoutParams().height = sizeH - yLd;
        bAgain.requestLayout();

        bUpperLower.getLayoutParams().height = sizeH - yLd;
        bUpperLower.getLayoutParams().width  = 2*bAgain.getWidth();
        bUpperLower.requestLayout();

        bHint.getLayoutParams().height = sizeH - yLd;
        bHint.getLayoutParams().width  = (int) (1.5*bAgain.getWidth());
        bHint.requestLayout();


    } //koniec metody()


    private void odblokujZablokujKlawiszeDodatkowe() {
    //Pokazanie (ewentualne) klawiszy pod Obszarem"

        if (mGlob.BPOMIN_ALL) bPomin.setVisibility(VISIBLE);
        else bPomin.setVisibility(INVISIBLE);

        if (mGlob.BUPLOW_ALL) bUpperLower.setVisibility(VISIBLE);
        else bUpperLower.setVisibility(INVISIBLE);

        if (mGlob.BAGAIN_ALL) {
            bAgain.setVisibility(VISIBLE);
            bAgain.setText(R.string.bAgain_text);  //odtwarzam, bo Animacja mogla zaburzyc...
        }
        else bAgain.setVisibility(INVISIBLE);

        if (mGlob.BHINT_ALL) bHint.setVisibility(VISIBLE);
        else bHint.setVisibility(INVISIBLE);


        /*if (mGlob.BPOMIN_ALL)*/ bPomin.setEnabled(true);
        /*if (mGlob.BUPLOW_ALL)*/ bUpperLower.setEnabled(true);
        /*if (mGlob.BAGAIN_ALL)*/ bAgain.setEnabled(true);
        /*if (mGlob.BHINT_ALL) */ bHint.setEnabled(true);
    }


    public void bHintOnClick(View view) {
    /* Podpowiada kolejna litere do ulozenia */
    /* Idea algorytmu - iteruje po currWord i wskazuje 1sza litere nie na swoim miejscu w Obszarze */

        /* Wziete z Sylabowanki (Lazarus):
        (* Idea algorytmu : przegladam wyraz sylaba po sylabie (od lewej) i jezeli przegladana sylaba nie jest na swoim       *)
        (* miejscu w ramce, to wyrozniam ją (inaczej: pokazuję pierwszą sylabę, która nie jest na swoim miejscu)              *)
        (* Inne podejscia prowadzily do b. skomplikowanego algorytmu.                                                         *)
        */

        final char[] wyraz = currWord.toCharArray();       //bo latwiej operowac na Char'ach

        for (int i = 0; i < wyraz.length; i++) {
            char litera = wyraz[i];
            if (!jestGdzieTrzeba(litera,i)) {
                podswietlLabel(wyraz[i]);
                break;
            }
        }
        return;
    }  //koniec Metody()


    private boolean jestGdzieTrzeba(char litera, int pozycja) {
    /* Bada, czy przekazana 'litera' znajduje sie na pozycji 'pozycja' w Obszarze */

        String textInArea = coWidacInObszar();

        if (textInArea==null) //w Obszarze nic jeszcze nie ma
            return false;

        if (pozycja > (textInArea.length() - 1))   //text w Obszarze jest krotszy niz pozycja litery
            return false;

        char[] tChar = textInArea.toCharArray();

        //Litera w tekscie w Obszarze i litera w parametrach jako Stringi (do porownań):
        String litWtext = Character.toString(tChar[pozycja]);
        String litWpar  = Character.toString(litera);
        //Bedziemy porownywac przez upperCasy - bezpieczniej:
        litWtext = litWtext.toUpperCase(Locale.getDefault());
        litWpar  = litWpar.toUpperCase(Locale.getDefault());

        return (litWpar.equals(litWtext));

    }

    private String coWidacInObszar() {
    /* ********************************************************** */
    /* Zwraca w postaci Stringa to, co AKTUALNIE widac w Obszarze */
    /* ********************************************************** */

        MojTV[] tRob = new MojTV[MAXL];                //tablica robocza, do dzialań
        //Wszystkie z Obszaru odzwierciedlam w tRob:
        int licznik = 0;                               //po wyjsciu z petli bedzie zawieral liczbe liter w Obszarze
        for (MojTV lb : lbs) {
            if (lb.isInArea()) {
                tRob[licznik] = lb;
                licznik++;
            }
        }

        if (licznik==0) return null;

        //Sortowanie (babelkowe) tRob względem lewej wspolrzednej:
        MojTV elRob = new MojTV(this);    //element roboczy
        boolean bylSort = true;
        while (bylSort) {
            bylSort = false;
            for (int j = 0; j < licznik-1;  j++) {
                if (tRob[j].getX() > tRob[j+1].getX()) {
                    elRob     = tRob[j+1];
                    tRob[j+1] = tRob[j];
                    tRob[j]   = elRob;
                    bylSort = true;
                }
            }
        }  //wile

        //Wypakowanie do Stringa i zwrot na zewnatrz:
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<licznik; i++) {
            //sb.append(tRob[i].getOrigL());
            sb.append(tRob[i].getText());
        }
        String coWidac = sb.toString();
        return coWidac;
    } //koniec Metody()


    private void podswietlLabel(char c) {
    //Podswietla pierwsza napotkana etykiete zawierajaca znak z parametru */

        String coDostalem = String.valueOf(c);

        for (MojTV lb : lbs) {
            if (!lb.equals("*")) {
                String etyk = lb.getOrigL();
                if (etyk.equals(coDostalem) && !lb.isInArea()) {  //tylko mrugamy poza Obszarem - inaczej niejednznacznosci....

                    //lb.blink(5); -- inna wersja
                    lb.makeMeBlink(400,5,4, RED);

                    return;
                }
            }
        }

        //Nie mrugnal litera spoza Obszaru, zatem walę po calym Obszarze, bo ulozono 'kaszanę' i trzeba jakos dac znac:
        //koniecznie odblokowuję bAgain (jesli zablokowany)....:
        bAgain.setEnabled(true);
        bAgain.setVisibility(VISIBLE);
        bAgain.setText(R.string.bAgain_text);
        makeMeBlink(bAgain,400,5,10,Color.BLUE);  //... i sugeruję, zeby to nacisnal
        for (MojTV lb : lbs) {
            if (lb.isInArea()) {
                lb.makeMeBlink( 400,5,4, Color.BLUE);
            }
        }

    }  //koniec Metody()


    /**
     * Make a View Blink for a desired duration
     *
     * @param obiekt   Button we blink the text on
     * @param duration for how long in ms will it blink
     * @param offset   start offset of the animation
     * @param ileRazy  ile razy ma mrugnac
     * @param kolor    jakim kolorem ma mrugac
     * zrodlo: https://gist.github.com/cesarferreira/4fcae632b18904035d3b
     * slaby punk: text na buttonie traci "dziewictwo" i pozostaje potem nie do ruszenia... - dlatego w innych punktach kodu niszcze go i regeneruję
     */

    private static void makeMeBlink(Button obiekt, int duration, int offset, int ileRazy, int kolor) {

        final int savedColor = obiekt.getCurrentTextColor();

        obiekt.setTextColor(kolor);
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(duration);
        anim.setStartOffset(offset);
        anim.setRepeatMode(Animation.REVERSE);
        //anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatCount(ileRazy);
        obiekt.startAnimation(anim);

        //Przywrocenie pierwotnego koloru klawiszowi po skonczonej animacji:
        final Button finalB = obiekt;
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                finalB.setTextColor(savedColor);
            }
        },duration*(ileRazy+2)+offset);  //wyr. arytm. - doswiadczalnie....


    } //koniec Metody()


    @Override
    protected void onDestroy() {
    /* Zapisanie (niektorych!) ustawienia w SharedPreferences na przyszła sesję */
        super.onDestroy();
        ZapishDoSharedPreferences();
    } //onDestroy

    private void ZapishDoSharedPreferences() {
    /* Zapisanie (niektorych!) ustawienia w SharedPreferences na przyszła sesję */

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor edit = sharedPreferences.edit();


        edit.putBoolean("Z_NAZWA",    mGlob.Z_NAZWA);
        edit.putBoolean("ZE_SPACING", mGlob.ZE_SPACING);

        edit.putInt("POZIOM", mGlob.POZIOM);

        edit.putBoolean("BEZ_DZWIEKU", mGlob.BEZ_DZWIEKU);

        edit.putBoolean("GLOS_OKLASKI",  mGlob.GLOS_OKLASKI);
        edit.putBoolean("BEZ_KOMENT",    mGlob.BEZ_KOMENT);
        edit.putBoolean("TYLKO_OKLASKI", mGlob.TYLKO_OKLASKI);
        edit.putBoolean("TYLKO_GLOS",    mGlob.TYLKO_GLOS);
        edit.putBoolean("CISZA",         mGlob.CISZA);

        edit.putBoolean("BHINT_ALL",   mGlob.BHINT_ALL);
        edit.putBoolean("BPOMIN_ALL",  mGlob.BPOMIN_ALL);
        edit.putBoolean("BUPLOW_ALL",  mGlob.BUPLOW_ALL);
        edit.putBoolean("BAGAIN_ALL",  mGlob.BAGAIN_ALL);

        edit.putBoolean("ODMOWA_DOST", mGlob.ODMOWA_DOST);

        edit.putBoolean("ROZNICUJ_OBRAZKI", mGlob.ROZNICUJ_OBRAZKI);

        edit.putBoolean("ZRODLEM_JEST_KATALOG", mGlob.ZRODLEM_JEST_KATALOG);
        edit.putString("WYBRANY_KATALOG", mGlob.WYBRANY_KATALOG);

        edit.apply();
    }


    private Bitmap obrocJesliTrzeba(Bitmap bitmap, String sciezkaDoPliku) {
    //Wykrywa(?) orientacje obrazka i ewentualnie obraca obrazek pobrany z dysku tak aby byl pokazany prawidłowo

        ExifInterface ei = null;
        try {
            ei = new ExifInterface(sciezkaDoPliku);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;

        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }

        return rotatedBitmap;

    } //koniec Metody();


    private Bitmap rotateImage(Bitmap source, float angle) {
        //Wyrzucic Toasta
        //Toast.makeText(this, "Będzie Obrót "+angle, Toast.LENGTH_LONG).show();

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    } //koniec Metody()



    /*Klasa do sprawdzania czy podczas zmiany ustawien uzytkownik zmienil (klikaniem) zrodlo obrazkow */
    /* (lub wykonal dzialanie rownowazne zmianie zrodla) */
    /* dotyczy (na razie) tylko zródła i ścieżki */
    class KombinacjaOpcji {

        private boolean ZRODLEM_JEST_KATALOG;
        private String  WYBRANY_KATALOG;
        private int     POZIOM;

        KombinacjaOpcji() {
            pobierzZeZmiennychGlobalnych();
        }

        void pobierzZeZmiennychGlobalnych() {
            ZRODLEM_JEST_KATALOG = mGlob.ZRODLEM_JEST_KATALOG;
            WYBRANY_KATALOG = mGlob.WYBRANY_KATALOG;
            POZIOM = mGlob.POZIOM;
        }

        /*Sprawdza, czy kombinacje wybranych opcji sa takie same*/
        boolean takaSamaJak(KombinacjaOpcji nowaKombinacja) {
            if (this.ZRODLEM_JEST_KATALOG != nowaKombinacja.ZRODLEM_JEST_KATALOG)
                return false;
            if (!this.WYBRANY_KATALOG.equals(nowaKombinacja.WYBRANY_KATALOG))
                return  false;
            if (!(this.POZIOM == nowaKombinacja.POZIOM)) {
                return false;
            }
            return true;
        }
    } //class wewnetrzna



    private void wypiszOstrzezenie(String tekscik) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder1.setMessage(tekscik);
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    } //koniec Metody()


    public int dpToPx(int dp) {
    //Convert dp to pixel:
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }


    public int pxToDp(int px) {
    //Convert pixel to dp:
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public  int pxToSp(int px) {
        //Convert pixel to sp:
        float scaledDensity = this.getResources().getDisplayMetrics().scaledDensity;
        return Math.round(px /scaledDensity);
    }



}

