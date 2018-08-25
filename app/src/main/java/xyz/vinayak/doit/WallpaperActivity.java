package xyz.vinayak.doit;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class WallpaperActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = "Touch";

    private static final float MIN_ZOOM = 1.0f;

    private static final float MAX_ZOOM = 5.0f;

    // These matrices will be used to move and zoom image

    Matrix matrix = new Matrix();

    Matrix savedMatrix = new Matrix();

    // We can be in one of these 3 states

    static final int NONE = 0;

    static final int DRAG = 1;

    static final int ZOOM = 2;

    int mode = NONE;

    // Remember some things for zooming

    PointF start = new PointF();

    PointF mid = new PointF();

    float oldDist = 1f;

    ImageView iv, ivText;
    TextView tv;
    Button btnSetWallpaper, setDefaultWallpaper;
    Todo todo = null;
    FrameLayout frameLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);

        iv = findViewById(R.id.iv);
        ivText = findViewById(R.id.ivText);
        tv = findViewById(R.id.tv);
        btnSetWallpaper = findViewById(R.id.btnSetWallpaper);
        setDefaultWallpaper = findViewById(R.id.btnSetDefaultWallpaper);

        Intent i = getIntent();
        if (i.hasExtra("todoObject")) {
            todo = (Todo) i.getSerializableExtra("todoObject");
        }

        try {
            todo.getCategory();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e("WallpaperActivity", "onCreate: Todo Object is Empty");
        }

        String noteTitle = todo.getNoteText();

        final Drawable wallpaperDrawable = getCurrentSystemWallpaper();

        iv.setImageDrawable(wallpaperDrawable);
        ivText.setOnTouchListener(this);

        tv.setText(noteTitle);
        tv.setDrawingCacheEnabled(true);
        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        tv.buildDrawingCache(true);
        Bitmap tvImage = Bitmap.createBitmap(tv.getDrawingCache());
        tv.setDrawingCacheEnabled(false);
        ivText.setImageBitmap(tvImage);

        frameLayout = findViewById(R.id.frameLayout);
//        frameLayout.setDrawingCacheEnabled(true);
//
//        // this is the important code :)
//        // Without it the view will have a dimension of 0,0 and the bitmap will be null
//        frameLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        frameLayout.layout(0, 0, frameLayout.getMeasuredWidth(), frameLayout.getMeasuredHeight());
//
//        frameLayout.buildDrawingCache(true);
//        final Bitmap b = Bitmap.createBitmap(frameLayout.getDrawingCache());
//        frameLayout.setDrawingCacheEnabled(false); // clear drawing cache



        btnSetWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetWallpaperTask t = new SetWallpaperTask();
                t.execute();
            }
        });

        setDefaultWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
                startActivityForResult(Intent.createChooser(intent, "Select Wallpaper"),132);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 132) {
            finish();
            Intent i = new Intent(this,WallpaperActivity.class);
            i.putExtra("todoObject",todo);
            startActivity(i);
        }
    }

    public static Bitmap loadBitmapFromView(View v) {
        int width = v.getWidth();
        int height = v.getHeight();
        Bitmap b = Bitmap.createBitmap(width , height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;
    }

    Drawable getCurrentSystemWallpaper() {
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        return wallpaperManager.getDrawable();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        ImageView view = (ImageView) v;

        view.setScaleType(ImageView.ScaleType.MATRIX);

        float scale;

        // Dump touch event to log

        dumpEvent(event);

        // Handle touch events here...

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: // first finger down only

                savedMatrix.set(matrix);

                start.set(event.getX(), event.getY());

//                Log.d(TAG, "mode=DRAG");

                mode = DRAG;

                break;

            case MotionEvent.ACTION_UP: // first finger lifted

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                mode = NONE;

//                Log.d(TAG, "mode=NONE");

                break;

            case MotionEvent.ACTION_POINTER_DOWN: // second finger down

                oldDist = spacing(event);

//                Log.d(TAG, "oldDist=" + oldDist);

                if (oldDist > 5f) {

                    savedMatrix.set(matrix);

                    midPoint(mid, event);

                    mode = ZOOM;

//                    Log.d(TAG, "mode=ZOOM");

                }

                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG) { // movement of first finger

                    matrix.set(savedMatrix);

                    if (view.getLeft() >= -392) {

                        matrix.postTranslate(event.getX() - start.x, event.getY()
                                - start.y);

                    }

                } else if (mode == ZOOM) { // pinch zooming

                    float newDist = spacing(event);

//                    Log.d(TAG, "newDist=" + newDist);

                    if (newDist > 5f) {

                        matrix.set(savedMatrix);

                        scale = newDist / oldDist; /*
                         * thinking i need to play
                         * around with this value to
                         * limit it
                         */

                        matrix.postScale(scale, scale, mid.x, mid.y);

                    }

                }

                break;

        }

        // Perform the transformation

        view.setImageMatrix(matrix);

        return true; // indicate event was handled

    }

    private float spacing(MotionEvent event) {

        float x = event.getX(0) - event.getX(1);

        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);

    }

    private void midPoint(PointF point, MotionEvent event) {

        float x = event.getX(0) + event.getX(1);

        float y = event.getY(0) + event.getY(1);

        point.set(x / 2, y / 2);

    }

    /**
     * Show an event in the LogCat view, for debugging
     */

    private void dumpEvent(MotionEvent event) {

        String names[] = {"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",

                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};

        StringBuilder sb = new StringBuilder();

        int action = event.getAction();

        int actionCode = action & MotionEvent.ACTION_MASK;

        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN

                || actionCode == MotionEvent.ACTION_POINTER_UP) {

            sb.append("(pid ").append(

                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);

            sb.append(")");

        }

        sb.append("[");

        for (int i = 0; i < event.getPointerCount(); i++) {

            sb.append("#").append(i);

            sb.append("(pid ").append(event.getPointerId(i));

            sb.append(")=").append((int) event.getX(i));

            sb.append(",").append((int) event.getY(i));

            if (i + 1 < event.getPointerCount())

                sb.append(";");

        }

        sb.append("]");

//        Log.d(TAG, sb.toString());

    }

    private class SetWallpaperTask extends AsyncTask<Void, Void, Void> {

        Drawable wallpaperDrawable;

        @Override
        protected void onPreExecute() {
            // Runs on the UI thread
            // Do any pre-executing tasks here, for example display a progress bar
            Log.d(TAG, "About to set wallpaper...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Runs on the background thread
            WallpaperManager myWallpaperManager
                    = WallpaperManager.getInstance(getApplicationContext());
            try {
                myWallpaperManager.setBitmap(loadBitmapFromView(frameLayout));
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            // Runs on the UI thread
            // Here you can perform any post-execute tasks, for example remove the
            // progress bar (if you set one).
        }

    }
}