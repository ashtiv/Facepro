package com.ashtiv.facepro;

/*package whatever do not write package name here*/

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView cambutton;

    // whenever we request for our customized permission, we
    // need to declare an integer and initialize it to some
    // value .
    private final static int REQUEST_IMAGE_CAPTURE = 124;
    FirebaseVisionImage image;
    FirebaseVisionFaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.black));
            window.setNavigationBarColor(this.getResources().getColor(R.color.black));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing our firebase in main activity
        FirebaseApp.initializeApp(this);

        // finding the elements by their id's alloted.
        cambutton = findViewById(R.id.camera_button);

        // setting an onclick listener to the button so as
        // to request image capture using camera
        cambutton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {

                        // makin a new intent for opening camera
                        Intent intent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);
                        if (intent.resolveActivity(
                                getPackageManager())
                                != null) {
                            startActivityForResult(
                                    intent, REQUEST_IMAGE_CAPTURE);
                        }
                        else {
                            // if the image is not captured, set
                            // a toast to display an error image.
                            Toast
                                    .makeText(
                                            MainActivity.this,
                                            "Something went wrong",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data)
    {
        // after the image is captured, ML Kit provides an
        // easy way to detect faces from variety of image
        // types like Bitmap

        super.onActivityResult(requestCode, resultCode,
                data);
        if (requestCode == REQUEST_IMAGE_CAPTURE
                && resultCode == RESULT_OK) {
            Bundle extra = data.getExtras();
            Bitmap bitmap = (Bitmap)extra.get("data");
            detectFace(bitmap);
        }
    }

    // If you want to configure your face detection model
    // according to your needs, you can do that with a
    // FirebaseVisionFaceDetectorOptions object.
    private void detectFace(Bitmap bitmap)
    {
        FirebaseVisionFaceDetectorOptions options
                = new FirebaseVisionFaceDetectorOptions
                .Builder()
                .setModeType(
                        FirebaseVisionFaceDetectorOptions
                                .ACCURATE_MODE)
                .setLandmarkType(
                        FirebaseVisionFaceDetectorOptions
                                .ALL_LANDMARKS)
                .setClassificationType(
                        FirebaseVisionFaceDetectorOptions
                                .ALL_CLASSIFICATIONS)
                .build();

        // we need to create a FirebaseVisionImage object
        // from the above mentioned image types(bitmap in
        // this case) and pass it to the model.
        try {
            image = FirebaseVisionImage.fromBitmap(bitmap);
            detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // It’s time to prepare our Face Detection model.
        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace> >() {
                    @Override
                    // adding an onSuccess Listener, i.e, in case
                    // our image is successfully detected, it will
                    // append it's attribute to the result
                    // textview in result dialog box.
                    public void onSuccess(
                            List<FirebaseVisionFace>
                                    firebaseVisionFaces)
                    {
                        String resultText = "";
                        int i = 1;


                        for (FirebaseVisionFace face :
                                firebaseVisionFaces) {
                            String smi;
                            if(face.getSmilingProbability()*100>=50){
                                smi="YES";
                            }
                            else{
                                smi="NO";
                            }
                            String leftopen;
                            if(face.getLeftEyeOpenProbability()*100>=35){
                                leftopen="YES";
                            }
                            else{
                                leftopen="NO";
                            }
                            String rightopen;
                            if(face.getRightEyeOpenProbability()*100>=35){
                                rightopen="YES";
                            }
                            else{
                                rightopen="NO";
                            }
                            resultText
                                    = resultText
                                    .concat("\nFACE NUMBER. "
                                            + i + ": ")
                                    .concat(
                                            "\nSmile: "
                                                    + smi)
                                    .concat(
                                            "\nleft eye open: "
                                                    + leftopen)
                                    .concat(
                                            "\nright eye open: "
                                                    + rightopen);
                            i++;
                        }

                        // if no face is detected, give a toast
                        // message.
                        if (firebaseVisionFaces.size() == 0) {
                            Toast
                                    .makeText(MainActivity.this,
                                            "NO FACE DETECT",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                        else {
                            Bundle bundle = new Bundle();
                            bundle.putString(
                                    Facedetect.RESULT_TEXT,
                                    resultText);
                            DialogFragment resultDialog
                                    = new results();
                            resultDialog.setArguments(bundle);
                            resultDialog.setCancelable(true);
                            resultDialog.show(
                                    getSupportFragmentManager(),
                                    Facedetect.RESULT_DIALOG);
                        }
                    }
                }) // adding an onfailure listener as well if
                // something goes wrong.
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast
                                .makeText(
                                        MainActivity.this,
                                        "Oops, Something went wrong",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }
}
