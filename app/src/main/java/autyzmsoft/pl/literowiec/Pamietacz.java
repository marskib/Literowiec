package autyzmsoft.pl.literowiec;

import static autyzmsoft.pl.literowiec.MainActivity.listaOper;

import java.util.ArrayList;

/**
 * Created by developer on 2018-09-02.
 * obiekt zapewniajacy wybor 'niepowtarzalnego' OBRAZKA za każdym kliknieciem
 * Na wzor obiektu TPamietacz z pascalowej wersji ProfMarcina.
 * Pamiętane sa indeksy plikow w listaOper
 */

public class Pamietacz {

    private ArrayList<Integer> listaZasobow;         //'wewnetrzna' lista jeszcze nie uzytych (=nie wyswietlonych) zasobow

    //Numer poprzednio wylosowanego obrazka (przy TEJ SAMEJ listaZasobow; chodzi o to, zeby przy wyczerpaniu sie listy,
    //nie startowal z nowa lista od tego samego obrazka (szczegiolnie widiczne przy krotkich listach):
    private int popObr;

    public Pamietacz() {
        listaZasobow = new ArrayList<Integer>();
        listaZasobow.clear();    //na wszelki wypadek
        wypelnijListeZasobow();
        popObr = -1;
    }  //konie Konstruktora

    private void wypelnijListeZasobow() {
        int rozmiarListy = listaOper.length;
        for (int i=0; i<rozmiarListy; i++)
            listaZasobow.add(i);
    } //koniec Metody()


    public int dajSwiezyZasob() {
        int nrObrazka;
        int idxWylosowany;

        if (listaZasobow.size()==0) {    //'wyczerpano' juz wszystkie obrazki - zaczynamy na nowo...; 'odnawiamy' liste
            wypelnijListeZasobow();      //lista 'odnowiona'
            if (listaZasobow.size()==1)
                    popObr = -1;            //zabezpieczenie przed petla nieskonczona ponizej
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