package autyzmsoft.pl.literowiec;


/**
 * Zawiera ekran z Ustawieniami. Wywolywana na long toucha na obrazku.
 * Dawniej (w innych apkach) pod nazwą  'SplashKlasa'
 */
import static autyzmsoft.pl.literowiec.ZmienneGlobalne.LATWE;
import static autyzmsoft.pl.literowiec.ZmienneGlobalne.SREDNIE;
import static autyzmsoft.pl.literowiec.ZmienneGlobalne.TRUDNE;
import static autyzmsoft.pl.literowiec.ZmienneGlobalne.WSZYSTKIE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;



public class UstawieniaActivity extends Activity implements View.OnClickListener{

  public static final int REQUEST_CODE_WRACAM_Z_APKA_INFO = 222;

  RadioButton rb_zAssets;
  RadioButton rb_zKatalogu;

  CheckBox cb_Podp;
  CheckBox cb_Pomin;
  CheckBox cb_Nazwa;
  CheckBox cb_UpLo;
  CheckBox cb_Again;
  CheckBox cb_Spacing;
  CheckBox cb_RoznObr;

  RadioButton rb_NoPictures;
  RadioButton rb_NoSound;
  RadioButton rb_SoundPicture;

  RadioButton rb_Latwe;
  RadioButton rb_Srednie;
  RadioButton rb_Trudne;
  RadioButton rb_Wszystkie;

  RadioButton rb_NoComments;
  RadioButton rb_TylkoOklaski;
  RadioButton rb_TylkoGlos;
  RadioButton rb_Cisza;
  TextView sciezka; //informacyjny teksci pokazujacy biezacy katalogAssets i/lub liczbe obrazkow
  
  ZmienneGlobalne mGlob;


  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    //pobranie zmiennych globalnych (ustawien):
    mGlob = (ZmienneGlobalne) getApplication();
    
    //Uwaga - wywoluje sie rowniez po wejsciu z MainActivity przez LongClick na obrazku(!)
    //na caly ekran:
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);


    setContentView(R.layout.activity_ustawienia);

    ustawKontrolki(); //kontrolki <-- ZmienneGlobalne

    //Jesli wysylam do Testerow, to ukrywam info o www:
    if (mGlob.DLA_KRZYSKA) {
      findViewById(R.id.bInfo).setOnClickListener(new OnClickListener() {
        public void onClick(View view) {
          Toast.makeText(UstawieniaActivity.this, "Jeszcze nie zaimplementowane", Toast.LENGTH_LONG).show();
        }
      });
    }


  }  //koniec Metody()




  @Override
  protected void onPause() {
    //*******************************************//
    //Przekazanie ustawien na --> ZmienneGlobalne//
    //*******************************************//
    super.onPause();

    //Przekazanie checkboxow:
    boolean isCheckedPodp    = cb_Podp.isChecked();
    boolean isCheckedPomin   = cb_Pomin.isChecked();
    boolean isCheckedNazwa   = cb_Nazwa.isChecked();
    boolean isCheckedUpLo    = cb_UpLo.isChecked();
    boolean isCheckedAgain   = cb_Again.isChecked();
    boolean isCheckedRozObr  = cb_RoznObr.isChecked();
    boolean isCheckedSpacing = cb_Spacing.isChecked();
    mGlob.BHINT_ALL        = isCheckedPodp;
    mGlob.BPOMIN_ALL       = isCheckedPomin;
    mGlob.Z_NAZWA          = isCheckedNazwa;
    mGlob.BUPLOW_ALL       = isCheckedUpLo;
    mGlob.BAGAIN_ALL       = isCheckedAgain;
    mGlob.ROZNICUJ_OBRAZKI = isCheckedRozObr;
    mGlob.ZE_SPACING       = isCheckedSpacing;

    //Przekazanie Poziomu trudnosci:
    boolean isCheckedLatwe     = rb_Latwe.isChecked();
    boolean isCheckedSrednie   = rb_Srednie.isChecked();
    boolean isCheckedTrudne    = rb_Trudne.isChecked();
    boolean isCheckedWszystkie = rb_Wszystkie.isChecked();
    if (isCheckedLatwe)     mGlob.POZIOM = LATWE;
    if (isCheckedSrednie)   mGlob.POZIOM = SREDNIE;
    if (isCheckedTrudne)    mGlob.POZIOM = TRUDNE;
    if (isCheckedWszystkie) mGlob.POZIOM = WSZYSTKIE;


    //Komentarze/Nagrody:
    boolean isCheckedNoComments  = rb_NoComments.isChecked();
    boolean isCheckedTylOkl      = rb_TylkoOklaski.isChecked();
    boolean isCheckedTylGlos     = rb_TylkoGlos.isChecked();
    boolean isCheckedCisza       = rb_Cisza.isChecked();
    mGlob.BEZ_KOMENT    = isCheckedNoComments;
    mGlob.TYLKO_OKLASKI = isCheckedTylOkl;
    mGlob.TYLKO_GLOS    = isCheckedTylGlos;
    mGlob.CISZA         = isCheckedCisza;


    //Kwestia bez obrazków/bez dźwieku - tutaj trzeba uważać, żeby nie wyszło coś bez sensu i nie bylo crashu:
    boolean isCheckedNoPictures = rb_NoPictures.isChecked();
    boolean isCheckedNoSound    = rb_NoSound.isChecked();
    if (!isCheckedNoPictures && !isCheckedNoSound) { //z obrazkiem i dzwiekiem
      mGlob.BEZ_OBRAZKOW = false;
      mGlob.BEZ_DZWIEKU  = false;
    } else {
      if (isCheckedNoPictures) {  //bez obrazkow (ale musimy zapewnic dzwiek no matter what...)
        mGlob.BEZ_OBRAZKOW = true;
        mGlob.BEZ_DZWIEKU  = false;
      } else {
        if (isCheckedNoSound) {  //bez dzwieku (ale musimy zapewnic obrazki no matter what..)
          mGlob.BEZ_OBRAZKOW = false;
          mGlob.BEZ_DZWIEKU  = true;
        } else { //na wszelki wypadek...
          mGlob.BEZ_OBRAZKOW = false;
          mGlob.BEZ_DZWIEKU  = false;
        }
      }
    }
    //
  } //koniec Metody()


  @Override
  protected void onResume() {
    /* ******************************************************************************************/
    /* Na ekranie (splashScreenie) pokazywane sa aktualne ustawienia.                           */
    /* Wywolywana (nie bezposrednio, ale jako skutek) na long touch na obrazku - wtedy          */
    /* przywolywana jest UstawieniaActivity z pokazanymi ustawieniami -> MainActivity.onLOngClick */
    /* Wywolywana rowniez przy starcie aplikacji(!)                                             */
    /* **************************************************************************************** */
    super.onResume();
    this.ustawKontrolki();


    //Ponizszy kod istotny przy konczeniu wyboru katalogu zewnetrznego (ale wywola sie tez na onCreate):
    if (mGlob.ZRODLEM_JEST_KATALOG) {
      String strKatalog = mGlob.WYBRANY_KATALOG;
      int liczbaObrazkow = policzObrazki(strKatalog);  //informacyjnie i po ostrzezenie
      if (liczbaObrazkow>0) {
        if (!mGlob.PELNA_WERSJA) {
          if (liczbaObrazkow>5) {  //werja Demo, a wybrano katalogAssets z wiecej niz 5 obrazkami
            ostrzegajPowyzej5();
            //przywrócenie wyboru 'domyslnego' - z zasobów aplikacji:
            onClick(rb_zAssets);
            rb_zAssets.setChecked(true);
          }
          else { //wersja Demo, wybór OK
            toast("Liczba obrazków: " + liczbaObrazkow);
            rb_zKatalogu.setChecked(true);
            sciezka.setText(strKatalog+"   "+liczbaObrazkow+" szt.");
          }
        }
        else {  //wersja Pełna, wybór OK
          toast("Liczba obrazków: " + liczbaObrazkow);
          rb_zKatalogu.setChecked(true);
          sciezka.setText(strKatalog+"   "+liczbaObrazkow+" szt.");
        }
      }
      else { //nie ma obrazkow w wybranym katalogu (dot. werski Pelnej i Demo)
        ostrzegajBrakObrazkow();
        //przywrócenie wyboru 'domyslnego' - z zasobów aplikacji:
        onClick(rb_zAssets);
        rb_zAssets.setChecked(true);
      }
    }
    else { //wybrano zasoby aplikacji
      rb_zAssets.setChecked(true);
      int liczbaObrazkow = MainActivity.listaObrazkowAssets.length;
      sciezka.setText(liczbaObrazkow+" szt.");
    }

  } //onResume - koniec


  public void bStartClick(View v) {
    //Przejscie do MainActivity
    //i wywola sie onPause... :)
    finish();
  }



  public void bInfoClick(View v) {
    //Toast.makeText(this, "Jeszcze nie zaimplementowane...", Toast.LENGTH_SHORT).show();
    Intent intent = new Intent(getApplicationContext(), ApkaInfo.class);
    this.startActivityForResult(intent, REQUEST_CODE_WRACAM_Z_APKA_INFO);
  }


  private void przywrocUstDomyslne() {
    /**
     * Przywrócenie domyślnych ustawien aplikacji.
     */

    mGlob.ustawParametryDefault();
    this.ustawKontrolki();

/*
    cb_Podp.setChecked(false);
    cb_Pomin.setChecked(false);
    cb_Nazwa.setChecked(false);
    cb_UpLo.setChecked(true);
    cb_Again.setChecked(false);

    rb_NoPictures.setChecked(false);
    rb_NoSound.setChecked(false);
    rb_TylkoOklaski.setChecked(false);
    rb_TylkoGlos.setChecked(false);
    rb_NoComments.setChecked(false);

    //inicjacja, bo tego nie ma w skladowych klasy:
    RadioButton rb_SoundPicture = (RadioButton) findViewById(R.id.rb_SoundPicture);
    rb_SoundPicture.setChecked(true);
    //inicjacja, bo tego nie ma w skladowych klasy:
    RadioButton rb_GlosOklaski = (RadioButton) findViewById(R.id.rb_GlosOklaski);
    rb_GlosOklaski.setChecked(true);

    mGlob.POZIOM = 4;
    */

  }


  private Dialog createAlertDialogWithButtons() {
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    dialogBuilder.setTitle("Przywracanie ustawień domyślnych");
    dialogBuilder.setMessage("Czy przywrócić domyślne ustawienia?");
    dialogBuilder.setCancelable(true); //czy można wychodzić przez esc
    dialogBuilder.setPositiveButton("Tak", new Dialog.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int whichButton) {
        przywrocUstDomyslne();
        toast("Przywrócono domyślne....");
      }
    });
    dialogBuilder.setNegativeButton("Nie", new Dialog.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int whichButton) {
        //nie robimy nic - powrot z dialogu ; toast("You picked negative button");
      }
    });
    return dialogBuilder.create();
  }

  public void bDefaultClick(View v) {
    //toast("bDefaultClick");
    Dialog zapytanie;
    zapytanie = createAlertDialogWithButtons();
    zapytanie.show();
  }


  private void ostrzegajBrakObrazkow(){
    /* **************************************************************** */
    /* Wyswietlany, gdy user wybierze katalog nie zawierajacy obrazkow. */
    /* **************************************************************** */
    wypiszOstrzezenie("Brak obrazków w wybranym katalogu.\nZostanie zastosowany wybór\nz zasobów aplikacji.");
  }


  private void ostrzegajPowyzej5() {
    /* ************************************************************************************ */
    /* Wyswietlany, gdy user wybierze katalogAssets z wiecej niz 5 obrazkami, a wersja jest Demo. */
    /* ************************************************************************************ */
    wypiszOstrzezenie("Uwaga - używasz wersji Demonstracyjnej.\nWybrano katalogAssets zawierający więcej niż 5 obrazków.\nZostanie przywrócony wybór\nz zasobów aplikacji.");
  }


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

  public void onClick(View arg0) {
    /* ********************************************************************************************** */
    /* Obsluga klikniec na radio buttony 'Obrazki z zasobow aplikacji', 'Obrazki z wlasnego katalogu' */
    /* ********************************************************************************************** */

    if (arg0==rb_zAssets) {
      sciezka.setText(""); //kosmetyka - znika z ekranu
      //jesli kliknieto na "z zasobow aplikacji", to przełączam się na to nowe źródło:
      mGlob.ZRODLEM_JEST_KATALOG = false;
      //policzenie obrazkow w zasobach aplikacji (zeby uswiadomic usera...):
      int liczba = MainActivity.listaObrazkowAssets.length;
      sciezka.setText(liczba+" szt.");
      toast("Liczba obrazków: "+liczba);
      return;
    }

    if (arg0==rb_zKatalogu) {

      if (mGlob.ODMOWA_DOST) {
        wypiszOstrzezenie("Opcja nieaktywna. Odmówiłeś dostępu do katalogów na urządzeniu.");
        rb_zAssets.setChecked(true);
        return;
      }

      /*Wywolanie activity do wyboru miedzy karta zewnetrzna SD, a pamiecia urzadzenia:*/

      if (mGlob.PELNA_WERSJA) {
        Intent intent = new Intent(this, InternalExternalKlasa.class);
        this.startActivity(intent);
      }
      //Wersja demo::
      else {
        Intent intent = new Intent(this, WersjaDemoOstrzez.class);
        this.startActivity(intent); //w srodku zostanie wywolana InternalExternalKlasa
      }

      return;
    }
  } //koniec Metody()


  static int policzObrazki(String strKatalog) {
    /* ******************************************************** */
    /* Liczy obrazki (=pliki .jpg .bmp .png) w zadanym katalogu */
    /* zwraca po prostu rozmiar tablicy                         */
    /* ******************************************************** */

    return MainActivity.findObrazki(new File(strKatalog)).length;

  } //koniec Metody()


  private void toast(String napis) {
    Toast.makeText(getApplicationContext(),napis,Toast.LENGTH_SHORT).show();
  }


  private void ustawKontrolki() {
    /*******************************************************************************************/
    //Ustawienie kontrolek na layoucie splash.xml na wartosci inicjacyjne ze ZmiennychGlobalnych
    /*******************************************************************************************/

    cb_RoznObr = (CheckBox) findViewById(R.id.cb_RoznicujObrazki);
    boolean isChecked = mGlob.ROZNICUJ_OBRAZKI;
    cb_RoznObr.setChecked(isChecked);

    cb_Spacing = (CheckBox) findViewById(R.id.cb_Spacing);
    isChecked  = mGlob.ZE_SPACING;
    cb_Spacing.setChecked(isChecked);

    cb_Podp = (CheckBox) findViewById(R.id.cb_Podp);
    isChecked = mGlob.BHINT_ALL;
    cb_Podp.setChecked(isChecked);

    cb_Pomin  = (CheckBox) findViewById(R.id.cb_Pomin);
    isChecked = mGlob.BPOMIN_ALL;
    cb_Pomin.setChecked(isChecked);

    cb_Nazwa  = (CheckBox) findViewById(R.id.cb_Nazwa);
    isChecked = mGlob.Z_NAZWA;
    cb_Nazwa.setChecked(isChecked);

    cb_UpLo  = (CheckBox) findViewById(R.id.cb_UpLo);
    isChecked = mGlob.BUPLOW_ALL;
    cb_UpLo.setChecked(isChecked);

    cb_Again  = (CheckBox) findViewById(R.id.cb_Again);
    isChecked = mGlob.BAGAIN_ALL;
    cb_Again.setChecked(isChecked);

    /* zobrazowanie: */
    rb_NoPictures = (RadioButton) findViewById(R.id.rb_noPicture);
    isChecked     = mGlob.BEZ_OBRAZKOW;
    rb_NoPictures.setChecked(isChecked);

    rb_NoSound = (RadioButton) findViewById(R.id.rb_noSound);
    isChecked  = mGlob.BEZ_DZWIEKU;
    rb_NoSound.setChecked(isChecked);

    rb_SoundPicture = (RadioButton) findViewById(R.id.rb_SoundPicture);
    isChecked = (!mGlob.BEZ_OBRAZKOW && !mGlob.BEZ_DZWIEKU);
    rb_SoundPicture.setChecked(isChecked);
    /* zobrazowanie - koniec */


    /* poziomy trudnosci: */
    rb_Latwe     = (RadioButton) findViewById(R.id.rb_latwe);
    isChecked    = (mGlob.POZIOM == LATWE);
    rb_Latwe.setChecked(isChecked);

    rb_Srednie   = (RadioButton) findViewById(R.id.rb_srednie);
    isChecked    = (mGlob.POZIOM == SREDNIE);
    rb_Srednie.setChecked(isChecked);

    rb_Trudne    = (RadioButton) findViewById(R.id.rb_trudne);
    isChecked    = (mGlob.POZIOM == TRUDNE);
    rb_Trudne.setChecked(isChecked);

    rb_Wszystkie = (RadioButton) findViewById(R.id.rb_wszystkie);
    isChecked    = (mGlob.POZIOM == WSZYSTKIE);
    rb_Wszystkie.setChecked(isChecked);
    /* poziomy trudnosci - koniec */


    /* nagrody: */
    rb_NoComments = (RadioButton) findViewById(R.id.rb_No_Comments);
    isChecked = mGlob.BEZ_KOMENT;
    rb_NoComments.setChecked(isChecked);

    rb_TylkoOklaski = (RadioButton) findViewById(R.id.rb_TylkoOklaski);
    isChecked = mGlob.TYLKO_OKLASKI;
    rb_TylkoOklaski.setChecked(isChecked);

    rb_TylkoGlos = (RadioButton) findViewById(R.id.rb_TylkoGlos);
    isChecked = mGlob.TYLKO_GLOS;
    rb_TylkoGlos.setChecked(isChecked);

    rb_Cisza = (RadioButton) findViewById(R.id.rb_Cisza);
    isChecked = mGlob.CISZA;
    rb_Cisza.setChecked(isChecked);
    /* nagrody - koniec */



    rb_zAssets = (RadioButton) findViewById(R.id.rb_zAssets);
    isChecked  = !mGlob.ZRODLEM_JEST_KATALOG;
    rb_zAssets.setChecked(isChecked);
    rb_zAssets.setOnClickListener(this);

    rb_zKatalogu = (RadioButton) findViewById(R.id.rb_zKatalogu);
    isChecked    = mGlob.ZRODLEM_JEST_KATALOG;
    rb_zKatalogu.setChecked(isChecked);
    rb_zKatalogu.setOnClickListener(this);


    //Wypisanie ewentualnej sciezki i liczby obrazkow:
    sciezka = (TextView) findViewById(R.id.tv_sciezkaKatalog);
    if (mGlob.ZRODLEM_JEST_KATALOG) {
      int liczba = policzObrazki(mGlob.WYBRANY_KATALOG);
      String strLiczba = Integer.toString(liczba);
      sciezka.setText(mGlob.WYBRANY_KATALOG+"   "+strLiczba+" szt.");
    }
    else {
      int liczba = MainActivity.listaObrazkowAssets.length;
      String strLiczba = Integer.toString(liczba);
      sciezka.setText(strLiczba+" szt.");
    }
  } //koniec Metody()



  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_CODE_WRACAM_Z_APKA_INFO) {
      //toast("Wrocilem z apkaInfo");
      if (resultCode == Activity.RESULT_OK) { //to musi byc na wypadek powrotu przez klawisz Back (zeby kod ponizej sie nie wykonal, bo error..)
        String message = data.getStringExtra("MESSAGE");
        if (message.equals("KL_START"))
          this.finish();
      }
    }
    //toast(Integer.toString(resultCode));
  } //koniec metody()


}
