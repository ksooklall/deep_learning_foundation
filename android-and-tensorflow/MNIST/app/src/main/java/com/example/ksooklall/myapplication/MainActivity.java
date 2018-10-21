package com.example.ksooklall.myapplication;

import android.app.Activity;
import android.app.RemoteInput;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import mariannelinhares.mnistandroidmodels.Classification;

public class MainActivity extends Activity implements View.OnClickListener, View.OnTouchListener {

    private static final int PIXEL_WIDTH = 28;

    // ui elements
    private Button clearBtn, classBtn;
    private TextView resText;
    private List<Classifier> mClassifier = new ArrayList<>();

    // views
    private DrawModel drawModel;
    private DrawView drawView;
    // coordinates that the user touches
    private PointF mTmpPiont = new PointF();

    // coordinates
    private float mLastX;
    private float mLasyY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get drawing view from XML (finger location)
        drawView = (DrawView) findViewById(R.id.draw);
        drawModel = new DrawModel(PIXEL_WIDTH, PIXEL_WIDTH);

        drawView.setModel(drawModel);
        drawView.setOnTouchListener(this);

        // clear button - Clears the screen
        clearBtn = (Button) findViewById(R.id.btn_clear);
        clearBtn.setOnClickListener(this);

        // class button - Detects what class the images belongs to
        classBtn = (Button) findViewById(R.id.btn_class);
        classBtn.setOnClickListener(this);

        // res text - Shows the output of the classifier
        resText = (TextView) findViewById(R.id.tfRes);

        // load model
        LoadModel();
    }

    @Override
    // Save the currect state of the app, if the user click home and then the app, onResume will
    // display last statue
    protected  void onResume() {
        drawView.onResume();
        super.onResume();
    }

    @Override
    // Save the current state of the app if the user receives a phone call
    protected void onPause() {
        drawView.onPause();
        super.onPause();
    }

    private void LoadModel() {
        new Thread(
                try {
                    mClassifier.add(TensorFlowClassifier.create(getAssets(), "TensorFlow",
                            "path to mnist.pb", "labels.txt", PIXEL_WIDTH, "input", "output", true));
            } catch (final Exception e) {
                    throw new RuntimeException("Error initializing classifiers!");
        }
        ).start();
    }

    @Override
    // handle when one any button is clicked
    public void onClick(View view) {
        // when the user clicks clear, clear everthing
        if (view.getId() == R.id.btn_clear) {
            drawModel.clear();
            drawView.reset();
            drawView.invalidate();

        } else if (view.getId() == R.id.btn_class) {
            float pixles[] = drawView.getPixledata();
            String text = "";

            for (Classifier classifier: mClassifier) {
                final Classification res = classifier.recognize(pixles);
                if (res.getLabel() == null) {
                    test += classifier.name() + ": ?\n";
                } else {
                    test += String.format(("%s: %s, %f\n", classifier.name(), res.getLabel(), res.getConf()))
                }

            }
            resText.setText(text);
        }
    }

    @Override
    // Draw where user is touching
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        // if touched
        if (action == MotionEvent.ACTION_DOWN) {
            processTouchDown(event);
            return true;
        } else if (action == MotionEvent.ACTION_MOVE) {
            processTouchMove(event);
            return true;
        } else if (action == MotionEvent.ACTION_UP) {
            processTouchUp(event);
            return true;
        }
        return false;
    }
}
