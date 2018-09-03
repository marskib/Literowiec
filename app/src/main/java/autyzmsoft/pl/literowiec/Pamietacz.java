package autyzmsoft.pl.literowiec;

import java.util.ArrayList;

/**
 * Created by developer on 2018-09-02.
 * obiekt zapewniajacy wybor 'niepowtarzalnego' OBRAZKA za każdym kliknieciem
 * Na wzor obiektu TPamietacz z pascalowej wersji ProfMarcina.
 * Pamiętane sa indeksy plikow w listaOper
 */

public class Pamietacz {

    private ArrayList<Integer> listaZasobow;         //'wewnetrzna' lista jeszcze nie uzytych (=nie wyswietlonych) zasobow, odpowiednik Zbioru w Pascalu
    private ArrayList<Integer> listaShadow;          //kiedy glowna lista sie wyczerpie, odtworzymy ją z Shadow

    //Numer poprzednio wylosowanego obrazka (przy TEJ SAMEJ listaZasobow; chodzi o to, zeby przy wyczerpaniu sie listy,
    //nie startowal z nowa lista od tego samego obrazka (szczegiolnie widiczne przy krotkich listach):
    private int popObr;

    public Pamietacz(String[] tabZrodlo) {
        listaZasobow = new ArrayList<Integer>();
        listaShadow  = new ArrayList<Integer>();
        listaZasobow.clear();    //na wszelki wypadek
        listaShadow.clear();     //na wszelki wypadek
        wypelnijListeZasobow(tabZrodlo);
        popObr = -1;
    }  //konie Konstruktora

    private void wypelnijListeZasobow(String[] tabZrodlo) {
        int rozmiarListy = tabZrodlo.length;
        for (int i=0; i<rozmiarListy; i++) {
            listaZasobow.add(i);
            listaShadow.add(i);
        }
    } //koniec Metody()

    private void odtworzListe() {
        for (int i = 0; i < listaShadow.size(); i++) {
            listaZasobow.add(listaShadow.get(i));
        }
    }


    public int dajSwiezyZasob() {
        int nrObrazka;
        int idxWylosowany;

        if (listaZasobow.size()==0) {    //'wyczerpano' juz wszystkie obrazki - zaczynamy na nowo...; 'odnawiamy' liste
            odtworzListe();              //lista 'odnowiona'
            if (listaZasobow.size()==1)
                    popObr = -1;         //zabezpieczenie przed petla nieskonczona ponizej
        }

        //Losowanie zasobu:
        do { //petla jest na wypadek losowania z 'ODNOWIONEJ' listy - zeby nie wypadl ten sam obrazek co przy skonczeniu poprzedniej
            idxWylosowany = (int) (Math.random() * listaZasobow.size());
        } while (listaZasobow.get(idxWylosowany) == popObr);

        nrObrazka = listaZasobow.get(idxWylosowany);  //numerem obrazka jest to, co jest pod wylosowanym indexem(!)
        popObr = nrObrazka;
        listaZasobow.remove(idxWylosowany);           //usuwamy ten zasob z listy (zeby juz wiecej nie wypadl w losowaniu)

        return nrObrazka;

    } //koniec Metody()



}