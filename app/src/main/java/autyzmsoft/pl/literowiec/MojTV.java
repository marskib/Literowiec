package autyzmsoft.pl.literowiec;



import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class MojTV extends TextView {

    private boolean inArea = false; //czy jest w Obszarze
    private String  origL  = "*";   //Litera z oryginalu; rozwiazuje problem Ola->OLA->Ola

    public MojTV(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isInArea() {
        return inArea;
    }

    public void setInArea(boolean inArea) {
        this.inArea = inArea;
    }

    public String getOrigL() {
        return origL;
    }

    public void setOrigL(String origL) {
        this.origL = origL;
    }
}
