package autyzmsoft.pl.literowiec;

import static autyzmsoft.pl.literowiec.ZmienneGlobalne.LATWE;
import static autyzmsoft.pl.literowiec.ZmienneGlobalne.SREDNIE;
import static autyzmsoft.pl.literowiec.ZmienneGlobalne.TRUDNE;
import static autyzmsoft.pl.literowiec.ZmienneGlobalne.WSZYSTKIE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
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
import java.io.IOException;

/**
 * Zawiera ekran z Ustawieniami. Wywolywana na long toucha na obrazku.
 * Dawniej (w innych apkach) pod nazwą  'SplashKlasa'
 */


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

  RadioButton rb_GlosOklaski;
  RadioButton rb_NoComments;
  RadioButton rb_TylkoOklaski;
  RadioButton rb_TylkoGlos;
  CheckBox    cb_Dezap;

  CheckBox cb_ImgTurnEf;
  CheckBox cb_WordShakeEf;
  CheckBox cb_LetterHoppEf;
  CheckBox cb_SndErrEf;
  CheckBox cb_SndOKEf;
  CheckBox cb_SndVictEf;

  CheckBox cb_ang;


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
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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

    //Jezeli starsza wersja systemu, to wygaszamy Spacing, bo nie obslugiwany przez API:
    if (Build.VERSION.SDK_INT < 21) {
        cb_Spacing.setVisibility(View.GONE);
        ((TextView) findViewById(R.id.tv_Spacing)).setVisibility(View.GONE);
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
    boolean isCheckedGlosOkl     = rb_GlosOklaski.isChecked();
    boolean isCheckedTylOkl      = rb_TylkoOklaski.isChecked();
    boolean isCheckedTylGlos     = rb_TylkoGlos.isChecked();
    boolean isCheckedDezap       = cb_Dezap.isChecked();
    mGlob.BEZ_KOMENT    = isCheckedNoComments;
    mGlob.TYLKO_OKLASKI = isCheckedTylOkl;
    mGlob.GLOS_OKLASKI  = isCheckedGlosOkl;
    mGlob.TYLKO_GLOS    = isCheckedTylGlos;
    mGlob.DEZAP         = isCheckedDezap;

    //Efekciarstwo:
    boolean isCheckedImgTurnEf   = cb_ImgTurnEf.isChecked();
    boolean isCheckedWordShakeEf = cb_WordShakeEf.isChecked();
    boolean isCheckedLetterHopp  = cb_LetterHoppEf.isChecked();
    boolean isCheckedSndErrEf    = cb_SndErrEf.isChecked();
    boolean isCheckedSndOKEf     = cb_SndOKEf.isChecked();
    boolean isCheckedSndVictEf   = cb_SndVictEf.isChecked();
    mGlob.IMG_TURN_EF     = isCheckedImgTurnEf;
    mGlob.WORD_SHAKE_EF   = isCheckedWordShakeEf;
    mGlob.LETTER_HOPP_EF  = isCheckedLetterHopp;
    mGlob.SND_ERROR_EF    = isCheckedSndErrEf;
    mGlob.SND_LETTER_OK_EF= isCheckedSndOKEf;
    mGlob.SND_VICTORY_EF  = isCheckedSndVictEf;


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
          if (liczbaObrazkow > mGlob.MAX_OBR_LIMIT) {  //werja Demo, a wybrano katalog z wiecej niz 5 obrazkami
            ostrzegajPowyzej3();
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



/* stara wersja, gdy byly 3 jezyki.
Zostawiam, bo dobry przyklad jak z 3 opcij checkBox zrobic 'ideologiczną' RadioGrupe...

  public void jezykiOnClick(View view) {
  //Wybor jezyka.
  //Kod ponizej symuluje zachowanie checkbox'ów jak radiobattons w RadioGrup

    int licznik = 0;
    if (cb_ang.isChecked())   licznik++;
    if (cb_niem.isChecked())  licznik++;
    if (cb_franc.isChecked()) licznik++;

    //wylaczenie kliknietego wlączonego checkboxa:
    if (licznik==0) {
      ((CheckBox)view).setChecked(false);
      if (view==cb_ang)   mGlob.ANG   = false;
      if (view==cb_niem)  mGlob.NIEM  = false;
      if (view==cb_franc) mGlob.FRANC = false;
      return;
    }

    cb_ang.setChecked(false);
    cb_niem.setChecked(false);
    cb_franc.setChecked(false);
    mGlob.ANG   = false;
    mGlob.NIEM  = false;
    mGlob.FRANC = false;

    ((CheckBox)view).setChecked(true);
    if (view==cb_ang)   mGlob.ANG   = true;
    if (view==cb_niem)  mGlob.NIEM  = true;
    if (view==cb_franc) mGlob.FRANC = true;

    //Przejscie na Assets, zeby nie bylo 'kwasow' na onResume w MainActivity.
    //Jest to pojscie na skroty i na 'łatwiznę'..., ale... :
    rb_zAssets.setChecked(true);

  }
  */

  public void jezykiOnClick(View view) {
      mGlob.ANG = cb_ang.isChecked();
      if (cb_ang.isChecked()) {
          //ostrzezenie o 'ulomnosci' tej opcji:
          Dialog zapytanie;
          zapytanie = createAlertDialogWithButtons_Jezyki();
          zapytanie.show();
          //pokazanie 'nowej' liczby obrazkow (atrapa/wymazanie de facto, maniana, bo j.obce to wstawka....):
          sciezka.setText("        ");
      }
  }


  private void przywrocUstDomyslne() {
    /**
     * Przywrócenie domyślnych ustawien aplikacji.
     */

    mGlob.ustawParametryDefault();
    this.ustawKontrolki();

  }


  private Dialog createAlertDialogWithButtons_Domyslne() {
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

    private Dialog createAlertDialogWithButtons_Jezyki() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Język obcy");
        CharSequence kom = "Wybrana opcja ma na celu zademonstrować, że aplikacji można również użyć do nauki pisowni w językach obcych.";
        kom = kom + "\nMateriał przygotowuje się tak samo jak materiał w języku polskim - wykonując samodzielnie zdjęcia oraz nagrania i umieszczając je w katalogu na urządzeniu.";
        kom = kom + "\n";
        kom = kom + "\nPrezentowana w aplikacji wymowa nie może być uznana za wzorcową wymowę angielską.";
        dialogBuilder.setMessage(kom);

        dialogBuilder.setCancelable(true); //czy można wychodzić przez esc

        dialogBuilder.setPositiveButton("OK", new Dialog.OnClickListener() {
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
    zapytanie = createAlertDialogWithButtons_Domyslne();
    zapytanie.show();
  }


  private void ostrzegajBrakObrazkow(){
    /* **************************************************************** */
    /* Wyswietlany, gdy user wybierze katalog nie zawierajacy obrazkow. */
    /* **************************************************************** */
    wypiszOstrzezenie("Brak obrazków w wybranym katalogu.\nZostanie zastosowany wybór\nz zasobów aplikacji.");
  }


  private void ostrzegajPowyzej3() {
    /* ****************************************************************************************** */
    /* Wyswietlany, gdy user wybierze katalogAssets z wiecej niz 5 obrazkami, a wersja jest Demo. */
    /* ****************************************************************************************** */
    wypiszOstrzezenie("Uwaga - używasz wersji Demonstracyjnej.\nWybrano katalog zawierający więcej niż "+mGlob.MAX_OBR_LIMIT+" obrazki.\nZostanie przywrócony wybór\nz zasobów aplikacji.");
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

      //Jesli jest jezyk, to usuwam, zeby nie bylo niedomowien/problemow: (doklejka po dodaniu funkcjonalnosci 'j.obcy'),
      //a liste z Assets ustawiam na standardową:
      cb_ang.setChecked(false);
      mGlob.ANG = false;
      MainActivity.katalogAssets = "obrazki_demo_ver"; //w 'sztuczny; sposob przywracam wartosci, zeby dobrze policzylo ponizej...
      if (mGlob.PELNA_WERSJA) {
        MainActivity.katalogAssets = "obrazki_pelna_ver";
      }
      AssetManager mgr = getAssets();
      try {
        MainActivity.listaObrazkowAssets = mgr.list(MainActivity.katalogAssets);  //laduje wszystkie obrazki z Assets
      } catch (IOException e) {
        e.printStackTrace();
      }
      //koniec poprawek zwiazanych z 'jezykiem obcym'


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
    /* Dotyczy tylko katalogow na zewnetrznej SD                */
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
    rb_GlosOklaski = (RadioButton) findViewById(R.id.rb_GlosOklaski);
    isChecked = mGlob.GLOS_OKLASKI;
    rb_GlosOklaski.setChecked(isChecked);

    rb_NoComments = (RadioButton) findViewById(R.id.rb_No_Comments);
    isChecked = mGlob.BEZ_KOMENT;
    rb_NoComments.setChecked(isChecked);

    rb_TylkoOklaski = (RadioButton) findViewById(R.id.rb_TylkoOklaski);
    isChecked = mGlob.TYLKO_OKLASKI;
    rb_TylkoOklaski.setChecked(isChecked);

    rb_TylkoGlos = (RadioButton) findViewById(R.id.rb_TylkoGlos);
    isChecked = mGlob.TYLKO_GLOS;
    rb_TylkoGlos.setChecked(isChecked);

    cb_Dezap = (CheckBox) findViewById(R.id.cb_Dezap);
    isChecked = mGlob.DEZAP;
    cb_Dezap.setChecked(isChecked);
    /* nagrody - koniec */

    /* efekciarstwo: */
    cb_ImgTurnEf = (CheckBox) findViewById(R.id.cb_ImgTurnEf);
    isChecked    = mGlob.IMG_TURN_EF;
    cb_ImgTurnEf.setChecked(isChecked);

    cb_WordShakeEf  = (CheckBox) findViewById(R.id.cb_WordShakeEf);
    isChecked = mGlob.WORD_SHAKE_EF;
    cb_WordShakeEf.setChecked(isChecked);

    cb_LetterHoppEf = (CheckBox) findViewById(R.id.cb_LetterHoppEf);
    isChecked       = mGlob.LETTER_HOPP_EF;
    cb_LetterHoppEf.setChecked(isChecked);

    cb_SndErrEf = (CheckBox) findViewById(R.id.cb_SndErrEf);
    isChecked   = mGlob.SND_ERROR_EF;
    cb_SndErrEf.setChecked(isChecked);

    cb_SndOKEf = (CheckBox) findViewById(R.id.cb_SndOKEf);
    isChecked  = mGlob.SND_LETTER_OK_EF;
    cb_SndOKEf.setChecked(isChecked);

    cb_SndVictEf = (CheckBox) findViewById(R.id.cb_SndVictEf);
    isChecked    = mGlob.SND_VICTORY_EF;
    cb_SndVictEf.setChecked(isChecked);
    /* efekciarstwo - koniec */

    /* jezyki obce: */
    cb_ang    = (CheckBox) findViewById(R.id.cb_ang);
    isChecked = mGlob.ANG;
    cb_ang.setChecked(isChecked);

    //cb_niem    = (CheckBox) findViewById(R.id.cb_niem);
    //isChecked  = mGlob.NIEM;
    //cb_niem.setChecked(isChecked);

    //cb_franc   = (CheckBox) findViewById(R.id.cb_franc);
    //isChecked  = mGlob.FRANC;
    //cb_franc.setChecked(isChecked);
    /* jezyki obce - koniec: */


    /* żródło: */
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
