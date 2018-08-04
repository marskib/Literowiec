package autyzmsoft.pl.literowiec;
/**
 * Klasa na wyswietlenie obrazka z rounded-corners
 * Na podstawie: http://www.curious-creature.com/2012/12/11/android-recipe-1-image-with-rounded-corners/
 * autor: developer, 2018-08-02
 */

import static autyzmsoft.pl.literowiec.MainActivity.currImage;
import static autyzmsoft.pl.literowiec.MainActivity.katalog;
import static autyzmsoft.pl.literowiec.MainActivity.listaObrazkowAssets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class MojImageView extends ImageView {

    public MojImageView(Context context,   @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas plotno) {
        super.onDraw(plotno);


        String nazwaObrazka = listaObrazkowAssets[currImage];
        InputStream streamSki = null;
        try {
            streamSki = getContext().getAssets().open(katalog + "/" + nazwaObrazka);
        } catch (IOException e) {
            e.printStackTrace();
        }



        Bitmap bmpSki = BitmapFactory.decodeStream(streamSki);
        Drawable drawable = Drawable.createFromStream(streamSki, null);


        BitmapShader shader;
        shader = new BitmapShader(bmpSki, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);


        /* rzeczywiste wymiary obrazka w this: *********/
        //Drawable drawable = this.getDrawable();
//you should call after the bitmap drawn
        Rect bounds = drawable.getBounds();
        int width = bounds.width();
        int height = bounds.height();
        int bitmapWidth = drawable.getIntrinsicWidth(); //this is the bitmap's width
        int bitmapHeight = drawable.getIntrinsicHeight(); //this is the bitmap's height

        //width  = Math.min(width,bitmapWidth);
        //height = Math.min(height,bitmapHeight);

        /***************************/



//        RectF rect = new RectF(0.0f, 0.0f, this.getWidth(), this.getHeight());
        RectF rect = new RectF(0.0f, 0.0f, bitmapWidth, bitmapHeight);
        //RectF rect = new RectF(0.0f, 0.0f, width, height);

//        canvas.drawRoundRect(rect, radius, radius, paint);
        plotno.drawRoundRect(rect, 30, 30, paint);


    }
}
